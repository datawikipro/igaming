# Implementation Tasks: [smm-multimedia] Видео-конвейер YouTube Shorts & TikTok: генерация 9:16 видео, озвучка и автопубликация
- [x] 1. Разработать архитектуру сервиса и базовую инфраструктуру видео-конвейера (ShortsScriptGenerator, CloudRenderJob, domain models, HTTP-сервер, CLI entrypoint)
- [x] 2. Реализовать генерацию видео-контента через облачные AI API (HeyGen, D-ID, Mock) и подготовку мультиплатформенных бандлов (YouTube Shorts, Instagram Reels, TikTok) с публикацией в Redis
- [x] 3. Обновить OpenSpec (social-media-bot/spec.md) с требованиями видео-конвейера и исправить K8s манифест smm-video-shorts.yaml (образ, удалить obsolete ConfigMap volume)
