# Proposal: [crawlers-probes] Тюнинг таймаутов Actuator проб для тяжелых JVM-краулеров (Zenit, Bookmaker, Melbet, Olimpbet KZ)

## Context
Plane Task ID: `ef8103ab-f6f8-4d92-a2c3-f1fc6736e9b9`
Target Module: `igaming-source-betsson`

## Description
**Проблема:** Тяжелые краулеры (`zenit-crawler` с 35 рестартами, `bookmaker` с 29 рестартами, `melbet-crawler` с 19 рестартами, `olimpbet-kz-json` с 18 рестартами, `betsafe/betsson` с 13 рестартами) периодически убиваются kubelet'ом по SIGKILL (Exit Code 137), так как Actuator-пробы отваливаются по таймауту `timeoutSeconds: 5` во время пауз Stop-The-World GC или разбора больших партий линий (2000+ событий).

**Что сделать:**

- В Kubernetes-манифестах для указанных краулеров поднять `timeoutSeconds: 10`, `failureThreshold: 6`, `periodSeconds: 15`.
- Обеспечить параметры JVM heap `-Xms256m -Xmx512m` в K8s env.
