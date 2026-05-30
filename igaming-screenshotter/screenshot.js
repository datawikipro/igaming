#!/usr/bin/env node

import { chromium } from 'playwright-chromium';
import path from 'path';
import fs from 'fs';

// Helper to print usage info
function printUsage() {
  console.log(`
🏆 iGaming Standalone Screenshot Utility
======================================
Usage:
  node screenshot.js <url> <outputPath> [width] [height] [--fullpage]

Arguments:
  url          The target HTTP/HTTPS URL of the match/event to open.
  outputPath   Path where the resulting PNG screenshot will be saved.
  width        Viewport width (default: 1280).
  height       Viewport height (default: 720).
  --fullpage   Capture a full-page screenshot instead of viewport.

Examples:
  node screenshot.js "https://fon.bet/sports/football" "./match.png"
  node screenshot.js "https://pari.ru/event/12345" "./pari.png" 1920 1080 --fullpage
  `);
}

async function main() {
  const args = process.argv.slice(2);

  // Check for help flags
  if (args.includes('--help') || args.includes('-h') || args.length < 2) {
    printUsage();
    process.exit(0);
  }

  // Parse arguments
  const url = args[0];
  const outputPath = path.resolve(args[1]);
  
  // Optional parameters
  const isFullPage = args.includes('--fullpage');
  
  // Filter out flags from remaining args to parse dimensions
  const dimensions = args.slice(2).filter(arg => !arg.startsWith('--'));
  const width = dimensions[0] ? parseInt(dimensions[0], 10) : 1280;
  const height = dimensions[1] ? parseInt(dimensions[1], 10) : 720;

  if (isNaN(width) || isNaN(height)) {
    console.error('❌ Error: Width and height must be valid integers.');
    process.exit(1);
  }

  console.log(`🚀 Starting screenshot capture...`);
  console.log(`🔗 Target URL:  ${url}`);
  console.log(`📂 Output Path: ${outputPath}`);
  console.log(`📐 Viewport:    ${width}x${height} (Fullpage: ${isFullPage})`);

  // Ensure output directory exists
  const outputDir = path.dirname(outputPath);
  if (!fs.existsSync(outputDir)) {
    fs.mkdirSync(outputDir, { recursive: true });
  }

  let browser;
  try {
    // Launch Chromium with stealth-oriented arguments
    browser = await chromium.launch({
      headless: true,
      args: [
        '--disable-web-security',
        '--disable-features=IsolateOrigins,site-per-process',
        '--blink-settings=imagesEnabled=true'
      ]
    });

    const context = await browser.newContext({
      viewport: { width, height },
      userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
      deviceScaleFactor: 1
    });

    const page = await context.newPage();
    
    console.log(`⏱️ Navigating to URL...`);
    await page.goto(url, {
      waitUntil: 'domcontentloaded',
      timeout: 20000 // 20s timeout
    });

    console.log(`⏳ Waiting for dynamic content/odds to settle...`);
    // Wait a bit to let client-side JS framework (React/Vue/Nuxt) render odds
    await page.waitForTimeout(3000);

    console.log(`📸 Taking screenshot...`);
    await page.screenshot({
      path: outputPath,
      fullPage: isFullPage
    });

    console.log(`✅ Success! Screenshot saved to: ${outputPath}`);
  } catch (error) {
    console.error(`❌ Failed to capture screenshot:`, error.message);
    process.exit(1);
  } finally {
    if (browser) {
      await browser.close();
    }
  }
}

main();
