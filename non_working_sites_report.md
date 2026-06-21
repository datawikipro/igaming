# Отчет по доступности Idle-букмекеров (21 Июня 2026)

В этом отчете собраны результаты проверки 25 букмекеров, которые находились в статусе `idle` или `offline`. Сайты проверены обычным HTTP-запросом.

## 1. Открываются штатно (OK — Status 200)
Эти букмекеры отдают нормальную страницу без капчи и блокировок для обычного запроса без JS. Если они у нас `idle`, проблема локализована в нашем коде парсера, конфигурации (селекторы, таймауты) или прокси-пуле для конкретного сервиса.

* 1xbet.com
* 22bet.com
* 888sport.com
* betandyou.com
* betlabel.com
* betwinner.com
* betcity.com
* betsson.com
* caesars.com
* dafabet.com
* fanduel.com
* fan-sport.com
* megapari.com
* sportbet.ru
* sunbet.co.za
* fon.bet (Fonbet)

## 2. Глухая блокировка Cloudflare (Status 403 Access Denied)
Сайты отклоняют запрос на уровне CDN. Требуется использование `HEADLESS_STEALTH` / `XVFB_HEADED` профилей и качественных резидентных прокси.

* bet365.com
* betmgm.com

## 3. Интерактивная защита Cloudflare (Status 200, отдается JS-заглушка)
Сайт доступен, но вместо контента отдает страницу с проверкой браузера (Cloudflare Turnstile/Captcha). Краулер застревает на этом экране. Требуется полноценный `stealth`-режим в Playwright.

* bwin.com
* marathonbet.com
* pinnacle.com
* rushbet.co

## 4. Специфические ошибки и Geo-блоки
* ligastavok.ru — возвращает ошибку `401 Unauthorized` от сервера защиты **Qrator**. (Блокировка по гео-признаку или сигнатуре).
* linebet.com — возвращает нестандартный статус `203 Non-Authoritative Information` от `nginx`.
* melbet.com — возвращает статус `203` от веб-сервера `angie`. 
