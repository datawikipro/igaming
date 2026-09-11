## Why

Несколько модулей в K8s кластере (`igaming-master`, `igaming-dev`, `plane`) оказались неработоспособными из-за накопившихся конфигурационных ошибок: неверных nodeSelector, отсутствующих секретов и повреждённого nginx ConfigMap. Ремонт необходим для восстановления базовой работоспособности платформы.

## What Changes

- **Исправлен** `plane-proxy-nginx-config` ConfigMap — переменные nginx (`$host`, `$remote_addr`, `$proxy_add_x_forwarded_for`) были заменены PowerShell-интерполяцией на пустые строки; восстановлены правильные значения через bash heredoc
- **Исправлен** `igaming-redis` Deployment — удалён несуществующий nodeSelector `role: worker`, который не совпадал ни с одной нодой кластера
- **Исправлен** `kafka` StatefulSet + PV/PVC — nodeSelector и PV nodeAffinity указывали на несуществующую ноду `k8s-master-1`; перенастроены на `k8s-master-3`
- **Исправлен** отсутствующий секрет `regcred` в namespace `igaming-dev` — скопирован из `igaming-master`

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
<!-- Это чистый инфраструктурный ремонт без изменения требований к сервисам -->
<!-- skip_specs: true задан в .openspec.yaml -->

## Impact

- **K8s namespaces**: `igaming-master`, `igaming-dev`, `plane`
- **Поды после ремонта**: `kafka-0`, `igaming-redis`, `plane-proxy`, `igaming-crawler-frontend` — все переведены в `1/1 Running`
- **Данные**: kafka данные сохранены через hostPath `/var/data/kafka-data` на `k8s-master-3`
- **Код сервисов**: не изменялся
