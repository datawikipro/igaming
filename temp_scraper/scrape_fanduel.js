const { chromium } = require('playwright');
const fs = require('fs');

(async () => {
  const browser = await chromium.launch({ headless: true }); // Headless might be blocked, but let's try
  const context = await browser.newContext({
    userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
  });
  const page = await context.newPage();
  
  let captured = false;
  
  page.on('response', async response => {
    const url = response.url();
    // FanDuel APIs use something like /api/v5/eventgroups
    if (url.includes('eventgroup')) {
      console.log('Intercepted:', url);
      try {
        const text = await response.text();
        fs.writeFileSync('fanduel_response.json', text);
        captured = true;
        console.log('Saved to fanduel_response.json');
      } catch (e) {
        console.error('Failed to read response body', e);
      }
    }
  });

  try {
    await page.goto('https://sportsbook.fanduel.com/', { waitUntil: 'domcontentloaded', timeout: 30000 });
    // Wait a bit to ensure all background XHRs fire
    await page.waitForTimeout(10000);
  } catch (e) {
    console.log('Navigation error:', e);
  }
  
  await browser.close();
  if (captured) {
      console.log("SUCCESS");
  } else {
      console.log("NO_JSON_CAPTURED");
  }
})();
