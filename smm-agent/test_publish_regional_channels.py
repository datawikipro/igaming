import json
import sys
import urllib.parse
import urllib.request

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")
import os
TOKEN = os.getenv("TELEGRAM_BOT_TOKEN", "")

POSTS = {
    "@SmartBetGuruEN": (
        "🎯 <b>FREE BET MATHEMATICAL ARBITRAGE</b>\n\n"
        "⚽ <b>Liverpool vs Chelsea — Premier League</b>\n"
        "📌 Bet365 £100 Free Bet ➔ <b>£82 GUARANTEED NET CASH</b>\n\n"
        "🧮 <b>Mathematical Proof (SNR Freebet = 80% Cash):</b>\n"
        "• Back: High odds outcome @ 5.50 (Stake £100 Freebet)\n"
        "• Lay/Hedge: Opposite outcome @ 1.24\n"
        "• Conversion rate: η = (5.5 - 1) × (1.24 - 1) / 1.24 ≈ <b>82.1%</b>\n\n"
        "💰 Regardless of match outcome: <b>+£82.10 Pure Profit!</b>\n\n"
        '📊 Calculate any bonus: <a href="https://smartbet.guru/tools/freebet-calculator">smartbet.guru/tools/freebet-calculator</a>\n'
        '⚡ Live Scanner: <a href="https://smartbet.guru">smartbet.guru</a>'
    ),
    "@SmartBetGuruFR": (
        "🎯 <b>ARBITRAGE MATHÉMATIQUE SUR PARIS GRATUITS</b>\n\n"
        "⚽ <b>PSG vs Marseille — Ligue 1</b>\n"
        "📌 Pari Gratuit 100 € ➔ <b>80 € DE CASH GARANTI</b>\n\n"
        "🧮 <b>Preuve Mathématique (Freebet SNR = 80% Réel):</b>\n"
        "• Issue 1: Cote élevée @ 5.00 (Mise: 100 € Freebet)\n"
        "• Couverture: Contre-pari @ 1.25\n"
        "• Taux de conversion garanti: <b>80.0%</b>\n\n"
        "💰 Quel que soit le résultat du match: <b>+80.00 € nets sur votre compte !</b>\n\n"
        '📊 Calculateur officiel: <a href="https://smartbet.guru/tools/freebet-calculator">smartbet.guru/tools/freebet-calculator</a>\n'
        '⚡ Scanner en direct: <a href="https://smartbet.guru">smartbet.guru</a>'
    ),
    "@SmartBetGuruES": (
        "🎯 <b>ARBITRAJE MATEMÁTICO DE APUESTAS GRATIS</b>\n\n"
        "⚽ <b>Real Madrid vs Barcelona — La Liga</b>\n"
        "📌 Apuesta Gratis 100 € ➔ <b>80 € EN EFECTIVO GARANTIZADO</b>\n\n"
        "🧮 <b>Fórmula Matemática (Freebet SNR = 80% Cash Real):</b>\n"
        "• Opción 1: Cuota alta @ 5.20 (Apuesta 100 € Freebet)\n"
        "• Cobertura: Cuota baja @ 1.25\n"
        "• Conversión garantizada: <b>80.5%</b>\n\n"
        "💰 Sin importar el resultado final: <b>+80.50 € de beneficio neto seguro.</b>\n\n"
        '📊 Calculadora oficial: <a href="https://smartbet.guru/tools/freebet-calculator">smartbet.guru/tools/freebet-calculator</a>\n'
        '⚡ Escáner en vivo: <a href="https://smartbet.guru">smartbet.guru</a>'
    )
}

def publish_all():
    print("Testing Telegram publishing across regional channels...")
    results = {}
    for ch, text in POSTS.items():
        data = urllib.parse.urlencode({
            "chat_id": ch,
            "text": text,
            "parse_mode": "HTML",
            "disable_web_page_preview": "false"
        }).encode("utf-8")
        url = f"https://api.telegram.org/bot{TOKEN}/sendMessage"
        req = urllib.request.Request(url, data=data)
        try:
            with urllib.request.urlopen(req, timeout=10) as r:
                res = json.loads(r.read().decode())
                msg_id = res["result"]["message_id"]
                print(f"[+] {ch}: SUCCESS! Published message ID {msg_id}")
                results[ch] = {"status": "SUCCESS", "message_id": msg_id}
        except Exception as e:
            print(f"[-] {ch}: FAILED ({e})")
            results[ch] = {"status": "FAILED", "error": str(e)}
    return results

if __name__ == "__main__":
    publish_all()
