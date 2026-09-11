## 1. Диагностика кластера

- [x] 1.1 Подключиться к `k8s-master-3` через `gcloud compute ssh` и получить список всех подов (`kubectl get pods -A`)
- [x] 1.2 Отфильтровать поды не в состоянии `Running`/`Completed` и систематизировать проблемы по категориям
- [x] 1.3 Проверить события (`kubectl describe pod`) для каждого проблемного пода

## 2. Исправление plane-proxy (nginx ConfigMap)

- [x] 2.1 Получить текущий ConfigMap `plane-proxy-nginx-config` и убедиться в наличии повреждённых `proxy_set_header` директив
- [x] 2.2 Создать fix-скрипт с правильным nginx конфигом через bash `'EOF'` heredoc (без интерполяции PowerShell)
- [x] 2.3 Загрузить скрипт на ноду через `gcloud compute scp` и выполнить `sudo bash`
- [x] 2.4 Применить ConfigMap через `kubectl create configmap --from-file` и перезапустить deployment
- [x] 2.5 Проверить: `plane-proxy` в состоянии `1/1 Running`

## 3. Исправление igaming-redis (nodeSelector)

- [x] 3.1 Установить причину `Pending`: nodeSelector `role: worker` не совпадает ни с одной нодой
- [x] 3.2 Удалить несуществующий label `role: worker` через `kubectl patch deployment igaming-redis --type=json`
- [x] 3.3 Проверить: `igaming-redis` в состоянии `1/1 Running`

## 4. Исправление Kafka (StatefulSet + PV/PVC)

- [x] 4.1 Установить причину `Pending`: nodeSelector указывает на несуществующую ноду `k8s-master-1`
- [x] 4.2 Обновить nodeSelector StatefulSet `kafka` → `k8s-master-3` через `kubectl patch`
- [x] 4.3 Установить причину повторного `Pending`: PV `kafka-pv` имеет nodeAffinity → `k8s-master-1`
- [x] 4.4 Scale down StatefulSet до 0 реплик
- [x] 4.5 Удалить PVC `kafka-pvc` и PV `kafka-pv`
- [x] 4.6 Создать директорию `/var/data/kafka-data` на `k8s-master-3`
- [x] 4.7 Создать новый PV с nodeAffinity → `k8s-master-3` и новый PVC
- [x] 4.8 Scale up StatefulSet до 1 реплики
- [x] 4.9 Проверить: `kafka-0` в состоянии `1/1 Running`

## 5. Исправление igaming-crawler-frontend (regcred secret)

- [x] 5.1 Установить причину `ImagePullBackOff`: отсутствует secret `regcred` в namespace `igaming-dev`
- [x] 5.2 Скопировать secret `regcred` из `igaming-master` в `igaming-dev`
- [x] 5.3 Удалить застрявший pod для принудительного пересоздания с новым секретом
- [x] 5.4 Проверить: `igaming-crawler-frontend` в состоянии `1/1 Running`

## 6. Верификация DoD (5-минутный таймер)

- [x] 6.1 Дождаться 5 минут стабильной работы всех исправленных подов без ошибок в логах
- [x] 6.2 Убедиться что все ключевые сервисы `igaming-master` в `Running`: aggregator, portal, sources-db, redis, kafka, auth
- [x] 6.3 Убедиться что `plane` namespace полностью здоров (все поды `Running`)
