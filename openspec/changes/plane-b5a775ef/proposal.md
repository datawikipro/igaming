# Proposal: [source-roobet] Исправление конфигурации и образа краулера Roobet

## Context
Plane Task ID: `b5a775ef-8807-4b93-91ee-ca535070a0e3`
Target Module: `monorepo`

## Description
**Проблема:** Деплоймент `igaming-source-roobet` развернут с образом `cloudbet:latest`, обращается к Cloudbet API и использует некорректный адрес Kafka `kafka.kafka.svc...`.

**Что сделать:**

- Скорректировать манифест `igaming-source-roobet`: образ `igaming-source-roobet:latest`, Kafka bootstrap `kafka.igaming-master.svc.cluster.local:9092`.
