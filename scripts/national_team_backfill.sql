-- =============================================================================
-- Backfill: national team detection + ISO country codes in match_cache
-- Task: #78 [team-national-detect]
-- Author: AI (plane-243ca9d1)
-- Date: 2026-10-02
--
-- Applies to: ALL igaming-source-* bookmaker databases (each has match_cache)
-- Also applies DDL: ALTER TABLE match_cache ADD COLUMN IF NOT EXISTS ...
-- =============================================================================

-- Step 1: DDL — add new columns (idempotent)
ALTER TABLE match_cache ADD COLUMN IF NOT EXISTS is_national_home BOOLEAN DEFAULT NULL;
ALTER TABLE match_cache ADD COLUMN IF NOT EXISTS is_national_away BOOLEAN DEFAULT NULL;
ALTER TABLE match_cache ADD COLUMN IF NOT EXISTS home_country_iso VARCHAR(10) DEFAULT NULL;
ALTER TABLE match_cache ADD COLUMN IF NOT EXISTS away_country_iso VARCHAR(10) DEFAULT NULL;

-- Step 2: Create ISO-lookup helper function
CREATE OR REPLACE FUNCTION detect_country_iso(team_name TEXT)
RETURNS VARCHAR(10) AS $$
DECLARE
    normalized TEXT := lower(trim(team_name));
BEGIN
    -- Special football associations (non-standard ISO)
    IF normalized IN ('england', 'english') THEN RETURN 'GB-ENG'; END IF;
    IF normalized IN ('scotland', 'scottish') THEN RETURN 'GB-SCT'; END IF;
    IF normalized IN ('wales', 'welsh') THEN RETURN 'GB-WLS'; END IF;
    IF normalized IN ('northern ireland') THEN RETURN 'GB-NIR'; END IF;

    -- Europe
    IF normalized IN ('russia', 'russian federation', 'россия', 'рф', 'russia national') THEN RETURN 'RU'; END IF;
    IF normalized IN ('germany', 'deutschland', 'германия') THEN RETURN 'DE'; END IF;
    IF normalized IN ('france', 'французская', 'франция') THEN RETURN 'FR'; END IF;
    IF normalized IN ('spain', 'españa', 'espana', 'испания') THEN RETURN 'ES'; END IF;
    IF normalized IN ('italy', 'italia', 'италия') THEN RETURN 'IT'; END IF;
    IF normalized IN ('portugal', 'португалия') THEN RETURN 'PT'; END IF;
    IF normalized IN ('netherlands', 'holland', 'нидерланды', 'голландия') THEN RETURN 'NL'; END IF;
    IF normalized IN ('belgium', 'belgique', 'бельгия') THEN RETURN 'BE'; END IF;
    IF normalized IN ('switzerland', 'suisse', 'швейцария') THEN RETURN 'CH'; END IF;
    IF normalized IN ('austria', 'австрия') THEN RETURN 'AT'; END IF;
    IF normalized IN ('sweden', 'швеция') THEN RETURN 'SE'; END IF;
    IF normalized IN ('norway', 'норвегия') THEN RETURN 'NO'; END IF;
    IF normalized IN ('denmark', 'дания') THEN RETURN 'DK'; END IF;
    IF normalized IN ('finland', 'финляндия') THEN RETURN 'FI'; END IF;
    IF normalized IN ('poland', 'польша') THEN RETURN 'PL'; END IF;
    IF normalized IN ('czech republic', 'czechia', 'чехия', 'czech') THEN RETURN 'CZ'; END IF;
    IF normalized IN ('slovakia', 'словакия') THEN RETURN 'SK'; END IF;
    IF normalized IN ('hungary', 'венгрия') THEN RETURN 'HU'; END IF;
    IF normalized IN ('romania', 'румыния') THEN RETURN 'RO'; END IF;
    IF normalized IN ('bulgaria', 'болгария') THEN RETURN 'BG'; END IF;
    IF normalized IN ('serbia', 'сербия') THEN RETURN 'RS'; END IF;
    IF normalized IN ('croatia', 'hrvatska', 'хорватия') THEN RETURN 'HR'; END IF;
    IF normalized IN ('slovenia', 'словения') THEN RETURN 'SI'; END IF;
    IF normalized IN ('ukraine', 'украина') THEN RETURN 'UA'; END IF;
    IF normalized IN ('turkey', 'türkiye', 'турция') THEN RETURN 'TR'; END IF;
    IF normalized IN ('greece', 'греция') THEN RETURN 'GR'; END IF;
    IF normalized IN ('albania', 'албания') THEN RETURN 'AL'; END IF;
    IF normalized IN ('north macedonia', 'macedonia', 'македония') THEN RETURN 'MK'; END IF;
    IF normalized IN ('bosnia', 'bosnia and herzegovina', 'босния', 'босния и герцеговина') THEN RETURN 'BA'; END IF;
    IF normalized IN ('montenegro', 'черногория') THEN RETURN 'ME'; END IF;
    IF normalized IN ('moldova', 'молдова') THEN RETURN 'MD'; END IF;
    IF normalized IN ('belarus', 'белоруссия', 'беларусь') THEN RETURN 'BY'; END IF;
    IF normalized IN ('latvia', 'латвия') THEN RETURN 'LV'; END IF;
    IF normalized IN ('lithuania', 'литва') THEN RETURN 'LT'; END IF;
    IF normalized IN ('estonia', 'эстония') THEN RETURN 'EE'; END IF;
    IF normalized IN ('iceland', 'исландия') THEN RETURN 'IS'; END IF;
    IF normalized IN ('ireland', 'republic of ireland', 'ирландия') THEN RETURN 'IE'; END IF;
    IF normalized IN ('luxembourg', 'люксембург') THEN RETURN 'LU'; END IF;
    IF normalized IN ('cyprus', 'кипр') THEN RETURN 'CY'; END IF;
    IF normalized IN ('israel', 'израиль') THEN RETURN 'IL'; END IF;
    IF normalized IN ('kazakhstan', 'казахстан') THEN RETURN 'KZ'; END IF;
    IF normalized IN ('azerbaijan', 'азербайджан') THEN RETURN 'AZ'; END IF;
    IF normalized IN ('armenia', 'армения') THEN RETURN 'AM'; END IF;
    IF normalized IN ('georgia', 'грузия') THEN RETURN 'GE'; END IF;
    IF normalized IN ('uzbekistan', 'узбекистан') THEN RETURN 'UZ'; END IF;
    IF normalized IN ('kyrgyzstan', 'кыргызстан', 'киргизия') THEN RETURN 'KG'; END IF;
    IF normalized IN ('tajikistan', 'таджикистан') THEN RETURN 'TJ'; END IF;
    IF normalized IN ('turkmenistan', 'туркменистан') THEN RETURN 'TM'; END IF;

    -- Americas
    IF normalized IN ('brazil', 'brasil', 'бразилия') THEN RETURN 'BR'; END IF;
    IF normalized IN ('argentina', 'аргентина') THEN RETURN 'AR'; END IF;
    IF normalized IN ('colombia', 'колумбия') THEN RETURN 'CO'; END IF;
    IF normalized IN ('chile', 'чили') THEN RETURN 'CL'; END IF;
    IF normalized IN ('uruguay', 'уругвай') THEN RETURN 'UY'; END IF;
    IF normalized IN ('peru', 'перу') THEN RETURN 'PE'; END IF;
    IF normalized IN ('venezuela', 'венесуэла') THEN RETURN 'VE'; END IF;
    IF normalized IN ('ecuador', 'эквадор') THEN RETURN 'EC'; END IF;
    IF normalized IN ('bolivia', 'боливия') THEN RETURN 'BO'; END IF;
    IF normalized IN ('paraguay', 'парагвай') THEN RETURN 'PY'; END IF;
    IF normalized IN ('mexico', 'méxico', 'мексика') THEN RETURN 'MX'; END IF;
    IF normalized IN ('usa', 'united states', 'us', 'сша', 'америка') THEN RETURN 'US'; END IF;
    IF normalized IN ('canada', 'канада') THEN RETURN 'CA'; END IF;
    IF normalized IN ('costa rica', 'коста-рика', 'коста рика') THEN RETURN 'CR'; END IF;
    IF normalized IN ('jamaica', 'ямайка') THEN RETURN 'JM'; END IF;
    IF normalized IN ('trinidad and tobago', 'trinidad', 'тринидад') THEN RETURN 'TT'; END IF;
    IF normalized IN ('haiti', 'гаити') THEN RETURN 'HT'; END IF;
    IF normalized IN ('cuba', 'куба') THEN RETURN 'CU'; END IF;
    IF normalized IN ('panama', 'панама') THEN RETURN 'PA'; END IF;
    IF normalized IN ('honduras', 'гондурас') THEN RETURN 'HN'; END IF;
    IF normalized IN ('el salvador', 'сальвадор') THEN RETURN 'SV'; END IF;
    IF normalized IN ('guatemala', 'гватемала') THEN RETURN 'GT'; END IF;
    IF normalized IN ('nicaragua', 'никарагуа') THEN RETURN 'NI'; END IF;

    -- Asia
    IF normalized IN ('japan', 'япония') THEN RETURN 'JP'; END IF;
    IF normalized IN ('china', 'china pr', 'кнр', 'китай') THEN RETURN 'CN'; END IF;
    IF normalized IN ('south korea', 'korea republic', 'korea', 'южная корея', 'корея') THEN RETURN 'KR'; END IF;
    IF normalized IN ('north korea', 'dpr korea', 'северная корея') THEN RETURN 'KP'; END IF;
    IF normalized IN ('iran', 'иран') THEN RETURN 'IR'; END IF;
    IF normalized IN ('saudi arabia', 'саудовская аравия') THEN RETURN 'SA'; END IF;
    IF normalized IN ('australia', 'австралия') THEN RETURN 'AU'; END IF;
    IF normalized IN ('india', 'индия') THEN RETURN 'IN'; END IF;
    IF normalized IN ('thailand', 'таиланд') THEN RETURN 'TH'; END IF;
    IF normalized IN ('vietnam', 'вьетнам') THEN RETURN 'VN'; END IF;
    IF normalized IN ('indonesia', 'индонезия') THEN RETURN 'ID'; END IF;
    IF normalized IN ('philippines', 'филиппины') THEN RETURN 'PH'; END IF;
    IF normalized IN ('malaysia', 'малайзия') THEN RETURN 'MY'; END IF;
    IF normalized IN ('singapore', 'сингапур') THEN RETURN 'SG'; END IF;
    IF normalized IN ('myanmar', 'мьянма') THEN RETURN 'MM'; END IF;
    IF normalized IN ('cambodia', 'камбоджа') THEN RETURN 'KH'; END IF;
    IF normalized IN ('iraq', 'ирак') THEN RETURN 'IQ'; END IF;
    IF normalized IN ('jordan', 'иордания') THEN RETURN 'JO'; END IF;
    IF normalized IN ('uae', 'united arab emirates', 'оаэ') THEN RETURN 'AE'; END IF;
    IF normalized IN ('qatar', 'катар') THEN RETURN 'QA'; END IF;
    IF normalized IN ('kuwait', 'кувейт') THEN RETURN 'KW'; END IF;
    IF normalized IN ('bahrain', 'бахрейн') THEN RETURN 'BH'; END IF;
    IF normalized IN ('oman', 'оман') THEN RETURN 'OM'; END IF;
    IF normalized IN ('pakistan', 'пакистан') THEN RETURN 'PK'; END IF;
    IF normalized IN ('bangladesh', 'бангладеш') THEN RETURN 'BD'; END IF;
    IF normalized IN ('sri lanka', 'шри-ланка') THEN RETURN 'LK'; END IF;
    IF normalized IN ('nepal', 'непал') THEN RETURN 'NP'; END IF;
    IF normalized IN ('mongolia', 'монголия') THEN RETURN 'MN'; END IF;
    IF normalized IN ('new zealand', 'новая зеландия') THEN RETURN 'NZ'; END IF;

    -- Africa
    IF normalized IN ('nigeria', 'нигерия') THEN RETURN 'NG'; END IF;
    IF normalized IN ('ghana', 'гана') THEN RETURN 'GH'; END IF;
    IF normalized IN ('senegal', 'сенегал') THEN RETURN 'SN'; END IF;
    IF normalized IN ('ivory coast', 'cote d''ivoire', 'côte d''ivoire', 'берег слоновой кости') THEN RETURN 'CI'; END IF;
    IF normalized IN ('cameroon', 'камерун') THEN RETURN 'CM'; END IF;
    IF normalized IN ('egypt', 'египет') THEN RETURN 'EG'; END IF;
    IF normalized IN ('morocco', 'марокко') THEN RETURN 'MA'; END IF;
    IF normalized IN ('algeria', 'алжир') THEN RETURN 'DZ'; END IF;
    IF normalized IN ('tunisia', 'тунис') THEN RETURN 'TN'; END IF;
    IF normalized IN ('south africa', 'южно-африканская республика', 'юар') THEN RETURN 'ZA'; END IF;
    IF normalized IN ('mali', 'мали') THEN RETURN 'ML'; END IF;
    IF normalized IN ('burkina faso', 'буркина-фасо') THEN RETURN 'BF'; END IF;
    IF normalized IN ('guinea', 'гвинея') THEN RETURN 'GN'; END IF;
    IF normalized IN ('kenya', 'кения') THEN RETURN 'KE'; END IF;
    IF normalized IN ('ethiopia', 'эфиопия') THEN RETURN 'ET'; END IF;
    IF normalized IN ('tanzania', 'танзания') THEN RETURN 'TZ'; END IF;
    IF normalized IN ('uganda', 'уганда') THEN RETURN 'UG'; END IF;
    IF normalized IN ('zambia', 'замбия') THEN RETURN 'ZM'; END IF;
    IF normalized IN ('zimbabwe', 'зимбабве') THEN RETURN 'ZW'; END IF;
    IF normalized IN ('angola', 'ангола') THEN RETURN 'AO'; END IF;
    IF normalized IN ('mozambique', 'мозамбик') THEN RETURN 'MZ'; END IF;
    IF normalized IN ('libya', 'ливия') THEN RETURN 'LY'; END IF;
    IF normalized IN ('sudan', 'судан') THEN RETURN 'SD'; END IF;
    IF normalized IN ('somalia', 'сомали') THEN RETURN 'SO'; END IF;
    IF normalized IN ('namibia', 'намибия') THEN RETURN 'NA'; END IF;
    IF normalized IN ('botswana', 'ботсвана') THEN RETURN 'BW'; END IF;
    IF normalized IN ('cape verde', 'кабо-верде') THEN RETURN 'CV'; END IF;
    IF normalized IN ('mauritius', 'маврикий') THEN RETURN 'MU'; END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Step 3: Create helper function — is_national_league
CREATE OR REPLACE FUNCTION is_national_league(league_name TEXT)
RETURNS BOOLEAN AS $$
DECLARE
    normalized TEXT := lower(trim(league_name));
BEGIN
    RETURN (
        normalized LIKE '%world cup%'
        OR normalized LIKE '%nations league%'
        OR normalized LIKE '%лига наций%'
        OR normalized LIKE '%euro %' OR normalized LIKE '%euro 20%'
        OR normalized LIKE '%european championship%'
        OR normalized LIKE '%чемпионат европы%'
        OR normalized LIKE '%copa america%'
        OR normalized LIKE '%copa américa%'
        OR normalized LIKE '%кубок америки%'
        OR normalized LIKE '%african cup%'
        OR normalized LIKE '%кубок африки%'
        OR normalized LIKE '%asian cup%'
        OR normalized LIKE '%кубок азии%'
        OR normalized LIKE '%gold cup%'
        OR normalized LIKE '%confederations cup%'
        OR normalized LIKE '%olympic%'
        OR normalized LIKE '%олимпийские%'
        OR normalized LIKE '%чемпионат мира%'
        OR normalized LIKE '%отбор%чемпионат%'
        OR normalized LIKE '%qualifying%world%'
        OR normalized LIKE '%world cup qualif%'
        OR normalized LIKE '%international friendl%'
        OR normalized LIKE '%international%cup%'
        OR normalized LIKE '%continental cup%'
        OR normalized LIKE '%afcon%'
    );
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Step 4: Create helper function — is_club_team (anti-pattern check)
CREATE OR REPLACE FUNCTION is_club_team(team_name TEXT)
RETURNS BOOLEAN AS $$
DECLARE
    normalized TEXT := lower(trim(team_name));
BEGIN
    RETURN (
        normalized LIKE '% fc%'
        OR normalized LIKE 'fc %'
        OR normalized LIKE '% f.c.%'
        OR normalized LIKE '% sc %'
        OR normalized LIKE '% ac %'
        OR normalized LIKE 'ac %'
        OR normalized LIKE '% united%'
        OR normalized LIKE '% city%'
        OR normalized LIKE '% town%'
        OR normalized LIKE '% club%'
        OR normalized LIKE '% sporting%'
        OR normalized LIKE '% athletic%'
        OR normalized LIKE 'real %'
        OR normalized LIKE 'atletico %'
        OR normalized LIKE 'atlético %'
        OR normalized LIKE 'olympique %'
        OR normalized LIKE 'olympiakos%'
        OR normalized LIKE '%dynamo%'
        OR normalized LIKE '%динамо%'
        OR normalized LIKE '%spartak%'
        OR normalized LIKE '%спартак%'
        OR normalized LIKE '%cska%'
        OR normalized LIKE '%цска%'
        OR normalized LIKE '%zenit%'
        OR normalized LIKE '%зенит%'
        OR normalized LIKE '%lokomotiv%'
        OR normalized LIKE '%локомотив%'
        OR normalized LIKE '%shakhtar%'
        OR normalized LIKE '%шахтер%'
        OR normalized LIKE '%rapid%'
        OR normalized LIKE '%hajduk%'
        OR normalized LIKE '%partizan%'
        OR normalized LIKE '%anderlecht%'
        OR normalized LIKE '%ajax%'
        OR normalized LIKE '%psv%'
        OR normalized LIKE '%benfica%'
        OR normalized LIKE '%porto%'
        OR normalized LIKE '%juventus%'
        OR normalized LIKE '%milan%'
        OR normalized LIKE '%roma%'
        OR normalized LIKE '%napoli%'
        OR normalized LIKE '%lazio%'
        OR normalized LIKE '%chelsea%'
        OR normalized LIKE '%arsenal%'
        OR normalized LIKE '%liverpool%'
        OR normalized LIKE '%manchester%'
        OR normalized LIKE '%tottenham%'
        OR normalized LIKE '%celtic%'
        OR normalized LIKE '%rangers%'
        OR normalized LIKE '%barcelona%'
        OR normalized LIKE '%valencia%'
        OR normalized LIKE '%sevilla%'
        OR normalized LIKE '%villarreal%'
        OR normalized LIKE '%atletico%'
        OR normalized LIKE '%galatasaray%'
        OR normalized LIKE '%fenerbahce%'
        OR normalized LIKE '%besiktas%'
        OR normalized LIKE '%paris sg%'
        OR normalized LIKE '%psg%'
        OR normalized LIKE '%lyon%'
        OR normalized LIKE '%monaco%'
        OR normalized LIKE '%leverkusen%'
        OR normalized LIKE '%dortmund%'
        OR normalized LIKE '%frankfurt%'
        OR normalized LIKE '%hoffenheim%'
        OR normalized LIKE '%gladbach%'
    );
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Step 5: Backfill existing rows in match_cache
-- Pass 1: Detect by league name (high confidence)
UPDATE match_cache mc
SET
    is_national_home = TRUE,
    is_national_away = TRUE,
    home_country_iso = detect_country_iso(mc.team1),
    away_country_iso = detect_country_iso(mc.team2)
WHERE
    is_national_home IS NULL
    AND is_national_away IS NULL
    AND is_national_league(mc.league_name);

-- Pass 2: Detect by team name — positive match AND not a club
UPDATE match_cache mc
SET
    is_national_home = CASE WHEN NOT is_club_team(mc.team1) AND detect_country_iso(mc.team1) IS NOT NULL THEN TRUE ELSE FALSE END,
    is_national_away = CASE WHEN NOT is_club_team(mc.team2) AND detect_country_iso(mc.team2) IS NOT NULL THEN TRUE ELSE FALSE END,
    home_country_iso = CASE WHEN NOT is_club_team(mc.team1) AND detect_country_iso(mc.team1) IS NOT NULL THEN detect_country_iso(mc.team1) ELSE NULL END,
    away_country_iso = CASE WHEN NOT is_club_team(mc.team2) AND detect_country_iso(mc.team2) IS NOT NULL THEN detect_country_iso(mc.team2) ELSE NULL END
WHERE
    is_national_home IS NULL
    AND is_national_away IS NULL;

-- Pass 3: Set FALSE for all remaining NULL (unrecognized = club by default)
UPDATE match_cache
SET
    is_national_home = FALSE,
    is_national_away = FALSE
WHERE
    is_national_home IS NULL
    OR is_national_away IS NULL;

-- Step 6: Statistics report
SELECT
    COUNT(*) AS total_matches,
    COUNT(*) FILTER (WHERE is_national_home = TRUE) AS national_home_count,
    COUNT(*) FILTER (WHERE is_national_away = TRUE) AS national_away_count,
    COUNT(*) FILTER (WHERE is_national_home = TRUE AND is_national_away = TRUE) AS both_national,
    COUNT(*) FILTER (WHERE home_country_iso IS NOT NULL) AS home_with_iso,
    COUNT(*) FILTER (WHERE away_country_iso IS NOT NULL) AS away_with_iso
FROM match_cache;
