# ✅ Реестр валидированных букмекеров (SmartBet.guru)

**Дата полной валидации кластера:** 20 сентября 2026 г.  
**Метод валидации:** `SELECT count(*) FROM match_cache;` через прямой запрос в PostgreSQL StatefulSets в namespace `igaming-source`.  
**Критерий готовности (Golden Rule #8):** $\ge 500$ активных матчей на каждый источник.  
**Экономия ресурсов (Rule #2):** Все deployments источников смасштабированы в `replicas=0` после фиксации линии.

---

## 🏆 ИТОГОВАЯ СВОДКА КЛАСТЕРА

| Метрика | Значение | Описание |
|---|---|---|
| **Всего источников в кластере** | **124** | Полный охват экосистемы SmartBet.guru |
| **VALIDATED OK ($\ge 500$ матчей)** | **124 (100%)** | Все 124 источника удовлетворяют Golden Rule #8 |
| **LOW (1–499 матчей)** | **0 (0%)** | Нет источников с неполной линией |
| **ZERO (0 матчей)** | **0 (0%)** | Все базы данных инициализированы и наполнены |
| **Активных подов краулеров/лоадеров** | **0** | Все 124 деплоймента в `replicas=0` для разгрузки Xeon CPU/RAM |

---

## 📊 ПОЛНЫЙ РЕЕСТР ИСТОЧНИКОВ (124 БУКМЕКЕРА)

| № | Букмекер | База данных (StatefulSet) | Матчей в базе | Статус DoD | K8s режим |
|---|---|---|---|---|---|
| 1 | **leon** | `igaming_leon` (`igaming-source-leon-db-0`) | 5 426 | VALIDATED | replicas=0 |
| 2 | **betm** | `igaming_betm` (`igaming-source-betm-db-0`) | 4 026 | VALIDATED | replicas=0 |
| 3 | **zenit** | `igaming_zenit` (`igaming-source-zenit-db-0`) | 3 781 | VALIDATED | replicas=0 |
| 4 | **fon-bet-kz** | `igaming_fonbet_kz` (`igaming-source-fon-bet-kz-db-0`) | 3 753 | VALIDATED | replicas=0 |
| 5 | **fon-bet-ru** | `igaming_fonbet` (`igaming-source-fon-bet-ru-db-0`) | 3 741 | VALIDATED | replicas=0 |
| 6 | **fon-bet-by** | `igaming_fonbet_by` (`igaming-source-fon-bet-by-db-0`) | 3 739 | VALIDATED | replicas=0 |
| 7 | **bettery** | `igaming_bettery` (`igaming-source-bettery-db-0`) | 3 736 | VALIDATED | replicas=0 |
| 8 | **pari** | `igaming_pari` (`igaming-source-pari-db-0`) | 3 454 | VALIDATED | replicas=0 |
| 9 | **baltbet** | `igaming_baltbet` (`igaming-source-baltbet-db-0`) | 3 167 | VALIDATED | replicas=0 |
| 10 | **winline** | `igaming_winline` (`igaming-source-winline-db-0`) | 3 069 | VALIDATED | replicas=0 |
| 11 | **betcity** | `igaming_betcity` (`igaming-source-betcity-db-0`) | 2 973 | VALIDATED | replicas=0 |
| 12 | **marathonbet-by** | `igaming_marathonbet_by` (`igaming-source-marathonbet-by-db-0`) | 2 603 | VALIDATED | replicas=0 |
| 13 | **marathonbet** | `igaming_marathonbet` (`igaming-source-marathonbet-db-0`) | 2 570 | VALIDATED | replicas=0 |
| 14 | **bovada** | `igaming_bovada` (`igaming-source-bovada-db-0`) | 2 247 | VALIDATED | replicas=0 |
| 15 | **pinnacle** | `igaming_pinnacle` (`igaming-source-pinnacle-db-0`) | 2 211 | VALIDATED | replicas=0 |
| 16 | **olimpbet** | `igaming_olimpbet` (`igaming-source-olimpbet-db-0`) | 2 182 | VALIDATED | replicas=0 |
| 17 | **melbet** | `igaming_melbet` (`igaming-source-melbet-db-0`) | 1 549 | VALIDATED | replicas=0 |
| 18 | **tennisi** | `igaming_tennisi` (`igaming-source-tennisi-db-0`) | 1 501 | VALIDATED | replicas=0 |
| 19 | **888starz** | `igaming_888starz` (`igaming-source-888starz-db-0`) | 1 486 | VALIDATED | replicas=0 |
| 20 | **betandyou** | `igaming_betandyou` (`igaming-source-betandyou-db-0`) | 1 486 | VALIDATED | replicas=0 |
| 21 | **betlabel** | `igaming_betlabel` (`igaming-source-betlabel-db-0`) | 1 479 | VALIDATED | replicas=0 |
| 22 | **apuestatotal** | `igaming_apuestatotal` (`igaming-source-apuestatotal-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 23 | **bcgame** | `igaming_bcgame` (`igaming-source-bcgame-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 24 | **bet365** | `igaming_bet365` (`igaming-source-bet365-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 25 | **bet7k** | `igaming_bet7k` (`igaming-source-bet7k-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 26 | **bet9ja** | `igaming_bet9ja` (`igaming-source-bet9ja-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 27 | **betano** | `igaming_betano` (`igaming-source-betano-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 28 | **betboom** | `igaming_betboom` (`igaming-source-betboom-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 29 | **betdaq** | `igaming_betdaq` (`igaming-source-betdaq-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 30 | **betesporte** | `igaming_betesporte` (`igaming-source-betesporte-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 31 | **betmgm** | `igaming_betmgm` (`igaming-source-betmgm-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 32 | **betmotion** | `igaming_betmotion` (`igaming-source-betmotion-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 33 | **betnacional** | `igaming_betnacional` (`igaming-source-betnacional-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 34 | **betvictor** | `igaming_betvictor` (`igaming-source-betvictor-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 35 | **betway** | `igaming_betway` (`igaming-source-betway-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 36 | **bookmaker** | `igaming_bookmaker` (`igaming-source-bookmaker-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 37 | **bwin** | `igaming_bwin` (`igaming-source-bwin-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 38 | **crickex** | `igaming_crickex` (`igaming-source-crickex-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 39 | **digitain** | `igaming_digitain` (`igaming-source-digitain-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 40 | **draftkings** | `igaming_draftkings` (`igaming-source-draftkings-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 41 | **esportesdasorte** | `igaming_esportesdasorte` (`igaming-source-esportesdasorte-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 42 | **fanduel** | `igaming_fanduel` (`igaming-source-fanduel-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 43 | **favbet** | `igaming_favbet` (`igaming-source-favbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 44 | **iddaa** | `igaming_iddaa` (`igaming-source-iddaa-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 45 | **interwetten** | `igaming_interwetten` (`igaming-source-interwetten-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 46 | **ligastavok** | `igaming_ligastavok` (`igaming-source-ligastavok-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 47 | **norsktipping** | `igaming_norsktipping` (`igaming-source-norsktipping-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 48 | **novig** | `novig` (`igaming-source-novig-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 49 | **parimatch** | `igaming_parimatch` (`igaming-source-parimatch-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 50 | **polymarket** | `igaming_polymarket` (`igaming-source-polymarket-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 51 | **sbobet** | `igaming_sbobet` (`igaming-source-sbobet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 52 | **sportbet** | `igaming_sportbet` (`igaming-source-sportbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 53 | **stake** | `igaming_stake` (`igaming-source-stake-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 54 | **sts** | `igaming_sts` (`igaming-source-sts-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 55 | **superbet** | `igaming_superbet` (`igaming-source-superbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 56 | **swisslos** | `igaming_swisslos` (`igaming-source-swisslos-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 57 | **tipsport** | `igaming_tipsport` (`igaming-source-tipsport-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 58 | **vaidebet** | `igaming_vaidebet` (`igaming-source-vaidebet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 59 | **vbet** | `igaming_vbet` (`igaming-source-vbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 60 | **veikkaus** | `igaming_veikkaus` (`igaming-source-veikkaus-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 61 | **wplay** | `igaming_wplay` (`igaming-source-wplay-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 62 | **1xbit** | `igaming_1xbit` (`igaming-source-1xbit-db-0`) | 1 423 | VALIDATED | replicas=0 |
| 63 | **fansport** | `igaming_fansport` (`igaming-source-fansport-db-0`) | 1 414 | VALIDATED | replicas=0 |
| 64 | **22bet** | `igaming_22bet` (`igaming-source-22bet-db-0`) | 1 411 | VALIDATED | replicas=0 |
| 65 | **spinbetter** | `igaming_spinbetter` (`igaming-source-spinbetter-db-0`) | 1 400 | VALIDATED | replicas=0 |
| 66 | **1xbet** | `igaming_xbet` (`igaming-source-1xbet-db-0`) | 1 394 | VALIDATED | replicas=0 |
| 67 | **melbet-com** | `igaming_melbet_com` (`igaming-source-melbet-com-db-0`) | 1 394 | VALIDATED | replicas=0 |
| 68 | **betwinner** | `igaming_betwinner` (`igaming-source-betwinner-db-0`) | 1 391 | VALIDATED | replicas=0 |
| 69 | **linebet** | `igaming_linebet` (`igaming-source-linebet-db-0`) | 1 389 | VALIDATED | replicas=0 |
| 70 | **betcity-com** | `igaming_betcity_com` (`igaming-source-betcity-com-db-0`) | 1 362 | VALIDATED | replicas=0 |
| 71 | **megapari** | `igaming_megapari` (`igaming-source-megapari-db-0`) | 1 009 | VALIDATED | replicas=0 |
| 72 | **ballybet** | `ballybet` (`igaming-source-ballybet-db-0`) | 817 | VALIDATED | replicas=0 |
| 73 | **stoiximan** | `igaming_stoiximan` (`igaming-source-stoiximan-db-0`) | 817 | VALIDATED | replicas=0 |
| 74 | **tonybet** | `igaming_tonybet` (`igaming-source-tonybet-db-0`) | 817 | VALIDATED | replicas=0 |
| 75 | **10bet** | `igaming_10bet` (`igaming-source-10bet-db-0`) | 813 | VALIDATED | replicas=0 |
| 76 | **betman** | `igaming_betman` (`igaming-source-betman-db-0`) | 813 | VALIDATED | replicas=0 |
| 77 | **winner** | `igaming_winner` (`igaming-source-winner-db-0`) | 813 | VALIDATED | replicas=0 |
| 78 | **betbra** | `igaming_betbra` (`igaming-source-betbra-db-0`) | 809 | VALIDATED | replicas=0 |
| 79 | **mozzartbet** | `igaming_mozzartbet` (`igaming-source-mozzartbet-db-0`) | 809 | VALIDATED | replicas=0 |
| 80 | **novibet** | `igaming_novibet` (`igaming-source-novibet-db-0`) | 809 | VALIDATED | replicas=0 |
| 81 | **tipico** | `igaming_tipico` (`igaming-source-tipico-db-0`) | 809 | VALIDATED | replicas=0 |
| 82 | **paf** | `igaming_paf` (`igaming-source-paf-db-0`) | 803 | VALIDATED | replicas=0 |
| 83 | **expekt** | `igaming_expekt` (`igaming-source-expekt-db-0`) | 799 | VALIDATED | replicas=0 |
| 84 | **grosvenor** | `igaming_grosvenor` (`igaming-source-grosvenor-db-0`) | 799 | VALIDATED | replicas=0 |
| 85 | **smarkets** | `smarkets` (`igaming-source-smarkets-db-0`) | 798 | VALIDATED | replicas=0 |
| 86 | **luckia** | `igaming_luckia` (`igaming-source-luckia-db-0`) | 797 | VALIDATED | replicas=0 |
| 87 | **betrivers** | `igaming_betrivers` (`igaming-source-betrivers-db-0`) | 791 | VALIDATED | replicas=0 |
| 88 | **casumo** | `igaming_casumo` (`igaming-source-casumo-db-0`) | 791 | VALIDATED | replicas=0 |
| 89 | **betplay** | `igaming_betplay` (`igaming-source-betplay-db-0`) | 790 | VALIDATED | replicas=0 |
| 90 | **betcitynl** | `igaming_betcitynl` (`igaming-source-betcitynl-db-0`) | 786 | VALIDATED | replicas=0 |
| 91 | **dafabet** | `igaming_dafabet` (`igaming-source-dafabet-db-0`) | 700 | VALIDATED | replicas=0 |
| 92 | **caesars** | `igaming_caesars` (`igaming-source-caesars-db-0`) | 699 | VALIDATED | replicas=0 |
| 93 | **matchbook** | `matchbook` (`igaming-source-matchbook-db-0`) | 681 | VALIDATED | replicas=0 |
| 94 | **unibet** | `igaming_unibet` (`igaming-source-unibet-db-0`) | 660 | VALIDATED | replicas=0 |
| 95 | **betfair** | `igaming_betfair` (`igaming-source-betfair-db-0`) | 653 | VALIDATED | replicas=0 |
| 96 | **tab** | `igaming_tab` (`igaming-source-tab-db-0`) | 651 | VALIDATED | replicas=0 |
| 97 | **fortuna** | `igaming_fortuna` (`igaming-source-fortuna-db-0`) | 650 | VALIDATED | replicas=0 |
| 98 | **888sport** | `igaming_888sport` (`igaming-source-888sport-db-0`) | 648 | VALIDATED | replicas=0 |
| 99 | **leovegas** | `igaming_leovegas` (`igaming-source-leovegas-db-0`) | 648 | VALIDATED | replicas=0 |
| 100 | **mrgreen** | `igaming_mrgreen` (`igaming-source-mrgreen-db-0`) | 648 | VALIDATED | replicas=0 |
| 101 | **rushbet** | `igaming_rushbet` (`igaming-source-rushbet-db-0`) | 648 | VALIDATED | replicas=0 |
| 102 | **sunbet** | `igaming_sunbet` (`igaming-source-sunbet-db-0`) | 648 | VALIDATED | replicas=0 |
| 103 | **atg** | `igaming_atg` (`igaming-source-atg-db-0`) | 642 | VALIDATED | replicas=0 |
| 104 | **betsafe** | `igaming_betsafe` (`igaming-source-betsafe-db-0`) | 635 | VALIDATED | replicas=0 |
| 105 | **betsson** | `igaming_betsson` (`igaming-source-betsson-db-0`) | 635 | VALIDATED | replicas=0 |
| 106 | **nordicbet** | `igaming_nordicbet` (`igaming-source-nordicbet-db-0`) | 635 | VALIDATED | replicas=0 |
| 107 | **olimpbet-kz** | `igaming_olimpbet_kz` (`igaming-source-olimpbet-kz-db-0`) | 632 | VALIDATED | replicas=0 |
| 108 | **marathonbet-com** | `igaming_marathonbet_com` (`igaming-source-marathonbet-com-db-0`) | 554 | VALIDATED | replicas=0 |
| 109 | **cloudbet** | `igaming_cloudbet` (`igaming-source-cloudbet-db-0`) | 508 | VALIDATED | replicas=0 |
| 110 | **caliente** | `igaming_caliente` (`igaming-source-caliente-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 111 | **estrelabet** | `igaming_estrelabet` (`igaming-source-estrelabet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 112 | **codere** | `igaming_codere` (`igaming-source-codere-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 113 | **winamax** | `igaming_winamax` (`igaming-source-winamax-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 114 | **supersport** | `igaming_supersport` (`igaming-source-supersport-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 115 | **nike** | `igaming_nike` (`igaming-source-nike-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 116 | **estave** | `igaming_estave` (`igaming-source-estave-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 117 | **sportybet** | `igaming_sportybet` (`igaming-source-sportybet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 118 | **circasports** | `igaming_circasports` (`igaming-source-circasports-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 119 | **superbook** | `igaming_superbook` (`igaming-source-superbook-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 120 | **fliff** | `igaming_fliff` (`igaming-source-fliff-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 121 | **pixbet** | `igaming_pixbet` (`igaming-source-pixbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 122 | **coolbet** | `igaming_coolbet` (`igaming-source-coolbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 123 | **sisal** | `igaming_sisal` (`igaming-source-sisal-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 124 | **snai** | `igaming_snai` (`igaming-source-snai-db-0`) | 1 453 | VALIDATED | replicas=0 |

---

## 🚧 Источники в разработке и бэклоге (Next Waves)

| Букмекер | Регион / Описание | База / Модуль | Матчей в базе | Статус |
|---|---|---|---|---|
| **ballybet-us** | США Tier-2 | `igaming-source-ballybet` | — | ⚪ Ожидает разработки (Backlog) |
| **pointsbet** | США / Австралия | `igaming-source-pointsbet` | — | ⚪ Ожидает разработки (Backlog) |
| **espnbet** | США (Penn Entertainment) | `igaming-source-espnbet` | — | ⚪ Ожидает разработки (Backlog) |
| **betfred** | Великобритания / США | `igaming-source-betfred` | — | ⚪ Ожидает разработки (Backlog) |

---

## 🛠️ Управление подами источников

### Масштабирование в 0 (сохранение ресурсов Xeon):
```bash
# Масштабирование любого источника в 0:
kubectl scale deployment/igaming-source-<bookmaker> --replicas=0 -n igaming-source
```

### Возобновление сбора котировок (Live):
```bash
kubectl scale deployment/igaming-source-<bookmaker> --replicas=1 -n igaming-source
```
