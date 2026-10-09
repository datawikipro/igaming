import pyotp
import time
from typing import Optional, Dict, Any

class TotpService:
    @staticmethod
    def generate_current_code(secret: str) -> Dict[str, Any]:
        """
        Generates current 6-digit TOTP code and time remaining.
        Secret must be a Base32 encoded string.
        """
        # Clean secret (remove spaces/dashes)
        clean_secret = secret.replace(" ", "").replace("-", "").strip().upper()
        totp = pyotp.TOTP(clean_secret)
        code = totp.now()
        
        # Calculate seconds remaining until next 30-sec window
        now = time.time()
        time_remaining = int(30 - (now % 30))
        
        return {
            "code": code,
            "expires_in_seconds": time_remaining,
            "period": 30
        }

    @staticmethod
    def verify_code(secret: str, code: str) -> bool:
        """
        Verifies if provided code is valid for secret (allowing +-1 step drift).
        """
        clean_secret = secret.replace(" ", "").replace("-", "").strip().upper()
        totp = pyotp.TOTP(clean_secret)
        return bool(totp.verify(code, valid_window=1))
