-- Migration: Add Bench aggregation and Micro-Matchup synthesis to v_match_analyst_dossier & fn_format_match_analyst_prompt

CREATE OR REPLACE VIEW v_match_analyst_dossier AS
 WITH team1_history AS (
         SELECT tmh.team_name,
            jsonb_agg(jsonb_build_object('match_date', tmh.match_date, 'opponent', tmh.opponent_name, 'is_home', tmh.is_home, 'result', tmh.match_result, 'team_score', tmh.team_score, 'opponent_score', tmh.opponent_score, 'clean_sheet', tmh.clean_sheet, 'failed_to_score', tmh.failed_to_score, 'yellow_cards', tmh.yellow_cards, 'red_cards', tmh.red_cards) ORDER BY tmh.match_date DESC) AS recent_matches
           FROM ( SELECT team_match_history.team_name,
                    team_match_history.opponent_name,
                    team_match_history.is_home,
                    team_match_history.match_result,
                    team_match_history.team_score,
                    team_match_history.opponent_score,
                    team_match_history.clean_sheet,
                    team_match_history.failed_to_score,
                    team_match_history.yellow_cards,
                    team_match_history.red_cards,
                    team_match_history.match_date,
                    row_number() OVER (PARTITION BY team_match_history.team_name ORDER BY team_match_history.match_date DESC) AS rn
                   FROM team_match_history) tmh
          WHERE tmh.rn <= 5
          GROUP BY tmh.team_name
        ), team2_history AS (
         SELECT tmh.team_name,
            jsonb_agg(jsonb_build_object('match_date', tmh.match_date, 'opponent', tmh.opponent_name, 'is_home', tmh.is_home, 'result', tmh.match_result, 'team_score', tmh.team_score, 'opponent_score', tmh.opponent_score, 'clean_sheet', tmh.clean_sheet, 'failed_to_score', tmh.failed_to_score, 'yellow_cards', tmh.yellow_cards, 'red_cards', tmh.red_cards) ORDER BY tmh.match_date DESC) AS recent_matches
           FROM ( SELECT team_match_history.team_name,
                    team_match_history.opponent_name,
                    team_match_history.is_home,
                    team_match_history.match_result,
                    team_match_history.team_score,
                    team_match_history.opponent_score,
                    team_match_history.clean_sheet,
                    team_match_history.failed_to_score,
                    team_match_history.yellow_cards,
                    team_match_history.red_cards,
                    team_match_history.match_date,
                    row_number() OVER (PARTITION BY team_match_history.team_name ORDER BY team_match_history.match_date DESC) AS rn
                   FROM team_match_history) tmh
          WHERE tmh.rn <= 5
          GROUP BY tmh.team_name
        ), lineups_agg AS (
         SELECT mlp.match_id,
            mlp.team_id,
            jsonb_agg(jsonb_build_object('player_name', mlp.player_name, 'position', mlp."position", 'jersey_number', mlp.jersey_number, 'season_rating', mlp.season_rating, 'is_captain', mlp.is_captain) 
                      ORDER BY CASE 
                          WHEN mlp."position" = 'GK' THEN 1
                          WHEN mlp."position" IN ('CB', 'LB', 'RB', 'DF') THEN 2
                          WHEN mlp."position" IN ('CDM', 'CM', 'CAM', 'LM', 'RM', 'MF') THEN 3
                          WHEN mlp."position" IN ('LW', 'RW', 'ST', 'CF', 'FW') THEN 4
                          ELSE 5
                      END, mlp.player_name) FILTER (WHERE mlp.is_starter = true) AS starters,
            jsonb_agg(jsonb_build_object('player_name', mlp.player_name, 'position', mlp."position", 'jersey_number', mlp.jersey_number, 'season_rating', mlp.season_rating, 'is_captain', mlp.is_captain) 
                      ORDER BY mlp.season_rating DESC NULLS LAST, mlp.player_name) FILTER (WHERE mlp.is_starter = false AND mlp.is_missing = false) AS bench,
            jsonb_agg(jsonb_build_object('player_name', mlp.player_name, 'position', mlp."position", 'missing_reason', mlp.missing_reason, 'missing_type', mlp.missing_type) 
                      ORDER BY mlp.player_name) FILTER (WHERE mlp.is_missing = true) AS missing_players
           FROM match_lineup_player mlp
          GROUP BY mlp.match_id, mlp.team_id
        ), odds_consensus AS (
         SELECT o.match_id,
            count(DISTINCT o.bet_source_id) AS bookmakers_count,
            round(max(
                CASE
                    WHEN ot.code::text = 'WIN1'::text THEN o.value
                    ELSE NULL::double precision
                END)::numeric, 3) AS best_win1,
            round(avg(
                CASE
                    WHEN ot.code::text = 'WIN1'::text THEN o.value
                    ELSE NULL::double precision
                END)::numeric, 3) AS avg_win1,
            round(max(
                CASE
                    WHEN ot.code::text = 'DRAW'::text THEN o.value
                    ELSE NULL::double precision
                END)::numeric, 3) AS best_draw,
            round(avg(
                CASE
                    WHEN ot.code::text = 'DRAW'::text THEN o.value
                    ELSE NULL::double precision
                END)::numeric, 3) AS avg_draw,
            round(max(
                CASE
                    WHEN ot.code::text = 'WIN2'::text THEN o.value
                    ELSE NULL::double precision
                END)::numeric, 3) AS best_win2,
            round(avg(
                CASE
                    WHEN ot.code::text = 'WIN2'::text THEN o.value
                    ELSE NULL::double precision
                END)::numeric, 3) AS avg_win2,
            round(max(
                CASE
                    WHEN ot.code::text = 'TOTAL_OVER'::text AND o.param = 2.5::double precision THEN o.value
                    ELSE NULL::double precision
                END)::numeric, 3) AS best_to25,
            round(avg(
                CASE
                    WHEN ot.code::text = 'TOTAL_OVER'::text AND o.param = 2.5::double precision THEN o.value
                    ELSE NULL::double precision
                END)::numeric, 3) AS avg_to25,
            round(max(
                CASE
                    WHEN ot.code::text = 'TOTAL_UNDER'::text AND o.param = 2.5::double precision THEN o.value
                    ELSE NULL::double precision
                END)::numeric, 3) AS best_tu25,
            round(avg(
                CASE
                    WHEN ot.code::text = 'TOTAL_UNDER'::text AND o.param = 2.5::double precision THEN o.value
                    ELSE NULL::double precision
                END)::numeric, 3) AS avg_tu25
           FROM odds_actual o
             JOIN odds_type ot ON o.odds_type_id = ot.id
          GROUP BY o.match_id
        )
 SELECT m.id AS match_id,
    m.sport_name,
    COALESCE(l.name, m.sport_name, 'Tournament'::character varying) AS league_name,
    m.start_time,
    to_char((to_timestamp((m.start_time::numeric / 1000.0)::double precision) AT TIME ZONE 'UTC'::text), 'YYYY-MM-DD"T"HH24:MI:SS"Z"'::text) AS start_time_utc,
    m.is_live,
    m.is_lineup_confirmed,
    m.team1_id,
    COALESCE(t1.default_display_name, 'Team 1'::character varying) AS team1_name,
    t1.recent_form AS team1_form,
    COALESCE(NULLIF(tdp1.dna_tags, ''), tdp1_en.dna_tags) AS team1_dna_tags,
    m.missing_count_team1,
    m.rotation_count_team1,
    m.avg_rating_team1,
    COALESCE(l1.starters, '[]'::jsonb) AS team1_starters,
    COALESCE(l1.missing_players, '[]'::jsonb) AS team1_missing,
    COALESCE(NULLIF(th1.recent_matches, '[]'::jsonb), th1_en.recent_matches, '[]'::jsonb) AS team1_recent_matches,
    m.team2_id,
    COALESCE(t2.default_display_name, 'Team 2'::character varying) AS team2_name,
    t2.recent_form AS team2_form,
    COALESCE(NULLIF(tdp2.dna_tags, ''), tdp2_en.dna_tags) AS team2_dna_tags,
    m.missing_count_team2,
    m.rotation_count_team2,
    m.avg_rating_team2,
    COALESCE(l2.starters, '[]'::jsonb) AS team2_starters,
    COALESCE(l2.missing_players, '[]'::jsonb) AS team2_missing,
    COALESCE(NULLIF(th2.recent_matches, '[]'::jsonb), th2_en.recent_matches, '[]'::jsonb) AS team2_recent_matches,
    COALESCE(oc.bookmakers_count, 0::bigint) AS bookmakers_count,
    oc.best_win1,
    oc.avg_win1,
    oc.best_draw,
    oc.avg_draw,
    oc.best_win2,
    oc.avg_win2,
    oc.best_to25,
    oc.avg_to25,
    oc.best_tu25,
    oc.avg_tu25,
    m.storyline_json,
    m.h2h_json,
    m.closing_narrative,
    m.motivation_desc,
    COALESCE(l1.bench, '[]'::jsonb) AS team1_bench,
    COALESCE(l2.bench, '[]'::jsonb) AS team2_bench
   FROM match_record m
     LEFT JOIN team t1 ON m.team1_id = t1.id
     LEFT JOIN team t2 ON m.team2_id = t2.id
     LEFT JOIN league l ON m.league_id = l.id
     LEFT JOIN team_dna_profile tdp1 ON tdp1.team_name::text = t1.default_display_name::text
     LEFT JOIN team_dna_profile tdp1_en ON t1.name_english IS NOT NULL AND tdp1_en.team_name::text = t1.name_english::text
     LEFT JOIN team_dna_profile tdp2 ON tdp2.team_name::text = t2.default_display_name::text
     LEFT JOIN team_dna_profile tdp2_en ON t2.name_english IS NOT NULL AND tdp2_en.team_name::text = t2.name_english::text
     LEFT JOIN team1_history th1 ON th1.team_name::text = t1.default_display_name::text
     LEFT JOIN team1_history th1_en ON t1.name_english IS NOT NULL AND th1_en.team_name::text = t1.name_english::text
     LEFT JOIN team2_history th2 ON th2.team_name::text = t2.default_display_name::text
     LEFT JOIN team2_history th2_en ON t2.name_english IS NOT NULL AND th2_en.team_name::text = t2.name_english::text
     LEFT JOIN lineups_agg l1 ON l1.match_id = m.id AND l1.team_id = m.team1_id
     LEFT JOIN lineups_agg l2 ON l2.match_id = m.id AND l2.team_id = m.team2_id
     LEFT JOIN odds_consensus oc ON oc.match_id = m.id;

-- Function: Enhanced fn_format_match_analyst_prompt with Player Archetypes, Bench Jokers & Micro-Matchups
CREATE OR REPLACE FUNCTION fn_format_match_analyst_prompt(p_match_id bigint)
RETURNS text AS $$
DECLARE
    rec RECORD;
    v_prompt text;
    v_starters1 text := '';
    v_bench1 text := '';
    v_bench_jokers1 text := '';
    v_missing1 text := '';
    v_starters2 text := '';
    v_bench2 text := '';
    v_bench_jokers2 text := '';
    v_missing2 text := '';
    v_recent1 text := '';
    v_recent2 text := '';
    item jsonb;
    p_name text;
    p_pos text;
    p_rating numeric;
    p_tag text;
    is_atk boolean;
BEGIN
    SELECT * INTO rec FROM v_match_analyst_dossier WHERE match_id = p_match_id;
    IF NOT FOUND THEN
        RETURN 'MATCH_NOT_FOUND: ' || p_match_id;
    END IF;

    -- Format starters 1 with Player Tactical Archetypes
    FOR item IN SELECT * FROM jsonb_array_elements(rec.team1_starters) LOOP
        p_name := item->>'player_name';
        p_pos := COALESCE(item->>'position', '?');
        p_rating := (item->>'season_rating')::numeric;
        
        -- Archetype classification
        p_tag := '';
        IF p_pos IN ('ST', 'CF', 'LW', 'RW', 'FW') THEN
            IF p_rating >= 7.3 THEN p_tag := ' [🔥 Опасный форвард / Высокий xG threat]';
            ELSIF p_rating >= 6.8 THEN p_tag := ' [Атакующий завершитель]';
            ELSE p_tag := ' [Прессинг-форвард / Низкая реализация]';
            END IF;
        ELSIF p_pos IN ('CAM', 'CM', 'LM', 'RM', 'MF') THEN
            IF p_rating >= 7.2 THEN p_tag := ' [Созидатель темпа / Ключевой пас]';
            ELSE p_tag := ' [Связующий полузащитник]';
            END IF;
        ELSIF p_pos = 'CDM' THEN
            p_tag := ' [Волнорез / Разрушитель атак]';
        ELSIF p_pos IN ('CB', 'DF') THEN
            IF p_rating >= 7.3 THEN p_tag := ' [Оборонительный оплот / Верховые дуэли]';
            ELSE p_tag := ' [Позиционный защитник]';
            END IF;
        ELSIF p_pos IN ('LB', 'RB') THEN
            p_tag := ' [Фланговый защитник]';
        ELSIF p_pos = 'GK' THEN
            IF p_rating >= 7.2 THEN p_tag := ' [Надежный шот-стоппер]';
            ELSE p_tag := ' [Вратарь линии]';
            END IF;
        END IF;

        v_starters1 := v_starters1 || '  * ' || p_name || ' (' || p_pos || ', Рейт: ' || COALESCE(p_rating::text, 'N/A') || ')' || p_tag || E'\n';
    END LOOP;
    IF v_starters1 = '' THEN v_starters1 := '  * Составы еще не опубликованы (ожидаются за 40-50 мин до матча)' || E'\n'; END IF;

    -- Format bench 1 & identify Bench Jokers / Chaos Agents
    FOR item IN SELECT * FROM jsonb_array_elements(rec.team1_bench) LOOP
        p_name := item->>'player_name';
        p_pos := COALESCE(item->>'position', '?');
        p_rating := (item->>'season_rating')::numeric;
        is_atk := p_pos IN ('ST', 'CF', 'LW', 'RW', 'FW', 'CAM');

        IF is_atk AND (p_rating >= 7.0 OR p_rating IS NULL) THEN
            v_bench_jokers1 := v_bench_jokers1 || '  ⚡ [ДЖОКЕР АТАКИ] ' || p_name || ' (' || p_pos || ', Рейт: ' || COALESCE(p_rating::text, 'N/A') || ') — способен взломать темп во 2-м тайме!' || E'\n';
        END IF;
        v_bench1 := v_bench1 || p_name || ' (' || p_pos || ', ' || COALESCE(p_rating::text, '-') || '), ';
    END LOOP;
    IF v_bench1 = '' THEN v_bench1 := 'Информация о скамейке отсутствует'; ELSE v_bench1 := rtrim(v_bench1, ', '); END IF;

    -- Format missing 1
    FOR item IN SELECT * FROM jsonb_array_elements(rec.team1_missing) LOOP
        v_missing1 := v_missing1 || (item->>'player_name') || ' (' || COALESCE(item->>'missing_reason', 'травма/дискв.') || '), ';
    END LOOP;
    IF v_missing1 = '' THEN v_missing1 := 'Критических потерь не зафиксировано'; ELSE v_missing1 := rtrim(v_missing1, ', '); END IF;

    -- Format starters 2 with Player Tactical Archetypes
    FOR item IN SELECT * FROM jsonb_array_elements(rec.team2_starters) LOOP
        p_name := item->>'player_name';
        p_pos := COALESCE(item->>'position', '?');
        p_rating := (item->>'season_rating')::numeric;
        
        p_tag := '';
        IF p_pos IN ('ST', 'CF', 'LW', 'RW', 'FW') THEN
            IF p_rating >= 7.3 THEN p_tag := ' [🔥 Опасный форвард / Высокий xG threat]';
            ELSIF p_rating >= 6.8 THEN p_tag := ' [Атакующий завершитель]';
            ELSE p_tag := ' [Прессинг-форвард / Низкая реализация]';
            END IF;
        ELSIF p_pos IN ('CAM', 'CM', 'LM', 'RM', 'MF') THEN
            IF p_rating >= 7.2 THEN p_tag := ' [Созидатель темпа / Ключевой пас]';
            ELSE p_tag := ' [Связующий полузащитник]';
            END IF;
        ELSIF p_pos = 'CDM' THEN
            p_tag := ' [Волнорез / Разрушитель атак]';
        ELSIF p_pos IN ('CB', 'DF') THEN
            IF p_rating >= 7.3 THEN p_tag := ' [Оборонительный оплот / Верховые дуэли]';
            ELSE p_tag := ' [Позиционный защитник]';
            END IF;
        ELSIF p_pos IN ('LB', 'RB') THEN
            p_tag := ' [Фланговый защитник]';
        ELSIF p_pos = 'GK' THEN
            IF p_rating >= 7.2 THEN p_tag := ' [Надежный шот-стоппер]';
            ELSE p_tag := ' [Вратарь линии]';
            END IF;
        END IF;

        v_starters2 := v_starters2 || '  * ' || p_name || ' (' || p_pos || ', Рейт: ' || COALESCE(p_rating::text, 'N/A') || ')' || p_tag || E'\n';
    END LOOP;
    IF v_starters2 = '' THEN v_starters2 := '  * Составы еще не опубликованы (ожидаются за 40-50 мин до матча)' || E'\n'; END IF;

    -- Format bench 2 & identify Bench Jokers / Chaos Agents
    FOR item IN SELECT * FROM jsonb_array_elements(rec.team2_bench) LOOP
        p_name := item->>'player_name';
        p_pos := COALESCE(item->>'position', '?');
        p_rating := (item->>'season_rating')::numeric;
        is_atk := p_pos IN ('ST', 'CF', 'LW', 'RW', 'FW', 'CAM');

        IF is_atk AND (p_rating >= 7.0 OR p_rating IS NULL) THEN
            v_bench_jokers2 := v_bench_jokers2 || '  ⚡ [ДЖОКЕР АТАКИ] ' || p_name || ' (' || p_pos || ', Рейт: ' || COALESCE(p_rating::text, 'N/A') || ') — способен взломать темп во 2-м тайме!' || E'\n';
        END IF;
        v_bench2 := v_bench2 || p_name || ' (' || p_pos || ', ' || COALESCE(p_rating::text, '-') || '), ';
    END LOOP;
    IF v_bench2 = '' THEN v_bench2 := 'Информация о скамейке отсутствует'; ELSE v_bench2 := rtrim(v_bench2, ', '); END IF;

    -- Format missing 2
    FOR item IN SELECT * FROM jsonb_array_elements(rec.team2_missing) LOOP
        v_missing2 := v_missing2 || (item->>'player_name') || ' (' || COALESCE(item->>'missing_reason', 'травма/дискв.') || '), ';
    END LOOP;
    IF v_missing2 = '' THEN v_missing2 := 'Критических потерь не зафиксировано'; ELSE v_missing2 := rtrim(v_missing2, ', '); END IF;

    -- Format recent 1
    FOR item IN SELECT * FROM jsonb_array_elements(rec.team1_recent_matches) LOOP
        v_recent1 := v_recent1 || '  * vs ' || (item->>'opponent') || ' (' || 
                     CASE WHEN (item->>'is_home')::boolean THEN 'Дома' ELSE 'В гостях' END || '): ' ||
                     (item->>'result') || ' ' || (item->>'team_score') || ':' || (item->>'opponent_score') || E'\n';
    END LOOP;
    IF v_recent1 = '' THEN v_recent1 := '  * Нет данных в локальной истории' || E'\n'; END IF;

    -- Format recent 2
    FOR item IN SELECT * FROM jsonb_array_elements(rec.team2_recent_matches) LOOP
        v_recent2 := v_recent2 || '  * vs ' || (item->>'opponent') || ' (' || 
                     CASE WHEN (item->>'is_home')::boolean THEN 'Дома' ELSE 'В гостях' END || '): ' ||
                     (item->>'result') || ' ' || (item->>'team_score') || ':' || (item->>'opponent_score') || E'\n';
    END LOOP;
    IF v_recent2 = '' THEN v_recent2 := '  * Нет данных в локальной истории' || E'\n'; END IF;

    v_prompt := '# 📋 ДОСЬЕ МАТЧА ДЛЯ AI-АНАЛИТИКА (SmartBet.guru Quantum Dossier)' || E'\n\n' ||
        '## ⚽ Матч: ' || rec.team1_name || ' (Хозяева) — ' || rec.team2_name || ' (Гости)' || E'\n' ||
        '- **ID матча**: ' || rec.match_id || E'\n' ||
        '- **Турнир**: ' || rec.league_name || ' (' || rec.sport_name || ')' || E'\n' ||
        '- **Время начала (UTC)**: ' || rec.start_time_utc || E'\n' ||
        '- **Статус составов**: ' || CASE WHEN rec.is_lineup_confirmed THEN 'ОФИЦИАЛЬНО ПОДТВЕРЖДЕНЫ' ELSE 'ПРЕДВАРИТЕЛЬНЫЕ' END || E'\n\n' ||
        '## 🧬 ДНК и тактические профили команд:' || E'\n' ||
        '- **' || rec.team1_name || '**: ' || COALESCE(rec.team1_dna_tags, 'Сбалансированный стиль') || 
            ' | Средний рейтинг: ' || COALESCE(round(rec.avg_rating_team1::numeric, 2)::text, 'N/A') || 
            ' | Форма: ' || COALESCE(rec.team1_form, 'N/A') || E'\n' ||
        '- **' || rec.team2_name || '**: ' || COALESCE(rec.team2_dna_tags, 'Сбалансированный стиль') || 
            ' | Средний рейтинг: ' || COALESCE(round(rec.avg_rating_team2::numeric, 2)::text, 'N/A') || 
            ' | Форма: ' || COALESCE(rec.team2_form, 'N/A') || E'\n\n' ||
        '## 👤 ИНДИВИДУАЛЬНЫЕ ПРОФИЛИ ИГРОКОВ (Player-Level DNA & Archetypes):' || E'\n' ||
        '### ' || rec.team1_name || ' (Стартовый состав):' || E'\n' || v_starters1 ||
        '### ' || rec.team2_name || ' (Стартовый состав):' || E'\n' || v_starters2 || E'\n' ||
        '## ⚡ ФАКТОР СКАМЕЙКИ И ДЖОКЕРЫ (Bench Volatility & Impact Jokers):' || E'\n' ||
        '### Скамейка ' || rec.team1_name || ':' || E'\n' ||
        CASE WHEN v_bench_jokers1 <> '' THEN v_bench_jokers1 ELSE '  (Выраженных джокеров-разрушителей темпа не обнаружено)' || E'\n' END ||
        '  Состав лавки: ' || v_bench1 || E'\n\n' ||
        '### Скамейка ' || rec.team2_name || ':' || E'\n' ||
        CASE WHEN v_bench_jokers2 <> '' THEN v_bench_jokers2 ELSE '  (Выраженных джокеров-разрушителей темпа не обнаружено)' || E'\n' END ||
        '  Состав лавки: ' || v_bench2 || E'\n\n' ||
        '## ⚠️ Кадровые потери и ротация:' || E'\n' ||
        '- **' || rec.team1_name || '**: ' || v_missing1 || ' (потерь: ' || rec.missing_count_team1 || ', ротаций: ' || rec.rotation_count_team1 || ')' || E'\n' ||
        '- **' || rec.team2_name || '**: ' || v_missing2 || ' (потерь: ' || rec.missing_count_team2 || ', ротаций: ' || rec.rotation_count_team2 || ')' || E'\n\n' ||
        '## 📜 Последние матчи команд:' || E'\n' ||
        '### ' || rec.team1_name || ':' || E'\n' || v_recent1 ||
        '### ' || rec.team2_name || ':' || E'\n' || v_recent2 || E'\n' ||
        '## 📝 Сюжетные линии и мотивация:' || E'\n' ||
        COALESCE(rec.closing_narrative, rec.motivation_desc, 'Обычный регулярный тур чемпионата.') || E'\n';

    RETURN v_prompt;
END;
$$ LANGUAGE plpgsql;
