const { chromium } = require('playwright');
const fs = require('fs');

const USERNAME = 'smartbetguru';
const PASSWORD = 'Racoiaws12'; // Из passwords.txt
const IMAGE_PATH = '/Users/aleksei.chernousov/.gemini/antigravity/brain/40a6de76-884e-489a-97bf-de5d1cb2a9be/ig_test_post_1782034408165.png'; // Наша тестовая картинка

(async () => {
    console.log("Подключаемся к браузеру...");
    const browser = await chromium.connectOverCDP('http://127.0.0.1:9222');
    const context = browser.contexts()[0];
    
    let page = context.pages().find(p => p.url().includes('instagram.com'));
    if (!page) {
        page = await context.newPage();
    } else {
        await page.bringToFront();
    }
    
    await page.goto('https://www.instagram.com/', { waitUntil: 'networkidle' });
    
    // Проверка на баннер с куками
    try {
        const cookieBtn = page.locator('button:has-text("Allow all cookies")');
        if (await cookieBtn.isVisible({ timeout: 2000 })) {
            await cookieBtn.click();
        }
    } catch (e) {}

    // 1. АВТОРИЗАЦИЯ
    const loginInput = page.locator('input[name="username"]');
    if (await loginInput.isVisible({ timeout: 3000 })) {
        console.log("Выполняем вход в аккаунт...");
        await loginInput.fill(USERNAME);
        await page.locator('input[name="password"]').fill(PASSWORD);
        await page.locator('button[type="submit"]').click();
        await page.waitForLoadState('networkidle');
        
        // Обработка окон "Save info" или "Turn on Notifications" если они появятся
        try {
            const notNowBtn = page.locator('button:has-text("Not Now"), div[role="button"]:has-text("Not Now")').first();
            if (await notNowBtn.isVisible({ timeout: 5000 })) {
                await notNowBtn.click();
            }
        } catch (e) {}
    } else {
        console.log("Уже авторизованы!");
    }

    // 2. ПУБЛИКАЦИЯ ПОСТА
    console.log("Начинаем публикацию...");
    // Ищем кнопку создания поста (обычно это SVG с aria-label "New post" или ссылка)
    // Альтернативно можно напрямую перейти на страницу создания, если она поддерживается,
    // но в веб-версии Инстаграма это модальное окно.
    try {
        // Кликаем на кнопку "Create" в боковом меню
        const createBtn = page.locator('svg[aria-label="New post"]').locator('..').locator('..');
        await createBtn.click();
        
        console.log("Ожидаем окно загрузки файла...");
        // Инстаграм использует input type="file" скрытый в DOM
        const [fileChooser] = await Promise.all([
            page.waitForEvent('filechooser'),
            page.locator('button:has-text("Select from computer")').click()
        ]);
        
        console.log("Загружаем картинку...");
        await fileChooser.setFiles(IMAGE_PATH);
        
        // Шаг "Crop" - жмем Next
        console.log("Шаг 1: Next...");
        await page.locator('div[role="button"]:has-text("Next")').first().click();
        
        // Шаг "Filters" - жмем Next
        console.log("Шаг 2: Next...");
        await page.locator('div[role="button"]:has-text("Next")').first().click();
        
        // Шаг "Caption" - вводим текст
        console.log("Вводим описание...");
        const captionArea = page.locator('div[aria-label="Write a caption..."]');
        await captionArea.fill('Тестовый пост. Автоматизация через Playwright! 🚀 #automation #playwright');
        
        // Жмем Share
        console.log("Публикуем (Share)...");
        await page.locator('div[role="button"]:has-text("Share")').first().click();
        
        // Ждем завершения
        await page.waitForSelector('img[alt="Animated checkmark"]', { timeout: 15000 });
        console.log("Пост успешно опубликован!");
        
    } catch (err) {
        console.error("Ошибка при публикации:", err.message);
        await page.screenshot({ path: 'error_state.png' });
        console.log("Скриншот ошибки сохранен в error_state.png");
    }

    await browser.close();
    process.exit(0);
})();
