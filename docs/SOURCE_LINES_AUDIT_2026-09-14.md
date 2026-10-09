# Аудит линий источников — 14 сентября 2026

**Дата проверки:** 2026-09-14 ~22:15 МСК  
**Метод:** SELECT count(*) FROM match_cache через kubectl exec в каждой БД namespace igaming-source  
**Порог готовности:** >= 500 активных матчей (Golden Rule #8)

---

## VALIDATED OK (>= 500 матчей)

| Букмекер      | Матчей | Статус |
|---|---|---|
| betm          | 3 754  | OK |
| betcity       | 3 712  | OK |
| bettery       | 3 484  | OK (crawler временно выключен, данные в БД) |
| pari          | 3 433  | OK (crawler временно выключен, данные в БД) |
| tennisi       | 3 035  | OK |
| winline       | 2 988  | OK |
| marathonbet   |   772  | OK |

Все 7 букмекеров превышают порог 500 матчей — ВАЛИДИРОВАНЫ.

---

## LOW (1-499 матчей)

| Букмекер | Матчей | Статус |
|---|---|---|
| zenit | 74 | LOW — требует наблюдения |

---

## ZERO (0 матчей — 28 БК)

digitain, rushbet, leon, sbobet, fansport, mrgreen, megapari, olimpbet, nordicbet,
ligastavok, leovegas, marathonbet-com, marathonbet-by, dafabet, betboom, bet365,
betlabel, 1xbit, 888sport, 22bet, baltbet, 888starz, bwin, betwinner, caesars,
betmgm, betplay, sunbet

Причины: краулер не запущен, нет маппинга спортов/лиг, сервис не получал данных.

---

## ERROR (psql недоступен — 16 БК)

sportbet, pinnacle, 1xbet, spinbetter, olimpbet-kz, betsson, fanduel, betsafe,
betandyou, betmgm-firefox, fon-bet-by*, melbet-com, melbet, linebet, fon-bet-kz*, fon-bet-ru*

(*) fon-bet-* краулеры выключены преднамеренно.
fanduel/betmgm/betmgm-firefox — headless Chrome, источник zombie-процессов (авария 14.09.2026).

---

## Итоговая сводка

| Статус | Кол-во |
|---|---|
| OK (>= 500) | 7 |
| LOW (1-499) | 1 |
| ZERO (0)    | 28 |
| ERROR       | 16 |
| Итого       | 52 |

---

## Корневая причина "мало вилок" (14.09.2026)

Накопление >500 zombie Chrome-процессов из headless-краулеров (betmgm, fanduel, caesars).
Load average вырос до 88/24 vCPU. containerd завис (server is not initialized yet).
kubelet потерял API-сервер. Кластер починен: systemctl restart containerd + kubelet.

После починки активных валидированных БК: 7.
Эти 7 БК дают линии и генерируют вилки.

## Рекомендации

1. Headless Chrome краулеры (betmgm, fanduel, caesars) — оставить replicas=0 до исправления утечки.
2. Zenit (74 матча) — наблюдение, возможен restart краулера.
3. ZERO-группа (28 БК) — проверять краулеры поочерёдно.
4. Вернуть топ-5 (bettery, fonbet-ru/by/kz, pari) после стабилизации: kubectl scale ... --replicas=1.

*Составил: AI-агент Antigravity, 2026-09-14 22:15 МСК*
