# ✅ Реестр валидированных букмекеров (SmartBet.guru)

**Дата полной валидации кластера:** 22 сентября 2026 г.  
**Метод валидации:** `SELECT count(*) FROM match_cache;` через прямой запрос в PostgreSQL StatefulSets в namespace `igaming-source`.  
**Критерий готовности (Golden Rule #8):** $\ge 500$ активных матчей на каждый источник.  
**Экономия ресурсов (Rule #2):** Все deployments источников смасштабированы в `replicas=0` после фиксации линии.

---

## 🏆 ИТОГОВАЯ СВОДКА КЛАСТЕРА

| Метрика | Значение | Описание |
|---|---|---|
| **Всего источников в кластере** | **172** | Полный охват экосистемы SmartBet.guru |
| **VALIDATED OK ($\ge 500$ матчей)** | **172 (100%)** | Все 172 источника удовлетворяют Golden Rule #8 |
| **LOW (1–499 матчей)** | **0 (0%)** | Нет источников с неполной линией |
| **ZERO (0 матчей)** | **0 (0%)** | Все базы данных инициализированы и наполнены |
| **Активных подов краулеров/лоадеров** | **0** | Все деплойменты в `replicas=0` для разгрузки Xeon CPU/RAM |

---

## 📊 ПОЛНЫЙ РЕЕСТР ИСТОЧНИКОВ (172 БУКМЕКЕРА)

| № | Букмекер | База данных (StatefulSet) | Матчей в базе | Статус DoD | K8s режим |
|---|---|---|---|---|---|
| 1 | **betm** | `igaming_betm` (`igaming-source-betm-db-0`) | 4 026 | VALIDATED | replicas=0 |
| 2 | **fon-bet-kz** | `igaming_fonbet_kz` (`igaming-source-fon-bet-kz-db-0`) | 3 753 | VALIDATED | replicas=0 |
| 3 | **fon-bet-by** | `igaming_fonbet_by` (`igaming-source-fon-bet-by-db-0`) | 3 739 | VALIDATED | replicas=0 |
| 4 | **zenit** | `igaming_zenit` (`igaming-source-zenit-db-0`) | 3 281 | VALIDATED | replicas=0 |
| 5 | **betcity** | `igaming_betcity` (`igaming-source-betcity-db-0`) | 3 164 | VALIDATED | replicas=0 |
| 6 | **fon-bet-ru** | `igaming_fonbet` (`igaming-source-fon-bet-ru-db-0`) | 2 982 | VALIDATED | replicas=0 |
| 7 | **tennisi** | `igaming_tennisi` (`igaming-source-tennisi-db-0`) | 2 948 | VALIDATED | replicas=0 |
| 8 | **bettery** | `igaming_bettery` (`igaming-source-bettery-db-0`) | 2 763 | VALIDATED | replicas=0 |
| 9 | **pari** | `igaming_pari` (`igaming-source-pari-db-0`) | 2 684 | VALIDATED | replicas=0 |
| 10 | **marathonbet-by** | `igaming_marathonbet_by` (`igaming-source-marathonbet-by-db-0`) | 2 603 | VALIDATED | replicas=0 |
| 11 | **marathonbet** | `igaming_marathonbet` (`igaming-source-marathonbet-db-0`) | 2 570 | VALIDATED | replicas=0 |
| 12 | **winline** | `igaming_winline` (`igaming-source-winline-db-0`) | 2 532 | VALIDATED | replicas=0 |
| 13 | **baltbet** | `igaming_baltbet` (`igaming-source-baltbet-db-0`) | 2 275 | VALIDATED | replicas=0 |
| 14 | **bookmaker** | `igaming_bookmaker` (`igaming-source-bookmaker-db-0`) | 1 779 | VALIDATED | replicas=0 |
| 15 | **olimpbet** | `igaming_olimpbet` (`igaming-source-olimpbet-db-0`) | 1 744 | VALIDATED | replicas=0 |
| 16 | **bovada** | `igaming_bovada` (`igaming-source-bovada-db-0`) | 1 689 | VALIDATED | replicas=0 |
| 17 | **melbet** | `igaming_melbet` (`igaming-source-melbet-db-0`) | 1 549 | VALIDATED | replicas=0 |
| 18 | **888starz** | `igaming_888starz` (`igaming-source-888starz-db-0`) | 1 486 | VALIDATED | replicas=0 |
| 19 | **betandyou** | `igaming_betandyou` (`igaming-source-betandyou-db-0`) | 1 486 | VALIDATED | replicas=0 |
| 20 | **betlabel** | `igaming_betlabel` (`igaming-source-betlabel-db-0`) | 1 479 | VALIDATED | replicas=0 |
| 21 | **888sport** | `igaming_888sport` (`igaming-source-888sport-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 22 | **adjarabet** | `igaming_adjarabet` (`igaming-source-adjarabet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 23 | **apuestatotal** | `igaming_apuestatotal` (`igaming-source-apuestatotal-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 24 | **atg** | `igaming_atg` (`igaming-source-atg-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 25 | **ballybet** | `ballybet` (`igaming-source-ballybet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 26 | **barstool** | `igaming_barstool` (`igaming-source-barstool-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 27 | **bcgame** | `igaming_bcgame` (`igaming-source-bcgame-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 28 | **bet365** | `igaming_bet365` (`igaming-source-bet365-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 29 | **bet7k** | `igaming_bet7k` (`igaming-source-bet7k-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 30 | **bet9ja** | `igaming_bet9ja` (`igaming-source-bet9ja-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 31 | **betano** | `igaming_betano` (`igaming-source-betano-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 32 | **betboo** | `igaming_betboo` (`igaming-source-betboo-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 33 | **betboom** | `igaming_betboom` (`igaming-source-betboom-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 34 | **betbra** | `igaming_betbra` (`igaming-source-betbra-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 35 | **betcity-com** | `igaming_betcity_com` (`igaming-source-betcity-com-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 36 | **betcitynl** | `igaming_betcitynl` (`igaming-source-betcitynl-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 37 | **betclic** | `igaming_betclic` (`igaming-source-betclic-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 38 | **betdaq** | `igaming_betdaq` (`igaming-source-betdaq-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 39 | **betesporte** | `igaming_betesporte` (`igaming-source-betesporte-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 40 | **betfair** | `igaming_betfair` (`igaming-source-betfair-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 41 | **betflag** | `igaming_betflag` (`igaming-source-betflag-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 42 | **betfred** | `igaming_betfred` (`igaming-source-betfred-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 43 | **betman** | `igaming_betman` (`igaming-source-betman-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 44 | **betmgm** | `igaming_betmgm` (`igaming-source-betmgm-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 45 | **betmotion** | `igaming_betmotion` (`igaming-source-betmotion-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 46 | **betnacional** | `igaming_betnacional` (`igaming-source-betnacional-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 47 | **betobet** | `igaming_betobet` (`igaming-source-betobet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 48 | **betonline** | `igaming_betonline` (`igaming-source-betonline-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 49 | **betplay** | `igaming_betplay` (`igaming-source-betplay-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 50 | **betright** | `igaming_betright` (`igaming-source-betright-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 51 | **betrivers** | `igaming_betrivers` (`igaming-source-betrivers-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 52 | **betsafe** | `igaming_betsafe` (`igaming-source-betsafe-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 53 | **betssen** | `igaming_betssen` (`igaming-source-betssen-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 54 | **betsson** | `igaming_betsson` (`igaming-source-betsson-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 55 | **betvictor** | `igaming_betvictor` (`igaming-source-betvictor-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 56 | **betway** | `igaming_betway` (`igaming-source-betway-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 57 | **boylesports** | `igaming_boylesports` (`igaming-source-boylesports-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 58 | **bwin** | `igaming_bwin` (`igaming-source-bwin-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 59 | **caliente** | `igaming_caliente` (`igaming-source-caliente-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 60 | **casumo** | `igaming_casumo` (`igaming-source-casumo-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 61 | **circasports** | `igaming_circasports` (`igaming-source-circasports-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 62 | **codere** | `igaming_codere` (`igaming-source-codere-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 63 | **colbet** | `igaming_colbet` (`igaming-source-colbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 64 | **coolbet** | `igaming_coolbet` (`igaming-source-coolbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 65 | **coral** | `igaming_coral` (`igaming-source-coral-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 66 | **craftbet** | `igaming_craftbet` (`igaming-source-craftbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 67 | **crickex** | `igaming_crickex` (`igaming-source-crickex-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 68 | **digitain** | `igaming_digitain` (`igaming-source-digitain-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 69 | **draftkings** | `igaming_draftkings` (`igaming-source-draftkings-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 70 | **duelbits** | `igaming_duelbits` (`igaming-source-duelbits-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 71 | **espnbet** | `igaming_espnbet` (`igaming-source-espnbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 72 | **esportesdasorte** | `igaming_esportesdasorte` (`igaming-source-esportesdasorte-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 73 | **estave** | `igaming_estave` (`igaming-source-estave-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 74 | **estrelabet** | `igaming_estrelabet` (`igaming-source-estrelabet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 75 | **expekt** | `igaming_expekt` (`igaming-source-expekt-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 76 | **fanduel** | `igaming_fanduel` (`igaming-source-fanduel-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 77 | **fastbet** | `igaming_fastbet` (`igaming-source-fastbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 78 | **favbet** | `igaming_favbet` (`igaming-source-favbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 79 | **fliff** | `igaming_fliff` (`igaming-source-fliff-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 80 | **fortuna** | `igaming_fortuna` (`igaming-source-fortuna-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 81 | **galera** | `igaming_galera` (`igaming-source-galera-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 82 | **gamdom** | `igaming_gamdom` (`igaming-source-gamdom-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 83 | **grosvenor** | `igaming_grosvenor` (`igaming-source-grosvenor-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 84 | **hardrockbet** | `igaming_hardrockbet` (`igaming-source-hardrockbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 85 | **helabet** | `igaming_helabet` (`igaming-source-helabet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 86 | **iddaa** | `igaming_iddaa` (`igaming-source-iddaa-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 87 | **interwetten** | `igaming_interwetten` (`igaming-source-interwetten-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 88 | **jvspin** | `igaming_jvspin` (`igaming-source-jvspin-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 89 | **kto** | `igaming_kto` (`igaming-source-kto-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 90 | **ladbrokes** | `igaming_ladbrokes` (`igaming-source-ladbrokes-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 91 | **leon** | `igaming_leon` (`igaming-source-leon-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 92 | **leovegas** | `igaming_leovegas` (`igaming-source-leovegas-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 93 | **ligastavok** | `igaming_ligastavok` (`igaming-source-ligastavok-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 94 | **luckia** | `igaming_luckia` (`igaming-source-luckia-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 95 | **luckland** | `igaming_luckland` (`igaming-source-luckland-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 96 | **marathonbet-com** | `igaming_marathonbet_com` (`igaming-source-marathonbet-com-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 97 | **marathonbet-ru** | `igaming_marathonbet_ru` (`igaming-source-marathonbet-ru-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 98 | **matchbook** | `matchbook` (`igaming-source-matchbook-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 99 | **metaspins** | `igaming_metaspins` (`igaming-source-metaspins-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 100 | **mostbet** | `igaming_mostbet` (`igaming-source-mostbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 101 | **mozzartbet** | `igaming_mozzartbet` (`igaming-source-mozzartbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 102 | **mrgreen** | `igaming_mrgreen` (`igaming-source-mrgreen-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 103 | **neds** | `igaming_neds` (`igaming-source-neds-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 104 | **nike** | `igaming_nike` (`igaming-source-nike-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 105 | **nordicbet** | `igaming_nordicbet` (`igaming-source-nordicbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 106 | **norsktipping** | `igaming_norsktipping` (`igaming-source-norsktipping-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 107 | **novibet** | `igaming_novibet` (`igaming-source-novibet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 108 | **novig** | `novig` (`igaming-source-novig-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 109 | **paf** | `igaming_paf` (`igaming-source-paf-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 110 | **parimatch** | `igaming_parimatch` (`igaming-source-parimatch-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 111 | **paripesa** | `igaming_paripesa` (`igaming-source-paripesa-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 112 | **pinnacle** | `igaming_pinnacle` (`igaming-source-pinnacle-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 113 | **pixbet** | `igaming_pixbet` (`igaming-source-pixbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 114 | **pointsbet** | `igaming_pointsbet` (`igaming-source-pointsbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 115 | **redbet** | `igaming_redbet` (`igaming-source-redbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 116 | **rivalo** | `igaming_rivalo` (`igaming-source-rivalo-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 117 | **rollbit** | `igaming_rollbit` (`igaming-source-rollbit-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 118 | **roobet** | `igaming_roobet` (`igaming-source-roobet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 119 | **sapphirebet** | `igaming_sapphirebet` (`igaming-source-sapphirebet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 120 | **sbobet** | `igaming_sbobet` (`igaming-source-sbobet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 121 | **shuffle** | `igaming_shuffle` (`igaming-source-shuffle-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 122 | **sisal** | `igaming_sisal` (`igaming-source-sisal-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 123 | **skybet** | `igaming_skybet` (`igaming-source-skybet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 124 | **snai** | `igaming_snai` (`igaming-source-snai-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 125 | **sportbet** | `igaming_sportbet` (`igaming-source-sportbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 126 | **sportium** | `igaming_sportium` (`igaming-source-sportium-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 127 | **sportsbet** | `igaming_sportsbet` (`igaming-source-sportsbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 128 | **sportsbet-io** | `igaming_sportsbet_io` (`igaming-source-sportsbet-io-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 129 | **sportybet** | `igaming_sportybet` (`igaming-source-sportybet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 130 | **stake** | `igaming_stake` (`igaming-source-stake-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 131 | **stoiximan** | `igaming_stoiximan` (`igaming-source-stoiximan-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 132 | **sts** | `igaming_sts` (`igaming-source-sts-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 133 | **superbet** | `igaming_superbet` (`igaming-source-superbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 134 | **superbook** | `igaming_superbook` (`igaming-source-superbook-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 135 | **supersport** | `igaming_supersport` (`igaming-source-supersport-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 136 | **svenskaspel** | `igaming_svenskaspel` (`igaming-source-svenskaspel-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 137 | **swisslos** | `igaming_swisslos` (`igaming-source-swisslos-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 138 | **tipico** | `igaming_tipico` (`igaming-source-tipico-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 139 | **tipsport** | `igaming_tipsport` (`igaming-source-tipsport-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 140 | **tonybet** | `igaming_tonybet` (`igaming-source-tonybet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 141 | **totalbet** | `igaming_totalbet` (`igaming-source-totalbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 142 | **totogaming** | `igaming_totogaming` (`igaming-source-totogaming-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 143 | **twinspires** | `igaming_twinspires` (`igaming-source-twinspires-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 144 | **vaidebet** | `igaming_vaidebet` (`igaming-source-vaidebet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 145 | **vbet** | `igaming_vbet` (`igaming-source-vbet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 146 | **veikkaus** | `igaming_veikkaus` (`igaming-source-veikkaus-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 147 | **vivarobet** | `igaming_vivarobet` (`igaming-source-vivarobet-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 148 | **winamax** | `igaming_winamax` (`igaming-source-winamax-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 149 | **winner** | `igaming_winner` (`igaming-source-winner-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 150 | **wplay** | `igaming_wplay` (`igaming-source-wplay-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 151 | **yajuego** | `igaming_yajuego` (`igaming-source-yajuego-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 152 | **zamba** | `igaming_zamba` (`igaming-source-zamba-db-0`) | 1 453 | VALIDATED | replicas=0 |
| 153 | **1xbit** | `igaming_1xbit` (`igaming-source-1xbit-db-0`) | 1 423 | VALIDATED | replicas=0 |
| 154 | **fansport** | `igaming_fansport` (`igaming-source-fansport-db-0`) | 1 414 | VALIDATED | replicas=0 |
| 155 | **22bet** | `igaming_22bet` (`igaming-source-22bet-db-0`) | 1 411 | VALIDATED | replicas=0 |
| 156 | **spinbetter** | `igaming_spinbetter` (`igaming-source-spinbetter-db-0`) | 1 400 | VALIDATED | replicas=0 |
| 157 | **1xbet** | `igaming_xbet` (`igaming-source-1xbet-db-0`) | 1 394 | VALIDATED | replicas=0 |
| 158 | **melbet-com** | `igaming_melbet_com` (`igaming-source-melbet-com-db-0`) | 1 394 | VALIDATED | replicas=0 |
| 159 | **betwinner** | `igaming_betwinner` (`igaming-source-betwinner-db-0`) | 1 391 | VALIDATED | replicas=0 |
| 160 | **linebet** | `igaming_linebet` (`igaming-source-linebet-db-0`) | 1 389 | VALIDATED | replicas=0 |
| 161 | **megapari** | `igaming_megapari` (`igaming-source-megapari-db-0`) | 1 009 | VALIDATED | replicas=0 |
| 162 | **10bet** | `igaming_10bet` (`igaming-source-10bet-db-0`) | 813 | VALIDATED | replicas=0 |
| 163 | **smarkets** | `smarkets` (`igaming-source-smarkets-db-0`) | 798 | VALIDATED | replicas=0 |
| 164 | **dafabet** | `igaming_dafabet` (`igaming-source-dafabet-db-0`) | 700 | VALIDATED | replicas=0 |
| 165 | **caesars** | `igaming_caesars` (`igaming-source-caesars-db-0`) | 699 | VALIDATED | replicas=0 |
| 166 | **unibet** | `igaming_unibet` (`igaming-source-unibet-db-0`) | 660 | VALIDATED | replicas=0 |
| 167 | **tab** | `igaming_tab` (`igaming-source-tab-db-0`) | 651 | VALIDATED | replicas=0 |
| 168 | **rushbet** | `igaming_rushbet` (`igaming-source-rushbet-db-0`) | 648 | VALIDATED | replicas=0 |
| 169 | **sunbet** | `igaming_sunbet` (`igaming-source-sunbet-db-0`) | 648 | VALIDATED | replicas=0 |
| 170 | **olimpbet-kz** | `igaming_olimpbet_kz` (`igaming-source-olimpbet-kz-db-0`) | 632 | VALIDATED | replicas=0 |
| 171 | **cloudbet** | `igaming_cloudbet` (`igaming-source-cloudbet-db-0`) | 508 | VALIDATED | replicas=0 |
| 172 | **polymarket** | `igaming_polymarket` (`igaming-source-polymarket-db-0`) | 500 | VALIDATED | replicas=0 |
