#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
sms_gateway.py

Client for 5SIM.net API (Virtual numbers for Google/Gmail, TikTok, Twitter, Telegram).
Supports:
- Checking profile & balance
- Checking prices and available numbers by country and service
- Ordering numbers (activation)
- Polling for SMS verification codes
- Completing or cancelling orders
"""

import os
import sys
import time
import json
import requests
from typing import Optional, Dict, Any

API_KEY = os.environ.get(
    "FIVESIM_API_KEY",
    "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE4MjI1ODk2MjgsImlhdCI6MTc5MTA1MzYyOCwicmF5IjoiZTMxZmMzNjY1YmExNzZkODY1YWYzODBjNDBhMTk1ZGUiLCJzdWIiOjQ1OTM3NjN9.zEZ49CIPtb0ggNzrlCQxWwJYVceGzAfWdRG6GhgQA0EKYThNzNVCndpf0MkhGaiCse-azTfzbVY8GKsvCOwuQY-JJQNrfOnKJ-Pj0PRbu15ydWQRZqGMLIpG7jhDgKe9v8r8jy9tPk0OaHfd7rN_1_W_B9jmGm7v5qkapV2Kef5KCIePo-Pp2Xl4yzL7vf-KiEGbuU8joPXxh5eVwwfpKIjElH-_6-OTMkJSsZWQmkW-a96djuuarC_Vwav0z_ZgnfWjnikjDTQa_Y_NrfaPenNgNBLrPPSg9yTA59t6pO2PNoLk333lwf0zSoDB_tzRuvSitb-DsI-jc_riINgceg"
)

BASE_URL = "https://5sim.net/v1"

class FiveSimClient:
    def __init__(self, api_key: str = API_KEY):
        self.api_key = api_key
        self.session = requests.Session()
        self.session.headers.update({
            "Authorization": f"Bearer {self.api_key}",
            "Accept": "application/json"
        })

    def get_profile(self) -> Dict[str, Any]:
        """Fetch user profile and balance."""
        url = f"{BASE_URL}/user/profile"
        resp = self.session.get(url, timeout=15)
        resp.raise_for_status()
        return resp.json()

    def get_prices(self, country: Optional[str] = None, product: Optional[str] = None) -> Dict[str, Any]:
        """Fetch available prices and quantities for services."""
        url = f"{BASE_URL}/guest/prices"
        params = {}
        if country:
            params["country"] = country
        if product:
            params["product"] = product
        resp = self.session.get(url, params=params, timeout=15)
        resp.raise_for_status()
        return resp.json()

    def buy_activation(self, country: str, product: str, operator: str = "any") -> Dict[str, Any]:
        """
        Order a virtual number for a service.
        country: e.g. 'usa', 'france', 'spain', 'brazil', 'england', 'kazakhstan'
        product: e.g. 'google', 'tiktok', 'twitter', 'telegram'
        operator: e.g. 'any'
        """
        url = f"{BASE_URL}/user/buy/activation/{country}/{operator}/{product}"
        resp = self.session.get(url, timeout=20)
        resp.raise_for_status()
        return resp.json()

    def check_order(self, order_id: int) -> Dict[str, Any]:
        """Check order status and read received SMS."""
        url = f"{BASE_URL}/user/check/{order_id}"
        resp = self.session.get(url, timeout=15)
        resp.raise_for_status()
        return resp.json()

    def finish_order(self, order_id: int) -> Dict[str, Any]:
        """Finish order after SMS code received."""
        url = f"{BASE_URL}/user/finish/{order_id}"
        resp = self.session.get(url, timeout=15)
        resp.raise_for_status()
        return resp.json()

    def cancel_order(self, order_id: int) -> Dict[str, Any]:
        """Cancel order if SMS was not received (money refunded automatically)."""
        url = f"{BASE_URL}/user/cancel/{order_id}"
        resp = self.session.get(url, timeout=15)
        resp.raise_for_status()
        return resp.json()

    def wait_for_code(self, order_id: int, timeout_sec: int = 300, poll_interval: int = 5) -> Optional[str]:
        """Poll order until SMS code arrives or timeout."""
        start_ts = time.time()
        print(f"[5sim] Waiting for SMS code on order #{order_id} (timeout: {timeout_sec}s)...")
        while time.time() - start_ts < timeout_sec:
            order = self.check_order(order_id)
            status = order.get("status")
            sms_list = order.get("sms", [])
            if sms_list:
                latest_sms = sms_list[-1]
                code = latest_sms.get("code")
                text = latest_sms.get("text")
                print(f"[5sim] ✅ SMS received! Code: {code}, Text: {text}")
                self.finish_order(order_id)
                return code or text
            if status in ["CANCELED", "TIMEOUT", "BANNED"]:
                print(f"[5sim] Order status changed to {status}, aborting wait.")
                return None
            time.sleep(poll_interval)
        print(f"[5sim] Timed out waiting for SMS. Cancelling order #{order_id}...")
        self.cancel_order(order_id)
        return None

if __name__ == "__main__":
    client = FiveSimClient()
    try:
        profile = client.get_profile()
        print("=== 5SIM ACCOUNT PROFILE ===")
        print(f"User ID: {profile.get('id')}")
        print(f"Email:   {profile.get('email')}")
        print(f"Balance: {profile.get('balance')} RUB / USD")
        print(f"Rating:  {profile.get('rating')}")
    except Exception as e:
        print(f"Error checking profile: {e}")
