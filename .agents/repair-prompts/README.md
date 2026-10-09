# 🚀 SmartBet.guru — Пакет промптов для пошагового ремонта источников

В этой директории находятся 8 готовых, автономных заданий для AI-агентов. Каждое задание содержит исчерпывающий контекст, точные команды диагностики, сборки на сервере Xeon и критерии Definition of Done (DoD).

Запускайте агентов последовательно (один за другим), передавая соответствующий файл:

| № | Файл задания | Букмекер | Модуль / Движок | Ключевая проблема |
|---|---|---|---|---|
| **01** | [`01-repair-pinnacle.md`](./01-repair-pinnacle.md) | **Pinnacle** | `igaming-source-pinnacle` | `GenericMatchLoadScheduler` DI UnsatisfiedDependency |
| **02** | [`02-repair-betfair.md`](./02-repair-betfair.md) | **Betfair** | `igaming-source-betfair` | Застрявший `wait-for-db` / containerd lockup |
| **03** | [`03-repair-stoiximan.md`](./03-repair-stoiximan.md) | **Stoiximan** | `igaming-source-888sport` | Ошибочный Direct bypass в VpnManager для оффшорной БК |
| **04** | [`04-repair-digitain.md`](./04-repair-digitain.md) | **Digitain** | `igaming-source-digitain` | Рассинхронизация портов/проб в crawler & loader |
| **05** | [`05-repair-svenskaspel.md`](./05-repair-svenskaspel.md) | **Svenskaspel** | `igaming-source-unibet` | SOCKS-прокси перехватывает JDBC к локальной БД PostgreSQL |
| **06** | [`06-repair-redbet.md`](./06-repair-redbet.md) | **Redbet** | `igaming-source-unibet` | SOCKS-прокси перехватывает JDBC к локальной БД PostgreSQL |
| **07** | [`07-repair-wplay.md`](./07-repair-wplay.md) | **Wplay** | `igaming-source-bet7k` | Затянутый старт Spring / Readiness Probe failure |
| **08** | [`08-repair-betmotion.md`](./08-repair-betmotion.md) | **Betmotion** | `igaming-source-bet7k` | Ошибки пула ghcr.io / перевод на локальный реестр 30500 |

---

### 📌 Непреложные правила выполнения для любого агента:
1. **5 минут чистого хода (DoD)**: под обязан отработать 5 минут в статусе `Running` без единого рестарта.
2. **Линия >= 500 матчей**: `SELECT count(*) FROM match_cache;` >= 500.
3. **Сборка только на Xeon**: `mvn ... jib:build` в реестр `100.78.183.101:30500`.
4. **Не пушить в master** до подтверждения зеленого статуса в кластере.
