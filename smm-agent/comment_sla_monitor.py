#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — Instagram Comment 15-min SLA Monitor
Task #071bdbbf [smm-instagram] — Sub-module 2

Отдельный K8s-деплоймент для мониторинга комментариев Instagram с 15-мин SLA
(AGENTS.md §11 — PatronCRM & Feedback Desk).

Функции:
1. Непрерывный polling входящих комментариев ко всем активным постам Swarm (все регионы).
2. Приоритизация P1/P2 (платные патроны: Boosty PRO, Patreon VIP, VK Donut, TG VIP).
3. Отслеживание 15-мин SLA-дедлайна в Redis (ключ: `smm:sla:comment:<id>`).
4. Алерт в Telegram-бот при нарушении SLA (интеграция с igaming-bot API).
5. Экспорт просроченных тикетов в Plane (REST API).
6. REST API для igaming-admin-frontend (/api/v1/sla/tickets, /api/v1/sla/stats).
7. K8s health: /actuator/health/liveness и /readiness.
"""

import json
import logging
import os
import threading
import time
from dataclasses import asdict, dataclass, field
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from typing import Any, Dict, List, Optional
from urllib.parse import urlparse

try:
    import redis as redis_lib
except ImportError:
    redis_lib = None

try:
    import requests as req_lib
except ImportError:
    req_lib = None

logger = logging.getLogger("CommentSLAMonitor")
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] [sla-monitor] %(message)s",
)

# ─────────────────────────────────────────────────────────────────────────────
# 1. Константы и конфигурация
# ─────────────────────────────────────────────────────────────────────────────

SLA_MINUTES = 15
REDIS_SLA_PREFIX = "smm:sla:comment:"
REDIS_TICKETS_LIST = "smm:sla:open_tickets"
REDIS_STATS_KEY = "smm:sla:stats"

PATRON_TIER_KEYWORDS = frozenset([
    "boosty", "patreon", "vk donut", "tg vip", "vip", "pro",
    "premium", "sponsor", "πρεμίουμ",
])

REGIONS = ["fr", "es", "de", "br", "uk"]

MANDATORY_DISCLAIMER_RU = (
    "Ставки на спорт сопряжены с финансовыми рисками. "
    "Мы против лудомании и необдуманного беттинга. Играйте ответственно."
)


@dataclass
class SLATicket:
    """Тикет комментария с SLA-таймером."""
    comment_id: str
    author: str
    text: str
    post_url: str
    platform: str = "instagram"
    region: str = "fr"
    priority: str = "P3"          # P1 = patron, P2 = engaged, P3 = general
    is_patron: bool = False
    registered_at: float = field(default_factory=time.time)
    deadline_ts: float = field(default_factory=lambda: time.time() + SLA_MINUTES * 60)
    replied: bool = False
    reply_text: str = ""
    sla_breached: bool = False
    plane_exported: bool = False

    @property
    def remaining_seconds(self) -> float:
        return max(0.0, self.deadline_ts - time.time())

    @property
    def is_overdue(self) -> bool:
        return not self.replied and time.time() > self.deadline_ts

    def to_dict(self) -> Dict[str, Any]:
        d = asdict(self)
        d["remaining_seconds"] = round(self.remaining_seconds, 1)
        d["is_overdue"] = self.is_overdue
        d["deadline_iso"] = datetime.fromtimestamp(
            self.deadline_ts, tz=timezone.utc
        ).isoformat()
        d["registered_at_iso"] = datetime.fromtimestamp(
            self.registered_at, tz=timezone.utc
        ).isoformat()
        return d


@dataclass
class SLAMonitorConfig:
    """Конфигурация SLA Monitor."""
    redis_url: str = os.getenv("REDIS_URL", "redis://igaming-redis:6379/0")
    igaming_bot_api: str = os.getenv("IGAMING_BOT_API", "http://igaming-bot:8080")
    portal_api_url: str = os.getenv("PORTAL_API_URL", "http://igaming-portal:80")
    plane_api_url: str = os.getenv("PLANE_API_URL", "")
    plane_project_id: str = os.getenv("PLANE_PROJECT_ID", "")
    poll_interval_seconds: int = int(os.getenv("POLL_INTERVAL_SECONDS", "90"))
    sla_minutes: int = int(os.getenv("SLA_MINUTES", "15"))
    healthcheck_port: int = int(os.getenv("PORT", "8080"))
    telegram_alert_enabled: bool = os.getenv("TELEGRAM_ALERT", "true").lower() == "true"
    dry_run: bool = os.getenv("DRY_RUN", "false").lower() == "true"


# ─────────────────────────────────────────────────────────────────────────────
# 2. Redis-backed Ticket Store
# ─────────────────────────────────────────────────────────────────────────────

class RedisTicketStore:
    """
    Персистирует тикеты в Redis с TTL.
    Позволяет igaming-admin-frontend читать состояние тикетов через Redis pub/sub.
    """

    def __init__(self, config: SLAMonitorConfig):
        self.config = config
        self._client: Any = None
        self._local_tickets: Dict[str, SLATicket] = {}
        self._lock = threading.Lock()
        self._init_redis()

    def _init_redis(self) -> None:
        if redis_lib is None:
            logger.warning("redis library not installed; using in-memory store only")
            return
        try:
            self._client = redis_lib.Redis.from_url(self.config.redis_url, decode_responses=False)
            self._client.ping()
            logger.info(f"Connected to Redis: {self.config.redis_url}")
        except Exception as e:
            logger.warning(f"Redis unavailable ({e}); using in-memory store")
            self._client = None

    def _redis_key(self, comment_id: str) -> str:
        return f"{REDIS_SLA_PREFIX}{comment_id}"

    def save_ticket(self, ticket: SLATicket) -> None:
        """Сохраняет тикет в Redis с TTL = SLA + 60 мин буфер."""
        with self._lock:
            self._local_tickets[ticket.comment_id] = ticket

        if self._client:
            try:
                ttl = int(ticket.remaining_seconds) + 3600  # SLA + 1h buffer
                data = json.dumps(ticket.to_dict(), ensure_ascii=False).encode("utf-8")
                self._client.setex(self._redis_key(ticket.comment_id), ttl, data)
                # Добавляем в список открытых тикетов
                self._client.sadd(REDIS_TICKETS_LIST, ticket.comment_id)
                logger.debug(f"[Redis] Saved ticket {ticket.comment_id} (TTL={ttl}s)")
            except Exception as e:
                logger.warning(f"[Redis] Save failed for {ticket.comment_id}: {e}")

    def get_ticket(self, comment_id: str) -> Optional[SLATicket]:
        with self._lock:
            return self._local_tickets.get(comment_id)

    def get_all_open_tickets(self) -> List[SLATicket]:
        with self._lock:
            return [t for t in self._local_tickets.values() if not t.replied]

    def get_overdue_tickets(self) -> List[SLATicket]:
        with self._lock:
            return [t for t in self._local_tickets.values()
                    if t.is_overdue and not t.sla_breached]

    def mark_replied(self, comment_id: str, reply_text: str = "") -> None:
        with self._lock:
            if comment_id in self._local_tickets:
                t = self._local_tickets[comment_id]
                t.replied = True
                t.reply_text = reply_text
                self.save_ticket(t)
                if self._client:
                    try:
                        self._client.srem(REDIS_TICKETS_LIST, comment_id)
                    except Exception:
                        pass

    def mark_sla_breached(self, comment_id: str) -> None:
        with self._lock:
            if comment_id in self._local_tickets:
                self._local_tickets[comment_id].sla_breached = True
                self.save_ticket(self._local_tickets[comment_id])

    def get_stats(self) -> Dict[str, Any]:
        with self._lock:
            tickets = list(self._local_tickets.values())
        total = len(tickets)
        open_count = sum(1 for t in tickets if not t.replied)
        breached = sum(1 for t in tickets if t.sla_breached)
        patron_open = sum(1 for t in tickets if not t.replied and t.is_patron)
        return {
            "total_registered": total,
            "open_tickets": open_count,
            "sla_breaches_total": breached,
            "patron_tickets_open": patron_open,
            "sla_minutes": self.config.sla_minutes,
            "timestamp": datetime.now(tz=timezone.utc).isoformat(),
        }


# ─────────────────────────────────────────────────────────────────────────────
# 3. Alert & Escalation Engine
# ─────────────────────────────────────────────────────────────────────────────

class AlertEngine:
    """
    Отправляет алерты о нарушении SLA в Telegram и экспортирует тикеты в Plane.
    Rule AGENTS.md §11: 15-мин SLA для платных патронов.
    """

    def __init__(self, config: SLAMonitorConfig):
        self.config = config
        self._session = req_lib.Session() if req_lib else None

    def _post(self, url: str, data: Dict[str, Any]) -> bool:
        if not self._session or self.config.dry_run:
            logger.info(f"[DRY-RUN] Would POST {url}: {json.dumps(data)[:200]}")
            return True
        try:
            r = self._session.post(url, json=data, timeout=5)
            return r.ok
        except Exception as e:
            logger.warning(f"[Alert] POST {url} failed: {e}")
            return False

    def alert_sla_breach(self, ticket: SLATicket) -> None:
        """Отправляет Telegram-алерт о нарушении SLA."""
        priority_emoji = "🚨" if ticket.is_patron else "⚠️"
        message = (
            f"{priority_emoji} **SLA BREACH** — Instagram comment не отвечен за "
            f"{self.config.sla_minutes} мин!\n\n"
            f"Region: `{ticket.region}`\n"
            f"Author: @{ticket.author}\n"
            f"Priority: **{ticket.priority}**\n"
            f"Patron: {'✅' if ticket.is_patron else '—'}\n"
            f"Comment: {ticket.text[:200]}\n"
            f"Post URL: {ticket.post_url}\n\n"
            f"⚡ Требуется немедленный ответ!"
        )
        payload = {
            "event": "sla_breach",
            "priority": ticket.priority,
            "region": ticket.region,
            "comment_id": ticket.comment_id,
            "author": ticket.author,
            "message": message,
            "is_patron": ticket.is_patron,
        }
        ok = self._post(f"{self.config.igaming_bot_api}/api/v1/alert/sla", payload)
        if ok:
            logger.info(f"[Alert] SLA breach alert sent for {ticket.comment_id}")
        else:
            logger.error(f"[Alert] Failed to send SLA breach alert for {ticket.comment_id}")

    def export_to_plane(self, ticket: SLATicket) -> bool:
        """Экспортирует тикет в Plane как issue [feature/feedback]."""
        if not self.config.plane_api_url or not self.config.plane_project_id:
            return False

        issue_title = (
            f"[feedback][instagram/{ticket.region}] "
            f"SLA BREACH @{ticket.author}: {ticket.text[:80]}"
        )
        issue_body = (
            f"**Platform:** Instagram\n"
            f"**Region:** {ticket.region}\n"
            f"**Author:** @{ticket.author}\n"
            f"**Priority:** {ticket.priority}\n"
            f"**Is Patron:** {ticket.is_patron}\n\n"
            f"**Comment:**\n{ticket.text}\n\n"
            f"**Post URL:** {ticket.post_url}\n\n"
            f"**SLA Deadline:** {datetime.fromtimestamp(ticket.deadline_ts, tz=timezone.utc).isoformat()}\n"
            f"**Status:** SLA BREACHED — requires manual response\n\n"
            f"---\n"
            f"*Auto-escalated by smm-instagram-sla-monitor*"
        )

        payload = {
            "name": issue_title,
            "description_html": issue_body.replace("\n", "<br>"),
            "priority": "urgent" if ticket.is_patron else "high",
            "label_ids": [],
        }
        ok = self._post(
            f"{self.config.plane_api_url}/api/v1/workspaces/"
            f"smartbet/projects/{self.config.plane_project_id}/issues/",
            payload,
        )
        if ok:
            ticket.plane_exported = True
            logger.info(f"[Plane] Exported ticket {ticket.comment_id} to Plane")
        return ok


# ─────────────────────────────────────────────────────────────────────────────
# 4. PatronCRM API Client (инструкция агентам писать комментарии)
# ─────────────────────────────────────────────────────────────────────────────

class PatronCRMSync:
    """
    Синхронизирует тикеты с PatronCRM endpoint igaming-portal.
    Polling входящих комментариев — агрегатор для всех регионов.
    """

    def __init__(self, config: SLAMonitorConfig, store: RedisTicketStore):
        self.config = config
        self.store = store
        self._session = req_lib.Session() if req_lib else None

    def fetch_pending_comments(self, region: str) -> List[Dict[str, Any]]:
        """Забирает новые незакрытые комментарии из PatronCRM API."""
        if self.config.dry_run or not self._session:
            # Симулируем тестовые комментарии
            return [
                {
                    "comment_id": f"mock_{region}_{int(time.time())}",
                    "author": f"testuser_{region}",
                    "text": "How to convert freebet to cash?",
                    "post_url": f"https://www.instagram.com/p/mock_{region}/",
                    "is_patron": False,
                    "region": region,
                },
            ]
        try:
            r = self._session.get(
                f"{self.config.portal_api_url}/api/v1/patron/feedback",
                params={"platform": "instagram", "region": region, "status": "pending"},
                timeout=5,
            )
            if r.ok:
                data = r.json()
                return data.get("comments", data if isinstance(data, list) else [])
            logger.warning(f"[PatronCRM] API returned {r.status_code} for region {region}")
        except Exception as e:
            logger.warning(f"[PatronCRM] Fetch failed for {region}: {e}")
        return []


# ─────────────────────────────────────────────────────────────────────────────
# 5. Основной монитор (SLA watchdog)
# ─────────────────────────────────────────────────────────────────────────────

class CommentSLAMonitor:
    """
    Основной SLA-монитор.
    Polling → регистрация тикетов → проверка SLA → алерты → экспорт в Plane.
    """

    def __init__(self, config: SLAMonitorConfig):
        self.config = config
        self.store = RedisTicketStore(config)
        self.alert = AlertEngine(config)
        self.crm_sync = PatronCRMSync(config, self.store)
        self._running = False
        self._stats: Dict[str, Any] = {
            "polls_completed": 0,
            "tickets_registered": 0,
            "sla_breaches": 0,
            "plane_exports": 0,
        }
        self._lock = threading.Lock()

    def _detect_patron(self, author: str, text: str) -> bool:
        """Определяет, является ли автор платным патроном."""
        combined = (author + " " + text).lower()
        return any(kw in combined for kw in PATRON_TIER_KEYWORDS)

    def _assign_priority(self, is_patron: bool, text: str) -> str:
        """P1 — патрон с вопросом, P2 — патрон без вопроса, P3 — общий."""
        if not is_patron:
            return "P3"
        question_indicators = ["?", "how", "comment", "como", "wie", "como", "qual", "что", "как"]
        has_question = any(ind in text.lower() for ind in question_indicators)
        return "P1" if has_question else "P2"

    def _poll_all_regions(self) -> int:
        """Опрашивает все регионы и регистрирует новые тикеты."""
        new_count = 0
        for region in REGIONS:
            comments = self.crm_sync.fetch_pending_comments(region)
            for c in comments:
                cid = c.get("comment_id", "")
                if not cid or self.store.get_ticket(cid):
                    continue  # уже зарегистрирован

                is_patron = c.get("is_patron", self._detect_patron(
                    c.get("author", ""), c.get("text", "")
                ))
                priority = self._assign_priority(is_patron, c.get("text", ""))

                ticket = SLATicket(
                    comment_id=cid,
                    author=c.get("author", "unknown"),
                    text=c.get("text", ""),
                    post_url=c.get("post_url", ""),
                    region=region,
                    priority=priority,
                    is_patron=is_patron,
                    deadline_ts=time.time() + self.config.sla_minutes * 60,
                )
                self.store.save_ticket(ticket)
                new_count += 1
                with self._lock:
                    self._stats["tickets_registered"] += 1

                logger.info(
                    f"[Monitor] New ticket {cid} | region={region} | "
                    f"priority={priority} | patron={is_patron} | "
                    f"SLA deadline in {self.config.sla_minutes} min"
                )

        return new_count

    def _check_sla_breaches(self) -> int:
        """Проверяет истёкшие SLA и отправляет алерты."""
        overdue = self.store.get_overdue_tickets()
        for ticket in overdue:
            logger.error(
                f"[Monitor] 🚨 SLA BREACH: {ticket.comment_id} | "
                f"region={ticket.region} | priority={ticket.priority} | "
                f"overdue by {abs(ticket.remaining_seconds):.0f}s"
            )
            # Алерт в Telegram
            if self.config.telegram_alert_enabled:
                self.alert.alert_sla_breach(ticket)

            # Экспорт в Plane (особенно для патронов)
            if ticket.is_patron and not ticket.plane_exported:
                if self.alert.export_to_plane(ticket):
                    with self._lock:
                        self._stats["plane_exports"] += 1

            self.store.mark_sla_breached(ticket.comment_id)
            with self._lock:
                self._stats["sla_breaches"] += 1

        return len(overdue)

    def run_poll_cycle(self) -> Dict[str, Any]:
        """Один цикл polling."""
        new_tickets = self._poll_all_regions()
        breaches = self._check_sla_breaches()
        open_count = len(self.store.get_all_open_tickets())

        with self._lock:
            self._stats["polls_completed"] += 1

        logger.info(
            f"[Monitor] Poll complete: new={new_tickets}, "
            f"open={open_count}, breaches={breaches}"
        )
        return {
            "new_tickets": new_tickets,
            "open_tickets": open_count,
            "sla_breaches_this_poll": breaches,
        }

    def run_forever(self) -> None:
        """Основной бесконечный цикл мониторинга."""
        self._running = True
        logger.info(
            f"[Monitor] SLA Monitor started: "
            f"poll_interval={self.config.poll_interval_seconds}s, "
            f"sla={self.config.sla_minutes} min, "
            f"regions={REGIONS}"
        )
        while self._running:
            try:
                self.run_poll_cycle()
            except Exception as e:
                logger.error(f"[Monitor] Poll cycle error: {e}", exc_info=True)
            time.sleep(self.config.poll_interval_seconds)

    def get_status(self) -> Dict[str, Any]:
        with self._lock:
            stats = dict(self._stats)
        stats.update(self.store.get_stats())
        stats["running"] = self._running
        stats["config"] = {
            "sla_minutes": self.config.sla_minutes,
            "poll_interval_seconds": self.config.poll_interval_seconds,
            "regions": REGIONS,
            "dry_run": self.config.dry_run,
        }
        return stats


# ─────────────────────────────────────────────────────────────────────────────
# 6. K8s Health Server
# ─────────────────────────────────────────────────────────────────────────────

class SLAHealthHandler(BaseHTTPRequestHandler):
    """HTTP health handler для K8s liveness/readiness probes."""

    monitor: Optional[CommentSLAMonitor] = None

    def _json(self, code: int, data: Any) -> None:
        body = json.dumps(data, ensure_ascii=False).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self) -> None:
        parsed = urlparse(self.path)

        if parsed.path in (
            "/healthz", "/health", "/ready",
            "/actuator/health",
            "/actuator/health/liveness",
            "/actuator/health/readiness",
        ):
            self._json(200, {
                "status": "UP",
                "service": "smm-instagram-sla-monitor",
                "timestamp": datetime.now(timezone.utc).isoformat(),
            })
            return

        if parsed.path == "/api/v1/sla/status":
            if self.monitor:
                self._json(200, self.monitor.get_status())
            else:
                self._json(200, {"status": "INITIALIZING"})
            return

        if parsed.path == "/api/v1/sla/tickets":
            if self.monitor:
                tickets = self.monitor.store.get_all_open_tickets()
                self._json(200, {
                    "open_tickets": [t.to_dict() for t in tickets],
                    "count": len(tickets),
                })
            else:
                self._json(200, {"open_tickets": [], "count": 0})
            return

        if parsed.path == "/api/v1/sla/stats":
            if self.monitor:
                self._json(200, self.monitor.store.get_stats())
            else:
                self._json(200, {})
            return

        self._json(404, {"error": "Not Found", "path": parsed.path})

    def do_POST(self) -> None:
        """Позволяет igaming-admin-frontend закрыть тикет вручную."""
        parsed = urlparse(self.path)
        if parsed.path == "/api/v1/sla/tickets/reply":
            length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(length)
            try:
                data = json.loads(body.decode("utf-8")) if body else {}
                comment_id = data.get("comment_id", "")
                reply_text = data.get("reply_text", "")
                if self.monitor and comment_id:
                    self.monitor.store.mark_replied(comment_id, reply_text)
                    self._json(200, {"status": "OK", "comment_id": comment_id})
                else:
                    self._json(400, {"error": "comment_id required"})
            except Exception as e:
                self._json(400, {"error": str(e)})
            return
        self._json(404, {"error": "Not Found"})

    def log_message(self, fmt: str, *args: Any) -> None:
        if args and "/healthz" in str(args[0]):
            return
        super().log_message(fmt, *args)


def start_health_server(
    monitor: CommentSLAMonitor, port: int = 8080
) -> ThreadingHTTPServer:
    SLAHealthHandler.monitor = monitor
    server = ThreadingHTTPServer(("0.0.0.0", port), SLAHealthHandler)
    t = threading.Thread(target=server.serve_forever, daemon=True)
    t.start()
    logger.info(
        f"SLA Monitor health server on http://0.0.0.0:{port} "
        f"(/healthz, /api/v1/sla/tickets, /api/v1/sla/stats)"
    )
    return server


# ─────────────────────────────────────────────────────────────────────────────
# 7. Entrypoint
# ─────────────────────────────────────────────────────────────────────────────

def main() -> None:
    import argparse
    p = argparse.ArgumentParser(description="SmartBet Instagram 15-min SLA Comment Monitor")
    p.add_argument("--mode", default=os.getenv("SMM_MODE", "server"),
                   choices=["server", "poll"])
    p.add_argument("--port", type=int, default=int(os.getenv("PORT", "8080")))
    p.add_argument("--poll-interval", type=int,
                   default=int(os.getenv("POLL_INTERVAL_SECONDS", "90")))
    p.add_argument("--sla-minutes", type=int,
                   default=int(os.getenv("SLA_MINUTES", "15")))
    p.add_argument("--dry-run", action="store_true",
                   default=os.getenv("DRY_RUN", "false").lower() == "true")
    args = p.parse_args()

    config = SLAMonitorConfig(
        poll_interval_seconds=args.poll_interval,
        sla_minutes=args.sla_minutes,
        healthcheck_port=args.port,
        dry_run=args.dry_run,
    )

    logger.info(
        f"=== Instagram Comment SLA Monitor starting | "
        f"sla={config.sla_minutes} min | "
        f"poll={config.poll_interval_seconds}s | "
        f"regions={REGIONS} | dry_run={config.dry_run} ==="
    )

    monitor = CommentSLAMonitor(config)
    health_server = start_health_server(monitor, port=config.healthcheck_port)

    if args.mode == "poll":
        result = monitor.run_poll_cycle()
        print(json.dumps(result, indent=2))
        return

    # server mode — daemon
    try:
        monitor.run_forever()
    except KeyboardInterrupt:
        logger.info("SLA Monitor shutdown")
        health_server.shutdown()


if __name__ == "__main__":
    main()
