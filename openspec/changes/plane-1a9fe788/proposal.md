# Proposal: #41: [frontend] Архитектурный рефакторинг маппинга исходов: ликвидация спагетти if-else, строгие коды маркетов и next-intl словари

## Context
Plane Task ID: `1a9fe788-bbc2-41a4-83b9-b00f691b376b`

## Description
Устранить процедурные спагетти-конструкции if-else и switch в компонентах фронтенда (MatchDetailClient.tsx, TournamentBracket.tsx, MatchesClient.tsx).
Перейти на строгий архитектурный контракт: передача канонических кодов маркетов/исходов из API и использование библиотеки локализации next-intl со словарями переводов (messages/ru.json, messages/en.json и др.).

### Архитектурные требования:
1. Создать канонический справочник кодов исходов и маркетов в `src/lib/constants/marketCodes.ts`.
2. Вынести все строковые представления рынков и исходов в i18n-словари (`messages/ru.json`, `messages/en.json` и др.) в секции `markets` и `outcomes`.
3. Реализовать единый хук/утилиту форматирования `useMarketFormatter()` и `formatOutcome(code, param, t)`.
4. Провести рефакторинг компонентов (`MatchDetailClient.tsx`, `TournamentBracket.tsx`, `MatchesClient.tsx`) с устранением спагетти `if-else` и проверок `currentLang === 'ru'`.
5. Обеспечить успешную компиляцию `npm run build` и мгновенное переключение RU <-> EN без дефектов верстки.
