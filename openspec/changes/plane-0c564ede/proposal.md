# Proposal: [enrichment] Добавить эндпоинт /resolve в MdmMatchController и поправить селектор Service/igaming-aggregator

## Context
Plane Task ID: `0c564ede-aecf-4c78-be7f-85a7925b99f4`
Target Module: `aggregator-core`

## Description
**Проблема:** `SofaTaskProcessor` вызывает `POST /api/v1/mdm/matches/resolve`, но контроллер `MdmMatchController.java` содержит только `@PostMapping("/find-or-create")` и `@GetMapping("/{id}")`. Запрос сопоставляется с `/{id}` (id='resolve') и отклоняется с кодом 405 Method Not Allowed.

**Что сделать:**

- В `MdmMatchController.java` добавить алиас: `@PostMapping({"/find-or-create", "/resolve"})`.
- Проверить компиляцию `igaming-mdm-service`.
