const { chromium } = require('playwright');
const fs = require('fs');

(async () => {
    console.log("Connecting to Chrome over CDP...");
    const browser = await chromium.connectOverCDP('http://127.0.0.1:9222');
    console.log("Connected.");
    
    const context = browser.contexts()[0];
    
    let page = context.pages().find(p => p.url().includes('instagram.com'));
    if (!page) {
        console.log("Creating new page...");
        page = await context.newPage();
    } else {
        console.log("Bringing existing Instagram page to front...");
        await page.bringToFront();
    }
    
    console.log("Navigating to Instagram...");
    await page.goto('https://www.instagram.com/', { waitUntil: 'networkidle' });
    console.log("Loaded:", await page.title());
    
    // Click "Allow all cookies" if present
    try {
        const cookieButton = page.locator('button:has-text("Allow all cookies")');
        if (await cookieButton.isVisible({ timeout: 3000 })) {
            console.log("Cookie button found, clicking...");
            await cookieButton.click();
            await page.waitForTimeout(2000); // Wait for the modal to disappear
        }
    } catch (e) {
        console.log("No cookie banner found.");
    }
    
    console.log("Taking screenshot after cookie banner check...");
    await page.screenshot({ path: 'instagram_home2.png' });
    console.log("Screenshot saved to instagram_home2.png");
    
    // Close the browser connection correctly
    await browser.close(); // for CDP this closes connection, not the browser process, actually wait, earlier it failed with browser.disconnect. Let's just exit process.
    console.log("Done.");
    process.exit(0);
})();
