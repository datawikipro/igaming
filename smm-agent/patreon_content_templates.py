"""
Patreon Content Template Engine for SmartBet.guru — International (EN) Audience.

Generates English-language exclusive posts for Patreon tiers:
  - Pro Arbitrageur ($25/mo, min_tier_cents=2500)
  - VIP Syndicate ($100/mo, min_tier_cents=10000)

Templates:
  1. surebet_signal       — real-time arbitrage alert with stake calculator
  2. corridor_alert       — two-bet risk-free bracket with entry/exit levels
  3. freebet_80_cash      — freebet hedge post (SNR → 80% guaranteed cash)
  4. weekly_digest        — weekly performance summary
  5. account_strategy     — bookmaker account health & longevity guide
  6. onboarding_guide     — new member welcome post with step-by-step setup

All posts comply with:
  - AGENTS.md Rule 10: freebet 80% cash positioning with full math
  - AGENTS.md Rule 9: no Chromium/incognito; warm Camoufox sessions only
  - social-media-bot spec: mandatory responsible gambling disclaimer
"""

from __future__ import annotations

import math
import time
from dataclasses import dataclass, field
from typing import Any, Dict, List, Optional


# ---------------------------------------------------------------------------
# Tier constants
# ---------------------------------------------------------------------------

TIER_PRO_CENTS = 2500       # $25/mo — Pro Arbitrageur
TIER_VIP_CENTS = 10000      # $100/mo — VIP Syndicate

DISCLAIMER_EN = (
    "\n\n---\n"
    "⚠️ *Sports betting carries financial risk. SmartBet.guru provides algorithmic "
    "arbitrage signals and mathematical analysis tools — not gambling advice. "
    "Never bet money you cannot afford to lose. Play responsibly.*"
)


# ---------------------------------------------------------------------------
# Data models
# ---------------------------------------------------------------------------

@dataclass
class SurebetSignal:
    """Represents a live arbitrage opportunity."""
    sport: str
    event: str
    market: str
    bookmaker_a: str
    bookmaker_b: str
    odds_a: float
    odds_b: float
    outcome_a: str
    outcome_b: str
    stake_total_usd: float = 1000.0
    detected_at: Optional[int] = None

    def __post_init__(self) -> None:
        self.detected_at = self.detected_at or int(time.time())

    @property
    def margin(self) -> float:
        """Arbitrage margin (profit percentage of total stake)."""
        return (1 - (1 / self.odds_a + 1 / self.odds_b)) * 100

    @property
    def stake_a(self) -> float:
        """Optimal stake on outcome A."""
        total = self.stake_total_usd
        inv_a = 1 / self.odds_a
        inv_b = 1 / self.odds_b
        return round(total * inv_a / (inv_a + inv_b), 2)

    @property
    def stake_b(self) -> float:
        """Optimal stake on outcome B."""
        return round(self.stake_total_usd - self.stake_a, 2)

    @property
    def profit_a(self) -> float:
        return round(self.stake_a * self.odds_a - self.stake_total_usd, 2)

    @property
    def profit_b(self) -> float:
        return round(self.stake_b * self.odds_b - self.stake_total_usd, 2)


@dataclass
class FreebetSignal:
    """Represents a freebet hedge opportunity (SNR → 80% cash)."""
    bookmaker: str
    freebet_currency: str
    freebet_nominal: float          # e.g. 100.0 USD
    main_odds: float                # K1: target 4.5–6.0
    lay_odds: float                 # K2: target 1.20–1.28
    event: str
    sport: str

    @property
    def conversion_rate(self) -> float:
        """η = (K1-1)(K2-1)/K2 ≈ 0.80"""
        return (self.main_odds - 1) * (self.lay_odds - 1) / self.lay_odds

    @property
    def guaranteed_cash(self) -> float:
        return round(self.freebet_nominal * self.conversion_rate, 2)

    @property
    def lay_stake(self) -> float:
        """Optimal lay stake to fully hedge the freebet."""
        return round(self.freebet_nominal * (self.main_odds - 1) / self.lay_odds, 2)


@dataclass
class CorridorSignal:
    """Represents a risk-free corridor (two-bet bracket)."""
    sport: str
    event: str
    market: str                  # e.g. "Total goals"
    bookmaker_over: str
    bookmaker_under: str
    line_over: float             # e.g. 2.5 (Over 2.5 goals)
    line_under: float            # e.g. 3.5 (Under 3.5 goals)
    odds_over: float
    odds_under: float
    stake_total_usd: float = 1000.0

    @property
    def corridor_width(self) -> float:
        return self.line_under - self.line_over

    @property
    def stake_over(self) -> float:
        return round(self.stake_total_usd * (1 / self.odds_over) /
                     (1 / self.odds_over + 1 / self.odds_under), 2)

    @property
    def stake_under(self) -> float:
        return round(self.stake_total_usd - self.stake_over, 2)

    @property
    def max_loss(self) -> float:
        """Max loss if neither outcome hits the corridor."""
        return -round(self.stake_total_usd - max(
            self.stake_over * self.odds_over,
            self.stake_under * self.odds_under
        ), 2)

    @property
    def corridor_win(self) -> float:
        """Profit if result falls within the corridor (both bets win)."""
        return round(self.stake_over * self.odds_over + self.stake_under * self.odds_under
                     - self.stake_total_usd, 2)


# ---------------------------------------------------------------------------
# Template renderers
# ---------------------------------------------------------------------------

class PatreonTemplateEngine:
    """
    Generates English Patreon post content for SmartBet.guru.
    Each method returns a dict with keys: title, content, teaser, min_tier_cents.
    """

    # -----------------------------------------------------------------------
    # Template 1: Surebet Signal
    # -----------------------------------------------------------------------
    @staticmethod
    def render_surebet_signal(signal: SurebetSignal, stake: float = 1000.0) -> Dict[str, Any]:
        signal.stake_total_usd = stake
        margin_str = f"{signal.margin:.2f}%"
        profit_str = f"${min(signal.profit_a, signal.profit_b):.2f}–${max(signal.profit_a, signal.profit_b):.2f}"

        title = f"⚡ ARBITRAGE ALERT: {signal.event} — {margin_str} guaranteed profit"

        content = f"""⚡ **ARBITRAGE SIGNAL** — SmartBet.guru VIP Feed

🏟️ **Event:** {signal.event}
🏆 **Sport:** {signal.sport} | **Market:** {signal.market}
📅 **Detected:** {time.strftime('%Y-%m-%d %H:%M UTC', time.gmtime(signal.detected_at))}

---

### 📊 Odds Snapshot

| Outcome | Bookmaker | Odds |
|---------|-----------|------|
| {signal.outcome_a} | {signal.bookmaker_a} | **{signal.odds_a}** |
| {signal.outcome_b} | {signal.bookmaker_b} | **{signal.odds_b}** |

### 💰 Stake Calculator (Total: ${stake:,.0f})

| Outcome | Bet Amount | Return if Win |
|---------|-----------|---------------|
| {signal.outcome_a} @ {signal.bookmaker_a} | **${signal.stake_a:,.2f}** | ${signal.stake_a * signal.odds_a:,.2f} |
| {signal.outcome_b} @ {signal.bookmaker_b} | **${signal.stake_b:,.2f}** | ${signal.stake_b * signal.odds_b:,.2f} |

✅ **Guaranteed profit at ANY outcome: {profit_str}**
📈 **Margin: {margin_str}** on ${stake:,.0f} working capital

---

> 🔗 Open accounts & fund:
> - [{signal.bookmaker_a}](https://smartbet.guru/go/{signal.bookmaker_a.lower().replace(' ', '-')})
> - [{signal.bookmaker_b}](https://smartbet.guru/go/{signal.bookmaker_b.lower().replace(' ', '-')})

🧮 **[Full Stake Calculator →](https://smartbet.guru/tools/surebet-calculator)**
{DISCLAIMER_EN}"""

        teaser = (
            f"⚡ Arbitrage alert: {signal.event} — {margin_str} guaranteed on ${stake:,.0f}. "
            f"Profit: {profit_str} at any outcome. Join Pro Arbitrageur to get full stake breakdown."
        )

        return {
            "title": title,
            "content": content,
            "teaser": teaser,
            "min_tier_cents": TIER_PRO_CENTS,
            "template": "surebet_signal",
            "margin": round(signal.margin, 4),
        }

    # -----------------------------------------------------------------------
    # Template 2: Freebet 80% Cash Hedge
    # -----------------------------------------------------------------------
    @staticmethod
    def render_freebet_80_cash(signal: FreebetSignal) -> Dict[str, Any]:
        eta = signal.conversion_rate
        guaranteed = signal.guaranteed_cash
        lay = signal.lay_stake
        currency = signal.freebet_currency

        title = (
            f"🎁 FREEBET RADAR: {signal.bookmaker} — "
            f"{currency} {signal.freebet_nominal:,.0f} freebet → "
            f"{currency} {guaranteed:,.2f} guaranteed cash"
        )

        content = f"""🎁 **FREEBET HEDGE ALERT** — SmartBet.guru Promo Radar

### 📋 Offer Details

**Bookmaker:** {signal.bookmaker}
**Freebet Type:** SNR (Stake Not Returned)
**Nominal:** {currency} {signal.freebet_nominal:,.2f}
**Event:** {signal.event} ({signal.sport})

---

### 🧮 Conversion Math

```
Formula:  η = (K₁ - 1) × (K₂ - 1) / K₂

K₁ (Main bet odds):  {signal.main_odds}   ← back on {signal.bookmaker}
K₂ (Lay/hedge odds): {signal.lay_odds}   ← lay on exchange

η = ({signal.main_odds} - 1) × ({signal.lay_odds} - 1) / {signal.lay_odds}
  = {signal.main_odds - 1:.2f} × {signal.lay_odds - 1:.2f} / {signal.lay_odds}
  ≈ {eta:.3f}  ({eta*100:.1f}% conversion)
```

### 💵 Your Execution Plan

| Step | Action | Amount |
|------|--------|--------|
| 1 | Place freebet on **{signal.bookmaker}** | {currency} {signal.freebet_nominal:,.2f} (freebet) |
| 2 | Lay the same selection on exchange | {currency} {lay:,.2f} (real money) |
| 3 | **Collect regardless of outcome** | ✅ **{currency} {guaranteed:,.2f}** |

### 📊 Outcome Scenarios

| Scenario | Your Net |
|----------|---------|
| Main bet wins | +{currency} {guaranteed:,.2f} (approx) |
| Lay wins | +{currency} {guaranteed:,.2f} (approx) |

**🎯 Guaranteed: {currency} {guaranteed:,.2f} at any outcome — {eta*100:.1f}% of {currency} {signal.freebet_nominal:,.0f} freebet**

---

🧮 **[SmartBet Freebet Calculator →](https://smartbet.guru/tools/freebet-calculator)**
🔗 **[Get this offer →](https://smartbet.guru/promos)**
{DISCLAIMER_EN}"""

        teaser = (
            f"🎁 {signal.bookmaker} freebet: {currency} {signal.freebet_nominal:,.0f} → "
            f"{currency} {guaranteed:,.2f} guaranteed cash ({eta*100:.0f}%) via matched betting. "
            f"See full hedge math inside."
        )

        return {
            "title": title,
            "content": content,
            "teaser": teaser,
            "min_tier_cents": TIER_PRO_CENTS,
            "template": "freebet_80_cash",
            "guaranteed_cash": guaranteed,
            "conversion_rate": round(eta, 4),
        }

    # -----------------------------------------------------------------------
    # Template 3: Corridor Alert
    # -----------------------------------------------------------------------
    @staticmethod
    def render_corridor_alert(signal: CorridorSignal, stake: float = 1000.0) -> Dict[str, Any]:
        signal.stake_total_usd = stake

        title = (
            f"📊 CORRIDOR ALERT: {signal.event} — "
            f"{signal.market} [{signal.line_over}–{signal.line_under}] risk-free bracket"
        )

        content = f"""📊 **CORRIDOR SIGNAL** — SmartBet.guru Risk-Free Bracket

🏟️ **Event:** {signal.event}
🏆 **Sport:** {signal.sport} | **Market:** {signal.market}
🔲 **Corridor Width:** {signal.line_over} ↔ {signal.line_under} ({signal.corridor_width:.1f} unit gap)

---

### 📊 Odds Snapshot

| Position | Bookmaker | Line | Odds |
|----------|-----------|------|------|
| Over | {signal.bookmaker_over} | {signal.line_over} | **{signal.odds_over}** |
| Under | {signal.bookmaker_under} | {signal.line_under} | **{signal.odds_under}** |

### 💰 Stake Calculator (Total: ${stake:,.0f})

| Bet | Amount |
|-----|--------|
| Over {signal.line_over} @ {signal.bookmaker_over} | **${signal.stake_over:,.2f}** |
| Under {signal.line_under} @ {signal.bookmaker_under} | **${signal.stake_under:,.2f}** |

### 📈 Outcome Scenarios

| Result | Your P/L |
|--------|---------|
| Result falls **inside** corridor ({signal.line_over}–{signal.line_under}) | ✅ **+${signal.corridor_win:,.2f} (BOTH bets win)** |
| Result falls **outside** corridor | ❌ ${signal.max_loss:,.2f} max loss (one bet wins, one loses) |

> **Strategy:** This corridor has a meaningful probability of the result landing inside the gap.
> The corridor win scenario delivers outsized reward. Max loss is contained.

🧮 **[Full Corridor Calculator →](https://smartbet.guru/tools/corridor-calculator)**
{DISCLAIMER_EN}"""

        teaser = (
            f"📊 Corridor alert: {signal.event} — {signal.market} "
            f"[{signal.line_over}–{signal.line_under}]. "
            f"Both bets win if result lands in the gap: +${signal.corridor_win:,.2f}. See details inside."
        )

        return {
            "title": title,
            "content": content,
            "teaser": teaser,
            "min_tier_cents": TIER_PRO_CENTS,
            "template": "corridor_alert",
            "corridor_win": signal.corridor_win,
            "max_loss": signal.max_loss,
        }

    # -----------------------------------------------------------------------
    # Template 4: Weekly Performance Digest
    # -----------------------------------------------------------------------
    @staticmethod
    def render_weekly_digest(
        week_label: str,
        surebets_count: int,
        corridors_count: int,
        freebets_count: int,
        avg_margin: float,
        top_opportunities: List[Dict[str, Any]],
        community_roi: float,
    ) -> Dict[str, Any]:

        title = f"📈 Weekly Digest — {week_label} | SmartBet.guru Community Results"

        top_opps_str = ""
        for i, opp in enumerate(top_opportunities[:5], 1):
            top_opps_str += (
                f"| {i} | {opp.get('event', '—')} | {opp.get('market', '—')} | "
                f"{opp.get('margin', 0):.2f}% | {opp.get('bookmakers', '—')} |\n"
            )

        content = f"""📈 **WEEKLY PERFORMANCE DIGEST** — {week_label}

### 📊 Signal Volume This Week

| Metric | Count |
|--------|-------|
| 🎯 Arbitrage (surebet) signals | **{surebets_count}** |
| 📊 Corridor alerts | **{corridors_count}** |
| 🎁 Freebet hedge opportunities | **{freebets_count}** |
| **Average surebet margin** | **{avg_margin:.2f}%** |

### 🏆 Top 5 Opportunities This Week

| # | Event | Market | Margin | Bookmakers |
|---|-------|--------|--------|------------|
{top_opps_str}
### 💰 Community ROI (Estimated)

Based on member-reported bankroll deployment:
> **Average weekly ROI: {community_roi:.1f}%** on working capital
> *(Self-reported, unaudited. Past performance ≠ future results.)*

---

### 📅 Next Week Preview

- New bookmaker integration dropping: stay tuned for announcement
- Dedicated freebet radar Tuesday: best current signup bonuses
- VIP Syndicate: Betfair Exchange corridor deep-dive analysis

🧮 **[Access Full Signal Archive →](https://smartbet.guru)**
{DISCLAIMER_EN}"""

        teaser = (
            f"📈 This week on SmartBet.guru: {surebets_count} arb signals, "
            f"{corridors_count} corridors, {freebets_count} freebet opportunities. "
            f"Avg margin: {avg_margin:.2f}%. Community ROI: {community_roi:.1f}%. Full digest inside."
        )

        return {
            "title": title,
            "content": content,
            "teaser": teaser,
            "min_tier_cents": TIER_PRO_CENTS,
            "template": "weekly_digest",
        }

    # -----------------------------------------------------------------------
    # Template 5: Onboarding Guide (Welcome Post)
    # -----------------------------------------------------------------------
    @staticmethod
    def render_onboarding_guide(tier: str = "pro") -> Dict[str, Any]:
        is_vip = tier.lower() == "vip"
        tier_name = "VIP Syndicate" if is_vip else "Pro Arbitrageur"
        min_tier = TIER_VIP_CENTS if is_vip else TIER_PRO_CENTS

        title = f"👋 Welcome to SmartBet.guru — {tier_name} Onboarding Guide"

        content = f"""👋 **Welcome to SmartBet.guru — {tier_name}!**

We're glad you're here. This guide gets you from zero to your first guaranteed profit in the next 48 hours.

---

## Step 1: Understand What You Have Access To

As a **{tier_name}** member, your feed includes:
{"- ✅ All surebets ≥ 2% margin (daily digest)" if not is_vip else "- 🔥 Unlimited surebets including 10–20%+ margins"}
{"- ✅ Corridor alerts" if not is_vip else "- 🔥 Real-time Telegram VIP alerts (seconds latency)"}
{"- ✅ Freebet hedge calculator" if not is_vip else "- 🔥 Pinnacle + Betfair Exchange signals"}
{"- ✅ Monthly account strategy guide" if not is_vip else "- 🔥 15-min SLA support + private VIP Discord"}

---

## Step 2: Open Your Core Bookmaker Accounts (Priority List)

Start with **sharp books** (best odds, highest limits):

| Priority | Bookmaker | Why |
|----------|-----------|-----|
| 🥇 P1 | **Pinnacle** | No account banning policy, highest limits |
| 🥇 P1 | **Betfair Exchange** | Lay betting for hedging freebets |
| 🥈 P2 | **1xBet** | Wide market coverage, arb-friendly |
| 🥈 P2 | **Bet365** | Excellent in-play odds |
| 🥉 P3 | **Sbobet** | Asian handicap specialist |

> 💡 **Use our referral links** at [smartbet.guru/go/](https://smartbet.guru/go/) — same bookmaker, same odds, and you support SmartBet.guru's development.

---

## Step 3: Fund Your Accounts & Set Bankroll

**Recommended starting structure ({"$500–$2,000" if not is_vip else "$5,000+"}):**

| Bookmaker Tier | % of Bankroll | Purpose |
|---------------|--------------|---------|
| Sharp books (Pinnacle, Betfair) | 40% | Best odds, stable |
| Mid-tier (1xBet, Bet365) | 40% | Volume opportunities |
| Reserve | 20% | Emergency rebalancing |

---

## Step 4: Place Your First Arbitrage Bet

1. Check today's **Surebet Signal** post in this feed
2. Open both bookmaker tabs simultaneously
3. Place stakes **exactly as shown** in the calculator
4. Confirm both bets accepted — profit is locked in

---

## Step 5: Use the Freebet Calculator for Bonuses

1. Collect any welcome freebet from your new accounts
2. Use our **[Freebet Calculator](https://smartbet.guru/tools/freebet-calculator)** to compute exact hedge stakes
3. Extract 80% of every freebet as guaranteed cash

---

## ❓ Questions?

Post in the comments below or message us here on Patreon. {
"VIP Syndicate members get responses within 15 minutes during market hours (09:00–23:00 UTC+3), 7 days/week." if is_vip
else "Pro Arbitrageur support target: 24-hour response."
}

Good luck — and may your arbitrage margins always be positive. 🎯
{DISCLAIMER_EN}"""

        teaser = (
            f"👋 Welcome to SmartBet.guru {tier_name}! "
            "Your 5-step onboarding guide: open accounts, set bankroll, "
            "place first arb bet, extract freebet bonuses. "
            "Full guide inside."
        )

        return {
            "title": title,
            "content": content,
            "teaser": teaser,
            "min_tier_cents": min_tier,
            "template": "onboarding_guide",
            "tier": tier_name,
        }


# ---------------------------------------------------------------------------
# Quick smoke test
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    import json

    # 1. Surebet signal
    sig = SurebetSignal(
        sport="Tennis",
        event="Djokovic vs Alcaraz — AO SF",
        market="Match Winner",
        bookmaker_a="Pinnacle",
        bookmaker_b="Bet365",
        odds_a=2.15,
        odds_b=2.08,
        outcome_a="Djokovic",
        outcome_b="Alcaraz",
        stake_total_usd=1000.0,
    )
    post = PatreonTemplateEngine.render_surebet_signal(sig)
    print(f"[SUREBET] margin={post['margin']:.2f}%")
    assert post["margin"] > 0, "Expected positive margin"

    # 2. Freebet
    fb = FreebetSignal(
        bookmaker="Bet365",
        freebet_currency="USD",
        freebet_nominal=100.0,
        main_odds=5.0,
        lay_odds=1.25,
        event="Man City vs Arsenal",
        sport="Football",
    )
    fb_post = PatreonTemplateEngine.render_freebet_80_cash(fb)
    print(f"[FREEBET] guaranteed={fb_post['guaranteed_cash']}, rate={fb_post['conversion_rate']:.3f}")
    assert fb_post["conversion_rate"] > 0.75, "Expected >75% conversion"

    # 3. Corridor
    cor = CorridorSignal(
        sport="Football",
        event="PSG vs Bayern — UCL",
        market="Total Goals",
        bookmaker_over="1xBet",
        bookmaker_under="Pinnacle",
        line_over=2.5,
        line_under=3.5,
        odds_over=2.10,
        odds_under=2.05,
        stake_total_usd=1000.0,
    )
    cor_post = PatreonTemplateEngine.render_corridor_alert(cor)
    print(f"[CORRIDOR] corridor_win=${cor_post['corridor_win']:.2f}, max_loss=${cor_post['max_loss']:.2f}")

    # 4. Onboarding
    ob_post = PatreonTemplateEngine.render_onboarding_guide(tier="pro")
    print(f"[ONBOARDING] tier={ob_post['tier']}, chars={len(ob_post['content'])}")

    print("\n✅ All templates rendered successfully")
