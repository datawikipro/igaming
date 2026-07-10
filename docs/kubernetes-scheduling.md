# Документация: Распределение ресурсов по Namespaces и правила планирования (Scheduling) в Kubernetes

Этот документ описывает разделение проекта **SmartBet.guru / igaming** на пространства имён (Namespaces), а также требования к расстановке нод и тегов (labels) для подов при развертывании в кластере Kubernetes.

---

## 🌐 1. Пространства имён (Namespaces)

Для изоляции инфраструктуры кластера и упрощения масштабирования проект разделен на два основных пространства имён:

1. **`igaming-source`**: содержит все компоненты букмекеров (источников данных).
   - Все краулеры (`*-crawler`).
   - Все лоадеры (`*-loader`).
   - Локальные базы данных букмекеров (`*-db` StatefulSet/Service) и сопутствующие инстансы Redis.
2. **`igaming-master`**: содержит центральную логику и общие/центральные сервисы.
   - API агрегатора событий и коэффициентов (`igaming-aggregator`).
   - Портал и API-шлюз (`igaming-portal`).
   - Telegram-боты уведомлений (`igaming-bot-*`).
   - Системы сбора результатов (`igaming-capture-*`).
   - Центральные базы данных и очереди сообщений (PostgreSQL, Kafka).
   - Микросервисы авторизации (`igaming-auth-*`).
   - Секреты автоматизации Meta/Instagram/Threads.

---

## 🏷️ 2. Теги Нод (Node Labels) и Топология Кластера

Ноды кластера Kubernetes должны быть помечены тегом `node-type` для правильного распределения нагрузок:

| Значение `node-type` | Роль и назначение | Характеристики ноды |
| :--- | :--- | :--- |
| **`master`** | Только для систем хранения данных, баз данных и критичных систем управления. | Высокая надежность, постоянные диски (Persistent Volumes). |
| **`standard`** | Для стабильных сервисов, API-шлюзов и центрального агрегатора. | Стабильные инстансы (On-Demand / standard). |
| **`spot`** | Для краулеров, лоадеров и вспомогательных скриптов. | Дешевые Spot-инстансы (могут быть удалены провайдером). |

---

## 🏗️ 3. Правила планирования подов (Pod Placement & Affinity Rules)

Спецификации подов опираются на `nodeSelector`, `affinity` (nodeAffinity) и `topologySpreadConstraints` для обеспечения отказоустойчивости и оптимизации стоимости ресурсов.

### 🗄️ А. Базы данных (`*-db` StatefulSets)
- **Центральные базы данных** (такие как `portal-postgres`, `igaming-sources-db` и др.) должны размещаться исключительно на мастер-нодах (`master`) для обеспечения максимальной надежности и стабильного дискового ввода-вывода.
  - **Конфигурация**:
    ```yaml
    nodeSelector:
      node-type: master
    ```
- **Локальные базы данных букмекеров** (такие как `igaming-source-betm-db`, `1xbet-db` и др.) размещаются на стабильных/стандартных нодах (`standard`) для разгрузки мастер-ноды.
  - **Конфигурация**:
    ```yaml
    nodeSelector:
      node-type: standard
    ```

### 🕷️ Б. Краулеры и Лоадеры (`*-crawler`, `*-loader`)
Краулеры и лоадеры должны равномерно распределяться между стабильными (`standard`) и временными (`spot`) нодами. Первая копия (реплика) по возможности должна запускаться на стабильной ноде, а дополнительные реплики — на спотовых.
- **Требование**: Предпочтительно стандартные ноды, допускаются спотовые.
- **Node Affinity**:
  ```yaml
  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
        - matchExpressions:
          - key: node-type
            operator: In
            values:
            - standard
            - stable
            - spot
      preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 100
        preference:
          matchExpressions:
          - key: node-type
            operator: In
            values:
            - standard
            - stable
      - weight: 10
        preference:
          matchExpressions:
          - key: node-type
            operator: In
            values:
            - spot
  ```
- **Равномерное распределение (Topology Spread Constraints)**:
  Используется для исключения ситуации, когда все поды краулера запускаются на одной ноде.
  ```yaml
  topologySpreadConstraints:
  - maxSkew: 1
    topologyKey: node-type
    whenUnsatisfiable: ScheduleAnyway
    labelSelector:
      matchLabels:
        app: <имя-деплоймента>
  ```

### 💻 В. Центральные Сервисы (`igaming-aggregator`, `igaming-portal`)
- **`igaming-aggregator`** (включая все surebet, middles, API и ingestion сервисы) должен размещаться исключительно на мастер-нодах (`master`) для стабильности и снижения сетевых задержек при работе с БД и Kafka.
  - **Конфигурация**:
    ```yaml
    nodeSelector:
      node-type: master
    ```
- **`igaming-portal`** (веб-портал и API-шлюз) работает на стабильных стандартных нодах (`standard`).
  - **Конфигурация**:
    ```yaml
    nodeSelector:
      node-type: standard
    ```

### 🤖 Г. Вспомогательные и фоновые сервисы (Captures, Bots, Proxy, etc.)
Сервисы сбора результатов, телеграм-боты и прокси-пулы могут запускаться как на стабильных, так и на спотовых нодах.
- **Требование**: Мягкое (Preferred).
- **Конфигурация**:
  ```yaml
  affinity:
    nodeAffinity:
      preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 10
        preference:
          matchExpressions:
          - key: node-type
            operator: In
            values:
            - standard
            - spot
  ```

---

## 🔗 4. Межпространственное взаимодействие (Cross-Namespace Resolution)

Так как компоненты теперь разделены по разным пространствам имён, обращение между ними по коротким DNS-именам невозможно. Для решения этой проблемы используются **`ExternalName`** сервисы.

Пример маршрутизации из `igaming-source` в `igaming-master`:
Создается виртуальный сервис `igaming-aggregator` в пространстве имён `igaming-source`, который перенаправляет трафик на реальный сервис в `igaming-master`:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: igaming-aggregator
  namespace: igaming-source
spec:
  type: ExternalName
  externalName: igaming-aggregator.igaming-master.svc.cluster.local
  ports:
  - port: 80
    targetPort: 80
```
Это позволяет краулерам подключаться по стандартному адресу `http://igaming-aggregator` без необходимости изменять внутренние настройки или переменные окружения.
