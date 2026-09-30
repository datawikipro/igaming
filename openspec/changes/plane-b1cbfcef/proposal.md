# Proposal: [video-shorts] Пайплайн автогенерации вертикальных видео (Shorts / Reels / TikTok) с разбором вилок и валуев

## Context
- **Plane Task ID**: `b1cbfcef-863a-4be2-b550-2c87ece37263`
- **Component**: `smm-agent` / `smm-video-shorts`
- **Relevant Golden Rules**:
  - Rule #1: Definition of Done & 5-минутный таймер
  - Rule #6: US Proxy routing (`100.83.113.50:3128` -> `outline-us` for American AI Video APIs like HeyGen / Runway / D-ID)
  - Rule #9: SMM & Браузерные ИИ-агенты (Firefox / Camoufox, запрет инкогнито, персистентность профилей и прогрев кэша)
  - Rule #10: Мониторинг фрибетов, партнерские ссылки и обязательные дисклеймеры

## Description
Автоматизированный пайплайн генерации вертикальных коротких видеороликов (формат 9:16) для YouTube Shorts, Instagram Reels и TikTok с разбором реальных вилочных ситуаций (>8% доходности) и валуев с привлечением вирального органического трафика на SmartBet.guru.

### Аппаратное ограничение сервера Xeon:
Сервер `xeon-srv` не имеет дискретного GPU. Локальный видеомонтаж и нейросетевой рендеринг строго вынесены в облачные внешние AI Video API (HeyGen, D-ID, Runway, Kling, InVideo) с асинхронной отдачей готового MP4 по вебхуку/URL без нагрузки на CPU хоста.

### Архитектура модуля:
1. **Генератор сценариев и хуков (Script & Hook Generator):**
   - Получение высокодоходных арбитражных ситуаций (>8%) из `igaming-portal` или пула вилок.
   - Синтез вирального сценария: цепляющий хук (первые 3 секунды), разбор плеч арбитража (букмекер 1, букмекер 2, коэффициенты), математический расчет гарантированной прибыли на 10 000 ₽ / 100$, призыв к действию (CTA) и обязательный дисклеймер ответственной игры.
   - Поддержка промптинга для LLM (через LLM Gateway или облачные модели).

2. **Облачный видеорендерер (Cloud Video API Client):**
   - Адаптеры интеграции с внешними AI Video сервисами:
     - HeyGen Video API (`/v2/video/generate`, проверка статуса, вебхук)
     - D-ID Talks API (`/talks`, вебхук)
     - Generic / Mock Cloud Video Provider для непрерывной интеграции и тестирования без внешних затрат.
   - Вебхук-сервер `/api/v1/shorts/webhook` и предоставление готового MP4 по прямой ссылке.

3. **Автопостинг и кросс-платформенная дистрибуция:**
   - Интеграция с `browser_manager.py` (Firefox/Camoufox persistent context, Rule 9) для безопасной публикации в YouTube Shorts, Instagram Reels и TikTok.
   - Обогащение описания реферальными ссылками через `affiliate_manager.py` (Rule 10) и UTM-метками.
   - Автоматическая генерация релевантных хэштегов (#shorts, #reels, #вилки, #арбитраж, #smartbet, #freebet).

4. **K8s манифест и Healthcheck:**
   - Деплоймент `igaming-k8s/smm-video-shorts.yaml` в namespace `igaming-dev`.
   - Actuator / HTTP пробы `/healthz` на порту 8080.
