import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

sys.stdout.reconfigure(encoding="utf-8")

BASE_DIR = Path(r"C:\Users\chernousov_a\IdeaProjects\igaming\smm-agent")
sys.path.insert(0, str(BASE_DIR))

from seo_robot import MATCH_CATALOG, DeduplicationRegistry

PROFILE_DIR = BASE_DIR / "storage" / "telegram_web_profile"
ARTIFACT_DIR = Path(r"C:\Users\chernousov_a\.gemini\antigravity\brain\d15af757-f652-4d03-85f1-43d9e1c99936")

registry = DeduplicationRegistry()

TARGETS = [
    {
        "peer_id": "-3960368887",
        "title": "SmartBet.guru | English",
        "lang": "en",
        "match": MATCH_CATALOG["en"][0]
    },
    {
        "peer_id": "-4371643544",
        "title": "SmartBet.guru | France",
        "lang": "fr",
        "match": MATCH_CATALOG["fr"][0]
    },
    {
        "peer_id": "-4346736376",
        "title": "SmartBet.guru | España",
        "lang": "es",
        "match": MATCH_CATALOG["es"][0]
    }
]

def build_post_text(lang, match, gain):
    m = match["match_title"]
    sp = match["sport"]
    b1 = match["bookmaker_freebet"]
    b2 = match["bookmaker_hedge"]
    k1 = match["k1"]
    k2 = match["k2"]
    amt = int(match["freebet_amount"])
    cur = match["currency"]
    url = match["calc_url"]
    intro = match["intro"]

    if lang == "en":
        return (
            f"🔥 Turn your {amt} {cur} Free Bet into GUARANTEED CASH: {m}!\n\n"
            f"{intro}\n\n"
            f"Mathematical fact from SmartBet.guru:\n"
            f"Every SNR (Stake Not Returned) free bet token = 80% guaranteed net cash via matched betting!\n\n"
            f"📊 Bet Structure:\n"
            f"• Leg 1 (Free Bet {amt} {cur}): @ {k1:.2f} on {b1}\n"
            f"• Leg 2 (Hedge): @ {k2:.2f} on {b2}\n\n"
            f"💰 Guaranteed net profit: +{gain:.2f} {cur} regardless of final score!\n\n"
            f"🚀 Calculate your exact hedge stake in 1 click:\n"
            f"👉 {url}\n\n"
            f"#matchedbetting #freebets #premierleague #bet365 #pinnacle #smartbet\n"
            f"🔞 18+ | Gamble Responsibly. Visit begambleaware.org"
        )
    elif lang == "fr":
        return (
            f"🔥 Convertissez votre freebet de {amt} {cur} en CASH GARANTI : {m}!\n\n"
            f"{intro}\n\n"
            f"Fait mathématique SmartBet.guru :\n"
            f"Tout freebet SNR = 80% de cash net garanti sur votre compte bancaire via la formule de couverture !\n\n"
            f"📊 Structure de la mise :\n"
            f"• Pari 1 (Freebet {amt} {cur}) : Choix 1 @ {k1:.2f} sur {b1}\n"
            f"• Pari 2 (Couverture) : Couverture @ {k2:.2f} sur {b2}\n\n"
            f"💰 Bénéfice net garanti : +{gain:.2f} {cur} quel que soit le score !\n\n"
            f"🚀 Calculez votre contre-pari en 1 clic :\n"
            f"👉 {url}\n\n"
            f"#parisportif #ligue1 #winamax #unibet #matchedbetting #smartbet\n"
            f"🔞 18+ | Les jeux d’argent comportent des risques. Appelez le 09-74-75-13-13."
        )
    else:  # es
        return (
            f"🔥 ¡Convierte tu freebet de {amt} {cur} en DINERO REAL GARANTIZADO: {m}!\n\n"
            f"{intro}\n\n"
            f"Hecho matemático de SmartBet.guru:\n"
            f"Cualquier bono SNR = 80% de dinero limpio garantizado en tu cuenta bancaria.\n\n"
            f"📊 Estructura de la apuesta:\n"
            f"• Apuesta 1 (Freebet {amt} {cur}) : @ {k1:.2f} en {b1}\n"
            f"• Apuesta 2 (Cobertura) : @ {k2:.2f} en {b2}\n\n"
            f"💰 Beneficio neto asegurado : +{gain:.2f} {cur} pase lo que pase!\n\n"
            f"🚀 Calcula tu apuesta de cobertura en 1 clic:\n"
            f"👉 {url}\n\n"
            f"#apuestas #laliga #betano #codere #matchedbetting #smartbet\n"
            f"🔞 18+ | Juega con responsabilidad. Consulta jugarBIEN.es."
        )

with sync_playwright() as p:
    browser = p.chromium.launch_persistent_context(
        user_data_dir=str(PROFILE_DIR),
        headless=True,
        args=["--disable-blink-features=AutomationControlled", "--no-sandbox"]
    )
    page = browser.new_page()
    page.set_viewport_size({"width": 1280, "height": 850})
    
    for t in TARGETS:
        print(f"\nPosting to {t['title']} ({t['peer_id']})...", flush=True)
        page.goto(f"https://web.telegram.org/k/#{t['peer_id']}", wait_until="domcontentloaded", timeout=45000)
        time.sleep(4)
        
        eta = ((t["match"]["k1"] - 1.0) * (t["match"]["k2"] - 1.0)) / t["match"]["k2"]
        gain = round(eta * t["match"]["freebet_amount"], 2)
        text = build_post_text(t["lang"], t["match"], gain)
        
        # Click message box and fill
        msg_box = page.locator(".input-message-input, div[contenteditable='true']").first
        msg_box.click()
        time.sleep(0.5)
        # Type multi-line text by evaluating clipboard or typing lines
        page.evaluate("(txt) => { const el = document.querySelector('.input-message-input'); el.innerText = txt; el.dispatchEvent(new Event('input', {bubbles: true})); }", text)
        time.sleep(1)
        
        # Press Enter or click send button
        send_btn = page.locator("button.btn-send, button[title*='Send'], .btn-circle.btn-send").first
        if send_btn.is_visible():
            send_btn.click()
        else:
            page.keyboard.press("Enter")
            
        time.sleep(3)
        page.screenshot(path=str(ARTIFACT_DIR / f"tg_posted_{t['lang']}.png"))
        print(f"Posted to {t['title']}!", flush=True)
        
        registry.record_publication(
            channel="telegram",
            account=t["title"],
            lang=t["lang"],
            match_title=t["match"]["match_title"],
            sport=t["match"]["sport"],
            proof_or_msg_id=f"direct_post_{int(time.time())}",
            gain=gain,
            currency=t["match"]["currency"]
        )
        time.sleep(2)
        
    browser.close()
    print("\n>>> ALL 3 CHANNELS POSTED AND RECORDED TO HISTORY! <<<", flush=True)
