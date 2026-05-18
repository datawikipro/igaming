const { chromium } = require('playwright');
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  await page.goto('https://winline.ru/live', { waitUntil: 'networkidle', timeout: 30000 });
  
  const js = () => {
      const rows = document.querySelectorAll('a[href*="/event/"]');
      return Array.from(rows).slice(0, 3).map(link => {
          const btns = Array.from(link.querySelectorAll('button'));
          const btnClasses = btns.map(b => b.className);
          const btnTexts = btns.map(b => b.textContent.trim());
          const divs = Array.from(link.querySelectorAll('div')).map(d => d.className);
          return { href: link.getAttribute('href'), btnClasses, btnTexts, first10Divs: divs.slice(0, 10) };
      });
  };
  
  const result = await page.evaluate(js);
  console.log(JSON.stringify(result, null, 2));
  await browser.close();
})();
