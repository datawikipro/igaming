# Proposal: #44: [aggregator] Синтетический арбитраж: мульти-букмекерские формулы вилок и коридоров для Чет/Нечет, BTTS, карт CS2 и тоталов статистики

## Context
Plane Task ID: `2c3f1de2-b988-4d24-b3ab-20555dfa20a2`
Sequence ID: 44
Module: `aggregator-surebet`

## Problem & Motivation

Текущий движок обнаружения арбитража (`aggregator-surebet`) поддерживает классические двусторонние и трёхсторонние формулы для исходов 1X2, двойного шанса (DC) и базовых тоталов (Больше/Меньше). При этом большинство современных букмекерских линий содержат обширные дополнительные рынки, которые движок не охватывает:

1. **Чет/Нечет (Even/Odd)** — тип ставки с двумя взаимоисключающими исходами, формально аналогичный ставке на исход 1X2 без ничьей. Разница в том, что эти исходы не имеют явного фаворита и котируются близко к 2.0 на большинстве букмекеров. Это создаёт арбитражные возможности при межбукмекерских расхождениях.

2. **BTTS (Both Teams To Score / Обе забьют)** — бинарный рынок (Да/Нет). Присутствует у всех основных букмекеров для футбольных матчей. Коэффициенты значительно отличаются между «soft» и «sharp» букмекерами, создавая устойчивые +EV и вилочные возможности.

3. **Карты CS2 / Тоталы раундов (карт)** — тип рынка, специфичный для киберспорта (Counter-Strike 2): тоталы карт (больше/меньше 2.5 карт), победитель карты, тоталы раундов на конкретной карте. Формульно является разновидностью тотала, но с целыми значениями и специфичными для BO1/BO3/BO5 форматами.

4. **Тоталы статистики** — расширенные рынки для угловых, жёлтых карточек, голов конкретного игрока, фолов, бросков по воротам. Эти рынки покрываются у разных букмекеров с разными линиями, что порождает коридорные ситуации (Middles).

Текущий движок не распознаёт эти типы ставок при матчинге по `bet_type` и не применяет к ним арбитражные формулы. Вилки и коридоры по этим рынкам остаются необнаруженными.

## Proposed Architecture & Changes

### 1. Расширение типологии ставок (BetType)

Добавить новые значения в перечисление `BetType`:

| BetType | Описание | Формула |
|---|---|---|
| `EVEN_ODD_EVEN` | Чётное число голов/очков | 2-way: $\frac{1}{O_{even}} + \frac{1}{O_{odd}} < 1$ |
| `EVEN_ODD_ODD` | Нечётное число голов/очков | Парный исход с `EVEN_ODD_EVEN` |
| `BTTS_YES` | Обе команды забьют — Да | 2-way: $\frac{1}{O_{yes}} + \frac{1}{O_{no}} < 1$ |
| `BTTS_NO` | Обе команды забьют — Нет | Парный исход с `BTTS_YES` |
| `MAP_TOTAL_OVER` | Тотал карт больше (CS2) | Тотал-формула с целочисленным handicap |
| `MAP_TOTAL_UNDER` | Тотал карт меньше (CS2) | Коридорная зона при over/under одного значения |
| `STAT_TOTAL_OVER` | Тотал статистики больше | Коридор при разных линиях тотала |
| `STAT_TOTAL_UNDER` | Тотал статистики меньше | Парный исход с `STAT_TOTAL_OVER` |

### 2. Формулы арбитража

#### Чет/Нечет (Even/Odd) — 2-way surebet
$$\text{Arb} = \frac{1}{O_{even}} + \frac{1}{O_{odd}} < 1.0$$

Оптимальное распределение ставок:
$$S_{even} = \frac{B}{O_{even} \cdot \left(\frac{1}{O_{even}} + \frac{1}{O_{odd}}\right)}, \quad S_{odd} = B - S_{even}$$

#### BTTS — 2-way surebet
$$\text{Arb} = \frac{1}{O_{yes}} + \frac{1}{O_{no}} < 1.0$$

Идентична формуле Чет/Нечет. Матчинг по паре `BTTS_YES` ↔ `BTTS_NO` на одном матче.

#### Тоталы карт CS2 / Тоталы статистики — Middle Bet (коридор)
Для двух букмекеров A и B с разными линиями тотала:
- БК A: Больше $T_A$ с коэффициентом $O_A^{over}$
- БК B: Меньше $T_B$ с коэффициентом $O_B^{under}$

**Условие коридора:** $T_A < T_B$ (зона двойного выигрыша существует)

$$P(\text{double win}) = P(T_A < X \leq T_B)$$

Вилка при стандартном тотале (Over/Under одной линии у разных БК):
$$\text{Arb} = \frac{1}{O_A^{over}} + \frac{1}{O_B^{under}} < 1.0$$

### 3. Нормализация именования рынков

Введение `BetTypeNormalizer` с правилами маппинга сырых строк от разных букмекеров к каноническим `BetType`:

```
"Чет" / "Чётное" / "Even" / "Even Total" → EVEN_ODD_EVEN
"Нечет" / "Нечётное" / "Odd" / "Odd Total" → EVEN_ODD_ODD
"Обе забьют: Да" / "BTTS: Yes" / "Both Teams Score" → BTTS_YES
"Обе забьют: Нет" / "BTTS: No" → BTTS_NO
"Карты: Больше 2.5" / "Maps O2.5" / "Total Maps Over" → MAP_TOTAL_OVER
"Карты: Меньше 2.5" / "Maps U2.5" → MAP_TOTAL_UNDER
"Угловые: Больше N" / "Corners Over" → STAT_TOTAL_OVER (subtype: CORNERS)
"Жёлтые карточки: Больше N" / "Cards Over" → STAT_TOTAL_OVER (subtype: CARDS)
"Броски по воротам: Больше N" / "Shots Over" → STAT_TOTAL_OVER (subtype: SHOTS)
```

### 4. Изменения в aggregator-surebet

- `SurebetRuleEvaluator`: добавить ветки `case EVEN_ODD_EVEN, BTTS_YES, MAP_TOTAL_OVER, STAT_TOTAL_OVER` для 2-way и middle расчётов.
- `MiddleDetector`: поддержать `MAP_TOTAL_OVER/UNDER` и `STAT_TOTAL_OVER/UNDER` с проверкой условия $T_A < T_B$.
- `BetTypeMatcher`: расширить матчинг парами `(EVEN_ODD_EVEN, EVEN_ODD_ODD)`, `(BTTS_YES, BTTS_NO)`, `(MAP_TOTAL_OVER, MAP_TOTAL_UNDER)`, `(STAT_TOTAL_OVER, STAT_TOTAL_UNDER)`.

## Capabilities

### Modified Capabilities
- `aggregator-core`: Расширяет движок обнаружения арбитража для покрытия рынков Чет/Нечет, BTTS, CS2-карт и тоталов статистики с применением корректных 2-way и middle формул.

## Impact

- **Affected Modules**:
  - `aggregator-surebet` (`SurebetRuleEvaluator`, `MiddleDetector`, `BetTypeMatcher`)
  - `aggregator-domain` (`BetType` enum, `BetTypeNormalizer`)
  - `igaming-dto` (`BetTypeDto`, `SurebetAlertDto` — subtype поле)
  - `igaming-source-core` (`AbstractBetTypeMapper` — правила маппинга новых типов)
