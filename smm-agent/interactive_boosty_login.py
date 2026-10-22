#!/usr/bin/env python3
"""
SmartBet.guru — Interactive Boosty Login & Token Capture (Variant 2)
Launches a headed Firefox browser on the user's desktop, initiates Boosty Google OAuth,
autofills credentials if possible, allows user interaction for 2FA/prompts,
and automatically captures auth tokens into Kubernetes secrets and local profile.
"""
import json
import os
import subprocess
import sys
import time
from pathlib import Path
from urllib.parse import unquote
from playwright.sync_api import sync_playwright

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

PROFILE_DIR = Path("smm-agent/storage/boosty_profile").resolve()
PROFILE_DIR.mkdir(parents=True, exist_ok=True)
SCREEN_DIR = Path("smm-agent/storage").resolve()
SCREEN_DIR.mkdir(parents=True, exist_ok=True)

EMAIL = "www.smartbet.guru@gmail.com"
PASSWORD = "Racoiaws12"
BLOG_NAME = "smartbetguru"

def update_k8s_secret(access_token: str, device_id: str, blog_name: str = BLOG_NAME):
    """Update Kubernetes secret igaming-boosty-bot-secrets with new tokens."""
    print(f"\n[K8S] Updating igaming-boosty-bot-secrets in namespace igaming-dev...")
    try:
        # Check if secret exists
        check_cmd = ["kubectl", "get", "secret", "igaming-boosty-bot-secrets", "-n", "igaming-dev"]
        res = subprocess.run(check_cmd, capture_output=True, text=True)
        if res.returncode == 0:
            patch_cmd = [
                "kubectl", "patch", "secret", "igaming-boosty-bot-secrets",
                "-n", "igaming-dev",
                "--type", "json",
                "-p", json.dumps([
                    {"op": "replace", "path": "/data/boosty-access-token", "value": subprocess.check_output(f'echo -n "{access_token}" | base64', shell=True).decode().strip()},
                    {"op": "replace", "path": "/data/boosty-device-id", "value": subprocess.check_output(f'echo -n "{device_id}" | base64', shell=True).decode().strip()}
                ])
            ]
            # Simpler and safer kubectl create ... --dry-run=client -o yaml | kubectl apply -f -
            yaml_content = f"""apiVersion: v1
kind: Secret
metadata:
  name: igaming-boosty-bot-secrets
  namespace: igaming-dev
type: Opaque
stringData:
  db-password: "igaming"
  boosty-blog-name: "{blog_name}"
  boosty-access-token: "{access_token}"
  boosty-device-id: "{device_id}"
"""
            p = subprocess.Popen(["kubectl", "apply", "-f", "-"], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
            out, err = p.communicate(input=yaml_content)
            print(f"[K8S] Secret applied: {out.strip()} {err.strip()}")
            
            # Restart deployment to pick up new tokens
            subprocess.run(["kubectl", "rollout", "restart", "deployment/igaming-boosty-bot", "-n", "igaming-dev"])
            print("[K8S] igaming-boosty-bot rollout restart triggered successfully.")
    except Exception as e:
        print(f"[K8S ERROR] Failed to update Kubernetes secret: {e}")

def main():
    print("=" * 60)
    print("SmartBet.guru — Boosty Interactive Login (Variant 2)")
    print("=" * 60)
    print(f"Profile directory: {PROFILE_DIR}")
    print(f"Target email: {EMAIL}")
    
    with sync_playwright() as p:
        print("\n[BROWSER] Launching headed Firefox window on desktop...")
        ctx = p.firefox.launch_persistent_context(
            user_data_dir=str(PROFILE_DIR),
            headless=False,
            viewport={"width": 1280, "height": 900}
        )
        
        main_page = ctx.pages[0] if ctx.pages else ctx.new_page()
        google_page = None
        
        def on_new_page(new_p):
            nonlocal google_page
            print(f"\n[POPUP DETECTED] URL: {new_p.url}")
            google_page = new_p
        
        ctx.on("page", on_new_page)
        
        print("[NAVIGATE] Opening https://boosty.to ...")
        main_page.goto("https://boosty.to", timeout=60000, wait_until="commit")
        time.sleep(3)
        
        # Check cookies button
        try:
            cookie_btn = main_page.locator("button:has-text('I AGREE'), button:has-text('СОГЛАСЕН')").first
            if cookie_btn.is_visible(timeout=2000):
                print("[ACTION] Accepting cookie banner...")
                cookie_btn.click(force=True)
                time.sleep(1)
        except Exception:
            pass
            
        main_page.screenshot(path=str(SCREEN_DIR / "boosty_interactive_step1.png"))
        
        # Look for SIGN UP / LOG IN
        try:
            signup = main_page.locator("button:has-text('SIGN UP'), a:has-text('SIGN UP'), button:has-text('РЕГИСТРАЦИЯ'), button:has-text('LOG IN'), button:has-text('ВОЙТИ')").first
            if signup.is_visible(timeout=3000):
                print("[ACTION] Clicking Sign Up / Log In button...")
                signup.click(force=True)
                time.sleep(2)
        except Exception as e:
            print(f"[NOTE] Sign up button check: {e}")
            
        main_page.screenshot(path=str(SCREEN_DIR / "boosty_interactive_step2_modal.png"))
        
        # Click Google OAuth button
        try:
            google_btn = main_page.locator('button[data-provider="google"], button:has-text("Google"), [data-test-id="oauth-google"]').first
            if google_btn.is_visible(timeout=3000):
                print("[ACTION] Clicking Google OAuth provider button...")
                google_btn.click(force=True)
                time.sleep(3)
            else:
                print("[NOTE] Google button selector not immediately visible, searching by mouse coordinates or icon...")
                # Also try finding any button containing google img/svg
                g_alt = main_page.locator('div[class*="OAuthButtons"] button, div[class*="Providers"] button').all()
                print(f"[INFO] Found {len(g_alt)} OAuth buttons")
        except Exception as e:
            print(f"[NOTE] Google button click: {e}")
            
        print("\n" + "=" * 60)
        print("INTERACTIVE SESSION ACTIVE!")
        print("Firefox window is open on the desktop.")
        print("Monitoring for successful authentication tokens...")
        print("=" * 60 + "\n")
        
        # Poll for auth tokens or successful login
        auth_found = False
        captured_data = {}
        
        # Max wait 300 seconds (5 minutes)
        start_time = time.time()
        attempt = 0
        
        while time.time() - start_time < 300:
            attempt += 1
            time.sleep(2)
            
            # Check open pages for Google login fields and auto-fill if visible
            pages_to_check = ctx.pages
            for pg in pages_to_check:
                try:
                    url = pg.url
                    if "accounts.google.com" in url:
                        # Auto-fill email if present
                        email_inp = pg.locator('input[type="email"], input[name="identifier"]').first
                        if email_inp.is_visible():
                            val = email_inp.input_value()
                            if not val:
                                print(f"[AUTOFILL] Entering Google email: {EMAIL}")
                                email_inp.fill(EMAIL)
                                time.sleep(0.5)
                                next_btn = pg.locator('button:has-text("Next"), #identifierNext').first
                                if next_btn.is_visible():
                                    next_btn.click()
                                    print("[AUTOFILL] Clicked Next on email")
                                    time.sleep(2)
                                    
                        # Auto-fill password if present
                        pwd_inp = pg.locator('input[type="password"], input[name="Passwd"]').first
                        if pwd_inp.is_visible():
                            val = pwd_inp.input_value()
                            if not val:
                                print("[AUTOFILL] Entering Google password...")
                                pwd_inp.fill(PASSWORD)
                                time.sleep(0.5)
                                next_btn = pg.locator('button:has-text("Next"), #passwordNext').first
                                if next_btn.is_visible():
                                    next_btn.click()
                                    print("[AUTOFILL] Clicked Next on password")
                                    time.sleep(3)
                except Exception:
                    pass
            
            # Check cookies in context
            cookies = ctx.cookies()
            for c in cookies:
                c_name = c.get("name", "")
                c_domain = c.get("domain", "")
                if c_name == "auth" and "boosty.to" in c_domain:
                    val = c.get("value", "")
                    try:
                        decoded_val = unquote(val)
                        data = json.loads(decoded_val)
                        if "accessToken" in data:
                            print("\n" + "🎉" * 30)
                            print("🎉 BOOSTY AUTHENTICATION SUCCESSFUL!")
                            print("🎉" * 30)
                            captured_data = data
                            auth_found = True
                            break
                    except Exception:
                        if len(val) > 20:
                            print(f"[AUTH DETECTED] auth cookie value length: {len(val)}")
                            captured_data = {"accessToken": val}
                            auth_found = True
                            break
            
            if auth_found:
                break
                
            # Periodically take screenshot for progress check
            if attempt % 5 == 0:
                try:
                    active_pg = ctx.pages[-1] if ctx.pages else main_page
                    active_pg.screenshot(path=str(SCREEN_DIR / "boosty_interactive_current.png"))
                    print(f"[{int(time.time() - start_time)}s] Current active URL: {active_pg.url} | Title: {active_pg.title()[:40]}")
                except Exception:
                    pass
                    
        if auth_found:
            access_token = captured_data.get("accessToken", "")
            refresh_token = captured_data.get("refreshToken", "")
            device_id = captured_data.get("deviceId", "boosty_desktop_firefox_session")
            
            print(f"\n[SUCCESS] Access Token: {access_token[:20]}...{access_token[-10:]}")
            print(f"[SUCCESS] Refresh Token: {refresh_token[:15]}... if present")
            print(f"[SUCCESS] Device ID: {device_id}")
            
            # Save cookies & tokens locally
            token_file = SCREEN_DIR / "boosty_tokens.json"
            with open(token_file, "w", encoding="utf-8") as f:
                json.dump(captured_data, f, indent=2)
            print(f"[STORAGE] Tokens saved to {token_file}")
            
            # Update Kubernetes secrets
            update_k8s_secret(access_token=access_token, device_id=device_id)
            
            # Final proof screenshot
            time.sleep(3)
            main_page.goto("https://boosty.to", timeout=30000, wait_until="domcontentloaded")
            time.sleep(3)
            main_page.screenshot(path=str(SCREEN_DIR / "boosty_logged_in_proof.png"))
            print(f"[PROOF] Screenshot saved to {SCREEN_DIR / 'boosty_logged_in_proof.png'}")
        else:
            print("\n[TIMEOUT] 5 minutes elapsed without detecting auth cookie.")
            main_page.screenshot(path=str(SCREEN_DIR / "boosty_interactive_timeout.png"))
            
        print("[BROWSER] Closing browser context...")
        ctx.close()
        print("[DONE] Script finished.")

if __name__ == "__main__":
    main()
