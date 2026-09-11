## Context

Кластер Kubernetes (6 нод: 1 control-plane `k8s-master-3`, 5 worker) работает со времени первоначального деплоя на нодах `k8s-master-1`/`k8s-master-2`, которые были впоследствии упразднены. Ряд манифестов содержал хардкод на несуществующие ноды. Дополнительно nginx ConfigMap был повреждён из-за применения через PowerShell, который интерполировал nginx-переменные как переменные окружения.

См. proposal.md — Why для мотивации.

## Goals / Non-Goals

**Goals:**
- Восстановить `1/1 Running` для всех критичных igaming-модулей и plane-proxy
- Исправить конфигурационные ошибки без изменения кода сервисов
- Задокументировать причины сбоев для предотвращения повторений

**Non-Goals:**
- Изменение бизнес-логики сервисов
- Исправление Airflow DAG-ошибок (проблемы в ETL-задачах, отдельная тема)
- Ремонт `proxy-vpn-pool` (VPN инфраструктура не относится к igaming)
- Восстановление `data-catalog-backend` (образ отсутствует в GHCR — требует отдельной сборки)

## Decisions

### 1. nginx ConfigMap — передача через SCP + bash heredoc
**Решение:** Файл создаётся Python/bash-скриптом на сервере через `'EOF'` heredoc (без интерполяции), а не через `kubectl apply` из PowerShell.  
**Альтернативы:**
- `kubectl create configmap --from-file` — был выбран этот подход; гарантирует сохранность `$`-переменных
- Прямой `kubectl patch` — сложнее контролировать многострочный YAML с `$`

**Причина:** PowerShell расширяет `$host` → `$env:host` (пусто), `$remote_addr` → пусто, `$proxy_add_x_forwarded_for` → пусто. В результате nginx получал directive с пустым значением → `emerg: invalid number of arguments`.

### 2. Kafka PV — пересоздание вместо патча
**Решение:** PV удалён и создан заново с новым nodeAffinity (`k8s-master-3`), т.к. `nodeAffinity` у bound PV — immutable поле.  
**Риск:** Потеря данных Kafka при удалении PV.  
**Митигация:** `reclaimPolicy: Retain` → PV удалён, но данные на hostPath (`/var/data/kafka-data`) сохранены на `k8s-master-3`.

### 3. regcred — копирование между namespace
**Решение:** Secret скопирован из `igaming-master` → `igaming-dev` через `kubectl get secret ... -o jsonpath`.  
**Альтернатива:** Пересоздать с новым token — требует PAT, которого нет в контексте.

## Risks / Trade-offs

- **[Risk] Kafka потеряла данные из-за смены ноды** → `k8s-master-1` уже не существовала, данных на ней нет. На `k8s-master-3` hostPath чистый — брокер стартует с пустым state, топики создадутся заново.
- **[Risk] regcred может протухнуть** → Токен в секрете имеет срок действия. При следующей ошибке ImagePull — проверить актуальность GitHub PAT.
- **[Risk] PowerShell-интерполяция повторится** → Задокументировано правило: все nginx-конфиги применять только через bash на сервере.

## Migration Plan

Все шаги уже выполнены (post-factum документация):

1. ✅ Загрузить fix-скрипт через `gcloud compute scp`
2. ✅ Применить исправленный ConfigMap `plane-proxy-nginx-config` через bash heredoc
3. ✅ `kubectl rollout restart deployment/plane-proxy -n plane`
4. ✅ Удалить label `role: worker` из nodeSelector `igaming-redis`
5. ✅ Обновить nodeSelector StatefulSet `kafka` → `k8s-master-3`
6. ✅ Пересоздать PV/PVC `kafka-pv`/`kafka-pvc` с nodeAffinity → `k8s-master-3`
7. ✅ Скопировать secret `regcred` в `igaming-dev`
8. ✅ Удалить pod `igaming-crawler-frontend` для принудительного restart

**Rollback:** Не применимо — ремонт восстанавливает предыдущее рабочее состояние.
