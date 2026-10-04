# Design: [smm-telegram] Мультиязычная сеть Telegram (RU, EN, FR, ES)

## Архитектура компонента `smm-telegram`

### 1. Топология каналов и привязанных групп обсуждения
Сеть каналов SmartBet.guru разделена по целевым гео-локациям:
- **RU (Россия / СНГ)**: канал `@SmartBetGuru`, группа обсуждения `@SmartBetGuruChat`.
- **EN (Global English)**: канал ID `-3960368887` (Telegram peer `-1003960368887`), публичный юзернейм `@SmartBetGuruEN`, группа обсуждения `SmartBet Community | English Discussion`.
- **FR (France / Francophone)**: канал ID `-4371643544` (Telegram peer `-1004371643544`), публичный юзернейм `@SmartBetGuruFR`, группа обсуждения `SmartBet Communauté | Discussion France`.
- **ES (España / Hispanophone)**: канал ID `-4346736376` (Telegram peer `-1004346736376`), публичный юзернейм `@SmartBetGuruES`, группа обсуждения `SmartBet Comunidad | Discusión España`.

Все каналы созданы под учетной записью владельца `+79137671550`.

### 2. Автоматизация Telegram Web (`smm-agent/telegram_web_manager.py`)
- **Управление сессией**:
  - Использование Firefox Persistent Context (`launch_persistent_context`) согласно Правилу 9 (AGENTS.md).
  - Профиль сессии архивируется и восстанавливается через `ProfileSyncManager` по ключу `smm:profile:telegram:+79137671550` в Redis.
  - Поддержка симуляции/тестового режима без графического сервера для детерминированного выполнения в CI/K8s.
- **Функционал управления правами**:
  - `assign_bot_as_admin(channel_id, bot_username)`: Находит канал в списке чатов Telegram Web K/Z или вызывает Bot API / MTProto, добавляет бота `@smartbet_guru_bot` в список администраторов с правами:
    - `can_post_messages: true`
    - `can_edit_messages: true`
    - `can_delete_messages: true`
    - `can_invite_users: true`
    - `can_manage_chat: true`
  - `set_public_username(channel_id, username)`: Открывает настройки канала (Channel Info -> Edit -> Channel Type) и устанавливает публичную ссылку `@username`.
  - `create_and_link_discussion_group(channel_id, group_title)`: Создает привязанную супергруппу (Discussion Group) и линкует её к каналу через `setChatDiscussionGroup` или веб-интерфейс.

### 3. Публикация карточек `ChannelPosterScheduler`
- **Мультиязычная локализация контента**:
  - Словари локализации для 4 языков (RU, EN, FR, ES) с терминологией беттинга (Арбитраж / Arbitrage / Paris sûrs / Apuestas seguras).
  - Форматирование карточек исходов:
    - Вид спорта, турнир, команды.
    - Плечо 1 и Плечо 2 (букмекеры, маркет, коэффициент).
    - Доходность вилки (`profit_percent`).
    - Расчет гарантированного 80% кэша с фрибета (Правило 10: `eta = ((K1 - 1)(K2 - 1)) / K2 approx 0.80`).
    - Обязательный дисклеймер об ответственной игре на целевом языке.
- **Интерактивные кнопки (Inline Keyboard)**:
  - Кнопка перехода на калькулятор вилки SmartBet.guru: `https://smartbet.guru/tools/freebet-calculator?arb_id={id}&utm_source=telegram&utm_medium=channel_{lang}`.
  - Кнопки прямых партнерских ссылок на плечи букмекеров: `https://smartbet.guru/go/{bookmaker}?utm_source=telegram&utm_medium=channel_{lang}`.

### 4. Вебхук и чтение комментариев (ИИ-суфлер)
- **Прием обновлений**:
  - Эндпоинт `POST /api/v1/telegram/webhook` или встроенный диспетчер обновлений.
  - Фильтрация сообщений из привязанных групп обсуждения: `message.reply_to_message` или `message.is_automatic_forward` (авто-форварды из канала) и пользовательские комментарии к ним.
- **ИИ-суфлер (AI Assistant)**:
  - Анализ контекста исходного поста канала и комментария пользователя.
  - Генерация черновика ответа или авто-ответ с экспертным объяснением маржинальности, правил БК и ответственной игры.
  - Пересылка сложных или спонсорских обращений в очередь Patron CRM: `feedback:queue:telegram` со структурой `feedback_item`.

### 5. Инфраструктура и мониторинг
- Healthcheck сервер: HTTP порт 8080 (`/healthz`, `/actuator/health`).
- Манифест развертывания K8s: `igaming-k8s/smm-bot-telegram.yaml`.
- Неблокирующий старт и отказоустойчивость при сетевых задержках Telegram Bot API.
