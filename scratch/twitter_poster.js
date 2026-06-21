const { chromium } = require('playwright');
const fs = require('fs');

const USERNAME = 'smartbetguru';
const EMAIL = 'www.smartbet.guru@gmail.com';
const PASSWORD = 'Racoiaws12'; // Предположительно тот же пароль, либо запросит почту
const IMAGE_PATH = '/Users/aleksei.chernousov/.gemini/antigravity/brain/40a6de76-884e-489a-97bf-de5d1cb2a9be/ig_test_post_1782034408165.png'; // Та же картинка

(async () => {
    console.log("Подключаемся к браузеру...");
    const browser = await chromium.connectOverCDP('http://127.0.0.1:9222');
    const context = browser.contexts()[0];
    
    let page = context.pages().find(p => p.url().includes('x.com') || p.url().includes('twitter.com'));
    if (!page) {
        page = await context.newPage();
    } else {
        await page.bringToFront();
    }
    
    await page.goto('https://x.com/', { waitUntil: 'networkidle' });
    
    // Проверяем куки
    try {
        const cookieBtn = page.locator('span:has-text("Accept all cookies")');
        if (await cookieBtn.isVisible({ timeout: 2000 })) {
            await cookieBtn.click();
        }
    } catch (e) {}

    // 1. АВТОРИЗАЦИЯ (если требуется)
    const loginBtn = page.locator('a[data-testid="loginButton"]');
    if (await loginBtn.isVisible({ timeout: 3000 })) {
        console.log("Выполняем вход в аккаунт X (Twitter)...");
        await loginBtn.click();
        
        // Вводим логин
        const usernameInput = page.locator('input[autocomplete="username"]');
        await usernameInput.waitFor({ state: 'visible' });
        await usernameInput.fill(USERNAME);
        await page.locator('button:has-text("Next")').click();
        
        // Иногда X запрашивает email для подтверждения перед паролем
        try {
            const emailInput = page.locator('input[data-testid="ocfEnterTextTextInput"]');
            if (await emailInput.isVisible({ timeout: 3000 })) {
                await emailInput.fill(EMAIL);
                await page.locator('button:has-text("Next")').click();
            }
        } catch(e) {}

        // Вводим пароль
        const passwordInput = page.locator('input[name="password"]');
        await passwordInput.waitFor({ state: 'visible' });
        await passwordInput.fill(PASSWORD);
        await page.locator('button[data-testid="LoginForm_Login_Button"]').click();
        
        await page.waitForLoadState('networkidle');
    } else {
        console.log("Уже авторизованы в X!");
    }

    // 2. ПУБЛИКАЦИЯ ПОСТА (Твита)
    console.log("Начинаем публикацию твита...");
    try {
        // Ждем текстовое поле
        const tweetBox = page.locator('div[data-testid="tweetTextarea_0"]');
        await tweetBox.waitFor({ state: 'visible', timeout: 10000 });
        
        // Вводим текст
        await tweetBox.click();
        await page.keyboard.type('Тестовый пост. Автоматизация через Playwright! 🚀 #automation #playwright');
        
        // Прикрепляем картинку
        console.log("Прикрепляем картинку...");
        const fileChooserPromise = page.waitForEvent('filechooser');
        // Клик по иконке загрузки медиа
        await page.locator('div[aria-label="Add photos or video"]').click();
        const fileChooser = await fileChooserPromise;
        await fileChooser.setFiles(IMAGE_PATH);
        
        // Ждем небольшую паузу для загрузки картинки в интерфейс
        await page.waitForTimeout(2000);
        
        // Отправляем твит
        console.log("Публикуем...");
        const sendTweetBtn = page.locator('button[data-testid="tweetButtonInline"]');
        await sendTweetBtn.click();
        
        // Ждем подтверждения "Your post was sent."
        await page.locator('span:has-text("Your post was sent.")').waitFor({ timeout: 10000 });
        console.log("Твит успешно опубликован!");
        
    } catch (err) {
        console.error("Ошибка при публикации твита:", err.message);
        await page.screenshot({ path: 'twitter_error_state.png' });
        console.log("Скриншот ошибки сохранен в twitter_error_state.png");
    }

    await browser.close();
    process.exit(0);
})();
