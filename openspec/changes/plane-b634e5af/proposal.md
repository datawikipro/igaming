# Proposal: [pod-patreon] Выделенный pod и агент Patreon: изолированная учетка, US Proxy, Webhooks & Member Desk

## Context
- Plane Task ID: `b634e5af-0435-465a-9c1c-61ad41ce0676`
- Component: `smm-bot-patreon` (SMM Growth Swarm / Patron CRM & Community Feedback Hub)
- Namespace: `igaming-dev`

## Description
Развертывание выделенного автономного пода в Kubernetes (`smm-bot-patreon`) с выделенной учетной записью создателя Patreon.
Модуль обеспечивает:
1. **Изолированную сессию в Redis**: персистентное хранилище `smm:session:patreon` (Patreon OAuth2 Refresh/Access tokens, webhook secrets, кэш уровней подписки и донатеров).
2. **Сетевую маршрутизацию через US Proxy**: принудительная маршрутизация исходящего трафика через кластерный прокси `http://100.83.113.50:3128` (выходной шлюз США `outline-us` `100.66.190.4`) во избежание региональных блокировок Patreon.
3. **Приём Webhook API v2**: эндпоинт `/webhooks/patreon` для обработки событий `members:pledge:create`, `members:pledge:update`, `members:pledge:delete`, `posts:comments:create` с обязательной валидацией HMAC-SHA256 подписи в заголовке `X-Patreon-Signature`.
4. **Member Desk & Регистрация в Feedback Item**:
   - Автоматическая трансляция обращений и комментариев патронов в структурированные тикеты с атрибутами: `is_paid=True`, `currency="USD"`, фиксация уровня поддержки ($25/mo, $100/mo) и LTV.
   - Назначение наивысшего приоритета `P1_URGENT_PATRON` для платных патронов.
5. **Публикация эксклюзивного премиум-контента**:
   - Пайплайн публикации закрытых постов на английском языке для зарубежных вилочников (Surebet сигналы >10%, коридоры, математика 80% фрибетов в кэш).
6. **Healthcheck & Definition of Done**:
   - Встроенный HTTP health endpoint `/healthz` и `/actuator/health` на порту 8080.
   - K8s Deployment `smm-bot-patreon` в `igaming-dev`.
   - 5-минутный тест стабильности (5-Minute Soak Window) без единой ошибки и рестарта.

## Success Criteria
- [x] Создан архитектурный документ `design.md` с описанием потоков данных, HMAC-валидации и сессионного хранилища.
- [x] Реализован модуль `smm-agent/patreon_agent.py` с обработчиком вебхуков, Member Desk, валидацией HMAC-SHA256 и интеграцией с Redis.
- [x] Разработан тестовый набор `smm-agent/test_patreon_agent.py` с полным покрытием бизнес-логики.
- [x] Подготовлен Kubernetes манифест `igaming-k8s/smm-bot-patreon.yaml`.
- [x] Произведена верификация в кластере Kubernetes `igaming-dev` с соблюдением 5-минутного правила стабильности.
