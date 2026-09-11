# Tasks: Audit and Scale Bookmakers Pipeline

## 1. Top Active Bookmakers Log Audit & Health Check
- [ ] 1.1 **Fonbet RU** (`igaming-source-fon-bet-ru`): проверить логи краулера и лоадера, ротацию прокси и парсинг росписи.
- [ ] 1.2 **Leon RU** (`igaming-source-leon`): проверить логи лоадера и краулера, отсутствие ошибок десериализации.
- [ ] 1.3 **Pari RU** (`igaming-source-pari`): проверить логи лоадера и краулера, маппинг исходов.
- [ ] 1.4 **Winline RU** (`igaming-source-winline`): проверить логи браузерного краулера и лоадера, исправить ошибки сессий.
- [ ] 1.5 **Zenit** (`igaming-source-zenit`): проверить логи прематч-парсера и отправку котировок в Kafka.
- [ ] 1.6 **Baltbet** (`igaming-source-baltbet`): проверить логи краулера и лоадера.
- [ ] 1.7 **Betcity RU & COM** (`igaming-source-betcity`, `igaming-source-betcity-com`): проверить логи и исходы.
- [ ] 1.8 **Marathonbet RU & COM** (`igaming-source-marathonbet`, `igaming-source-marathonbet-com`): проверить логи и сетевой стек.

## 2. International & Sharp Bookmakers Audit
- [ ] 2.1 **Pinnacle** (`igaming-source-pinnacle`): проверить логи коннектора, статус парсинга sharp-линий.
- [ ] 2.2 **Sbobet** (`igaming-source-sbobet`): проверить логи азиатской линии и устранить ошибки ретраев.
- [ ] 2.3 **Bwin** (`igaming-source-bwin`): проверить логи краулера и лоадера европейской линии.
- [ ] 2.4 **Dafabet** (`igaming-source-dafabet`): проверить логи краулера и лоадера азиатской линии.

## 3. 1xBet Group Pool Audit & Launching Additional Clones
- [ ] 3.1 **1xBet & Betwinner & Melbet**: аудит логов и устранение ошибок маппинга.
- [ ] 3.2 **Spinbetter & Linebet & Megapari & Fansport**: аудит логов и стабильности Kafka продюсера.
- [ ] 3.3 **22Bet & 1xBit & 888Starz & Betandyou**: развертывание манифестов и проверка запуска.

## 4. Other Regional Bookmakers Audit & Deployment
- [ ] 4.1 **Tennisi** (`igaming-source-tennisi`): аудит логов и устранение сбоев росписи.
- [ ] 4.2 **Liga Stavok** (`igaming-source-ligastavok`): аудит логов и статуса подключения.
- [ ] 4.3 **Olimpbet RU & KZ** (`igaming-source-olimpbet`, `igaming-source-olimpbet-kz`): аудит логов и запуск KZ.
- [ ] 4.4 **Fonbet KZ & BY** (`igaming-source-fon-bet-kz`, `igaming-source-fon-bet-by`): запуск и верификация.

## 5. End-to-End Cluster Validation & DoD
- [ ] 5.1 Проверить ресурсы кластера (`kubectl top nodes`) и стабильность памяти.
- [ ] 5.2 Проверить метрики `odds_actual` и генерации вилок в `surebet_alert`.
- [ ] 5.3 Выполнить `openspec validate --specs` и зафиксировать результаты.
