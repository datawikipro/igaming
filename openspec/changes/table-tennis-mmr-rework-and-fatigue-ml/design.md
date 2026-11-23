# Design: Table Tennis MMR Rework & Fatigue ML Architecture

## 1. Математическая модель прогрессивного асимметричного MMR

### 1.1. Асимметричное масштабирование победы фаворита
Пусть $\Delta = MMR_{\text{winner}} - MMR_{\text{loser}}$.
Если победитель имел более высокий рейтинг до матча ($\Delta > 0$, фаворит победил):
$$
\gamma_{\text{fav\_win}}(\Delta) = 
\begin{cases}
1.0, & \Delta \le 50 \\
1.0 - \frac{\Delta - 50}{100}, & 50 < \Delta < 150 \\
0.0, & \Delta \ge 150
\end{cases}
$$
То есть при разнице $\ge 150$ очков фаворит получает $+0.0$ MMR. Победа над значительно более слабым соперником является ожидаемой и не дает права на повышение рейтинга.

### 1.2. Прогрессивное наказание за сенсационное поражение (Upset Penalty)
Если фаворит проиграл андердогу ($\Delta = MMR_{\text{fav}} - MMR_{\text{underdog}} > 0$):
$$
\lambda_{\text{fav\_loss}}(\Delta) = 
\begin{cases}
1.0, & \Delta \le 50 \\
1.0 + \frac{\Delta - 50}{100} \cdot 0.75, & 50 < \Delta < 150 \\
1.75, & \Delta \ge 150
\end{cases}
$$
Фаворит, проигравший сопернику на 150+ очков слабее, теряет в $1.75\times$ больше очков, чем по стандартному Glicko-2. Это мгновенно сдувает случайно накрученный в замкнутом пуле рейтинг.

### 1.3. Затухание повторных встреч (H2H Rematch Decay)
Для каждой пары игроков $(p_1, p_2)$ отслеживается количество встреч за последние 7 дней $N_{7d}(p_1, p_2)$:
$$
\omega_{\text{h2h}} = \frac{1.0}{\sqrt{\max(1, N_{7d}(p_1, p_2))}}
$$
Дельта матча умножается на $\omega_{\text{h2h}}$.

## 2. Физиологическая модель усталости (Same-Day Fatigue Architecture)

В настольном теннисе матчи длятся по 20–30 минут, и спортсмены могут играть 4–10 матчей за день в турнирной сетке.
Пусть $k \ge 1$ — порядковый номер матча игрока в текущий календарный день (UTC/Local date).

### 2.1. Индекс накопленной усталости (Fatigue Index)
$$
\text{Fatigue}(k) = \sum_{i=1}^{k-1} \left( 1.0 + 0.25 \cdot (\text{sets}_i - 3) \right) \cdot e^{-\lambda \cdot \Delta t_i}
$$
где $\Delta t_i$ — время в часах с момента завершения $i$-го матча, $\lambda = 0.15$ (коэффициент метаболического восстановления).

### 2.2. Векторные размерности в пространстве признаков
Для сохранения строгой синхронизации Java (`TableTennisVectorExtractor.java`) и Python (`prepare_multitask_matrix.py`):
- `vec[34]` — `daily_matches_ratio`: $\min(1.0, \frac{\text{matches\_today}}{6.0})$ (доля от высокой дневной нагрузки).
- `vec[35]` — `daily_fatigue_level`: $\tanh(\frac{\text{fatigue\_index}}{4.0}) \in [0, 1]$.
- `vec[36]` — `fatigue_differential`: $\tanh(\frac{\text{fatigue}_{p1} - \text{fatigue}_{p2}}{3.0}) \in [-1, 1]$.
- `vec[37]` — `daily_win_momentum`: win rate в текущий день (если матчей $\ge 1$, иначе 0.5).
- `vec[38]` — `rest_hours_decay`: $1.0 / (1.0 + \Delta t_{\text{last}})$.

## 3. Схема данных и инфраструктура
- PostgreSQL `sport_mmr_rating`: обновление полей `mmr`, `rd`, `volatility`, `streak`, `peak_mmr`.
- ClickHouse `igaming_dds.sport_mmr_rating`: партиционированное хранение для быстрых Point-in-Time запросов.
- Хранение дневной истории в ClickHouse `table_tennis_match_fact` или агрегация на лету через оконные функции `ROW_NUMBER() OVER (PARTITION BY player_id, toDate(match_timestamp) ORDER BY match_timestamp)`.
