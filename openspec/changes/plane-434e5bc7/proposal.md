# Proposal: [smm-telegram] Мультиязычная сеть Telegram (RU, EN, FR, ES): бот-админ, публичные @username и чтение комментариев

## Context
Plane Task ID: `434e5bc7-7530-4ef1-8cba-aaa1f6707bec`
Module: `Social Media & Bot Ecosystem`
Parent Specs: `social-media-bot`, `verification-and-dod`

## Description
Развертывание и конфигурирование мультиязычной сети Telegram-каналов SmartBet.guru для 4 ключевых регионов вещания (RU, EN, FR, ES):
1. **Региональные каналы и идентификаторы**:
   - English (EN): ID `-3960368887` (peer `-1003960368887`), публичный юзернейм `@SmartBetGuruEN`.
   - France (FR): ID `-4371643544` (peer `-1004371643544`), публичный юзернейм `@SmartBetGuruFR`.
   - España (ES): ID `-4346736376` (peer `-1004346736376`), публичный юзернейм `@SmartBetGuruES`.
   - Россия (RU): мастер-канал `@SmartBetGuru` (или локализованный RU-канал).
   - Владелец каналов: аккаунт с телефонным номером `+79137671550`.

2. **Автоматизация управления через Telegram Web (`telegram_web_manager.py`)**:
   - Автоматическое назначение бота `@smartbet_guru_bot` администратором каналов с полными правами управления и публикации сообщений (`can_post_messages`, `can_edit_messages`, `can_delete_messages`, `can_invite_users`, `can_manage_chat`).
   - Установка и валидация публичных юзернеймов `@SmartBetGuruEN`, `@SmartBetGuruFR`, `@SmartBetGuruES`.
   - Создание и привязка групп обсуждения (Discussion Groups) к каждому региональному каналу для обеспечения комментариев под постами.
   - Персистентное хранение профиля сессии владельца в соответствии с Rule 9 (Firefox / Camoufox, `smm:profile:telegram:+79137671550` в Redis).

3. **Шедулер нативных публикаций `ChannelPosterScheduler`**:
   - Локализованный постинг сигналов по вилкам, валуйным ставкам и промо-акциям на 4 языках (RU, EN, FR, ES).
   - Расчет математического перекрытия фрибетов по формуле гарантированного 80% кэша (Rule 10: `eta = ((K1 - 1)(K2 - 1)) / K2 approx 0.80`).
   - Нативные карточки с бейджами ("🎯 Идеально для фрибета", "🎯 Freebet 80% Cash", "🎯 Idéal pour freebet", "🎯 Ideal para freebet").
   - Обязательный дисклеймер об ответственной игре (Responsible Gambling Disclaimer) на языке целевой аудитории.
   - Интерактивные кнопки (Inline Keyboard) с трекинг-ссылками на портал SmartBet.guru и букмекеров с UTM-параметрами.

4. **Интерактивное чтение комментариев и ИИ-суфлер**:
   - Подключение вебхука/поллинга для отслеживания входящих комментариев из привязанных групп обсуждения.
   - ИИ-суфлер (AI prompt assistant) для формулирования ответов на вопросы подписчиков.
   - Маршрутизация обращений пользователей в общую очередь Patron CRM (`feedback:queue:telegram`).
