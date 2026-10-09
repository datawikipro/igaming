# 📋 [IGAMING-SRC-NIKE] Интеграция и наполнение линии Niké (Словакия)

## 📌 Метаданные задачи (Plane)
- **ID**: `IGAMING-SRC-NIKE`
- **Эпик**: `[EPIC-CRAWLERS]`
- **Модуль**: `igaming-source-nike`
- **Манифест K8s**: `igaming-k8s/nike.yaml`
- **Приоритет**: `High`
- **Теги**: `nike`, `crawler`, `loader`, `slovakia`, `k8s-probes`, `robot-task`
- **Статус**: `In Progress`

---

## 🎯 Цели и критерии задачи
1. **Подключение и развертывание Niké**: Крупнейший букмекер Словакии (Niké, `nike.sk`).
2. **Golden Rule #8 (DoD)**: Наполнение базы данных `igaming_nike` не менее чем **500 активными матчами** (`SELECT count(*) FROM match_cache >= 500`).
3. **Экономия ресурсов Xeon (Rule #2)**: При достижении $\ge 500$ матчей в базе немедленно масштабировать деплоймент в `replicas=0` (`kubectl scale deployment/igaming-source-nike --replicas=0 -n igaming-source`).
4. **Стабильность**: 5 минут непрерывной работы без сбоев (`CrashLoopBackOff`) и ошибок парсинга перед выключением.

---

## 🤖 Пошаговый план реализации
1. **Проверка API и сетевого маршрута**:
   - Маршрутизация запросов к `nike.sk` через европейский egress `outline-fi` (`http://100.83.113.50:3128`).
   - Проверка структуры ответов REST API / WebSocket.
2. **Сборка и доставка OCI-образа**:
   - Компиляция `igaming-source-nike` на `xeon-srv`.
   - Упаковка образа `ghcr.io/datawikipro/igaming-source-nike:latest` через Jib.
3. **Развертывание в K8s**:
   - Применение манифеста `igaming-k8s/nike.yaml` (StatefulSet `igaming-source-nike-db` и Deployment `igaming-source-nike`).
   - Инициализация таблиц `match_cache`, `match_factor`, `sport_cache`, `league_cache`.
4. **Контроль и верификация (DoD)**:
   - Мониторинг наполнения базы до отметки $\ge 500$ матчей.
   - Масштабирование в `replicas=0`.
