# 📋 [IGAMING-SRC-WINLINE] Fail-Fast для Winline (Crawler + Loader) & K8s Probes

## 📌 Метаданные задачи (Plane)
- **ID**: `IGAMING-SRC-WINLINE`
- **Эпик**: `[EPIC-CRAWLERS]`
- **Модуль**: `igaming-source-winline`
- **Манифест K8s**: `igaming-k8s/winline.ru.yaml`
- **Приоритет**: `Urgent`
- **Теги**: `winline`, `crawler`, `loader`, `k8s-probes`, `robot-task`
- **Статус**: `Backlog`

---

## 🎯 Проблема и симптомы
1. При таймауте Winline SAPI (обычно 45-60 секунд) или блокировке Cloudflare Turnstile Playwright браузер зависает.
2. Процесс висит в K8s в статусе `2/2 Running`, но свежие коэффициенты не поступают.
3. В `winline.ru.yaml` отсутствуют секции `livenessProbe` и `readinessProbe`.

---

## 🤖 Пошаговая инструкция для робота

### Шаг 1: Проверка зависимостей
Открыть `igaming-source-winline/pom.xml`. Убедиться в наличии зависимости `igaming-source-core` (версии 0.0.1-SNAPSHOT).

### Шаг 2: Внедрение Fail-Fast в WinlineApiErrorTracker / SapiClient
1. Подключить `EmergencyIncidentTracker` в `WinlineApiErrorTracker`.
2. Если 3 раза подряд SAPI возвращает ошибку или Cloudflare блокировку:
   ```java
   emergencyIncidentTracker.recordFailure(
       EmergencySubsystem.PROXY_NETWORK, 
       "Winline SAPI request timeout / Turnstile block", 
       exception
   );
   ```

### Шаг 3: Обновление манифеста `igaming-k8s/winline.ru.yaml`
Добавить в секцию `containers` для `igaming-source-winline-crawler` и `igaming-source-winline-loader`:
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 3033
  initialDelaySeconds: 45
  periodSeconds: 15
  failureThreshold: 3
  timeoutSeconds: 5
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 3033
  initialDelaySeconds: 20
  periodSeconds: 10
  failureThreshold: 2
  timeoutSeconds: 3
```

### Шаг 4: Верификация
Выполнить:
```bash
mvn -pl igaming-source-winline test-compile
```

---

## ✅ Критерии приемки
1. Модуль компилируется.
2. В логах при 3 сбоях подряд отображается `🔥🔥🔥 [FATAL INCIDENT] 🔥🔥🔥`.
3. В `winline.ru.yaml` присутствуют `livenessProbe` и `readinessProbe` на порт 3033.
