# Proposal: [smm-multimedia] Видео-конвейер YouTube Shorts & TikTok: генерация 9:16 видео, озвучка и автопубликация

## Context
Plane Task ID: `119cc165-c6bc-4301-b4da-953db7304dd4`

## Description

Реализовать полностью автономный сервис `smm-video-shorts` (`smm-agent/video_shorts_pipeline.py`) для генерации вертикальных видео 9:16 на основе арбитражных возможностей (вилок ≥ 8%), с озвучкой через внешние облачные AI API (HeyGen, D-ID) и автоматической подготовкой мультиплатформенных бандлов для публикации на YouTube Shorts, Instagram Reels и TikTok.

### Архитектурные ограничения:
- Нода `xeon-srv` не имеет GPU → локальный рендеринг видео **запрещён**
- Вся видеосинтез делегируется внешним Cloud AI API (HeyGen v2, D-ID Talks)
- При отсутствии API-ключей — автоматический fallback на `MockCloudVideoProvider`
- HTTP-сервер на порту 8080 обслуживает health-пробы K8s (`/healthz`) и REST API

### Компоненты:
1. **`ShortsScriptGenerator`** — генерирует вирусные скрипты на основе данных из igaming-portal, с расчётом стейков, CTA, affiliate-ссылками с UTM и обязательным дисклеймером
2. **`BaseCloudVideoProvider` / `HeyGenCloudProvider` / `DIDCloudProvider`** — адаптеры к внешним AI Video API, async webhook-приём готовых MP4
3. **`ShortsAutoposter`** — подготавливает платформенные бандлы (YouTube Shorts, Reels, TikTok) и сохраняет в Redis (`smm:shorts:queue`)
4. **`VideoShortsHTTPHandler`** — HTTP-сервер с healthcheck, API `/api/v1/shorts/generate`, `/api/v1/shorts/latest`, `/api/v1/shorts/webhook`
5. **K8s Deployment** (`igaming-k8s/smm-video-shorts.yaml`) с liveness/readiness пробами и правильным образом `ghcr.io/datawikipro/smm-agent:latest`

### Связанные файлы:
- `smm-agent/video_shorts_pipeline.py` — основная реализация (937 строк)
- `smm-agent/runner.py` — интеграция режима `--mode shorts`/`video-shorts`
- `igaming-k8s/smm-video-shorts.yaml` — K8s Service + Deployment
- `openspec/specs/social-media-bot/spec.md` — обновлён с требованиями видео-конвейера
