# iGaming Standalone Screenshot Utility

A dedicated, lightweight Node.js utility powered by Playwright to take quick, high-quality screenshots of sports booking event pages and exit cleanly. By moving this logic out of the main JVM services, we achieve a much smaller memory and CPU footprint, cleaner container design, and lower overhead.

## Setup

1. **Install dependencies**:
   ```bash
   npm install
   ```

2. **Install Playwright Chromium**:
   ```bash
   npx playwright install chromium
   ```

## Usage

Run the utility from your command line:

```bash
node screenshot.js <url> <outputPath> [width] [height] [--fullpage]
```

### Examples

- **Standard viewport PNG**:
  ```bash
  node screenshot.js "https://fon.bet/sports/football" "./fonbet.png"
  ```

- **Full-page high-resolution custom screen size**:
  ```bash
  node screenshot.js "https://pari.ru/event/12345" "./pari.png" 1920 1080 --fullpage
  ```

## Integration with main iGaming suite

This project functions independently as a standalone worker or script. In production environments, it can be scheduled as a Kubernetes batch `Job`, run as an AWS Lambda / serverless container, or invoked via subprocess in any target environment.
