import json
import os
import redis
from typing import Optional, Dict, Any

REDIS_HOST = os.getenv("REDIS_HOST", "igaming-redis.igaming-dev.svc.cluster.local")
REDIS_PORT = int(os.getenv("REDIS_PORT", "6379"))
REDIS_DB = int(os.getenv("REDIS_DB", "0"))

class AccountStorage:
    def __init__(self):
        self.client = redis.Redis(
            host=REDIS_HOST,
            port=REDIS_PORT,
            db=REDIS_DB,
            decode_responses=True,
            socket_timeout=5,
            socket_connect_timeout=5
        )

    def ping(self) -> bool:
        try:
            return self.client.ping()
        except Exception:
            return False

    def save_account(self, account_id: str, data: Dict[str, Any]) -> None:
        key = f"smm:account:{account_id}"
        self.client.set(key, json.dumps(data, ensure_ascii=False))

    def get_account(self, account_id: str) -> Optional[Dict[str, Any]]:
        key = f"smm:account:{account_id}"
        raw = self.client.get(key)
        if not raw:
            return None
        return json.loads(raw)

    def list_accounts(self) -> list[Dict[str, Any]]:
        keys = self.client.keys("smm:account:*")
        accounts = []
        for k in keys:
            raw = self.client.get(k)
            if raw:
                try:
                    accounts.append(json.loads(raw))
                except Exception:
                    pass
        return accounts

    def save_cookies(self, account_id: str, cookies: Any) -> None:
        key = f"smm:cookies:{account_id}"
        self.client.set(key, json.dumps(cookies, ensure_ascii=False))

    def get_cookies(self, account_id: str) -> Optional[Any]:
        key = f"smm:cookies:{account_id}"
        raw = self.client.get(key)
        if not raw:
            return None
        return json.loads(raw)
