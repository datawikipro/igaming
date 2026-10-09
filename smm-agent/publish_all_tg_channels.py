import sys
import time
from pathlib import Path
from playwright.sync_api import sync_playwright

sys.stdout.reconfigure(encoding="utf-8")

# Add smm-agent to sys.path
BASE_DIR = Path(r"C:\Users\chernousov_a\IdeaProjects\igaming\smm-agent")
sys.path.insert(0, str(BASE_DIR))

from ephemeral_instagram_publisher import EphemeralInstagramPublisher
from seo_robot import MATCH_CATALOG, DeduplicationRegistry

PROFILE_DIR = BASE_DIR / "storage" / "telegram_web_profile"
ARTIFACT_DIR = Path(r"C:\Users\chernousov_a\.gemini\antigravity\brain\d15af757-f652-4d03-85f1-43d9e1c99936")

publisher = EphemeralInstagramPublisher()
registry = DeduplicationRegistry()

# 1. Generate Cards and Captions
CHANNELS_CONFIG = [
    {
        "channel_title": "SmartBet.guru | English",
        "lang": "en",
        "match": MATCH_CATALOG["en"][0],  # Liverpool vs Chelsea
        "account_handle": "en.smartbet.guru"
    },
    {
        "channel_title": "SmartBet.guru | France",
        "lang": "fr",
        "match": MATCH_CATALOG["fr"][0],  # Monaco vs Lyon
        "account_handle": "fr.smartbet.guru"
    },
    {
        "channel_title": "SmartBet.guru | España",
        "lang": "es",
        "match": MATCH_CATALOG["es"][0],  # Atlético Madrid vs Sevilla
        "account_handle": "es.smartbet.guru"
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

print("Starting Telegram Multi-Channel Publishing...", flush=True)

with sync_playwright() as p:
    browser = p.chromium.launch_persistent_context(
        user_data_dir=str(PROFILE_DIR),
        headless=True,
        args=["--disable-blink-features=AutomationControlled", "--no-sandbox"]
    )
    page = browser.new_page()
    page.set_viewport_size({"width": 1280, "height": 850})
    page.goto("https://web.telegram.org/k/", wait_until="domcontentloaded", timeout=45000)
    time.sleep(5)
    
    for cfg in CHANNELS_CONFIG:
        ch_title = cfg["channel_title"]
        lang = cfg["lang"]
        match = cfg["match"]
        
        print(f"\n==========================================", flush=True)
        print(f"Publishing to [{lang.upper()}] {ch_title} ...", flush=True)
        print(f"==========================================", flush=True)
        
        # 1. Generate Card
        card_path = publisher.generate_card(
            account_handle=cfg["account_handle"],
            match_title=match["match_title"],
            sport=match["sport"],
            bookmaker_freebet=match["bookmaker_freebet"],
            bookmaker_hedge=match["bookmaker_hedge"],
            freebet_amount=match["freebet_amount"],
            k1=match["k1"],
            k2=match["k2"]
        )
        print(f"Generated card: {card_path}", flush=True)
        
        eta = ((match["k1"] - 1.0) * (match["k2"] - 1.0)) / match["k2"]
        gain = round(eta * match["freebet_amount"], 2)
        caption_text = build_post_text(lang, match, gain)
        
        # 2. Open channel
        page.keyboard.press("Escape")
        time.sleep(1)
        ch_el = page.locator(".chatlist-chat", has_text=ch_title).first
        if not ch_el.is_visible():
            print(f"ERROR: Channel {ch_title} not found in chat list!", flush=True)
            continue
            
        ch_el.click()
        time.sleep(2)
        
        # 3. Attach Photo via input[type="file"]
        file_input = page.locator("input[type='file']").first
        if not file_input.count():
            # Trigger file chooser via attach button
            attach_btn = page.locator(".chat-input .btn-icon[title*='Attach'], .chat-input button[title*='Attach'], .btn-attach").first
            if attach_btn.is_visible():
                attach_btn.click()
                time.sleep(1)
                file_input = page.locator("input[type='file']").first
                
        print("Uploading photo card...", flush=True)
        file_input.set_input_files(str(card_path))
        time.sleep(2)
        
        # 4. Fill caption in media preview popup
        caption_box = page.locator(".popup-send-media .input-message-input, .popup .input-field-input, div[contenteditable='true']:visible").last
        if caption_box.is_visible():
            print("Filling caption in popup...", flush=True)
            caption_box.fill(caption_text)
            time.sleep(1)
            
            # Click Send button
            send_btn = page.locator(".popup-send-media button.btn-primary, .popup button:has-text('Send'), button.btn-circle.btn-corner:visible").last
            if send_btn.is_visible():
                print("Clicking Send...", flush=True)
                send_btn.click()
            else:
                page.keyboard.press("Enter")
        else:
            print("No media popup, typing in main input...", flush=True)
            msg_box = page.locator(".input-message-input").first
            msg_box.fill(caption_text)
            time.sleep(1)
            page.keyboard.press("Enter")
            
        time.sleep(4)
        screenshot_path = ARTIFACT_DIR / f"tg_post_{lang}_confirmed.png"
        page.screenshot(path=str(screenshot_path))
        print(f"Post confirmed: {screenshot_path}", flush=True)
        
        # 5. Record to registry
        registry.record_publication(
            channel="telegram",
            account=ch_title,
            lang=lang,
            match_title=match["match_title"],
            sport=match["sport"],
            proof_or_msg_id=f"web_post_{int(time.time())}",
            gain=gain,
            currency=match["currency"]
        )
        print(f"Recorded into DeduplicationRegistry: {ch_title} | {match['match_title']}", flush=True)
        time.sleep(3)
        
    browser.close()
    print("\n>>> ALL 3 TELEGRAM CHANNELS SUCCESSFULLY PUBLISHED! <<<", flush=True)
