# Design: [feedback-admin-ui] Единая админ-панель обратной связи в smartbet.guru (/admin/feedback)

## Architecture Overview

```mermaid
graph TD
    A[Входящий фидбек: Boosty / Patreon / VK Donut / TG VIP / Web] --> B[Feedback & Patron Desk UI: /[locale]/admin/feedback]
    B --> C[Метрики KPI: MRR, SLA VIP, Внедрено, Бэклог]
    B --> D[Фильтрация: Донатеры P1/P2, Фичи, Баги, Вопросы, Решенные]
    B --> E[Карточки обращений: Золотой VIP бейдж / Серый Free бейдж]
    E --> F1[🚀 В один клик в Plane -> Модалка создания задачи]
    E --> F2[⚡ AI-ответ -> Персонализированный драфт]
    E --> F3[💬 Ответить в канал -> Boosty / TG / VK / Email]
    E --> F4[✅ Архив / Решено -> Смена статуса]
```

## Component Architecture

1. **Маршрут страницы**:
   - `src/app/[locale]/admin/feedback/page.tsx`
   - Использование `useAuth` и `hasAdminAccess` для защиты маршрута.
   - Рендеринг `FeedbackAdminDashboard`.

2. **Основной компонент**:
   - `src/components/admin/FeedbackAdminDashboard.tsx`
   - Модели данных:
     - `FeedbackTicket`: тикет с полями источника, автора, статуса донатера (VIP-тир, месячный чек, LTV), категории, приоритета, текста, AI-выжимки, AI-ответа, таймера SLA, привязки к Plane Issue.
     - `DashboardKPIs`: суммарный MRR, открытые VIP, внедренные фичи, размер бэклога.
   - Интерактивные состояния:
     - Выбор активного таба («Все», «Донатеры P1/P2», «Фичи», «Баги», «Вопросы», «Соцсети», «Решенные»).
     - Полнотекстовый поиск по тексту обращения, никнейму автора, платформе.
     - Модальное окно `PlaneModal`: предзаполненная форма создания задачи в Plane с тегами `vip-patron`, `community-request`.
     - Модальное окно `ReplyModal`: отправка персонального ответа в соответствующую платформу (Boosty, TG, VK Donut, Patreon) с шаблонами и AI-драфтом.
     - Живой таймер обратного отсчета SLA для VIP-тикетов (15 минут).
     - Переключение светлой/темной темы через CSS-переменные дизайн-системы SmartBet.guru.

3. **Стилизация**:
   - `src/components/admin/FeedbackAdminDashboard.css`
   - Стеклянные карточки (backdrop-filter: blur(20px)).
   - Золотой градиент для VIP-патронов (`linear-gradient(135deg, #f59e0b, #d97706)`).
   - Анимированные индикаторы SLA, пульсирующие бейджи для P1_URGENT.
   - Адаптивная верстка (мобильные экраны, планшеты, десктоп).

4. **Интеграция в общую админ-панель**:
   - Обновление `src/app/[locale]/admin/page.tsx` с добавлением карточки "Обратная связь и Патроны (Patron Hub)".
