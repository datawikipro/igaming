
## Social Media Automation
When automating social networks (e.g., Threads or Instagram), DO NOT attempt to log in or create new sessions. An authenticated Chrome session is already configured and maintained.

- **Chrome Profile Location:** `/Users/aleksei.chernousov/Documents/igaming/sessions/chrome`
- **Credentials Location:** `/Users/aleksei.chernousov/Documents/igaming/passwords.txt`

**How to use:**
Launch headless Chrome with remote debugging on port 9222 using the existing profile:
```bash
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" --remote-debugging-port=9222 --headless=new --user-data-dir="/Users/aleksei.chernousov/Documents/igaming/sessions/chrome" --disable-gpu
```
Then read the websocket URL from `http://127.0.0.1:9222/json/version` and write it to `/Users/aleksei.chernousov/Library/Application Support/Google/Chrome/DevToolsActivePort` before invoking the browser subagent.
