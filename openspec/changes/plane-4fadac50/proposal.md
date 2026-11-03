# Proposal: [crawlers-routing] FQDN адресация внутренних сервисов в bet365 и sbobet crawlers

## Context
Plane Task ID: `4fadac50-9c8b-4a9a-af12-b007949819ae`
Target Module: `aggregator-core`

## Description
**Проблема:** `igaming-source-bet365-crawler` и `igaming-source-sbobet-crawler` используют короткие имена хостов: `http://igaming-aggregator` и `http://igaming-mdm-service`. Они не подпадают под маску `*.cluster.local` в `nonProxyHosts` и уходят в европейский прокси PureVPN (`purevpn-uk-direct`), который возвращает HTTP 500.

**Что сделать:**

- В модулях `igaming-source-bet365` и `igaming-source-sbobet` в конфигурациях по умолчанию указать полные FQDN: `http://igaming-aggregator.igaming-dev.svc.cluster.local:8080` и `http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095`.
- В настройках JVM и параметрах запуска добавить короткие имена `igaming-aggregator*|igaming-mdm-service*` в `http.nonProxyHosts`.
