#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SmartBet.guru — VK Donut Dedicated Pod & Community Agent
Task #74 [pod-vk-donut] (Patron CRM & Community Feedback Hub)

Features:
1. Isolated pod deployment in Kubernetes (smm-bot-vk).
2. Isolated Redis session storage (smm:session:vk).
3. VK Callback API handler:
   - Handshake confirmation code.
   - Secret key verification.
   - Event deduplication via Redis.
   - Donut events: donut_subscription_create, donut_subscription_prolonged,
     donut_subscription_price_changed, donut_subscription_cancelled,
     donut_subscription_expired.
   - Wall reply monitoring: checks donut.is_don == 1, routes VIP feedback tickets.
4. Patron CRM & Community Identity sync:
   - community_identity:vk:<user_id>
   - LTV tracking & tier assignment.
5. Exclusive Donut wall publisher (wall.post with donut_paid_duration).
6. HTTP Healthcheck (/healthz) on port 8080.
7. Direct home IP SPb network routing (ru-proxy:direct).
"""

import json
import logging
import os
import sys
import time
from dataclasses import asdict, dataclass, field
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from typing import Any, Dict, List, Optional
import urllib.parse
import urllib.request

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] [smm-bot-vk] %(message)s"
)
logger = logging.getLogger("smm-bot-vk")

# Environment configuration
VK_GROUP_ID = int(os.getenv("VK_GROUP_ID", "229123456"))
VK_CONFIRMATION_CODE = os.getenv("VK_CONFIRMATION_CODE", "sb_vk_donut_confirm_777")
VK_SECRET_KEY = os.getenv("VK_SECRET_KEY", "sb_vk_donut_secret_key_888")
VK_ACCESS_TOKEN = os.getenv("VK_ACCESS_TOKEN", "vk1.a.dummy_donut_community_token_for_dev_mode")
REDIS_HOST = os.getenv("REDIS_HOST", "igaming-redis.igaming-dev.svc.cluster.local")
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))
REDIS_DB = int(os.getenv("REDIS_DB", "0"))
APP_PORT = int(os.getenv("PORT", "8080"))


@dataclass
class VkDonutSubscriber:
    user_id: int
    status: str  # ACTIVE, CANCELLED, EXPIRED
    amount: float
    amount_without_fee: float
    first_subscribed_at: str
    last_renewed_at: str
    total_donated: float
    is_paid: bool = True
    patron_tier: str = "VK_DONUT"
    last_comment: Optional[str] = None


@dataclass
class FeedbackTicket:
    source_platform: str = "VK_DONUT"
    source_message_id: str = ""
    user_id: int = 0
    is_paid: bool = True
    patron_tier: str = "VK_DONUT"
    donor_ltv: float = 0.0
    category: str = "FEATURE_REQUEST"
    priority: str = "P1_URGENT_PATRON"
    content: str = ""
    created_at: str = ""


class MockRedisSession:
    """In-memory fallback when Redis is unreachable (for local testing/dry runs)."""

    def __init__(self):
        self._data: Dict[str, Any] = {}

    def hset(self, name: str, key: str, value: str):
        if name not in self._data:
            self._data[name] = {}
        self._data[name][key] = value

    def hget(self, name: str, key: str) -> Optional[str]:
        return self._data.get(name, {}).get(key)

    def hgetall(self, name: str) -> Dict[str, str]:
        return self._data.get(name, {}).copy()

    def set(self, name: str, value: str, ex: Optional[int] = None):
        self._data[name] = value

    def get(self, name: str) -> Optional[str]:
        return self._data.get(name)

    def incrbyfloat(self, name: str, amount: float) -> float:
        val = float(self._data.get(name, 0.0)) + amount
        self._data[name] = str(val)
        return val

    def rpush(self, name: str, value: str):
        if name not in self._data or not isinstance(self._data[name], list):
            self._data[name] = []
        self._data[name].append(value)

    def lrange(self, name: str, start: int, stop: int) -> List[str]:
        lst = self._data.get(name, [])
        if not isinstance(lst, list):
            return []
        if stop == -1:
            return lst[start:]
        return lst[start:stop + 1]

    def ping(self) -> bool:
        return True


class VkDonutSessionManager:
    """Manages isolated Redis storage under smm:session:vk."""

    def __init__(self, host: str = REDIS_HOST, port: int = REDIS_PORT, db: int = REDIS_DB):
        self.prefix = "smm:session:vk"
        self._redis = None
        self._init_redis(host, port, db)

    def _init_redis(self, host: str, port: int, db: int):
        try:
            import redis
            client = redis.Redis(
                host=host,
                port=port,
                db=db,
                socket_timeout=3.0,
                decode_responses=True
            )
            client.ping()
            self._redis = client
            logger.info(f"Connected to Redis at {host}:{port} for session {self.prefix}")
        except Exception as e:
            logger.warning(f"Could not connect to Redis ({e}). Using in-memory fallback session.")
            self._redis = MockRedisSession()

    @property
    def is_connected(self) -> bool:
        try:
            return self._redis.ping()
        except Exception:
            return False

    def is_event_processed(self, event_id: str) -> bool:
        if not event_id:
            return False
        key = f"{self.prefix}:processed_events:{event_id}"
        val = self._redis.get(key)
        return val is not None

    def mark_event_processed(self, event_id: str, ttl: int = 86400):
        if not event_id:
            return
        key = f"{self.prefix}:processed_events:{event_id}"
        self._redis.set(key, "1", ex=ttl)

    def save_subscriber(self, sub: VkDonutSubscriber):
        key = f"{self.prefix}:dons"
        data = json.dumps(asdict(sub), ensure_ascii=False)
        self._redis.hset(key, str(sub.user_id), data)

        # Cross-system identity update (Patron CRM)
        identity_key = f"community_identity:vk:{sub.user_id}"
        identity_data = {
            "user_id": sub.user_id,
            "vk_id": sub.user_id,
            "is_paid": sub.is_paid,
            "vip_tier": sub.patron_tier,
            "monthly_spend": sub.amount,
            "total_donated": sub.total_donated,
            "updated_at": datetime.now(timezone.utc).isoformat()
        }
        self._redis.set(identity_key, json.dumps(identity_data, ensure_ascii=False))

    def get_subscriber(self, user_id: int) -> Optional[VkDonutSubscriber]:
        key = f"{self.prefix}:dons"
        val = self._redis.hget(key, str(user_id))
        if not val:
            return None
        data = json.loads(val)
        return VkDonutSubscriber(**data)

    def get_all_subscribers(self) -> List[VkDonutSubscriber]:
        key = f"{self.prefix}:dons"
        raw_dict = self._redis.hgetall(key)
        subs = []
        for val in raw_dict.values():
            try:
                data = json.loads(val)
                subs.append(VkDonutSubscriber(**data))
            except Exception:
                continue
        return subs

    def update_ltv(self, user_id: int, amount: float) -> float:
        ltv_key = f"{self.prefix}:ltv:{user_id}"
        return float(self._redis.incrbyfloat(ltv_key, amount))

    def get_ltv(self, user_id: int) -> float:
        ltv_key = f"{self.prefix}:ltv:{user_id}"
        val = self._redis.get(ltv_key)
        return float(val) if val else 0.0

    def push_feedback_ticket(self, ticket: FeedbackTicket):
        queue_key = "feedback:queue:patrons"
        payload = json.dumps(asdict(ticket), ensure_ascii=False)
        self._redis.rpush(queue_key, payload)
        logger.info(f"Enqueued VIP feedback ticket for VK user {ticket.user_id}: {ticket.content[:50]}...")


class VkApiClient:
    """Lightweight VK API client with direct SPb routing (no overseas proxy)."""

    def __init__(self, access_token: str = VK_ACCESS_TOKEN):
        self.access_token = access_token
        self.api_version = "5.199"
        self.base_url = "https://api.vk.com/method"

    def _call(self, method: str, params: Dict[str, Any]) -> Dict[str, Any]:
        params["access_token"] = self.access_token
        params["v"] = self.api_version

        # In dev or dry mode without real token, return mock response
        if "dummy" in self.access_token:
            logger.info(f"[Mock VK API Call] {method} with params {params}")
            return {"response": 1}

        data = urllib.parse.urlencode(params).encode("utf-8")
        req = urllib.request.Request(f"{self.base_url}/{method}", data=data)
        req.add_header("User-Agent", "SmartBet-VK-Donut-Agent/1.0 (Direct SPb)")

        with urllib.request.urlopen(req, timeout=10) as resp:
            return json.loads(resp.read().decode("utf-8"))

    def post_exclusive_donut_wall(self, group_id: int, message: str, donut_paid_duration: int = -1) -> Dict[str, Any]:
        """
        Publishes a wall post exclusively visible to VK Donut supporters.
        donut_paid_duration: -1 means permanent exclusivity to dons.
        """
        params = {
            "owner_id": -abs(group_id),
            "from_group": 1,
            "message": message,
            "donut_paid_duration": donut_paid_duration
        }
        return self._call("wall.post", params)

    def reply_to_comment(self, group_id: int, post_id: int, reply_to_comment: int, message: str) -> Dict[str, Any]:
        """Replies to a comment on the community wall."""
        params = {
            "owner_id": -abs(group_id),
            "post_id": post_id,
            "reply_to_comment": reply_to_comment,
            "message": message
        }
        return self._call("wall.createComment", params)

    def send_message(self, user_id: int, message: str) -> Dict[str, Any]:
        """Sends a private message from community to a subscriber."""
        params = {
            "user_id": user_id,
            "random_id": int(time.time() * 1000) % 2147483647,
            "message": message
        }
        return self._call("messages.send", params)


class VkDonutAgentService:
    """Core domain logic for processing VK Donut events and interacting with dons."""

    def __init__(
        self,
        group_id: int = VK_GROUP_ID,
        confirmation_code: str = VK_CONFIRMATION_CODE,
        secret_key: str = VK_SECRET_KEY,
        session_mgr: Optional[VkDonutSessionManager] = None,
        vk_client: Optional[VkApiClient] = None
    ):
        self.group_id = group_id
        self.confirmation_code = confirmation_code
        self.secret_key = secret_key
        self.session_mgr = session_mgr or VkDonutSessionManager()
        self.vk_client = vk_client or VkApiClient()

    def verify_secret(self, secret: Optional[str]) -> bool:
        if not self.secret_key:
            return True
        return secret == self.secret_key

    def process_callback_event(self, event_data: Dict[str, Any]) -> str:
        """
        Processes a VK Callback API event.
        Must return confirmation code for 'confirmation', and 'ok' for other events.
        """
        event_type = event_data.get("type")
        group_id = event_data.get("group_id")
        secret = event_data.get("secret")
        event_id = event_data.get("event_id") or f"{event_type}_{time.time()}"

        # 1. Confirmation Handshake
        if event_type == "confirmation":
            logger.info(f"Handshake requested for group {group_id}. Returning confirmation code.")
            return self.confirmation_code

        # 2. Secret Key Validation
        if not self.verify_secret(secret):
            logger.warning(f"Invalid secret key in callback event: {secret}")
            raise PermissionError("Invalid secret key")

        # 3. Deduplication Check
        if self.session_mgr.is_event_processed(event_id):
            logger.info(f"Event {event_id} already processed. Skipping duplicate.")
            return "ok"

        # 4. Dispatch Event
        obj = event_data.get("object", {})
        now_iso = datetime.now(timezone.utc).isoformat()

        if event_type == "donut_subscription_create":
            self._handle_subscription_create(obj, now_iso)
        elif event_type == "donut_subscription_prolonged":
            self._handle_subscription_prolonged(obj, now_iso)
        elif event_type == "donut_subscription_price_changed":
            self._handle_price_changed(obj, now_iso)
        elif event_type in ("donut_subscription_cancelled", "donut_subscription_expired"):
            self._handle_subscription_ended(obj, event_type, now_iso)
        elif event_type == "wall_reply_new":
            self._handle_wall_reply(obj, now_iso)
        elif event_type == "message_new":
            self._handle_message(obj, now_iso)
        else:
            logger.info(f"Received unhandled VK event type: {event_type}")

        self.session_mgr.mark_event_processed(event_id)
        return "ok"

    def _handle_subscription_create(self, obj: Dict[str, Any], now_iso: str):
        user_id = obj.get("user_id", 0)
        amount = float(obj.get("amount", 0.0))
        amount_without_fee = float(obj.get("amount_without_fee", amount * 0.95))

        new_ltv = self.session_mgr.update_ltv(user_id, amount)
        sub = VkDonutSubscriber(
            user_id=user_id,
            status="ACTIVE",
            amount=amount,
            amount_without_fee=amount_without_fee,
            first_subscribed_at=now_iso,
            last_renewed_at=now_iso,
            total_donated=new_ltv,
            is_paid=True
        )
        self.session_mgr.save_subscriber(sub)
        logger.info(f"🎉 New VK Donut subscriber! User {user_id}, amount: {amount} RUB (LTV: {new_ltv} RUB)")

        # Send welcome message
        welcome_msg = (
            "👑 Добро пожаловать в закрытый клуб VK Donut SmartBet.guru!\n\n"
            "Вам открыт доступ к эксклюзивным сигналам вилок с доходностью от 10%, "
            "расчетам 80% гарантированного кэша с фрибетов и приоритетной линии обратной связи."
        )
        try:
            self.vk_client.send_message(user_id, welcome_msg)
        except Exception as e:
            logger.error(f"Error sending welcome message to don {user_id}: {e}")

    def _handle_subscription_prolonged(self, obj: Dict[str, Any], now_iso: str):
        user_id = obj.get("user_id", 0)
        amount = float(obj.get("amount", 0.0))
        new_ltv = self.session_mgr.update_ltv(user_id, amount)

        sub = self.session_mgr.get_subscriber(user_id)
        if sub:
            sub.status = "ACTIVE"
            sub.last_renewed_at = now_iso
            sub.total_donated = new_ltv
            sub.is_paid = True
        else:
            sub = VkDonutSubscriber(
                user_id=user_id,
                status="ACTIVE",
                amount=amount,
                amount_without_fee=amount * 0.95,
                first_subscribed_at=now_iso,
                last_renewed_at=now_iso,
                total_donated=new_ltv
            )
        self.session_mgr.save_subscriber(sub)
        logger.info(f"🔄 VK Donut subscription renewed: User {user_id}, amount: {amount} RUB (LTV: {new_ltv})")

    def _handle_price_changed(self, obj: Dict[str, Any], now_iso: str):
        user_id = obj.get("user_id", 0)
        amount_new = float(obj.get("amount_new", 0.0))
        sub = self.session_mgr.get_subscriber(user_id)
        if sub:
            sub.amount = amount_new
            self.session_mgr.save_subscriber(sub)
        logger.info(f"💲 VK Donut pledge price changed for User {user_id} to {amount_new} RUB")

    def _handle_subscription_ended(self, obj: Dict[str, Any], event_type: str, now_iso: str):
        user_id = obj.get("user_id", 0)
        status_name = "CANCELLED" if "cancelled" in event_type else "EXPIRED"
        sub = self.session_mgr.get_subscriber(user_id)
        if sub:
            sub.status = status_name
            sub.is_paid = False
            self.session_mgr.save_subscriber(sub)
        logger.info(f"⚠️ VK Donut subscription {status_name} for User {user_id}")

    def _handle_wall_reply(self, obj: Dict[str, Any], now_iso: str):
        post_id = obj.get("post_id", 0)
        comment_id = obj.get("id", 0)
        from_id = obj.get("from_id", 0)
        text = obj.get("text", "")
        donut_info = obj.get("donut", {})
        is_don = bool(donut_info.get("is_don", False))

        sub = self.session_mgr.get_subscriber(from_id)
        is_paid_donor = is_don or (sub is not None and sub.status == "ACTIVE")
        donor_ltv = sub.total_donated if sub else (self.session_mgr.get_ltv(from_id) if is_don else 0.0)

        logger.info(f"Wall reply on post {post_id} from {from_id} (is_don={is_don}): {text[:60]}")

        if is_paid_donor:
            ticket = FeedbackTicket(
                source_platform="VK_DONUT",
                source_message_id=f"wall_{post_id}_{comment_id}",
                user_id=from_id,
                is_paid=True,
                patron_tier="VK_DONUT",
                donor_ltv=donor_ltv,
                category="FEATURE_REQUEST" if any(w in text.lower() for w in ["добавьте", "сделайте", "хочу", "фича"]) else "QUESTION",
                priority="P1_URGENT_PATRON",
                content=text,
                created_at=now_iso
            )
            self.session_mgr.push_feedback_ticket(ticket)

            # Auto-reply with acknowledgment
            reply_text = (
                f"🤝 Спасибо за обращение, дон SmartBet!\n"
                f"Ваше сообщение передано разработчикам с наивысшим приоритетом (P1 Patron)."
            )
            try:
                self.vk_client.reply_to_comment(self.group_id, post_id, comment_id, reply_text)
            except Exception as e:
                logger.error(f"Failed to post wall reply: {e}")

    def _handle_message(self, obj: Dict[str, Any], now_iso: str):
        message = obj.get("message", obj)
        from_id = message.get("from_id", 0)
        text = message.get("text", "")
        msg_id = message.get("id", 0)

        sub = self.session_mgr.get_subscriber(from_id)
        is_paid = sub is not None and sub.status == "ACTIVE"

        if is_paid:
            ticket = FeedbackTicket(
                source_platform="VK_DONUT",
                source_message_id=f"msg_{msg_id}",
                user_id=from_id,
                is_paid=True,
                patron_tier="VK_DONUT",
                donor_ltv=sub.total_donated,
                category="QUESTION",
                priority="P1_URGENT_PATRON",
                content=text,
                created_at=now_iso
            )
            self.session_mgr.push_feedback_ticket(ticket)


# Global service instance
agent_service = VkDonutAgentService()


class VkDonutHttpHandler(BaseHTTPRequestHandler):
    """Zero-dependency HTTP Handler compatible with Python standard library."""

    def _send_json(self, status_code: int, data: Dict[str, Any]):
        body = json.dumps(data, ensure_ascii=False).encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _send_text(self, status_code: int, text: str):
        body = text.encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "text/plain; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path

        if path in ("/healthz", "/actuator/health", "/health"):
            all_subs = agent_service.session_mgr.get_all_subscribers()
            active_dons = [s for s in all_subs if s.status == "ACTIVE"]
            self._send_json(200, {
                "status": "UP",
                "service": "smm-bot-vk",
                "pod_name": os.getenv("HOSTNAME", "smm-bot-vk-dev"),
                "routing": "ru-proxy:direct",
                "active_dons_count": len(active_dons),
                "total_dons_count": len(all_subs),
                "redis_connected": agent_service.session_mgr.is_connected,
                "timestamp": datetime.now(timezone.utc).isoformat()
            })
        elif path == "/api/v1/vk/dons":
            all_subs = agent_service.session_mgr.get_all_subscribers()
            self._send_json(200, {"dons": [asdict(s) for s in all_subs]})
        else:
            self._send_json(404, {"error": "Not Found"})

    def do_POST(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path

        content_len = int(self.headers.get("Content-Length", 0))
        body_bytes = self.rfile.read(content_len) if content_len > 0 else b"{}"

        try:
            payload = json.loads(body_bytes.decode("utf-8"))
        except Exception:
            payload = {}

        if path in ("/api/v1/vk/callback", "/callback", "/webhook"):
            try:
                res = agent_service.process_callback_event(payload)
                self._send_text(200, res)
            except PermissionError:
                self._send_text(403, "Invalid secret key")
            except Exception as e:
                logger.error(f"Error handling callback: {e}")
                self._send_text(200, "ok")

        elif path == "/api/v1/vk/publish-donut-post":
            message = payload.get("message", "⚡ Эксклюзивный VIP-сигнал для донов SmartBet.guru")
            duration = payload.get("donut_paid_duration", -1)
            res = agent_service.vk_client.post_exclusive_donut_wall(
                group_id=agent_service.group_id,
                message=message,
                donut_paid_duration=duration
            )
            self._send_json(200, {"status": "success", "result": res})
        else:
            self._send_json(404, {"error": "Not Found"})

    def log_message(self, format, *args):
        # Suppress excessive access log noise in k8s
        pass


def run_http_server(port: int = APP_PORT):
    server = ThreadingHTTPServer(("0.0.0.0", port), VkDonutHttpHandler)
    logger.info(f"Starting smm-bot-vk HTTP server on port {port}...")
    server.serve_forever()


# Optional FastAPI compatibility if installed
try:
    from fastapi import FastAPI
    app = FastAPI(title="SmartBet VK Donut Agent")

    @app.get("/healthz")
    def fastapi_healthz():
        all_subs = agent_service.session_mgr.get_all_subscribers()
        active_dons = [s for s in all_subs if s.status == "ACTIVE"]
        return {
            "status": "UP",
            "service": "smm-bot-vk",
            "pod_name": os.getenv("HOSTNAME", "smm-bot-vk-dev"),
            "routing": "ru-proxy:direct",
            "active_dons_count": len(active_dons),
            "total_dons_count": len(all_subs),
            "redis_connected": agent_service.session_mgr.is_connected,
            "timestamp": datetime.now(timezone.utc).isoformat()
        }
except ImportError:
    app = None


if __name__ == "__main__":
    run_http_server(APP_PORT)
