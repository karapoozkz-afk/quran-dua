// Renders store/graphics.html into the PNGs Google Play asks for: icon 512×512, feature graphic 1024×500.
// Usage: node tools/render_store_graphics.js   (needs the playwright package and a Chromium)
const { chromium } = require('playwright');
const path = require('path');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ deviceScaleFactor: 1 });
  const src = 'file://' + path.resolve(__dirname, '../store/graphics.html');
  const jobs = [
    ['icon', 'icon-512.png', 512, 512],
    ['ru', 'feature-ru-1024x500.png', 1024, 500],
    ['en', 'feature-en-1024x500.png', 1024, 500],
  ];
  for (const [kind, file, w, h] of jobs) {
    await page.setViewportSize({ width: w, height: h });
    await page.goto(`${src}?kind=${kind}`);
    await page.evaluate(() => document.fonts.ready);
    await page.screenshot({ path: path.resolve(__dirname, '../store', file), clip: { x: 0, y: 0, width: w, height: h } });
    console.log('saved store/' + file);
  }
  await browser.close();
})();
