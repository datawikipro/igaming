import imaplib
import email
from email.header import decode_header
import re
import time
import logging
from typing import Optional, Dict, Any, List

logger = logging.getLogger("imap_service")

# Regex patterns for extracting 2FA verification codes
PATTERNS = {
    "instagram": [
        r"(\d{6})\s+is\s+your\s+Instagram\s+code",
        r"код\s+подтверждения[:\s]+(\d{6})",
        r"\b(\d{6})\b"
    ],
    "threads": [
        r"(\d{6})\s+is\s+your\s+Threads\s+code",
        r"\b(\d{6})\b"
    ],
    "reddit": [
        r"verification\s+code\s+is\s+(\d{6})",
        r"код\s+верификации[:\s]+(\d{6})",
        r"\b(\d{6})\b"
    ],
    "twitter": [
        r"confirmation\s+code\s+is\s+([A-Za-z0-9]{6,8})",
        r"ваш\s+код[:\s]+([A-Za-z0-9]{6,8})",
        r"\b(\d{6})\b"
    ],
    "default": [
        r"\b(\d{6})\b",
        r"\b(\d{4,8})\b"
    ]
}

def clean_subject(raw_subject: str) -> str:
    if not raw_subject:
        return ""
    decoded_fragments = decode_header(raw_subject)
    res = []
    for fragment, charset in decoded_fragments:
        if isinstance(fragment, bytes):
            try:
                res.append(fragment.decode(charset or "utf-8", errors="replace"))
            except Exception:
                res.append(fragment.decode("utf-8", errors="replace"))
        else:
            res.append(str(fragment))
    return "".join(res)

class ImapInboxService:
    @staticmethod
    def get_latest_code(
        host: str,
        user: str,
        password: str,
        service_name: str = "default",
        port: int = 993,
        timeout_seconds: int = 45,
        poll_interval: int = 3
    ) -> Optional[Dict[str, Any]]:
        """
        Polls IMAP mailbox for a new 2FA verification email and extracts the code.
        """
        start_time = time.time()
        service_key = service_name.lower().strip()
        regex_list = PATTERNS.get(service_key, PATTERNS["default"])

        while time.time() - start_time < timeout_seconds:
            mail = None
            try:
                mail = imaplib.IMAP4_SSL(host, port, timeout=10)
                mail.login(user, password)
                mail.select("INBOX")

                # Search UNSEEN or ALL messages from the last few minutes
                status, messages = mail.search(None, "UNSEEN")
                if status != "OK" or not messages[0]:
                    # Also try recent messages if none unread
                    status, messages = mail.search(None, "ALL")

                if status == "OK" and messages[0]:
                    msg_ids = messages[0].split()
                    # Inspect recent 5 messages in reverse
                    for msg_id in reversed(msg_ids[-5:]):
                        res, data = mail.fetch(msg_id, "(RFC822)")
                        if res != "OK":
                            continue

                        raw_email = data[0][1]
                        msg = email.message_from_bytes(raw_email)
                        subject = clean_subject(msg.get("Subject", ""))
                        sender = msg.get("From", "")

                        # Extract text payload
                        body_text = ""
                        if msg.is_multipart():
                            for part in msg.walk():
                                ctype = part.get_content_type()
                                if ctype in ["text/plain", "text/html"]:
                                    payload = part.get_payload(decode=True)
                                    if payload:
                                        body_text += payload.decode("utf-8", errors="replace") + "\n"
                        else:
                            payload = msg.get_payload(decode=True)
                            if payload:
                                body_text = payload.decode("utf-8", errors="replace")

                        full_content = f"{subject}\n{body_text}"

                        # Try to match patterns
                        for pattern in regex_list:
                            match = re.search(pattern, full_content, re.IGNORECASE)
                            if match:
                                code = match.group(1)
                                logger.info(f"Successfully matched 2FA code {code} from {sender} for {service_name}")
                                # Mark as read
                                try:
                                    mail.store(msg_id, "+FLAGS", "\\Seen")
                                except Exception:
                                    pass
                                return {
                                    "code": code,
                                    "service": service_name,
                                    "sender": sender,
                                    "subject": subject,
                                    "found_at": int(time.time())
                                }

            except Exception as e:
                logger.warning(f"Error checking IMAP inbox for {user}: {e}")
            finally:
                if mail:
                    try:
                        mail.close()
                        mail.logout()
                    except Exception:
                        pass

            time.sleep(poll_interval)

        return None
