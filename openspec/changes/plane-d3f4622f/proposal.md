# Proposal: #81: [player-faces-tennis] [Теннис/Единоборства] Сбор портретов спортсменов (Player Face Avatars) и отображение в карточках одиночных матчей

## Context
- **Plane Task ID**: `d3f4622f-8e23-48fd-a333-fc17f24e9e7d`
- **Sequence ID**: 81
- **Module**: `player-faces-tennis`
- **Depends On**: #79 (`frontend-team-flags`)

## Problem & Motivation
На платформе SmartBet.guru в карточках одиночных видов спорта (теннис ATP/WTA, единоборства MMA/UFC, бокс, настольный теннис) ранее участники матчей отображались в виде абстрактных монограмм или буквенных инициалов, аналогично клубным командам.
Для пользователей спортивного портала и сканера вилок это создает ряд ограничений:
1. **Низкая узнаваемость**: в теннисе и боксе ключевую роль играют личности спортсменов, их лица и статус в турнирной сетке (посев).
2. **Отсутствие специализированного UI**: карточка матча Джокович — Алькарас или Махачев — Царукян должна наглядно демонстрировать реальные портреты атлетов, их национальные флаги и номера посева (`[1]`, `[2]`, `[WC]`, `[Q]`).
3. **Отсутствие единого сервиса сбора и CDN-раздачи портретов**: необходим автономный механизм интеграции со свободными источниками фото (Wikidata, Wikimedia Commons, TheSportsDB, SofaScore), оптимизации изображений в современный формат WebP и надежного кэширования с CDN-раздачей и SVG-фолбэком.

## Proposed Solution & Architecture
1. **Модуль микросервиса `player-faces-tennis`**:
   - Автономный Spring Boot 3 сервис с REST API, неблокирующим HikariCP, Actuator health пробами (`/actuator/health/readiness`, `/actuator/health/liveness`).
   - Доменная модель `PlayerFace`, репозиторий с полнотекстовым/нормализованным поиском по спортсменам одиночных видов спорта.
2. **Сбор портретов (Player Headshot Harvesters)**:
   - Паттерн Harvester / Strategy для интеграции со свободными источниками:
     - `WikidataHeadshotHarvester`: поиск сущностей спортсменов в Wikidata (P18 image, P27 citizenship, P647 ranking/seed).
     - `WikimediaCommonsHarvester`: извлечение и масштабирование свободных портретов из Wikimedia Commons API.
     - `TheSportsDbHarvester`: поиск спортсменов по TheSportsDB Player API (`strThumb`, `strCutout`).
     - `SofaScorePlayerHarvester`: получение идентификаторов и ссылок на медиа профили SofaScore.
   - `PlayerHeadshotManagerService`: многоуровневый каскад сбора с автоматическим сидированием базы топовых теннисистов (ATP/WTA Top 50) и бойцов MMA/UFC.
3. **Оптимизация и CDN-кэширование (WebP & Image Optimizer)**:
   - Сервис `PlayerAvatarCdnService`: хранение и раздача оптимизированных WebP аватарок через локальный/кластерный CDN (`/cdn/avatars/{slug}.webp`).
   - Динамический генератор SVG-аватарок в фирменной киберпанк стилистике SmartBet при временной недоступности внешней фотографии.
4. **REST API & Модели карточек матчей**:
   - `GET /api/players/faces`: поиск и фильтрация по имени, виду спорта, туру.
   - `GET /api/players/faces/by-name`: мгновенный поиск спортсмена с нечеткой нормализацией.
   - `POST /api/players/faces/batch`: пакетное обогащение участников матчей для парсера линий и сканера вилок.
   - `GET /api/players/faces/card`: генерация структуры карточки одиночного матча (Head-to-Head) с портретами, посевом, флагами и котировками.
   - `GET /cdn/avatars/{filename}`: встроенная раздача изображений и векторных аватарок.
5. **Frontend-компоненты и UI Showcase**:
   - React / Next.js компонент `PlayerAvatar.tsx` с поддержкой WebP, бейджей посева и флагов стран.
   - Демо-витрина одиночных матчей тенниса `tennis-player-faces.html` с живым превью карточек матчей.
6. **K8s & CI/CD**:
   - Манифест `igaming-k8s/player-faces-tennis.yaml` с DNS-именами сервисов, Actuator readiness/liveness пробами и Jib-сборкой.
