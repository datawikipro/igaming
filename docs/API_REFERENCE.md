# 📡 Единый Каталог API и Справочник Сервисов — SmartBet.guru

> **Единый источник правды по REST API и сетевым сервисам экосистемы.**  
> Используйте этот документ как быстрый справочник по эндпоинтам, сетевым адресам (K8s DNS / Tailscale), структурам запросов/ответов и готовым примерам `curl`.

---

## 🧭 Сводная Таблица Сервисов и Портов

> 🗺️ **Подробная интерактивная топология и схема потоков**: см. [**Карту Межсервисного Взаимодействия**](file:///c:/Users/chernousov_a/IdeaProjects/igaming/docs/architecture/INTER_SERVICE_COMMUNICATION_MAP.md).

| Сервис | K8s Cluster DNS / Хост | Порт | Внешний / NodePort | Описание |
| :--- | :--- | :--- | :--- | :--- |
| **Institutional ML Inference** | `igaming-ml-inference.igaming-analytics.svc.cluster.local` | `8085` | `http://100.78.183.101:8085` | PyTorch MultiTaskTacticalNet v2/v3 инференс (< 5 мс) |
| **AI Analyst Core** | `igaming-ai-analyst.igaming-analytics.svc.cluster.local` | `8080` | NodePort `30895` | Квантовый анализ матчей, стратегии, досье, расчет ставок |
| **MDM Service** | `igaming-mdm-service.igaming-enrichment.svc.cluster.local` | `8095` | — | Master Data Management: канонические матчи, команды, Live-счета, Blueprint |
| **Aggregator API** | `igaming-aggregator-api.igaming-api.svc.cluster.local` | `80`, `8080` | — | Поиск вилок, валуев, коридоров, матчинг коэффициентов, heartbeats |
| **Portal Gateway** | `igaming-portal.igaming-api.svc.cluster.local` | `80` (app :8081) | NodePort `32081` / `https://smartbet.guru/api` | Публичный API-шлюз с проверкой тарифов и JWT |
| **Match Vision** | `igaming-match-vision.igaming-analytics.svc.cluster.local` | `8080` | NodePort `30896` | Микро-профилирование игроков и стартовых составов через Plane AI |
| **SofaScore Bridge** | `sofascore-bridge.igaming-enrichment.svc.cluster.local` | `8090` | — | FastAPI анти-бот скрапер SofaScore |
| **SofaScore On-Demand** | `igaming-capture-sofascore-on-demand.igaming-enrichment.svc.cluster.local` | `3041` / `80` | — | Он-деманд парсер матчей по запросу MDM/Anomaly-агента |
| **Anomaly Resolver Agent** | `igaming-anomaly-resolver-agent.igaming-enrichment.svc.cluster.local` | `8098` | — | Автоматический агент резолвинга нераспознанных команд и лиг |
| **ClickHouse DDS** | `clickhouse.igaming-analytics.svc.cluster.local` | `8123` (HTTP) / `9000` | — | Спортивно-аналитическое хранилище витрин и фичей |
| **Qdrant Vector DB** | `qdrant.igaming-analytics.svc.cluster.local` | `6333` (REST) / `6334` | — | Векторные эмбеддинги игровых стилей команд и игроков |
| **ML Pipeline Service** | `ml-pipeline-service.igaming-analytics.svc.cluster.local` | `8086` | — | Управление RunPod GPU тренировочными пайплайнами |
| **Kafka Cluster** | `kafka.igaming-master.svc.cluster.local` | `9092` (Broker), `9093` | — | Шина спортивных событий (`canonical.odds.updates`, `scores`, `errors`) |

---

## 1. 🧠 Institutional Tactical ML Inference Engine (`:8085`)

* **Кодовая база**: `scripts/ml_inference_service.py` (FastAPI + PyTorch)
* **Кластерный URL**: `http://igaming-ml-inference.igaming-analytics.svc.cluster.local:8085`
* **Локальный/хост запуск**: `python scripts/ml_inference_service.py` (порт по умолчанию 8085)
* **Java-клиент**: `pro.datawiki.igaming.analyst.service.MlInferenceClient.java`
* **Поддерживаемые виды спорта**: `football`, `tennis`, `basketball`, `ice-hockey`, `cs2`

### 1.1. `POST /predict` — Расчет тактических вероятностей матча

Главный высокоскоростной эндпоинт (< 5 мс на запрос). Рассчитывает вероятности исходов (1X2), тоталов, справедливые коэффициенты, стресс-тесты («улитки») и сигналы скамейки.

#### Схема Запроса (`MatchPredictionRequest`)
```json
{
  "match_id": 123456,
  "sport_name": "football",
  "features": [0.12, 0.85, ...], 
  "team1_id": 864,
  "team2_id": 865,
  "team1_name": "Barcelona",
  "team2_name": "Real Madrid",
  "start_time": 1730462400000,
  "team1_recent_matches": [
    {"result": "WIN", "team_score": 2, "opponent_score": 0, "clean_sheet": true, "match_date": 1730000000000}
  ],
  "team2_recent_matches": [
    {"result": "DRAW", "team_score": 1, "opponent_score": 1, "clean_sheet": false, "match_date": 1730000000000}
  ],
  "team1_starters": [{"name": "Lewandowski", "pos": "F", "avg_rating": 7.8}],
  "team2_starters": [{"name": "Vinicius", "pos": "F", "avg_rating": 8.1}],
  "team1_bench": [{"name": "Torres", "pos": "F", "avg_rating": 7.1}],
  "team2_bench": [],
  "team1_dna_tags": "HIGH_PRESS,FAST_TEMPO",
  "team2_dna_tags": "LOW_BLOCK,COUNTER_ATTACK",
  "is_lineup_confirmed": true
}
```

> ⚡ **Важные нюансы**:
> 1. Поле `features`: Если передан предварительно рассчитанный в Java 291-мерный вектор (`TacticalTensorBuilder`), сервис использует его напрямую для мгновенного тензорного инференса.
> 2. Если `features` не передан, сервис рассчитывает фичи сам по `team1_recent_matches` и `team2_recent_matches`. В этом случае у **обеих команд должно быть $\ge 2$ матчей**, иначе вернется `has_ml_evaluation: false` с причиной `"Insufficient history"`.

#### Схема Ответа (`MatchPredictionResponse`)
```json
{
  "has_ml_evaluation": true,
  "reason": null,
  "sport": "football",
  "prob_home": 0.5241,
  "prob_draw": 0.2415,
  "prob_away": 0.2344,
  "prob_over": 0.6120,
  "prob_under": 0.3880,
  "fair_odds_home": 1.91,
  "fair_odds_draw": 4.14,
  "fair_odds_away": 4.27,
  "fair_odds_over": 1.63,
  "fair_odds_under": 2.58,
  "confidence_tier": "ML_80",
  "recommended_market": "WIN1",
  "recommended_prob": 0.5241,
  "cohort_accuracy_pct": 84.5,
  "decision_reason_code": "FAVORITE_COVER",
  "signals": {
    "decision_reason_code": "FAVORITE_COVER",
    "snail_stress_tested": true,
    "snail_stress_downgraded": false,
    "worst_win_prob": 0.4820,
    "worst_dc_prob": 0.7410,
    "bench_chaos": false,
    "resilience_pct": 82.5,
    "edge_market": "WIN1",
    "edge_prob": 0.5241
  },
  "tactical_narrative": null,
  "bench_jokers_found": ["Torres"],
  "bench_chaos_warning": false,
  "is_lineup_projected": false,
  "lineup_resilience_pct": 82.5,
  "key_player_dependencies": []
}
```

* Коды `decision_reason_code`:
  - `FAVORITE_COVER`: уверенный перевес фаворита с прохождением стресс-тестов.
  - `SNAIL_STRESS_DOWNGRADE`: в худшей ветке ротации победа фаворита просела, ставка автоматически переведена в Двойной Шанс (`1X`/`X2`).
  - `BENCH_CHAOS_VETO`: на скамейке обнаружен джокер-разрушитель, вето на тотал меньше.
  - `COINFLIP_PASS`: равные шансы/монетка, автоматический пропуск (`PASS`).

#### Пример вызова `curl`:
```bash
curl -s -X POST http://100.78.183.101:8085/predict \
  -H "Content-Type: application/json" \
  -d '{
    "sport_name": "football",
    "team1_name": "Arsenal",
    "team2_name": "Chelsea",
    "team1_recent_matches": [
      {"result": "WIN", "team_score": 2, "opponent_score": 0, "match_date": 1730000000000},
      {"result": "WIN", "team_score": 3, "opponent_score": 1, "match_date": 1729500000000}
    ],
    "team2_recent_matches": [
      {"result": "DRAW", "team_score": 1, "opponent_score": 1, "match_date": 1730000000000},
      {"result": "LOSS", "team_score": 0, "opponent_score": 2, "match_date": 1729500000000}
    ]
  }'
```

---

### 1.2. `GET /health` — Статус ML-сервиса и загруженных моделей
```bash
curl -s http://100.78.183.101:8085/health
```
**Ответ:**
```json
{
  "status": "UP",
  "models_count": 5,
  "available_sports": ["football", "tennis", "basketball", "ice-hockey", "cs2"]
}
```

---

### 1.3. `GET /team_lineup_profile` — Профиль ротации и сценарии состава команды
* **Параметры**:
  - `team` (string, required): название команды (например, `Arsenal`)
  - `sport` (string, optional, default: `football`)

```bash
curl -s "http://100.78.183.101:8085/team_lineup_profile?team=Arsenal&sport=football"
```
**Ответ:**
```json
{
  "found": true,
  "team": "Arsenal",
  "total_players": 24,
  "gk": [...],
  "defenders": [...],
  "midfielders": [...],
  "attackers": [...],
  "scenarios": [
    {"name": "BASE_OPTIMAL", "starters_count": 11, "rotated_out": []},
    {"name": "ROTATED_FATIGUE", "starters_count": 11, "rotated_out": ["Saka"]},
    {"name": "JOKER_OFFENSIVE", "starters_count": 11, "rotated_out": []}
  ]
}
```

---

### 1.4. `POST /simulate_custom_lineup` — Интерактивная симуляция состава
Позволяет пользователю или фронтенду смоделировать матч при произвольном изменении старта/скамейки.

#### Пример запроса:
```bash
curl -s -X POST http://100.78.183.101:8085/simulate_custom_lineup \
  -H "Content-Type: application/json" \
  -d '{
    "sport_name": "football",
    "team1_name": "Arsenal",
    "team2_name": "Chelsea",
    "team1_starters": [
      {"name": "Raya", "pos": "G", "avg_rating": 7.2},
      {"name": "Saka", "pos": "F", "avg_rating": 8.0}
    ],
    "team2_starters": [
      {"name": "Sanchez", "pos": "G", "avg_rating": 6.8},
      {"name": "Palmer", "pos": "M", "avg_rating": 7.9}
    ]
  }'
```

---

## 2. 🏛️ AI Analyst API (`igaming-ai-analyst` :8080 / NodePort 30895)

* **Кластерный URL**: `http://igaming-ai-analyst.igaming-analytics.svc.cluster.local:8080`
* **NodePort URL**: `http://100.78.183.101:30895`

### 2.1. Рекомендации и Анализ
* **`POST /api/v1/ai-analyst/evaluate/{matchId}`**  
  Запуск полного квантового анализа конкретного матча по ID. Формирует тензор 291-dim, вызывает ML инференс, выполняет +EV оверлей и сохраняет результат в БД.
* **`GET /api/v1/ai-analyst/recommendations`**  
  Получение списка рекомендаций с фильтрацией и пагинацией:
  - `status`: `ACTIVE` (только актуальные) или `SETTLED` (рассчитанные архивы).
  - `sport`: `football`, `tennis`, `basketball`, `ice-hockey`, `cs2`.
  - `tier`: `ML_90`, `ML_80`, `ML_70`, `ML_60`, `PASS`.
  - `stage`: `PRE_LINEUP`, `LINEUP_CONFIRMED`.
  - `bookmaker`: фильтр по доступному букмекеру (например, `pinnacle`, `fonbet`).
  - `page`: номер страницы (начиная с 0).
  - `size`: размер страницы (по умолчанию 50, макс 100).
  - `sort`: `id,desc` или `matchStartTime,asc` (для режима Opportunities).
* **`GET /api/v1/ai-analyst/recommendations/{id}`**  
  Детали одной рекомендации по ее ID.
* **`GET /api/v1/ai-analyst/recommendations/match/{matchId}`**  
  Поиск рекомендации по ID матча.
* **`GET /api/v1/ai-analyst/dossier/{matchId}`**  
  Просмотр полного скомпилированного досье матча и метаданных.
* **`GET /api/v1/ai-analyst/stats`**  
  Сводная статистика: ROI %, Winrate %, распределение по тирам, чистая прибыль PnL в RUB.

### 2.2. Стратегии и Мульти-стратегический Движок
* **`GET /api/v1/ai-analyst/strategies`**  
  Список 7 предустановленных институциональных стратегий (Conservative Ladder, Value Edge Hunter, Draw Mastermind, Safe Cushion, Underdog Armor, Pure Mathematical Kelly, Anti-Fragile).
* **`GET /api/v1/ai-analyst/strategies/performance`**  
  Сравнительный PnL и Winrate по стратегиям (параметр `?sport=football`).
* **`GET /api/v1/ai-analyst/strategies/performance/by-sport`**  
  Матрица эффективности стратегий в разрезе каждого вида спорта.
* **`POST /api/v1/ai-analyst/strategies/recalculate?forceAll=false`**  
  Пересчет голосов стратегий под текущие риск-правила.

### 2.3. Сеттлмент и Пост-Мортем Аудит
* **`POST /api/v1/ai-analyst/settle`**  
  Запуск фонового расчета завершенных матчей по официальным счетам MDM/Redis.
* **`POST /api/v1/ai-analyst/reconcile`**  
  Сверка и автоматическое исправление расхождений за последние 6 часов.
* **`GET /api/v1/ai-analyst/memories`**  
  Список персистентных тактических уроков ИИ (`ai_learning_memory`).
* **`GET /api/v1/ai-analyst/forensic/context/{recommendationId}`**  
  Формирование структурированного контекста судебного аудита проигранной ставки.
* **`GET /api/v1/ai-analyst/forensic/prompt/{recommendationId}`**  
  Генерация полиморфного спортивного промпта для LLM-аудитора.
* **`POST /api/v1/ai-analyst/forensic/report/{recommendationId}`**  
  Запись вердикта LLM в память обучения с указанием тактической причины проигрыша.

---

## 3. 🎯 MDM Service (Master Data Management :8095)

* **Кластерный URL**: `http://igaming-mdm-service.igaming-enrichment.svc.cluster.local:8095` (или alias `http://igaming-mdm-service:8095`)
* **Роль**: Единый источник истины (Single Source of Truth) по матчам, командам и счетам в реальном времени.

### 3.1. Ключевые Эндпоинты
* **`GET /api/v1/mdm/blueprint/{systemCode}`**  
  Загрузка актуального слепка сопоставлений (Blueprint) для кэширования в памяти краулера (`MdmIngestionClient`).
* **`POST /api/v1/mdm/matches/find-or-create`**  
  Каноническое связывание события букмекера с единой сущностью матча.
* **`POST /api/v1/mdm/matches/{id}/live-score`**  
  Обновление текущего или финального счета матча. Записывает в Redis `live:match:{id}` и PostgreSQL `mdm_match`.
  ```json
  {
    "homeScore": 2,
    "awayScore": 1,
    "status": "FINISHED",
    "periodScores": ["1:0", "1:1"]
  }
  ```
* **`GET /api/v1/mdm/matches/{id}/live-score`**  
  Получение актуального счета матча из Redis/БД.
* **`GET /api/v1/mdm/matches/active`**  
  Список всех текущих активных канонических матчей.

---

## 4. ⚡ Aggregator Core & Portal Gateway (:80 / :8080)

* **Внутренний шлюз Aggregator**: `http://igaming-aggregator-api.igaming-api.svc.cluster.local/api` (alias `http://igaming-aggregator:80/api`)
* **Публичный шлюз Portal**: `http://igaming-portal.igaming-api.svc.cluster.local:80/api` (внешний `https://smartbet.guru/api`)

### 4.1. Котировки и Арбитраж
* **`GET /api/matches/{id}/odds`**  
  Получение полного плоского дедуплицированного списка котировок всех букмекеров по матчу.
* **`GET /api/matches/{id}`**  
  Метаданные матча, статус, счет, команды, турнир.
* **`GET /api/teams/{id}`** и **`GET /api/teams/{id}/upcoming`**  
  Информация о команде и ее ближайшие предстоящие события.
* **`GET /api/surebets`**  
  Активные вилки (Surebets) в реальном времени с фильтрацией по профиту и букмекерам.
* **`GET /api/valuebets`**  
  Ставки с положительным математическим ожиданием (+EV).
* **`GET /api/middles`**  
  Коридоры по тоталам и форам.

---

## 5. 🗄️ Базы Данных и Векторные Хранилища

### 5.1. ClickHouse DDS (:8123 HTTP / :9000 Native)
* **Кластерный URL**: `http://clickhouse.igaming-analytics.svc.cluster.local:8123`
* **Ключевые таблицы**:
  - `dds_sports_science_features`: витрина 64 спортивно-научных фичей по каждой команде (xG, темп, агрессия, PSxG).
  - `dds_player_match_stats`: микро-профилирование игроков, рейтинги, карты активности.
* **Пример запроса через curl**:
  ```bash
  curl -s "http://clickhouse.igaming-analytics.svc.cluster.local:8123/?query=SELECT+count()+FROM+dds_sports_science_features"
  ```

### 5.2. Qdrant Vector Engine (:6333 REST / :6334 gRPC)
* **Кластерный URL**: `http://qdrant.igaming-analytics.svc.cluster.local:6333`
* **Спортивные коллекции**:
  - `football_tactical_v1`
  - `tennis_tactical_v1`
  - `ice_hockey_tactical_v1`
  - `basketball_tactical_v1`
  - `cs2_tactical_v1`
* **Проверка коллекций**:
  ```bash
  curl -s http://qdrant.igaming-analytics.svc.cluster.local:6333/collections
  ```

---

## 6. 📬 Шина Данных Kafka и Потоковые Контракты (`:9092`)

* **Кластерный адрес**: `kafka.igaming-master.svc.cluster.local:9092` (или alias `kafka:9092`)
* **Спецификация топиков**:
  1. **`canonical.odds.updates`**  
     - **Формат**: JSON `CanonicalMatchEventDto`  
     - **Ключ партиционирования**: детерминированный `matchKey`  
     - **Потребитель**: `igaming-aggregator-ingestion` (concurrency 8)  
  2. **`sports.canonical.scores`**  
     - **Формат**: JSON `CanonicalLiveScoreDto`  
     - **Потребители**: MDM Service, Live-табло  
  3. **`mdm.anomaly.errors`**  
     - **Формат**: JSON `MdmAnomalyDto`  
     - **Потребитель**: `igaming-anomaly-resolver-agent` (запуск он-деманд парсинга)  
  4. **`odds.updates` (Legacy Fallback)**  
     - **Формат**: JSON `OddsUpdateRequest`  
     - **Статус консьюмера**: `APP_KAFKA_LISTENER_AUTO_STARTUP: false` в `igaming-aggregator-ingestion`.

---

## 6. 🚨 Правила Использования API для AI-Ассистентов

1. **Запрет слепого поиска по репозиторию**: при необходимости вызвать или проверить API сначала сверяйтесь с этим документом (`docs/API_REFERENCE.md`).
2. **K8s DNS имена**: внутри кластера используйте строгие K8s DNS (`igaming-ml-inference.igaming-analytics.svc.cluster.local:8085`).
3. **Локальный доступ / Ноутбук**: для внешнего тестирования используйте адрес `xeon-srv` в сети Tailscale: `http://100.78.183.101:<port>`.
4. **Не отбрасывать аномалии**: если API вернул неожиданный статус или ошибку 400/500 — рапортуйте и исправляйте причину в первоисточнике, а не скрывайте костылями.
