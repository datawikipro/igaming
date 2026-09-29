#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
generate_freebet_announcement_posts.py

Generates viral multi-platform announcement posts (Telegram, Threads, Instagram, Reddit)
for the SmartBet.guru Free Bet Calculator (/tools/freebet-calculator).

Demonstrates how amateurs lose 100% of bonus funds on crazy accumulators,
while smart players extract 80% guaranteed cash into their bank accounts via matched betting.
"""

import sys
import os
import json
import argparse

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

BASE_URL = "https://smartbet.guru"

POSTS_CATALOG = {
    "telegram": {
        "title": "💰 Как превратить любой фрибет в 80% живых денег на карту (без риска)",
        "channel": "@SmartBetGuru",
        "html": """🔥 <b>Хватит сливать фрибеты на экспрессах: забираем 80% чистыми деньгами на карту!</b>

Каждый раз, когда букмекер дает фрибет 3 000 ₽ или 5 000 ₽, 99% игроков собирают «паровоз» из 10 матчей с кэфом 50+ и закономерно проигрывают всё в ноль 📉

<b>Математический факт от SmartBet.guru:</b>
Любой сгораемый фрибет (SNR) — это <b>ровно 80% гарантированных живых денег</b> при любом исходе матча через калькулятор перекрытия!

<b>🧮 Реальный пример (Winline 3 000 ₽):</b>
1. Ставим бесплатный фрибет 3 000 ₽ на аутсайдера с кэфом <b>5.00</b> (например, ничья в равном матче).
2. Во второй конторе перекрываем противоположный исход ставкой <b>9 600 ₽</b> на кэф <b>1.25</b>.

<b>Что происходит после свистка?</b>
• Победил фрибет: выплата 12 000 ₽ - 9 600 ₽ (ставка 2) = <b>+2 400 ₽ чистыми</b>.
• Победило перекрытие: выплата 12 000 ₽ - 9 600 ₽ = <b>+2 400 ₽ чистыми</b>, а фрибет просто сгорел.

👉 <b>Итог: при ЛЮБОМ счете вы кладете в карман ровно 2 400 ₽ (80% от фрибета)!</b>

🎁 <b>Топ фрибетов для конвертации прямо сейчас:</b>
• Winline 3 000 ₽ ➔ <b>2 400 ₽</b> на карту
• PARI 5 000 ₽ ➔ <b>4 000 ₽</b> на карту
• Фонбет 2 000 ₽ ➔ <b>1 600 ₽</b> на карту
• Лига Ставок 4 444 ₽ ➔ <b>3 555 ₽</b> на карту

🚀 Рассчитайте точную сумму перекрытия под свой фрибет в 1 клик:
🔗 <a href="https://smartbet.guru/ru/tools/freebet-calculator?freebet=3000&k1=5.0&k2=1.25&bookie=winline">Открыть Калькулятор фрибетов SmartBet.guru</a>

#фрибеты #matchedbetting #смартбет #вилки #пассивныйдоход #арбитраж""",
        "markdown": """🔥 **Хватит сливать фрибеты на экспрессах: забираем 80% чистыми деньгами на карту!**

Каждый раз, когда букмекер дает фрибет 3 000 ₽ или 5 000 ₽, 99% игроков собирают «паровоз» из 10 матчей с кэфом 50+ и закономерно проигрывают всё в ноль 📉

**Математический факт от SmartBet.guru:**
Любой сгораемый фрибет (SNR) — это **ровно 80% гарантированных живых денег** при любом исходе матча через калькулятор перекрытия!

**🧮 Реальный пример (Winline 3 000 ₽):**
1. Ставим бесплатный фрибет 3 000 ₽ на аутсайдера с кэфом **5.00**.
2. Во второй конторе перекрываем противоположный исход ставкой **9 600 ₽** на кэф **1.25**.

**Что происходит после свистка?**
• Победил фрибет: выплата 12 000 ₽ - 9 600 ₽ = **+2 400 ₽ чистыми**.
• Победило перекрытие: выплата 12 000 ₽ - 9 600 ₽ = **+2 400 ₽ чистыми**.

👉 **Итог: при ЛЮБОМ счете вы кладете в карман ровно 2 400 ₽ (80% от бонуса)!**

🎁 **Топ бонусов недели:**
• Winline 3 000 ₽ ➔ 2 400 ₽ на карту
• PARI 5 000 ₽ ➔ 4 000 ₽ на карту
• Фонбет 2 000 ₽ ➔ 1 600 ₽ на карту

🚀 [Открыть Калькулятор фрибетов на SmartBet.guru](https://smartbet.guru/ru/tools/freebet-calculator?freebet=3000&k1=5.0&k2=1.25&bookie=winline)"""
    },
    "threads": {
        "text": """Как 99% людей используют фрибет 3 000 ₽:
Собирают экспресс из 8 матчей ➡️ сливают в ноль 😭

Как используют профи:
Открывают калькулятор перекрытия SmartBet.guru ➡️ ставят фрибет на кэф 5.0, а в другой БК перекрывают кэфом 1.25 ➡️ забирают ровно 2 400 ₽ (80% живыми деньгами на карту) при ЛЮБОМ счете матча 💳

Математика побеждает букмекеров. 

Ссылка на готовый расчет в шапке профиля или: smartbet.guru/ru/tools/freebet-calculator?freebet=3000&k1=5.0&k2=1.25"""
    },
    "reddit": {
        "subreddit": "r/matchedbetting",
        "title": "Why Free Bets are actually worth 80% in guaranteed cold hard cash (The SNR Hedge Formula & Calculator)",
        "content": """Most casual sports bettors view free bets as "lottery tickets" and throw them at 10-leg parlays with zero expected value.

In matched betting, a Stake Not Returned (SNR) free bet has an exact mathematical cash conversion value of **~78-82%**.

### The Math Behind 80% Retention:
For a free bet of nominal `$F$` placed at high odds `$K_1$` (optimal range: 4.50 to 6.00) hedged with outcome 2 at low odds `$K_2$` (1.20 to 1.28):

- **Hedge Stake:** `S2 = F * (K1 - 1) / K2`
- **Guaranteed Profit:** `Profit = F * (K1 - 1) * (K2 - 1) / K2`
- **Retention Rate:** `eta = ((K1 - 1) * (K2 - 1) / K2) * 100%`

### Quick Breakdown Example ($50 Free Bet):
- Free Bet $50 on Underdog @ 5.00
- Hedge Bet in Bookie #2: $50 * 4.0 / 1.25 = $160 @ 1.25
- If Outcome 1 wins: Payout = $50 * 4.0 - $160 = **+$40 pure cash**
- If Outcome 2 wins: Payout = $160 * 1.25 - $160 = **+$40 pure cash**
- **Outcome:** Exact equal return of **$40 (80.0%)** risk-free.

We built a dedicated interactive calculator with instant presets, scenario comparison, and deep linking:
👉 https://smartbet.guru/en/tools/freebet-calculator?freebet=50&k1=5.0&k2=1.25

What average retention rate are you guys locking in across your books?"""
    },
    "instagram": {
        "caption": """Фрибет = 80% чистых денег на карту 💸

Хватит надеяться на удачу. Если букмекер дал вам бонус 3 000 ₽ или 5 000 ₽ — это не повод ставить экспресс. Это повод забрать 2 400 ₽ или 4 000 ₽ на свою карту с нулевым риском.

Калькулятор SmartBet.guru рассчитывает точную сумму перекрытия за 2 секунды. 

Ссылка на инструмент в шапке профиля: smartbet.guru/tools/freebet-calculator ⚡

#ставки #фрибет #matchedbetting #лайфхак #пассивныйдоход #финансы #smartbet"""
    }
}


def main():
    parser = argparse.ArgumentParser(description="Generate Freebet Announcement Posts")
    parser.add_argument("--platform", choices=["all", "telegram", "threads", "reddit", "instagram"], default="all")
    parser.add_argument("--json", action="store_true", help="Output in JSON format")
    args = parser.parse_args()

    if args.json:
        if args.platform == "all":
            print(json.dumps(POSTS_CATALOG, ensure_ascii=False, indent=2))
        else:
            print(json.dumps({args.platform: POSTS_CATALOG[args.platform]}, ensure_ascii=False, indent=2))
        return

    print("=" * 70)
    print("📢 SmartBet.guru: Генератор анонс-постов для Калькулятора фрибетов (80% кэша)")
    print("=" * 70)

    platforms = [args.platform] if args.platform != "all" else ["telegram", "threads", "reddit", "instagram"]
    for p in platforms:
        data = POSTS_CATALOG[p]
        print(f"\n--- Platform: {p.upper()} ---")
        if p == "telegram":
            print(f"Title: {data['title']}")
            print(f"Channel: {data['channel']}")
            print("\n[HTML Content]:\n" + data['html'])
        elif p == "threads":
            print(data['text'])
        elif p == "reddit":
            print(f"Subreddit: {data['subreddit']}")
            print(f"Title: {data['title']}\n")
            print(data['content'])
        elif p == "instagram":
            print(data['caption'])
        print("-" * 70)


if __name__ == "__main__":
    main()
