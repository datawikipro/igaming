# Техническое задание: Разработка микросервиса-загрузчика `igaming-source-bovada`

---

## 1. Общие сведения и назначение

- **Наименование сервиса**: `igaming-source-bovada`
- **Модуль**: Подмодуль в `igaming` бэкенде
- **Язык / Фреймворк**: Java 21, Spring Boot 3.3+, Spring Data JPA, HikariCP
- **Целевой букмекер**: Bovada (`bovada.lv`) — крупнейший и наиболее ликвидный букмекер США в офшорном сегменте.
- **Цель разработки**: Непрерывный опрос открытого REST API Bovada через US Gateway `ru-proxy`, парсинг и нормализация спортивных событий, сохранение в локальную базу данных PostgreSQL (`match_cache`, `match_factor`) для агрегатора вилок и +EV `igaming-aggregator`.

---

## 2. Сетевая топология и требования к подключению

1. **Маршрутизация**:
   - Все запросы сервиса к доменам `bovada.lv` и `services.bovada.lv` обязаны идти через кластерный HTTP-прокси:
     ```properties
     app.proxy.host=100.83.113.50
     app.proxy.port=3128
     ```
   - На узле `ru-proxy` настроен маршрут `sing-box` на `outline-us` (IP США `34.9.20.96`, Google Cloud Iowa), что гарантирует отсутствие блокировок РКН и прохождение Cloudflare.
2. **Обязательные заголовки HTTP**:
   ```http
   User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36
   Accept: application/json, text/plain, */*
   Accept-Language: en-US,en;q=0.9
   Connection: keep-alive
   ```
   > [!IMPORTANT]
   > Заголовок `Accept-Language: en-US,en;q=0.9` является **критически обязательным**. При его отсутствии Cloudflare сбрасывает TCP-соединение (RST).

---

## 3. Спецификация REST API Bovada

API является открытым (Public REST API), не требует авторизации, токенов сессий и кук.

### 3.1. Эндпоинты сбора котировок (Endpoints)

Формат URL:
```
https://www.bovada.lv/services/sports/event/coupon/events/A/description/{sport_path}?lang=en
```

Список обязательных видов спорта для обхода:
1. **Американский футбол (NFL / NCAA Football)**:
   `https://www.bovada.lv/services/sports/event/coupon/events/A/description/football?lang=en`
2. **Баскетбол (NBA / NCAA Basketball / WNBA / Euroleague)**:
   `https://www.bovada.lv/services/sports/event/coupon/events/A/description/basketball?lang=en`
3. **Европейский футбол (Soccer: EPL, La Liga, Serie A, Champions League)**:
   `https://www.bovada.lv/services/sports/event/coupon/events/A/description/soccer?lang=en`
4. **Хоккей (NHL / KHL / European Leagues)**:
   `https://www.bovada.lv/services/sports/event/coupon/events/A/description/hockey?lang=en`
5. **Бейсбол (MLB)**:
   `https://www.bovada.lv/services/sports/event/coupon/events/A/description/baseball?lang=en`
6. **Теннис (ATP / WTA / Grand Slams / Challenger)**:
   `https://www.bovada.lv/services/sports/event/coupon/events/A/description/tennis?lang=en`
7. **Единоборства (UFC / MMA / Boxing)**:
   `https://www.bovada.lv/services/sports/event/coupon/events/A/description/ufc-mma?lang=en`

---

## 4. Структура JSON-ответа и DTO

Ответ представляет собой массив групп событий (`EventGroupDto`):

```json
[
  {
    "path": [
      {"id": "football", "description": "Football", "type": "SPORT"},
      {"id": "nfl", "description": "NFL", "type": "LEAGUE"}
    ],
    "events": [
      {
        "id": "10492811",
        "description": "Atlanta Falcons @ Pittsburgh Steelers",
        "startTime": 1789318800000,
        "live": false,
        "competitors": [
          {"id": "1", "name": "Atlanta Falcons", "home": false},
          {"id": "2", "name": "Pittsburgh Steelers", "home": true}
        ],
        "displayGroups": [
          {
            "id": "100",
            "description": "Game Lines",
            "markets": [
              {
                "id": "m1",
                "description": "Moneyline",
                "key": "2W-12",
                "period": {"description": "Match"},
                "outcomes": [
                  {
                    "description": "Atlanta Falcons",
                    "price": {"decimal": "3.400", "american": "+240", "handicap": "0"}
                  },
                  {
                    "description": "Pittsburgh Steelers",
                    "price": {"decimal": "1.345", "american": "-290", "handicap": "0"}
                  }
                ]
              },
              {
                "id": "m2",
                "description": "Point Spread",
                "period": {"description": "Match"},
                "outcomes": [
                  {
                    "description": "Atlanta Falcons",
                    "price": {"decimal": "1.870", "handicap": "+6.5"}
                  },
                  {
                    "description": "Pittsburgh Steelers",
                    "price": {"decimal": "1.952", "handicap": "-6.5"}
                  }
                ]
              },
              {
                "id": "m3",
                "description": "Total",
                "period": {"description": "Match"},
                "outcomes": [
                  {
                    "description": "Over",
                    "price": {"decimal": "1.909", "handicap": "44.5"}
                  },
                  {
                    "description": "Under",
                    "price": {"decimal": "1.909", "handicap": "44.5"}
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  }
]
```

---

## 5. Маппинг данных (Data Normalization)

### 5.1. Сущности `match_cache`
- `match_id`: `event.id` (уникальный идентификатор события)
- `sport`: из `path[type == 'SPORT'].description` (нормализуется через маппер видов спорта)
- `league`: из `path[type == 'LEAGUE'].description`
- `team1`: название гостевой/первой команды (`competitors[0].name`)
- `team2`: название домашней/второй команды (`competitors[1].name`)
- `start_time`: `event.startTime` (timestamp в миллисекундах $\rightarrow$ `LocalDateTime` UTC)
- `is_live`: `event.live`

### 5.2. Сущности `match_factor`
Маппер `BovadaBetTypeMapper` нормализует исходы по структуре:
- **Moneyline (Match)**:
  - Исход с `description == team1` $\rightarrow$ Исход `1` (`factor_id = 1`)
  - Исход с `description == team2` $\rightarrow$ Исход `2` (`factor_id = 2`)
  - Исход с `description == 'Tie'` / `'Draw'` $\rightarrow$ Исход `X` (`factor_id = 3`)
- **Point Spread (Фора)**:
  - `description == team1` $\rightarrow$ `F1` со значением форы `handicap`
  - `description == team2` $\rightarrow$ `F2` со значением форы `handicap`
- **Total (Тотал)**:
  - `description == 'Over'` $\rightarrow$ `TO` со значением тотала `handicap`
  - `description == 'Under'` $\rightarrow$ `TU` со значением тотала `handicap`
- **Коэффициент (`value`)**: парсится из `price.decimal` (тип `BigDecimal(10, 3)`).

---

## 6. Требования к отказоустойчивости и неблокирующему старту (Golden Rules)

В `application.properties` микросервиса обязательны следующие параметры:

```properties
spring.application.name=igaming-source-bovada
server.port=8080

# Неблокирующий старт HikariCP
spring.datasource.hikari.initialization-fail-timeout=0
spring.datasource.hikari.connection-timeout=5000
spring.datasource.hikari.validation-timeout=3000
spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false
spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO:update}

# Сетевой прокси ru-proxy
app.proxy.host=100.83.113.50
app.proxy.port=3128

# Actuator Health
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.probes.enabled=true
```

---

## 7. Инфраструктура и Kubernetes манифесты

1. **База данных PostgreSQL**:
   - StatefulSet: `igaming-source-bovada-db-0`
   - Имя базы: `igaming_bovada`
   - Пользователь: `postgres`
   - Service DNS: `igaming-source-bovada-db`
   - Ресурсы: `limits.memory: 512Mi`, `requests.memory: 128Mi`, `limits.cpu: 500m`
   - Оптимизация диска: `synchronous_commit = off`
2. **Загрузчик (Loader)**:
   - Deployment: `igaming-source-bovada-loader`
   - Образ: `ghcr.io/datawikipro/igaming-source-bovada:latest`
   - Ожидание БД в `initContainer`:
     ```bash
     until psql -h "igaming-source-bovada-db" -U "postgres" -d "igaming_bovada" -c '\q'; do sleep 2; done
     ```
   - Запрет IP: Строго использовать DNS имя `igaming-source-bovada-db`.

---

## 8. Definition of Done (DoD)

1. Под `igaming-source-bovada-loader` успешно задеплоен в Kubernetes namespace `igaming-dev` в статусе `Running 1/1`.
2. Эндпоинты `/actuator/health/readiness` и `/actuator/health/liveness` возвращают `HTTP 200 UP`.
3. **Критерий наполнения линии**:
   `SELECT count(*) FROM match_cache >= 500` (Bovada отдает более 1,500 активных матчей по всем видам спорта).
4. **5-минутный таймер (`schedule`)**: Сервис обязан непрерывно работать 5 минут без сбоев и ошибок в логах.
