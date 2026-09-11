BEGIN;
-- Temporary table for conflict-free odds merging

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13968 
          AND p.match_id = 7440
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7440 WHERE match_id = 13968;
UPDATE odds_snapshot SET match_id = 7440 WHERE match_id = 13968;
UPDATE surebet_alert SET match_id = 7440 WHERE match_id = 13968;
UPDATE middle_alert SET match_id = 7440 WHERE match_id = 13968;
UPDATE valuebet_alert SET match_id = 7440 WHERE match_id = 13968;
DELETE FROM match_record WHERE id = 13968;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7440)),
        updated_at = NOW()
    WHERE id = 7440;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16328 
          AND p.match_id = 13944
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13944 WHERE match_id = 16328;
UPDATE odds_snapshot SET match_id = 13944 WHERE match_id = 16328;
UPDATE surebet_alert SET match_id = 13944 WHERE match_id = 16328;
UPDATE middle_alert SET match_id = 13944 WHERE match_id = 16328;
UPDATE valuebet_alert SET match_id = 13944 WHERE match_id = 16328;
DELETE FROM match_record WHERE id = 16328;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13944)),
        updated_at = NOW()
    WHERE id = 13944;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 12553 
          AND p.match_id = 1506
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 1506 WHERE match_id = 12553;
UPDATE odds_snapshot SET match_id = 1506 WHERE match_id = 12553;
UPDATE surebet_alert SET match_id = 1506 WHERE match_id = 12553;
UPDATE middle_alert SET match_id = 1506 WHERE match_id = 12553;
UPDATE valuebet_alert SET match_id = 1506 WHERE match_id = 12553;
DELETE FROM match_record WHERE id = 12553;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 1506)),
        updated_at = NOW()
    WHERE id = 1506;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13956 
          AND p.match_id = 5978
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 5978 WHERE match_id = 13956;
UPDATE odds_snapshot SET match_id = 5978 WHERE match_id = 13956;
UPDATE surebet_alert SET match_id = 5978 WHERE match_id = 13956;
UPDATE middle_alert SET match_id = 5978 WHERE match_id = 13956;
UPDATE valuebet_alert SET match_id = 5978 WHERE match_id = 13956;
DELETE FROM match_record WHERE id = 13956;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 5978)),
        updated_at = NOW()
    WHERE id = 5978;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13850 
          AND p.match_id = 8213
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8213 WHERE match_id = 13850;
UPDATE odds_snapshot SET match_id = 8213 WHERE match_id = 13850;
UPDATE surebet_alert SET match_id = 8213 WHERE match_id = 13850;
UPDATE middle_alert SET match_id = 8213 WHERE match_id = 13850;
UPDATE valuebet_alert SET match_id = 8213 WHERE match_id = 13850;
DELETE FROM match_record WHERE id = 13850;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13854 
          AND p.match_id = 8213
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8213 WHERE match_id = 13854;
UPDATE odds_snapshot SET match_id = 8213 WHERE match_id = 13854;
UPDATE surebet_alert SET match_id = 8213 WHERE match_id = 13854;
UPDATE middle_alert SET match_id = 8213 WHERE match_id = 13854;
UPDATE valuebet_alert SET match_id = 8213 WHERE match_id = 13854;
DELETE FROM match_record WHERE id = 13854;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8213)),
        updated_at = NOW()
    WHERE id = 8213;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13950 
          AND p.match_id = 13289
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13289 WHERE match_id = 13950;
UPDATE odds_snapshot SET match_id = 13289 WHERE match_id = 13950;
UPDATE surebet_alert SET match_id = 13289 WHERE match_id = 13950;
UPDATE middle_alert SET match_id = 13289 WHERE match_id = 13950;
UPDATE valuebet_alert SET match_id = 13289 WHERE match_id = 13950;
DELETE FROM match_record WHERE id = 13950;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13289)),
        updated_at = NOW()
    WHERE id = 13289;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13955 
          AND p.match_id = 13311
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13311 WHERE match_id = 13955;
UPDATE odds_snapshot SET match_id = 13311 WHERE match_id = 13955;
UPDATE surebet_alert SET match_id = 13311 WHERE match_id = 13955;
UPDATE middle_alert SET match_id = 13311 WHERE match_id = 13955;
UPDATE valuebet_alert SET match_id = 13311 WHERE match_id = 13955;
DELETE FROM match_record WHERE id = 13955;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13311)),
        updated_at = NOW()
    WHERE id = 13311;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 15834 
          AND p.match_id = 13315
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13315 WHERE match_id = 15834;
UPDATE odds_snapshot SET match_id = 13315 WHERE match_id = 15834;
UPDATE surebet_alert SET match_id = 13315 WHERE match_id = 15834;
UPDATE middle_alert SET match_id = 13315 WHERE match_id = 15834;
UPDATE valuebet_alert SET match_id = 13315 WHERE match_id = 15834;
DELETE FROM match_record WHERE id = 15834;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13962 
          AND p.match_id = 13315
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13315 WHERE match_id = 13962;
UPDATE odds_snapshot SET match_id = 13315 WHERE match_id = 13962;
UPDATE surebet_alert SET match_id = 13315 WHERE match_id = 13962;
UPDATE middle_alert SET match_id = 13315 WHERE match_id = 13962;
UPDATE valuebet_alert SET match_id = 13315 WHERE match_id = 13962;
DELETE FROM match_record WHERE id = 13962;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13315)),
        updated_at = NOW()
    WHERE id = 13315;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13900 
          AND p.match_id = 7709
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7709 WHERE match_id = 13900;
UPDATE odds_snapshot SET match_id = 7709 WHERE match_id = 13900;
UPDATE surebet_alert SET match_id = 7709 WHERE match_id = 13900;
UPDATE middle_alert SET match_id = 7709 WHERE match_id = 13900;
UPDATE valuebet_alert SET match_id = 7709 WHERE match_id = 13900;
DELETE FROM match_record WHERE id = 13900;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7709)),
        updated_at = NOW()
    WHERE id = 7709;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13897 
          AND p.match_id = 13286
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13286 WHERE match_id = 13897;
UPDATE odds_snapshot SET match_id = 13286 WHERE match_id = 13897;
UPDATE surebet_alert SET match_id = 13286 WHERE match_id = 13897;
UPDATE middle_alert SET match_id = 13286 WHERE match_id = 13897;
UPDATE valuebet_alert SET match_id = 13286 WHERE match_id = 13897;
DELETE FROM match_record WHERE id = 13897;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13286)),
        updated_at = NOW()
    WHERE id = 13286;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13899 
          AND p.match_id = 13325
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13325 WHERE match_id = 13899;
UPDATE odds_snapshot SET match_id = 13325 WHERE match_id = 13899;
UPDATE surebet_alert SET match_id = 13325 WHERE match_id = 13899;
UPDATE middle_alert SET match_id = 13325 WHERE match_id = 13899;
UPDATE valuebet_alert SET match_id = 13325 WHERE match_id = 13899;
DELETE FROM match_record WHERE id = 13899;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13325)),
        updated_at = NOW()
    WHERE id = 13325;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 1536 
          AND p.match_id = 9178
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 9178 WHERE match_id = 1536;
UPDATE odds_snapshot SET match_id = 9178 WHERE match_id = 1536;
UPDATE surebet_alert SET match_id = 9178 WHERE match_id = 1536;
UPDATE middle_alert SET match_id = 9178 WHERE match_id = 1536;
UPDATE valuebet_alert SET match_id = 9178 WHERE match_id = 1536;
DELETE FROM match_record WHERE id = 1536;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 9178)),
        updated_at = NOW()
    WHERE id = 9178;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13901 
          AND p.match_id = 7511
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7511 WHERE match_id = 13901;
UPDATE odds_snapshot SET match_id = 7511 WHERE match_id = 13901;
UPDATE surebet_alert SET match_id = 7511 WHERE match_id = 13901;
UPDATE middle_alert SET match_id = 7511 WHERE match_id = 13901;
UPDATE valuebet_alert SET match_id = 7511 WHERE match_id = 13901;
DELETE FROM match_record WHERE id = 13901;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7511)),
        updated_at = NOW()
    WHERE id = 7511;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 15952 
          AND p.match_id = 14941
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14941 WHERE match_id = 15952;
UPDATE odds_snapshot SET match_id = 14941 WHERE match_id = 15952;
UPDATE surebet_alert SET match_id = 14941 WHERE match_id = 15952;
UPDATE middle_alert SET match_id = 14941 WHERE match_id = 15952;
UPDATE valuebet_alert SET match_id = 14941 WHERE match_id = 15952;
DELETE FROM match_record WHERE id = 15952;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14941)),
        updated_at = NOW()
    WHERE id = 14941;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13902 
          AND p.match_id = 7390
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7390 WHERE match_id = 13902;
UPDATE odds_snapshot SET match_id = 7390 WHERE match_id = 13902;
UPDATE surebet_alert SET match_id = 7390 WHERE match_id = 13902;
UPDATE middle_alert SET match_id = 7390 WHERE match_id = 13902;
UPDATE valuebet_alert SET match_id = 7390 WHERE match_id = 13902;
DELETE FROM match_record WHERE id = 13902;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7390)),
        updated_at = NOW()
    WHERE id = 7390;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13904 
          AND p.match_id = 8876
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8876 WHERE match_id = 13904;
UPDATE odds_snapshot SET match_id = 8876 WHERE match_id = 13904;
UPDATE surebet_alert SET match_id = 8876 WHERE match_id = 13904;
UPDATE middle_alert SET match_id = 8876 WHERE match_id = 13904;
UPDATE valuebet_alert SET match_id = 8876 WHERE match_id = 13904;
DELETE FROM match_record WHERE id = 13904;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8876)),
        updated_at = NOW()
    WHERE id = 8876;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13905 
          AND p.match_id = 13292
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13292 WHERE match_id = 13905;
UPDATE odds_snapshot SET match_id = 13292 WHERE match_id = 13905;
UPDATE surebet_alert SET match_id = 13292 WHERE match_id = 13905;
UPDATE middle_alert SET match_id = 13292 WHERE match_id = 13905;
UPDATE valuebet_alert SET match_id = 13292 WHERE match_id = 13905;
DELETE FROM match_record WHERE id = 13905;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13292)),
        updated_at = NOW()
    WHERE id = 13292;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13908 
          AND p.match_id = 7448
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7448 WHERE match_id = 13908;
UPDATE odds_snapshot SET match_id = 7448 WHERE match_id = 13908;
UPDATE surebet_alert SET match_id = 7448 WHERE match_id = 13908;
UPDATE middle_alert SET match_id = 7448 WHERE match_id = 13908;
UPDATE valuebet_alert SET match_id = 7448 WHERE match_id = 13908;
DELETE FROM match_record WHERE id = 13908;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7448)),
        updated_at = NOW()
    WHERE id = 7448;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16112 
          AND p.match_id = 13921
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13921 WHERE match_id = 16112;
UPDATE odds_snapshot SET match_id = 13921 WHERE match_id = 16112;
UPDATE surebet_alert SET match_id = 13921 WHERE match_id = 16112;
UPDATE middle_alert SET match_id = 13921 WHERE match_id = 16112;
UPDATE valuebet_alert SET match_id = 13921 WHERE match_id = 16112;
DELETE FROM match_record WHERE id = 16112;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13921)),
        updated_at = NOW()
    WHERE id = 13921;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 12550 
          AND p.match_id = 3440
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 3440 WHERE match_id = 12550;
UPDATE odds_snapshot SET match_id = 3440 WHERE match_id = 12550;
UPDATE surebet_alert SET match_id = 3440 WHERE match_id = 12550;
UPDATE middle_alert SET match_id = 3440 WHERE match_id = 12550;
UPDATE valuebet_alert SET match_id = 3440 WHERE match_id = 12550;
DELETE FROM match_record WHERE id = 12550;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 3440)),
        updated_at = NOW()
    WHERE id = 3440;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14084 
          AND p.match_id = 11547
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 11547 WHERE match_id = 14084;
UPDATE odds_snapshot SET match_id = 11547 WHERE match_id = 14084;
UPDATE surebet_alert SET match_id = 11547 WHERE match_id = 14084;
UPDATE middle_alert SET match_id = 11547 WHERE match_id = 14084;
UPDATE valuebet_alert SET match_id = 11547 WHERE match_id = 14084;
DELETE FROM match_record WHERE id = 14084;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 11547)),
        updated_at = NOW()
    WHERE id = 11547;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 12864 
          AND p.match_id = 3220
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 3220 WHERE match_id = 12864;
UPDATE odds_snapshot SET match_id = 3220 WHERE match_id = 12864;
UPDATE surebet_alert SET match_id = 3220 WHERE match_id = 12864;
UPDATE middle_alert SET match_id = 3220 WHERE match_id = 12864;
UPDATE valuebet_alert SET match_id = 3220 WHERE match_id = 12864;
DELETE FROM match_record WHERE id = 12864;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 3220)),
        updated_at = NOW()
    WHERE id = 3220;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13555 
          AND p.match_id = 10982
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 10982 WHERE match_id = 13555;
UPDATE odds_snapshot SET match_id = 10982 WHERE match_id = 13555;
UPDATE surebet_alert SET match_id = 10982 WHERE match_id = 13555;
UPDATE middle_alert SET match_id = 10982 WHERE match_id = 13555;
UPDATE valuebet_alert SET match_id = 10982 WHERE match_id = 13555;
DELETE FROM match_record WHERE id = 13555;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 10982)),
        updated_at = NOW()
    WHERE id = 10982;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13788 
          AND p.match_id = 7053
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7053 WHERE match_id = 13788;
UPDATE odds_snapshot SET match_id = 7053 WHERE match_id = 13788;
UPDATE surebet_alert SET match_id = 7053 WHERE match_id = 13788;
UPDATE middle_alert SET match_id = 7053 WHERE match_id = 13788;
UPDATE valuebet_alert SET match_id = 7053 WHERE match_id = 13788;
DELETE FROM match_record WHERE id = 13788;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7053)),
        updated_at = NOW()
    WHERE id = 7053;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13934 
          AND p.match_id = 11020
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 11020 WHERE match_id = 13934;
UPDATE odds_snapshot SET match_id = 11020 WHERE match_id = 13934;
UPDATE surebet_alert SET match_id = 11020 WHERE match_id = 13934;
UPDATE middle_alert SET match_id = 11020 WHERE match_id = 13934;
UPDATE valuebet_alert SET match_id = 11020 WHERE match_id = 13934;
DELETE FROM match_record WHERE id = 13934;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 11020)),
        updated_at = NOW()
    WHERE id = 11020;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16327 
          AND p.match_id = 13943
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13943 WHERE match_id = 16327;
UPDATE odds_snapshot SET match_id = 13943 WHERE match_id = 16327;
UPDATE surebet_alert SET match_id = 13943 WHERE match_id = 16327;
UPDATE middle_alert SET match_id = 13943 WHERE match_id = 16327;
UPDATE valuebet_alert SET match_id = 13943 WHERE match_id = 16327;
DELETE FROM match_record WHERE id = 16327;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13943)),
        updated_at = NOW()
    WHERE id = 13943;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13982 
          AND p.match_id = 7092
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7092 WHERE match_id = 13982;
UPDATE odds_snapshot SET match_id = 7092 WHERE match_id = 13982;
UPDATE surebet_alert SET match_id = 7092 WHERE match_id = 13982;
UPDATE middle_alert SET match_id = 7092 WHERE match_id = 13982;
UPDATE valuebet_alert SET match_id = 7092 WHERE match_id = 13982;
DELETE FROM match_record WHERE id = 13982;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7092)),
        updated_at = NOW()
    WHERE id = 7092;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16330 
          AND p.match_id = 13945
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13945 WHERE match_id = 16330;
UPDATE odds_snapshot SET match_id = 13945 WHERE match_id = 16330;
UPDATE surebet_alert SET match_id = 13945 WHERE match_id = 16330;
UPDATE middle_alert SET match_id = 13945 WHERE match_id = 16330;
UPDATE valuebet_alert SET match_id = 13945 WHERE match_id = 16330;
DELETE FROM match_record WHERE id = 16330;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13945)),
        updated_at = NOW()
    WHERE id = 13945;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16101 
          AND p.match_id = 627
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 627 WHERE match_id = 16101;
UPDATE odds_snapshot SET match_id = 627 WHERE match_id = 16101;
UPDATE surebet_alert SET match_id = 627 WHERE match_id = 16101;
UPDATE middle_alert SET match_id = 627 WHERE match_id = 16101;
UPDATE valuebet_alert SET match_id = 627 WHERE match_id = 16101;
DELETE FROM match_record WHERE id = 16101;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 627)),
        updated_at = NOW()
    WHERE id = 627;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13458 
          AND p.match_id = 13129
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13129 WHERE match_id = 13458;
UPDATE odds_snapshot SET match_id = 13129 WHERE match_id = 13458;
UPDATE surebet_alert SET match_id = 13129 WHERE match_id = 13458;
UPDATE middle_alert SET match_id = 13129 WHERE match_id = 13458;
UPDATE valuebet_alert SET match_id = 13129 WHERE match_id = 13458;
DELETE FROM match_record WHERE id = 13458;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13462 
          AND p.match_id = 13129
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13129 WHERE match_id = 13462;
UPDATE odds_snapshot SET match_id = 13129 WHERE match_id = 13462;
UPDATE surebet_alert SET match_id = 13129 WHERE match_id = 13462;
UPDATE middle_alert SET match_id = 13129 WHERE match_id = 13462;
UPDATE valuebet_alert SET match_id = 13129 WHERE match_id = 13462;
DELETE FROM match_record WHERE id = 13462;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13129)),
        updated_at = NOW()
    WHERE id = 13129;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 5936 
          AND p.match_id = 13949
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13949 WHERE match_id = 5936;
UPDATE odds_snapshot SET match_id = 13949 WHERE match_id = 5936;
UPDATE surebet_alert SET match_id = 13949 WHERE match_id = 5936;
UPDATE middle_alert SET match_id = 13949 WHERE match_id = 5936;
UPDATE valuebet_alert SET match_id = 13949 WHERE match_id = 5936;
DELETE FROM match_record WHERE id = 5936;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13949)),
        updated_at = NOW()
    WHERE id = 13949;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 5934 
          AND p.match_id = 13951
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13951 WHERE match_id = 5934;
UPDATE odds_snapshot SET match_id = 13951 WHERE match_id = 5934;
UPDATE surebet_alert SET match_id = 13951 WHERE match_id = 5934;
UPDATE middle_alert SET match_id = 13951 WHERE match_id = 5934;
UPDATE valuebet_alert SET match_id = 13951 WHERE match_id = 5934;
DELETE FROM match_record WHERE id = 5934;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13951)),
        updated_at = NOW()
    WHERE id = 13951;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13954 
          AND p.match_id = 13191
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13191 WHERE match_id = 13954;
UPDATE odds_snapshot SET match_id = 13191 WHERE match_id = 13954;
UPDATE surebet_alert SET match_id = 13191 WHERE match_id = 13954;
UPDATE middle_alert SET match_id = 13191 WHERE match_id = 13954;
UPDATE valuebet_alert SET match_id = 13191 WHERE match_id = 13954;
DELETE FROM match_record WHERE id = 13954;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13191)),
        updated_at = NOW()
    WHERE id = 13191;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16142 
          AND p.match_id = 7520
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7520 WHERE match_id = 16142;
UPDATE odds_snapshot SET match_id = 7520 WHERE match_id = 16142;
UPDATE surebet_alert SET match_id = 7520 WHERE match_id = 16142;
UPDATE middle_alert SET match_id = 7520 WHERE match_id = 16142;
UPDATE valuebet_alert SET match_id = 7520 WHERE match_id = 16142;
DELETE FROM match_record WHERE id = 16142;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7520)),
        updated_at = NOW()
    WHERE id = 7520;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13958 
          AND p.match_id = 13312
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13312 WHERE match_id = 13958;
UPDATE odds_snapshot SET match_id = 13312 WHERE match_id = 13958;
UPDATE surebet_alert SET match_id = 13312 WHERE match_id = 13958;
UPDATE middle_alert SET match_id = 13312 WHERE match_id = 13958;
UPDATE valuebet_alert SET match_id = 13312 WHERE match_id = 13958;
DELETE FROM match_record WHERE id = 13958;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13312)),
        updated_at = NOW()
    WHERE id = 13312;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13740 
          AND p.match_id = 13427
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13427 WHERE match_id = 13740;
UPDATE odds_snapshot SET match_id = 13427 WHERE match_id = 13740;
UPDATE surebet_alert SET match_id = 13427 WHERE match_id = 13740;
UPDATE middle_alert SET match_id = 13427 WHERE match_id = 13740;
UPDATE valuebet_alert SET match_id = 13427 WHERE match_id = 13740;
DELETE FROM match_record WHERE id = 13740;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13427)),
        updated_at = NOW()
    WHERE id = 13427;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14578 
          AND p.match_id = 13964
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13964 WHERE match_id = 14578;
UPDATE odds_snapshot SET match_id = 13964 WHERE match_id = 14578;
UPDATE surebet_alert SET match_id = 13964 WHERE match_id = 14578;
UPDATE middle_alert SET match_id = 13964 WHERE match_id = 14578;
UPDATE valuebet_alert SET match_id = 13964 WHERE match_id = 14578;
DELETE FROM match_record WHERE id = 14578;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13964)),
        updated_at = NOW()
    WHERE id = 13964;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13720 
          AND p.match_id = 11420
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 11420 WHERE match_id = 13720;
UPDATE odds_snapshot SET match_id = 11420 WHERE match_id = 13720;
UPDATE surebet_alert SET match_id = 11420 WHERE match_id = 13720;
UPDATE middle_alert SET match_id = 11420 WHERE match_id = 13720;
UPDATE valuebet_alert SET match_id = 11420 WHERE match_id = 13720;
DELETE FROM match_record WHERE id = 13720;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13736 
          AND p.match_id = 11420
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 11420 WHERE match_id = 13736;
UPDATE odds_snapshot SET match_id = 11420 WHERE match_id = 13736;
UPDATE surebet_alert SET match_id = 11420 WHERE match_id = 13736;
UPDATE middle_alert SET match_id = 11420 WHERE match_id = 13736;
UPDATE valuebet_alert SET match_id = 11420 WHERE match_id = 13736;
DELETE FROM match_record WHERE id = 13736;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 11420)),
        updated_at = NOW()
    WHERE id = 11420;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13963 
          AND p.match_id = 13188
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13188 WHERE match_id = 13963;
UPDATE odds_snapshot SET match_id = 13188 WHERE match_id = 13963;
UPDATE surebet_alert SET match_id = 13188 WHERE match_id = 13963;
UPDATE middle_alert SET match_id = 13188 WHERE match_id = 13963;
UPDATE valuebet_alert SET match_id = 13188 WHERE match_id = 13963;
DELETE FROM match_record WHERE id = 13963;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13966 
          AND p.match_id = 13188
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13188 WHERE match_id = 13966;
UPDATE odds_snapshot SET match_id = 13188 WHERE match_id = 13966;
UPDATE surebet_alert SET match_id = 13188 WHERE match_id = 13966;
UPDATE middle_alert SET match_id = 13188 WHERE match_id = 13966;
UPDATE valuebet_alert SET match_id = 13188 WHERE match_id = 13966;
DELETE FROM match_record WHERE id = 13966;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13188)),
        updated_at = NOW()
    WHERE id = 13188;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14397 
          AND p.match_id = 7296
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7296 WHERE match_id = 14397;
UPDATE odds_snapshot SET match_id = 7296 WHERE match_id = 14397;
UPDATE surebet_alert SET match_id = 7296 WHERE match_id = 14397;
UPDATE middle_alert SET match_id = 7296 WHERE match_id = 14397;
UPDATE valuebet_alert SET match_id = 7296 WHERE match_id = 14397;
DELETE FROM match_record WHERE id = 14397;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7296)),
        updated_at = NOW()
    WHERE id = 7296;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14463 
          AND p.match_id = 9344
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 9344 WHERE match_id = 14463;
UPDATE odds_snapshot SET match_id = 9344 WHERE match_id = 14463;
UPDATE surebet_alert SET match_id = 9344 WHERE match_id = 14463;
UPDATE middle_alert SET match_id = 9344 WHERE match_id = 14463;
UPDATE valuebet_alert SET match_id = 9344 WHERE match_id = 14463;
DELETE FROM match_record WHERE id = 14463;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14469 
          AND p.match_id = 9344
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 9344 WHERE match_id = 14469;
UPDATE odds_snapshot SET match_id = 9344 WHERE match_id = 14469;
UPDATE surebet_alert SET match_id = 9344 WHERE match_id = 14469;
UPDATE middle_alert SET match_id = 9344 WHERE match_id = 14469;
UPDATE valuebet_alert SET match_id = 9344 WHERE match_id = 14469;
DELETE FROM match_record WHERE id = 14469;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 9344)),
        updated_at = NOW()
    WHERE id = 9344;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14401 
          AND p.match_id = 13255
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13255 WHERE match_id = 14401;
UPDATE odds_snapshot SET match_id = 13255 WHERE match_id = 14401;
UPDATE surebet_alert SET match_id = 13255 WHERE match_id = 14401;
UPDATE middle_alert SET match_id = 13255 WHERE match_id = 14401;
UPDATE valuebet_alert SET match_id = 13255 WHERE match_id = 14401;
DELETE FROM match_record WHERE id = 14401;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13255)),
        updated_at = NOW()
    WHERE id = 13255;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 11793 
          AND p.match_id = 9904
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 9904 WHERE match_id = 11793;
UPDATE odds_snapshot SET match_id = 9904 WHERE match_id = 11793;
UPDATE surebet_alert SET match_id = 9904 WHERE match_id = 11793;
UPDATE middle_alert SET match_id = 9904 WHERE match_id = 11793;
UPDATE valuebet_alert SET match_id = 9904 WHERE match_id = 11793;
DELETE FROM match_record WHERE id = 11793;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 9904)),
        updated_at = NOW()
    WHERE id = 9904;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13979 
          AND p.match_id = 6482
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6482 WHERE match_id = 13979;
UPDATE odds_snapshot SET match_id = 6482 WHERE match_id = 13979;
UPDATE surebet_alert SET match_id = 6482 WHERE match_id = 13979;
UPDATE middle_alert SET match_id = 6482 WHERE match_id = 13979;
UPDATE valuebet_alert SET match_id = 6482 WHERE match_id = 13979;
DELETE FROM match_record WHERE id = 13979;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6482)),
        updated_at = NOW()
    WHERE id = 6482;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 12539 
          AND p.match_id = 6461
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6461 WHERE match_id = 12539;
UPDATE odds_snapshot SET match_id = 6461 WHERE match_id = 12539;
UPDATE surebet_alert SET match_id = 6461 WHERE match_id = 12539;
UPDATE middle_alert SET match_id = 6461 WHERE match_id = 12539;
UPDATE valuebet_alert SET match_id = 6461 WHERE match_id = 12539;
DELETE FROM match_record WHERE id = 12539;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6461)),
        updated_at = NOW()
    WHERE id = 6461;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13984 
          AND p.match_id = 13301
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13301 WHERE match_id = 13984;
UPDATE odds_snapshot SET match_id = 13301 WHERE match_id = 13984;
UPDATE surebet_alert SET match_id = 13301 WHERE match_id = 13984;
UPDATE middle_alert SET match_id = 13301 WHERE match_id = 13984;
UPDATE valuebet_alert SET match_id = 13301 WHERE match_id = 13984;
DELETE FROM match_record WHERE id = 13984;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13301)),
        updated_at = NOW()
    WHERE id = 13301;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 15968 
          AND p.match_id = 15137
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 15137 WHERE match_id = 15968;
UPDATE odds_snapshot SET match_id = 15137 WHERE match_id = 15968;
UPDATE surebet_alert SET match_id = 15137 WHERE match_id = 15968;
UPDATE middle_alert SET match_id = 15137 WHERE match_id = 15968;
UPDATE valuebet_alert SET match_id = 15137 WHERE match_id = 15968;
DELETE FROM match_record WHERE id = 15968;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 15137)),
        updated_at = NOW()
    WHERE id = 15137;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13987 
          AND p.match_id = 6608
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6608 WHERE match_id = 13987;
UPDATE odds_snapshot SET match_id = 6608 WHERE match_id = 13987;
UPDATE surebet_alert SET match_id = 6608 WHERE match_id = 13987;
UPDATE middle_alert SET match_id = 6608 WHERE match_id = 13987;
UPDATE valuebet_alert SET match_id = 6608 WHERE match_id = 13987;
DELETE FROM match_record WHERE id = 13987;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6608)),
        updated_at = NOW()
    WHERE id = 6608;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14058 
          AND p.match_id = 8296
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8296 WHERE match_id = 14058;
UPDATE odds_snapshot SET match_id = 8296 WHERE match_id = 14058;
UPDATE surebet_alert SET match_id = 8296 WHERE match_id = 14058;
UPDATE middle_alert SET match_id = 8296 WHERE match_id = 14058;
UPDATE valuebet_alert SET match_id = 8296 WHERE match_id = 14058;
DELETE FROM match_record WHERE id = 14058;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14050 
          AND p.match_id = 8296
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8296 WHERE match_id = 14050;
UPDATE odds_snapshot SET match_id = 8296 WHERE match_id = 14050;
UPDATE surebet_alert SET match_id = 8296 WHERE match_id = 14050;
UPDATE middle_alert SET match_id = 8296 WHERE match_id = 14050;
UPDATE valuebet_alert SET match_id = 8296 WHERE match_id = 14050;
DELETE FROM match_record WHERE id = 14050;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8296)),
        updated_at = NOW()
    WHERE id = 8296;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14019 
          AND p.match_id = 7187
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7187 WHERE match_id = 14019;
UPDATE odds_snapshot SET match_id = 7187 WHERE match_id = 14019;
UPDATE surebet_alert SET match_id = 7187 WHERE match_id = 14019;
UPDATE middle_alert SET match_id = 7187 WHERE match_id = 14019;
UPDATE valuebet_alert SET match_id = 7187 WHERE match_id = 14019;
DELETE FROM match_record WHERE id = 14019;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14022 
          AND p.match_id = 7187
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7187 WHERE match_id = 14022;
UPDATE odds_snapshot SET match_id = 7187 WHERE match_id = 14022;
UPDATE surebet_alert SET match_id = 7187 WHERE match_id = 14022;
UPDATE middle_alert SET match_id = 7187 WHERE match_id = 14022;
UPDATE valuebet_alert SET match_id = 7187 WHERE match_id = 14022;
DELETE FROM match_record WHERE id = 14022;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7187)),
        updated_at = NOW()
    WHERE id = 7187;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14016 
          AND p.match_id = 14020
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14020 WHERE match_id = 14016;
UPDATE odds_snapshot SET match_id = 14020 WHERE match_id = 14016;
UPDATE surebet_alert SET match_id = 14020 WHERE match_id = 14016;
UPDATE middle_alert SET match_id = 14020 WHERE match_id = 14016;
UPDATE valuebet_alert SET match_id = 14020 WHERE match_id = 14016;
DELETE FROM match_record WHERE id = 14016;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 7733 
          AND p.match_id = 14020
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14020 WHERE match_id = 7733;
UPDATE odds_snapshot SET match_id = 14020 WHERE match_id = 7733;
UPDATE surebet_alert SET match_id = 14020 WHERE match_id = 7733;
UPDATE middle_alert SET match_id = 14020 WHERE match_id = 7733;
UPDATE valuebet_alert SET match_id = 14020 WHERE match_id = 7733;
DELETE FROM match_record WHERE id = 7733;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14020)),
        updated_at = NOW()
    WHERE id = 14020;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14576 
          AND p.match_id = 12000
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 12000 WHERE match_id = 14576;
UPDATE odds_snapshot SET match_id = 12000 WHERE match_id = 14576;
UPDATE surebet_alert SET match_id = 12000 WHERE match_id = 14576;
UPDATE middle_alert SET match_id = 12000 WHERE match_id = 14576;
UPDATE valuebet_alert SET match_id = 12000 WHERE match_id = 14576;
DELETE FROM match_record WHERE id = 14576;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 12000)),
        updated_at = NOW()
    WHERE id = 12000;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13992 
          AND p.match_id = 7311
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7311 WHERE match_id = 13992;
UPDATE odds_snapshot SET match_id = 7311 WHERE match_id = 13992;
UPDATE surebet_alert SET match_id = 7311 WHERE match_id = 13992;
UPDATE middle_alert SET match_id = 7311 WHERE match_id = 13992;
UPDATE valuebet_alert SET match_id = 7311 WHERE match_id = 13992;
DELETE FROM match_record WHERE id = 13992;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7311)),
        updated_at = NOW()
    WHERE id = 7311;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13791 
          AND p.match_id = 7392
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7392 WHERE match_id = 13791;
UPDATE odds_snapshot SET match_id = 7392 WHERE match_id = 13791;
UPDATE surebet_alert SET match_id = 7392 WHERE match_id = 13791;
UPDATE middle_alert SET match_id = 7392 WHERE match_id = 13791;
UPDATE valuebet_alert SET match_id = 7392 WHERE match_id = 13791;
DELETE FROM match_record WHERE id = 13791;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13803 
          AND p.match_id = 7392
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7392 WHERE match_id = 13803;
UPDATE odds_snapshot SET match_id = 7392 WHERE match_id = 13803;
UPDATE surebet_alert SET match_id = 7392 WHERE match_id = 13803;
UPDATE middle_alert SET match_id = 7392 WHERE match_id = 13803;
UPDATE valuebet_alert SET match_id = 7392 WHERE match_id = 13803;
DELETE FROM match_record WHERE id = 13803;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7392)),
        updated_at = NOW()
    WHERE id = 7392;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14001 
          AND p.match_id = 7401
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7401 WHERE match_id = 14001;
UPDATE odds_snapshot SET match_id = 7401 WHERE match_id = 14001;
UPDATE surebet_alert SET match_id = 7401 WHERE match_id = 14001;
UPDATE middle_alert SET match_id = 7401 WHERE match_id = 14001;
UPDATE valuebet_alert SET match_id = 7401 WHERE match_id = 14001;
DELETE FROM match_record WHERE id = 14001;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7401)),
        updated_at = NOW()
    WHERE id = 7401;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13742 
          AND p.match_id = 7387
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7387 WHERE match_id = 13742;
UPDATE odds_snapshot SET match_id = 7387 WHERE match_id = 13742;
UPDATE surebet_alert SET match_id = 7387 WHERE match_id = 13742;
UPDATE middle_alert SET match_id = 7387 WHERE match_id = 13742;
UPDATE valuebet_alert SET match_id = 7387 WHERE match_id = 13742;
DELETE FROM match_record WHERE id = 13742;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13781 
          AND p.match_id = 7387
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7387 WHERE match_id = 13781;
UPDATE odds_snapshot SET match_id = 7387 WHERE match_id = 13781;
UPDATE surebet_alert SET match_id = 7387 WHERE match_id = 13781;
UPDATE middle_alert SET match_id = 7387 WHERE match_id = 13781;
UPDATE valuebet_alert SET match_id = 7387 WHERE match_id = 13781;
DELETE FROM match_record WHERE id = 13781;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7387)),
        updated_at = NOW()
    WHERE id = 7387;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13460 
          AND p.match_id = 7388
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7388 WHERE match_id = 13460;
UPDATE odds_snapshot SET match_id = 7388 WHERE match_id = 13460;
UPDATE surebet_alert SET match_id = 7388 WHERE match_id = 13460;
UPDATE middle_alert SET match_id = 7388 WHERE match_id = 13460;
UPDATE valuebet_alert SET match_id = 7388 WHERE match_id = 13460;
DELETE FROM match_record WHERE id = 13460;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13481 
          AND p.match_id = 7388
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7388 WHERE match_id = 13481;
UPDATE odds_snapshot SET match_id = 7388 WHERE match_id = 13481;
UPDATE surebet_alert SET match_id = 7388 WHERE match_id = 13481;
UPDATE middle_alert SET match_id = 7388 WHERE match_id = 13481;
UPDATE valuebet_alert SET match_id = 7388 WHERE match_id = 13481;
DELETE FROM match_record WHERE id = 13481;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7388)),
        updated_at = NOW()
    WHERE id = 7388;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14075 
          AND p.match_id = 7439
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7439 WHERE match_id = 14075;
UPDATE odds_snapshot SET match_id = 7439 WHERE match_id = 14075;
UPDATE surebet_alert SET match_id = 7439 WHERE match_id = 14075;
UPDATE middle_alert SET match_id = 7439 WHERE match_id = 14075;
UPDATE valuebet_alert SET match_id = 7439 WHERE match_id = 14075;
DELETE FROM match_record WHERE id = 14075;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7439)),
        updated_at = NOW()
    WHERE id = 7439;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13558 
          AND p.match_id = 7466
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7466 WHERE match_id = 13558;
UPDATE odds_snapshot SET match_id = 7466 WHERE match_id = 13558;
UPDATE surebet_alert SET match_id = 7466 WHERE match_id = 13558;
UPDATE middle_alert SET match_id = 7466 WHERE match_id = 13558;
UPDATE valuebet_alert SET match_id = 7466 WHERE match_id = 13558;
DELETE FROM match_record WHERE id = 13558;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7466)),
        updated_at = NOW()
    WHERE id = 7466;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14067 
          AND p.match_id = 12656
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 12656 WHERE match_id = 14067;
UPDATE odds_snapshot SET match_id = 12656 WHERE match_id = 14067;
UPDATE surebet_alert SET match_id = 12656 WHERE match_id = 14067;
UPDATE middle_alert SET match_id = 12656 WHERE match_id = 14067;
UPDATE valuebet_alert SET match_id = 12656 WHERE match_id = 14067;
DELETE FROM match_record WHERE id = 14067;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 12656)),
        updated_at = NOW()
    WHERE id = 12656;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13846 
          AND p.match_id = 12647
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 12647 WHERE match_id = 13846;
UPDATE odds_snapshot SET match_id = 12647 WHERE match_id = 13846;
UPDATE surebet_alert SET match_id = 12647 WHERE match_id = 13846;
UPDATE middle_alert SET match_id = 12647 WHERE match_id = 13846;
UPDATE valuebet_alert SET match_id = 12647 WHERE match_id = 13846;
DELETE FROM match_record WHERE id = 13846;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13853 
          AND p.match_id = 12647
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 12647 WHERE match_id = 13853;
UPDATE odds_snapshot SET match_id = 12647 WHERE match_id = 13853;
UPDATE surebet_alert SET match_id = 12647 WHERE match_id = 13853;
UPDATE middle_alert SET match_id = 12647 WHERE match_id = 13853;
UPDATE valuebet_alert SET match_id = 12647 WHERE match_id = 13853;
DELETE FROM match_record WHERE id = 13853;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 12647)),
        updated_at = NOW()
    WHERE id = 12647;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14045 
          AND p.match_id = 8405
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8405 WHERE match_id = 14045;
UPDATE odds_snapshot SET match_id = 8405 WHERE match_id = 14045;
UPDATE surebet_alert SET match_id = 8405 WHERE match_id = 14045;
UPDATE middle_alert SET match_id = 8405 WHERE match_id = 14045;
UPDATE valuebet_alert SET match_id = 8405 WHERE match_id = 14045;
DELETE FROM match_record WHERE id = 14045;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14060 
          AND p.match_id = 8405
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8405 WHERE match_id = 14060;
UPDATE odds_snapshot SET match_id = 8405 WHERE match_id = 14060;
UPDATE surebet_alert SET match_id = 8405 WHERE match_id = 14060;
UPDATE middle_alert SET match_id = 8405 WHERE match_id = 14060;
UPDATE valuebet_alert SET match_id = 8405 WHERE match_id = 14060;
DELETE FROM match_record WHERE id = 14060;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8405)),
        updated_at = NOW()
    WHERE id = 8405;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13778 
          AND p.match_id = 7394
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7394 WHERE match_id = 13778;
UPDATE odds_snapshot SET match_id = 7394 WHERE match_id = 13778;
UPDATE surebet_alert SET match_id = 7394 WHERE match_id = 13778;
UPDATE middle_alert SET match_id = 7394 WHERE match_id = 13778;
UPDATE valuebet_alert SET match_id = 7394 WHERE match_id = 13778;
DELETE FROM match_record WHERE id = 13778;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13789 
          AND p.match_id = 7394
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7394 WHERE match_id = 13789;
UPDATE odds_snapshot SET match_id = 7394 WHERE match_id = 13789;
UPDATE surebet_alert SET match_id = 7394 WHERE match_id = 13789;
UPDATE middle_alert SET match_id = 7394 WHERE match_id = 13789;
UPDATE valuebet_alert SET match_id = 7394 WHERE match_id = 13789;
DELETE FROM match_record WHERE id = 13789;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7394)),
        updated_at = NOW()
    WHERE id = 7394;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13973 
          AND p.match_id = 7837
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7837 WHERE match_id = 13973;
UPDATE odds_snapshot SET match_id = 7837 WHERE match_id = 13973;
UPDATE surebet_alert SET match_id = 7837 WHERE match_id = 13973;
UPDATE middle_alert SET match_id = 7837 WHERE match_id = 13973;
UPDATE valuebet_alert SET match_id = 7837 WHERE match_id = 13973;
DELETE FROM match_record WHERE id = 13973;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13978 
          AND p.match_id = 7837
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7837 WHERE match_id = 13978;
UPDATE odds_snapshot SET match_id = 7837 WHERE match_id = 13978;
UPDATE surebet_alert SET match_id = 7837 WHERE match_id = 13978;
UPDATE middle_alert SET match_id = 7837 WHERE match_id = 13978;
UPDATE valuebet_alert SET match_id = 7837 WHERE match_id = 13978;
DELETE FROM match_record WHERE id = 13978;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7837)),
        updated_at = NOW()
    WHERE id = 7837;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13687 
          AND p.match_id = 8478
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8478 WHERE match_id = 13687;
UPDATE odds_snapshot SET match_id = 8478 WHERE match_id = 13687;
UPDATE surebet_alert SET match_id = 8478 WHERE match_id = 13687;
UPDATE middle_alert SET match_id = 8478 WHERE match_id = 13687;
UPDATE valuebet_alert SET match_id = 8478 WHERE match_id = 13687;
DELETE FROM match_record WHERE id = 13687;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13690 
          AND p.match_id = 8478
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8478 WHERE match_id = 13690;
UPDATE odds_snapshot SET match_id = 8478 WHERE match_id = 13690;
UPDATE surebet_alert SET match_id = 8478 WHERE match_id = 13690;
UPDATE middle_alert SET match_id = 8478 WHERE match_id = 13690;
UPDATE valuebet_alert SET match_id = 8478 WHERE match_id = 13690;
DELETE FROM match_record WHERE id = 13690;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8478)),
        updated_at = NOW()
    WHERE id = 8478;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13994 
          AND p.match_id = 7891
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7891 WHERE match_id = 13994;
UPDATE odds_snapshot SET match_id = 7891 WHERE match_id = 13994;
UPDATE surebet_alert SET match_id = 7891 WHERE match_id = 13994;
UPDATE middle_alert SET match_id = 7891 WHERE match_id = 13994;
UPDATE valuebet_alert SET match_id = 7891 WHERE match_id = 13994;
DELETE FROM match_record WHERE id = 13994;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7891)),
        updated_at = NOW()
    WHERE id = 7891;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16393 
          AND p.match_id = 14957
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14957 WHERE match_id = 16393;
UPDATE odds_snapshot SET match_id = 14957 WHERE match_id = 16393;
UPDATE surebet_alert SET match_id = 14957 WHERE match_id = 16393;
UPDATE middle_alert SET match_id = 14957 WHERE match_id = 16393;
UPDATE valuebet_alert SET match_id = 14957 WHERE match_id = 16393;
DELETE FROM match_record WHERE id = 16393;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14957)),
        updated_at = NOW()
    WHERE id = 14957;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14398 
          AND p.match_id = 7843
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7843 WHERE match_id = 14398;
UPDATE odds_snapshot SET match_id = 7843 WHERE match_id = 14398;
UPDATE surebet_alert SET match_id = 7843 WHERE match_id = 14398;
UPDATE middle_alert SET match_id = 7843 WHERE match_id = 14398;
UPDATE valuebet_alert SET match_id = 7843 WHERE match_id = 14398;
DELETE FROM match_record WHERE id = 14398;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7843)),
        updated_at = NOW()
    WHERE id = 7843;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13744 
          AND p.match_id = 6347
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6347 WHERE match_id = 13744;
UPDATE odds_snapshot SET match_id = 6347 WHERE match_id = 13744;
UPDATE surebet_alert SET match_id = 6347 WHERE match_id = 13744;
UPDATE middle_alert SET match_id = 6347 WHERE match_id = 13744;
UPDATE valuebet_alert SET match_id = 6347 WHERE match_id = 13744;
DELETE FROM match_record WHERE id = 13744;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13749 
          AND p.match_id = 6347
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6347 WHERE match_id = 13749;
UPDATE odds_snapshot SET match_id = 6347 WHERE match_id = 13749;
UPDATE surebet_alert SET match_id = 6347 WHERE match_id = 13749;
UPDATE middle_alert SET match_id = 6347 WHERE match_id = 13749;
UPDATE valuebet_alert SET match_id = 6347 WHERE match_id = 13749;
DELETE FROM match_record WHERE id = 13749;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6347)),
        updated_at = NOW()
    WHERE id = 6347;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13726 
          AND p.match_id = 7901
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7901 WHERE match_id = 13726;
UPDATE odds_snapshot SET match_id = 7901 WHERE match_id = 13726;
UPDATE surebet_alert SET match_id = 7901 WHERE match_id = 13726;
UPDATE middle_alert SET match_id = 7901 WHERE match_id = 13726;
UPDATE valuebet_alert SET match_id = 7901 WHERE match_id = 13726;
DELETE FROM match_record WHERE id = 13726;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7901)),
        updated_at = NOW()
    WHERE id = 7901;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13730 
          AND p.match_id = 7902
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7902 WHERE match_id = 13730;
UPDATE odds_snapshot SET match_id = 7902 WHERE match_id = 13730;
UPDATE surebet_alert SET match_id = 7902 WHERE match_id = 13730;
UPDATE middle_alert SET match_id = 7902 WHERE match_id = 13730;
UPDATE valuebet_alert SET match_id = 7902 WHERE match_id = 13730;
DELETE FROM match_record WHERE id = 13730;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7902)),
        updated_at = NOW()
    WHERE id = 7902;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14002 
          AND p.match_id = 7847
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7847 WHERE match_id = 14002;
UPDATE odds_snapshot SET match_id = 7847 WHERE match_id = 14002;
UPDATE surebet_alert SET match_id = 7847 WHERE match_id = 14002;
UPDATE middle_alert SET match_id = 7847 WHERE match_id = 14002;
UPDATE valuebet_alert SET match_id = 7847 WHERE match_id = 14002;
DELETE FROM match_record WHERE id = 14002;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13999 
          AND p.match_id = 7847
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7847 WHERE match_id = 13999;
UPDATE odds_snapshot SET match_id = 7847 WHERE match_id = 13999;
UPDATE surebet_alert SET match_id = 7847 WHERE match_id = 13999;
UPDATE middle_alert SET match_id = 7847 WHERE match_id = 13999;
UPDATE valuebet_alert SET match_id = 7847 WHERE match_id = 13999;
DELETE FROM match_record WHERE id = 13999;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7847)),
        updated_at = NOW()
    WHERE id = 7847;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13456 
          AND p.match_id = 7990
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7990 WHERE match_id = 13456;
UPDATE odds_snapshot SET match_id = 7990 WHERE match_id = 13456;
UPDATE surebet_alert SET match_id = 7990 WHERE match_id = 13456;
UPDATE middle_alert SET match_id = 7990 WHERE match_id = 13456;
UPDATE valuebet_alert SET match_id = 7990 WHERE match_id = 13456;
DELETE FROM match_record WHERE id = 13456;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13461 
          AND p.match_id = 7990
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7990 WHERE match_id = 13461;
UPDATE odds_snapshot SET match_id = 7990 WHERE match_id = 13461;
UPDATE surebet_alert SET match_id = 7990 WHERE match_id = 13461;
UPDATE middle_alert SET match_id = 7990 WHERE match_id = 13461;
UPDATE valuebet_alert SET match_id = 7990 WHERE match_id = 13461;
DELETE FROM match_record WHERE id = 13461;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7990)),
        updated_at = NOW()
    WHERE id = 7990;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13620 
          AND p.match_id = 6441
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6441 WHERE match_id = 13620;
UPDATE odds_snapshot SET match_id = 6441 WHERE match_id = 13620;
UPDATE surebet_alert SET match_id = 6441 WHERE match_id = 13620;
UPDATE middle_alert SET match_id = 6441 WHERE match_id = 13620;
UPDATE valuebet_alert SET match_id = 6441 WHERE match_id = 13620;
DELETE FROM match_record WHERE id = 13620;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6441)),
        updated_at = NOW()
    WHERE id = 6441;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14069 
          AND p.match_id = 13359
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13359 WHERE match_id = 14069;
UPDATE odds_snapshot SET match_id = 13359 WHERE match_id = 14069;
UPDATE surebet_alert SET match_id = 13359 WHERE match_id = 14069;
UPDATE middle_alert SET match_id = 13359 WHERE match_id = 14069;
UPDATE valuebet_alert SET match_id = 13359 WHERE match_id = 14069;
DELETE FROM match_record WHERE id = 14069;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13359)),
        updated_at = NOW()
    WHERE id = 13359;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14070 
          AND p.match_id = 13375
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13375 WHERE match_id = 14070;
UPDATE odds_snapshot SET match_id = 13375 WHERE match_id = 14070;
UPDATE surebet_alert SET match_id = 13375 WHERE match_id = 14070;
UPDATE middle_alert SET match_id = 13375 WHERE match_id = 14070;
UPDATE valuebet_alert SET match_id = 13375 WHERE match_id = 14070;
DELETE FROM match_record WHERE id = 14070;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13375)),
        updated_at = NOW()
    WHERE id = 13375;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14068 
          AND p.match_id = 8130
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8130 WHERE match_id = 14068;
UPDATE odds_snapshot SET match_id = 8130 WHERE match_id = 14068;
UPDATE surebet_alert SET match_id = 8130 WHERE match_id = 14068;
UPDATE middle_alert SET match_id = 8130 WHERE match_id = 14068;
UPDATE valuebet_alert SET match_id = 8130 WHERE match_id = 14068;
DELETE FROM match_record WHERE id = 14068;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8130)),
        updated_at = NOW()
    WHERE id = 8130;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13728 
          AND p.match_id = 8139
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8139 WHERE match_id = 13728;
UPDATE odds_snapshot SET match_id = 8139 WHERE match_id = 13728;
UPDATE surebet_alert SET match_id = 8139 WHERE match_id = 13728;
UPDATE middle_alert SET match_id = 8139 WHERE match_id = 13728;
UPDATE valuebet_alert SET match_id = 8139 WHERE match_id = 13728;
DELETE FROM match_record WHERE id = 13728;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8139)),
        updated_at = NOW()
    WHERE id = 8139;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16748 
          AND p.match_id = 13302
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13302 WHERE match_id = 16748;
UPDATE odds_snapshot SET match_id = 13302 WHERE match_id = 16748;
UPDATE surebet_alert SET match_id = 13302 WHERE match_id = 16748;
UPDATE middle_alert SET match_id = 13302 WHERE match_id = 16748;
UPDATE valuebet_alert SET match_id = 13302 WHERE match_id = 16748;
DELETE FROM match_record WHERE id = 16748;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13302)),
        updated_at = NOW()
    WHERE id = 13302;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14073 
          AND p.match_id = 13388
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13388 WHERE match_id = 14073;
UPDATE odds_snapshot SET match_id = 13388 WHERE match_id = 14073;
UPDATE surebet_alert SET match_id = 13388 WHERE match_id = 14073;
UPDATE middle_alert SET match_id = 13388 WHERE match_id = 14073;
UPDATE valuebet_alert SET match_id = 13388 WHERE match_id = 14073;
DELETE FROM match_record WHERE id = 14073;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13388)),
        updated_at = NOW()
    WHERE id = 13388;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14076 
          AND p.match_id = 13372
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13372 WHERE match_id = 14076;
UPDATE odds_snapshot SET match_id = 13372 WHERE match_id = 14076;
UPDATE surebet_alert SET match_id = 13372 WHERE match_id = 14076;
UPDATE middle_alert SET match_id = 13372 WHERE match_id = 14076;
UPDATE valuebet_alert SET match_id = 13372 WHERE match_id = 14076;
DELETE FROM match_record WHERE id = 14076;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13372)),
        updated_at = NOW()
    WHERE id = 13372;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13569 
          AND p.match_id = 7185
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7185 WHERE match_id = 13569;
UPDATE odds_snapshot SET match_id = 7185 WHERE match_id = 13569;
UPDATE surebet_alert SET match_id = 7185 WHERE match_id = 13569;
UPDATE middle_alert SET match_id = 7185 WHERE match_id = 13569;
UPDATE valuebet_alert SET match_id = 7185 WHERE match_id = 13569;
DELETE FROM match_record WHERE id = 13569;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13597 
          AND p.match_id = 7185
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7185 WHERE match_id = 13597;
UPDATE odds_snapshot SET match_id = 7185 WHERE match_id = 13597;
UPDATE surebet_alert SET match_id = 7185 WHERE match_id = 13597;
UPDATE middle_alert SET match_id = 7185 WHERE match_id = 13597;
UPDATE valuebet_alert SET match_id = 7185 WHERE match_id = 13597;
DELETE FROM match_record WHERE id = 13597;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7185)),
        updated_at = NOW()
    WHERE id = 7185;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13745 
          AND p.match_id = 7049
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7049 WHERE match_id = 13745;
UPDATE odds_snapshot SET match_id = 7049 WHERE match_id = 13745;
UPDATE surebet_alert SET match_id = 7049 WHERE match_id = 13745;
UPDATE middle_alert SET match_id = 7049 WHERE match_id = 13745;
UPDATE valuebet_alert SET match_id = 7049 WHERE match_id = 13745;
DELETE FROM match_record WHERE id = 13745;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13752 
          AND p.match_id = 7049
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7049 WHERE match_id = 13752;
UPDATE odds_snapshot SET match_id = 7049 WHERE match_id = 13752;
UPDATE surebet_alert SET match_id = 7049 WHERE match_id = 13752;
UPDATE middle_alert SET match_id = 7049 WHERE match_id = 13752;
UPDATE valuebet_alert SET match_id = 7049 WHERE match_id = 13752;
DELETE FROM match_record WHERE id = 13752;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7049)),
        updated_at = NOW()
    WHERE id = 7049;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13743 
          AND p.match_id = 8201
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8201 WHERE match_id = 13743;
UPDATE odds_snapshot SET match_id = 8201 WHERE match_id = 13743;
UPDATE surebet_alert SET match_id = 8201 WHERE match_id = 13743;
UPDATE middle_alert SET match_id = 8201 WHERE match_id = 13743;
UPDATE valuebet_alert SET match_id = 8201 WHERE match_id = 13743;
DELETE FROM match_record WHERE id = 13743;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13760 
          AND p.match_id = 8201
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8201 WHERE match_id = 13760;
UPDATE odds_snapshot SET match_id = 8201 WHERE match_id = 13760;
UPDATE surebet_alert SET match_id = 8201 WHERE match_id = 13760;
UPDATE middle_alert SET match_id = 8201 WHERE match_id = 13760;
UPDATE valuebet_alert SET match_id = 8201 WHERE match_id = 13760;
DELETE FROM match_record WHERE id = 13760;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8201)),
        updated_at = NOW()
    WHERE id = 8201;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14081 
          AND p.match_id = 6626
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6626 WHERE match_id = 14081;
UPDATE odds_snapshot SET match_id = 6626 WHERE match_id = 14081;
UPDATE surebet_alert SET match_id = 6626 WHERE match_id = 14081;
UPDATE middle_alert SET match_id = 6626 WHERE match_id = 14081;
UPDATE valuebet_alert SET match_id = 6626 WHERE match_id = 14081;
DELETE FROM match_record WHERE id = 14081;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6626)),
        updated_at = NOW()
    WHERE id = 6626;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14077 
          AND p.match_id = 6375
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6375 WHERE match_id = 14077;
UPDATE odds_snapshot SET match_id = 6375 WHERE match_id = 14077;
UPDATE surebet_alert SET match_id = 6375 WHERE match_id = 14077;
UPDATE middle_alert SET match_id = 6375 WHERE match_id = 14077;
UPDATE valuebet_alert SET match_id = 6375 WHERE match_id = 14077;
DELETE FROM match_record WHERE id = 14077;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6375)),
        updated_at = NOW()
    WHERE id = 6375;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14078 
          AND p.match_id = 7509
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7509 WHERE match_id = 14078;
UPDATE odds_snapshot SET match_id = 7509 WHERE match_id = 14078;
UPDATE surebet_alert SET match_id = 7509 WHERE match_id = 14078;
UPDATE middle_alert SET match_id = 7509 WHERE match_id = 14078;
UPDATE valuebet_alert SET match_id = 7509 WHERE match_id = 14078;
DELETE FROM match_record WHERE id = 14078;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7509)),
        updated_at = NOW()
    WHERE id = 7509;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14004 
          AND p.match_id = 8526
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8526 WHERE match_id = 14004;
UPDATE odds_snapshot SET match_id = 8526 WHERE match_id = 14004;
UPDATE surebet_alert SET match_id = 8526 WHERE match_id = 14004;
UPDATE middle_alert SET match_id = 8526 WHERE match_id = 14004;
UPDATE valuebet_alert SET match_id = 8526 WHERE match_id = 14004;
DELETE FROM match_record WHERE id = 14004;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14011 
          AND p.match_id = 8526
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8526 WHERE match_id = 14011;
UPDATE odds_snapshot SET match_id = 8526 WHERE match_id = 14011;
UPDATE surebet_alert SET match_id = 8526 WHERE match_id = 14011;
UPDATE middle_alert SET match_id = 8526 WHERE match_id = 14011;
UPDATE valuebet_alert SET match_id = 8526 WHERE match_id = 14011;
DELETE FROM match_record WHERE id = 14011;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8526)),
        updated_at = NOW()
    WHERE id = 8526;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14665 
          AND p.match_id = 14087
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14087 WHERE match_id = 14665;
UPDATE odds_snapshot SET match_id = 14087 WHERE match_id = 14665;
UPDATE surebet_alert SET match_id = 14087 WHERE match_id = 14665;
UPDATE middle_alert SET match_id = 14087 WHERE match_id = 14665;
UPDATE valuebet_alert SET match_id = 14087 WHERE match_id = 14665;
DELETE FROM match_record WHERE id = 14665;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14087)),
        updated_at = NOW()
    WHERE id = 14087;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14086 
          AND p.match_id = 13367
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13367 WHERE match_id = 14086;
UPDATE odds_snapshot SET match_id = 13367 WHERE match_id = 14086;
UPDATE surebet_alert SET match_id = 13367 WHERE match_id = 14086;
UPDATE middle_alert SET match_id = 13367 WHERE match_id = 14086;
UPDATE valuebet_alert SET match_id = 13367 WHERE match_id = 14086;
DELETE FROM match_record WHERE id = 14086;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13367)),
        updated_at = NOW()
    WHERE id = 13367;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14572 
          AND p.match_id = 6366
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6366 WHERE match_id = 14572;
UPDATE odds_snapshot SET match_id = 6366 WHERE match_id = 14572;
UPDATE surebet_alert SET match_id = 6366 WHERE match_id = 14572;
UPDATE middle_alert SET match_id = 6366 WHERE match_id = 14572;
UPDATE valuebet_alert SET match_id = 6366 WHERE match_id = 14572;
DELETE FROM match_record WHERE id = 14572;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6366)),
        updated_at = NOW()
    WHERE id = 6366;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13985 
          AND p.match_id = 8205
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8205 WHERE match_id = 13985;
UPDATE odds_snapshot SET match_id = 8205 WHERE match_id = 13985;
UPDATE surebet_alert SET match_id = 8205 WHERE match_id = 13985;
UPDATE middle_alert SET match_id = 8205 WHERE match_id = 13985;
UPDATE valuebet_alert SET match_id = 8205 WHERE match_id = 13985;
DELETE FROM match_record WHERE id = 13985;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8205)),
        updated_at = NOW()
    WHERE id = 8205;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13721 
          AND p.match_id = 12352
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 12352 WHERE match_id = 13721;
UPDATE odds_snapshot SET match_id = 12352 WHERE match_id = 13721;
UPDATE surebet_alert SET match_id = 12352 WHERE match_id = 13721;
UPDATE middle_alert SET match_id = 12352 WHERE match_id = 13721;
UPDATE valuebet_alert SET match_id = 12352 WHERE match_id = 13721;
DELETE FROM match_record WHERE id = 13721;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13729 
          AND p.match_id = 12352
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 12352 WHERE match_id = 13729;
UPDATE odds_snapshot SET match_id = 12352 WHERE match_id = 13729;
UPDATE surebet_alert SET match_id = 12352 WHERE match_id = 13729;
UPDATE middle_alert SET match_id = 12352 WHERE match_id = 13729;
UPDATE valuebet_alert SET match_id = 12352 WHERE match_id = 13729;
DELETE FROM match_record WHERE id = 13729;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 12352)),
        updated_at = NOW()
    WHERE id = 12352;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13976 
          AND p.match_id = 8204
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8204 WHERE match_id = 13976;
UPDATE odds_snapshot SET match_id = 8204 WHERE match_id = 13976;
UPDATE surebet_alert SET match_id = 8204 WHERE match_id = 13976;
UPDATE middle_alert SET match_id = 8204 WHERE match_id = 13976;
UPDATE valuebet_alert SET match_id = 8204 WHERE match_id = 13976;
DELETE FROM match_record WHERE id = 13976;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8204)),
        updated_at = NOW()
    WHERE id = 8204;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14009 
          AND p.match_id = 8439
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8439 WHERE match_id = 14009;
UPDATE odds_snapshot SET match_id = 8439 WHERE match_id = 14009;
UPDATE surebet_alert SET match_id = 8439 WHERE match_id = 14009;
UPDATE middle_alert SET match_id = 8439 WHERE match_id = 14009;
UPDATE valuebet_alert SET match_id = 8439 WHERE match_id = 14009;
DELETE FROM match_record WHERE id = 14009;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14024 
          AND p.match_id = 8439
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8439 WHERE match_id = 14024;
UPDATE odds_snapshot SET match_id = 8439 WHERE match_id = 14024;
UPDATE surebet_alert SET match_id = 8439 WHERE match_id = 14024;
UPDATE middle_alert SET match_id = 8439 WHERE match_id = 14024;
UPDATE valuebet_alert SET match_id = 8439 WHERE match_id = 14024;
DELETE FROM match_record WHERE id = 14024;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8439)),
        updated_at = NOW()
    WHERE id = 8439;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13566 
          AND p.match_id = 7294
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7294 WHERE match_id = 13566;
UPDATE odds_snapshot SET match_id = 7294 WHERE match_id = 13566;
UPDATE surebet_alert SET match_id = 7294 WHERE match_id = 13566;
UPDATE middle_alert SET match_id = 7294 WHERE match_id = 13566;
UPDATE valuebet_alert SET match_id = 7294 WHERE match_id = 13566;
DELETE FROM match_record WHERE id = 13566;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13570 
          AND p.match_id = 7294
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7294 WHERE match_id = 13570;
UPDATE odds_snapshot SET match_id = 7294 WHERE match_id = 13570;
UPDATE surebet_alert SET match_id = 7294 WHERE match_id = 13570;
UPDATE middle_alert SET match_id = 7294 WHERE match_id = 13570;
UPDATE valuebet_alert SET match_id = 7294 WHERE match_id = 13570;
DELETE FROM match_record WHERE id = 13570;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7294)),
        updated_at = NOW()
    WHERE id = 7294;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14017 
          AND p.match_id = 6283
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6283 WHERE match_id = 14017;
UPDATE odds_snapshot SET match_id = 6283 WHERE match_id = 14017;
UPDATE surebet_alert SET match_id = 6283 WHERE match_id = 14017;
UPDATE middle_alert SET match_id = 6283 WHERE match_id = 14017;
UPDATE valuebet_alert SET match_id = 6283 WHERE match_id = 14017;
DELETE FROM match_record WHERE id = 14017;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14023 
          AND p.match_id = 6283
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6283 WHERE match_id = 14023;
UPDATE odds_snapshot SET match_id = 6283 WHERE match_id = 14023;
UPDATE surebet_alert SET match_id = 6283 WHERE match_id = 14023;
UPDATE middle_alert SET match_id = 6283 WHERE match_id = 14023;
UPDATE valuebet_alert SET match_id = 6283 WHERE match_id = 14023;
DELETE FROM match_record WHERE id = 14023;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6283)),
        updated_at = NOW()
    WHERE id = 6283;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13970 
          AND p.match_id = 8593
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8593 WHERE match_id = 13970;
UPDATE odds_snapshot SET match_id = 8593 WHERE match_id = 13970;
UPDATE surebet_alert SET match_id = 8593 WHERE match_id = 13970;
UPDATE middle_alert SET match_id = 8593 WHERE match_id = 13970;
UPDATE valuebet_alert SET match_id = 8593 WHERE match_id = 13970;
DELETE FROM match_record WHERE id = 13970;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8593)),
        updated_at = NOW()
    WHERE id = 8593;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14063 
          AND p.match_id = 5883
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 5883 WHERE match_id = 14063;
UPDATE odds_snapshot SET match_id = 5883 WHERE match_id = 14063;
UPDATE surebet_alert SET match_id = 5883 WHERE match_id = 14063;
UPDATE middle_alert SET match_id = 5883 WHERE match_id = 14063;
UPDATE valuebet_alert SET match_id = 5883 WHERE match_id = 14063;
DELETE FROM match_record WHERE id = 14063;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 5883)),
        updated_at = NOW()
    WHERE id = 5883;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13849 
          AND p.match_id = 7201
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7201 WHERE match_id = 13849;
UPDATE odds_snapshot SET match_id = 7201 WHERE match_id = 13849;
UPDATE surebet_alert SET match_id = 7201 WHERE match_id = 13849;
UPDATE middle_alert SET match_id = 7201 WHERE match_id = 13849;
UPDATE valuebet_alert SET match_id = 7201 WHERE match_id = 13849;
DELETE FROM match_record WHERE id = 13849;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13851 
          AND p.match_id = 7201
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7201 WHERE match_id = 13851;
UPDATE odds_snapshot SET match_id = 7201 WHERE match_id = 13851;
UPDATE surebet_alert SET match_id = 7201 WHERE match_id = 13851;
UPDATE middle_alert SET match_id = 7201 WHERE match_id = 13851;
UPDATE valuebet_alert SET match_id = 7201 WHERE match_id = 13851;
DELETE FROM match_record WHERE id = 13851;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7201)),
        updated_at = NOW()
    WHERE id = 7201;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14013 
          AND p.match_id = 7404
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7404 WHERE match_id = 14013;
UPDATE odds_snapshot SET match_id = 7404 WHERE match_id = 14013;
UPDATE surebet_alert SET match_id = 7404 WHERE match_id = 14013;
UPDATE middle_alert SET match_id = 7404 WHERE match_id = 14013;
UPDATE valuebet_alert SET match_id = 7404 WHERE match_id = 14013;
DELETE FROM match_record WHERE id = 14013;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7404)),
        updated_at = NOW()
    WHERE id = 7404;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14064 
          AND p.match_id = 5929
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 5929 WHERE match_id = 14064;
UPDATE odds_snapshot SET match_id = 5929 WHERE match_id = 14064;
UPDATE surebet_alert SET match_id = 5929 WHERE match_id = 14064;
UPDATE middle_alert SET match_id = 5929 WHERE match_id = 14064;
UPDATE valuebet_alert SET match_id = 5929 WHERE match_id = 14064;
DELETE FROM match_record WHERE id = 14064;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 5929)),
        updated_at = NOW()
    WHERE id = 5929;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13848 
          AND p.match_id = 7537
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7537 WHERE match_id = 13848;
UPDATE odds_snapshot SET match_id = 7537 WHERE match_id = 13848;
UPDATE surebet_alert SET match_id = 7537 WHERE match_id = 13848;
UPDATE middle_alert SET match_id = 7537 WHERE match_id = 13848;
UPDATE valuebet_alert SET match_id = 7537 WHERE match_id = 13848;
DELETE FROM match_record WHERE id = 13848;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13852 
          AND p.match_id = 7537
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7537 WHERE match_id = 13852;
UPDATE odds_snapshot SET match_id = 7537 WHERE match_id = 13852;
UPDATE surebet_alert SET match_id = 7537 WHERE match_id = 13852;
UPDATE middle_alert SET match_id = 7537 WHERE match_id = 13852;
UPDATE valuebet_alert SET match_id = 7537 WHERE match_id = 13852;
DELETE FROM match_record WHERE id = 13852;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7537)),
        updated_at = NOW()
    WHERE id = 7537;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13696 
          AND p.match_id = 8330
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8330 WHERE match_id = 13696;
UPDATE odds_snapshot SET match_id = 8330 WHERE match_id = 13696;
UPDATE surebet_alert SET match_id = 8330 WHERE match_id = 13696;
UPDATE middle_alert SET match_id = 8330 WHERE match_id = 13696;
UPDATE valuebet_alert SET match_id = 8330 WHERE match_id = 13696;
DELETE FROM match_record WHERE id = 13696;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8330)),
        updated_at = NOW()
    WHERE id = 8330;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16926 
          AND p.match_id = 14133
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14133 WHERE match_id = 16926;
UPDATE odds_snapshot SET match_id = 14133 WHERE match_id = 16926;
UPDATE surebet_alert SET match_id = 14133 WHERE match_id = 16926;
UPDATE middle_alert SET match_id = 14133 WHERE match_id = 16926;
UPDATE valuebet_alert SET match_id = 14133 WHERE match_id = 16926;
DELETE FROM match_record WHERE id = 16926;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14133)),
        updated_at = NOW()
    WHERE id = 14133;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13891 
          AND p.match_id = 6829
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6829 WHERE match_id = 13891;
UPDATE odds_snapshot SET match_id = 6829 WHERE match_id = 13891;
UPDATE surebet_alert SET match_id = 6829 WHERE match_id = 13891;
UPDATE middle_alert SET match_id = 6829 WHERE match_id = 13891;
UPDATE valuebet_alert SET match_id = 6829 WHERE match_id = 13891;
DELETE FROM match_record WHERE id = 13891;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6829)),
        updated_at = NOW()
    WHERE id = 6829;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14014 
          AND p.match_id = 7303
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7303 WHERE match_id = 14014;
UPDATE odds_snapshot SET match_id = 7303 WHERE match_id = 14014;
UPDATE surebet_alert SET match_id = 7303 WHERE match_id = 14014;
UPDATE middle_alert SET match_id = 7303 WHERE match_id = 14014;
UPDATE valuebet_alert SET match_id = 7303 WHERE match_id = 14014;
DELETE FROM match_record WHERE id = 14014;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14015 
          AND p.match_id = 7303
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7303 WHERE match_id = 14015;
UPDATE odds_snapshot SET match_id = 7303 WHERE match_id = 14015;
UPDATE surebet_alert SET match_id = 7303 WHERE match_id = 14015;
UPDATE middle_alert SET match_id = 7303 WHERE match_id = 14015;
UPDATE valuebet_alert SET match_id = 7303 WHERE match_id = 14015;
DELETE FROM match_record WHERE id = 14015;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7303)),
        updated_at = NOW()
    WHERE id = 7303;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14018 
          AND p.match_id = 7405
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7405 WHERE match_id = 14018;
UPDATE odds_snapshot SET match_id = 7405 WHERE match_id = 14018;
UPDATE surebet_alert SET match_id = 7405 WHERE match_id = 14018;
UPDATE middle_alert SET match_id = 7405 WHERE match_id = 14018;
UPDATE valuebet_alert SET match_id = 7405 WHERE match_id = 14018;
DELETE FROM match_record WHERE id = 14018;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13330 
          AND p.match_id = 7405
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7405 WHERE match_id = 13330;
UPDATE odds_snapshot SET match_id = 7405 WHERE match_id = 13330;
UPDATE surebet_alert SET match_id = 7405 WHERE match_id = 13330;
UPDATE middle_alert SET match_id = 7405 WHERE match_id = 13330;
UPDATE valuebet_alert SET match_id = 7405 WHERE match_id = 13330;
DELETE FROM match_record WHERE id = 13330;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7405)),
        updated_at = NOW()
    WHERE id = 7405;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13977 
          AND p.match_id = 8108
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8108 WHERE match_id = 13977;
UPDATE odds_snapshot SET match_id = 8108 WHERE match_id = 13977;
UPDATE surebet_alert SET match_id = 8108 WHERE match_id = 13977;
UPDATE middle_alert SET match_id = 8108 WHERE match_id = 13977;
UPDATE valuebet_alert SET match_id = 8108 WHERE match_id = 13977;
DELETE FROM match_record WHERE id = 13977;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13990 
          AND p.match_id = 8108
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8108 WHERE match_id = 13990;
UPDATE odds_snapshot SET match_id = 8108 WHERE match_id = 13990;
UPDATE surebet_alert SET match_id = 8108 WHERE match_id = 13990;
UPDATE middle_alert SET match_id = 8108 WHERE match_id = 13990;
UPDATE valuebet_alert SET match_id = 8108 WHERE match_id = 13990;
DELETE FROM match_record WHERE id = 13990;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8108)),
        updated_at = NOW()
    WHERE id = 8108;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13708 
          AND p.match_id = 7722
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7722 WHERE match_id = 13708;
UPDATE odds_snapshot SET match_id = 7722 WHERE match_id = 13708;
UPDATE surebet_alert SET match_id = 7722 WHERE match_id = 13708;
UPDATE middle_alert SET match_id = 7722 WHERE match_id = 13708;
UPDATE valuebet_alert SET match_id = 7722 WHERE match_id = 13708;
DELETE FROM match_record WHERE id = 13708;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7722)),
        updated_at = NOW()
    WHERE id = 7722;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14681 
          AND p.match_id = 14147
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14147 WHERE match_id = 14681;
UPDATE odds_snapshot SET match_id = 14147 WHERE match_id = 14681;
UPDATE surebet_alert SET match_id = 14147 WHERE match_id = 14681;
UPDATE middle_alert SET match_id = 14147 WHERE match_id = 14681;
UPDATE valuebet_alert SET match_id = 14147 WHERE match_id = 14681;
DELETE FROM match_record WHERE id = 14681;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14147)),
        updated_at = NOW()
    WHERE id = 14147;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 6454 
          AND p.match_id = 6341
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6341 WHERE match_id = 6454;
UPDATE odds_snapshot SET match_id = 6341 WHERE match_id = 6454;
UPDATE surebet_alert SET match_id = 6341 WHERE match_id = 6454;
UPDATE middle_alert SET match_id = 6341 WHERE match_id = 6454;
UPDATE valuebet_alert SET match_id = 6341 WHERE match_id = 6454;
DELETE FROM match_record WHERE id = 6454;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6341)),
        updated_at = NOW()
    WHERE id = 6341;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14054 
          AND p.match_id = 7960
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7960 WHERE match_id = 14054;
UPDATE odds_snapshot SET match_id = 7960 WHERE match_id = 14054;
UPDATE surebet_alert SET match_id = 7960 WHERE match_id = 14054;
UPDATE middle_alert SET match_id = 7960 WHERE match_id = 14054;
UPDATE valuebet_alert SET match_id = 7960 WHERE match_id = 14054;
DELETE FROM match_record WHERE id = 14054;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14055 
          AND p.match_id = 7960
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7960 WHERE match_id = 14055;
UPDATE odds_snapshot SET match_id = 7960 WHERE match_id = 14055;
UPDATE surebet_alert SET match_id = 7960 WHERE match_id = 14055;
UPDATE middle_alert SET match_id = 7960 WHERE match_id = 14055;
UPDATE valuebet_alert SET match_id = 7960 WHERE match_id = 14055;
DELETE FROM match_record WHERE id = 14055;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7960)),
        updated_at = NOW()
    WHERE id = 7960;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14052 
          AND p.match_id = 13344
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13344 WHERE match_id = 14052;
UPDATE odds_snapshot SET match_id = 13344 WHERE match_id = 14052;
UPDATE surebet_alert SET match_id = 13344 WHERE match_id = 14052;
UPDATE middle_alert SET match_id = 13344 WHERE match_id = 14052;
UPDATE valuebet_alert SET match_id = 13344 WHERE match_id = 14052;
DELETE FROM match_record WHERE id = 14052;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13344)),
        updated_at = NOW()
    WHERE id = 13344;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13998 
          AND p.match_id = 13326
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13326 WHERE match_id = 13998;
UPDATE odds_snapshot SET match_id = 13326 WHERE match_id = 13998;
UPDATE surebet_alert SET match_id = 13326 WHERE match_id = 13998;
UPDATE middle_alert SET match_id = 13326 WHERE match_id = 13998;
UPDATE valuebet_alert SET match_id = 13326 WHERE match_id = 13998;
DELETE FROM match_record WHERE id = 13998;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13326)),
        updated_at = NOW()
    WHERE id = 13326;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14065 
          AND p.match_id = 13345
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13345 WHERE match_id = 14065;
UPDATE odds_snapshot SET match_id = 13345 WHERE match_id = 14065;
UPDATE surebet_alert SET match_id = 13345 WHERE match_id = 14065;
UPDATE middle_alert SET match_id = 13345 WHERE match_id = 14065;
UPDATE valuebet_alert SET match_id = 13345 WHERE match_id = 14065;
DELETE FROM match_record WHERE id = 14065;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13345)),
        updated_at = NOW()
    WHERE id = 13345;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13719 
          AND p.match_id = 13130
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13130 WHERE match_id = 13719;
UPDATE odds_snapshot SET match_id = 13130 WHERE match_id = 13719;
UPDATE surebet_alert SET match_id = 13130 WHERE match_id = 13719;
UPDATE middle_alert SET match_id = 13130 WHERE match_id = 13719;
UPDATE valuebet_alert SET match_id = 13130 WHERE match_id = 13719;
DELETE FROM match_record WHERE id = 13719;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13727 
          AND p.match_id = 13130
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13130 WHERE match_id = 13727;
UPDATE odds_snapshot SET match_id = 13130 WHERE match_id = 13727;
UPDATE surebet_alert SET match_id = 13130 WHERE match_id = 13727;
UPDATE middle_alert SET match_id = 13130 WHERE match_id = 13727;
UPDATE valuebet_alert SET match_id = 13130 WHERE match_id = 13727;
DELETE FROM match_record WHERE id = 13727;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13130)),
        updated_at = NOW()
    WHERE id = 13130;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13961 
          AND p.match_id = 12999
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 12999 WHERE match_id = 13961;
UPDATE odds_snapshot SET match_id = 12999 WHERE match_id = 13961;
UPDATE surebet_alert SET match_id = 12999 WHERE match_id = 13961;
UPDATE middle_alert SET match_id = 12999 WHERE match_id = 13961;
UPDATE valuebet_alert SET match_id = 12999 WHERE match_id = 13961;
DELETE FROM match_record WHERE id = 13961;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 12999)),
        updated_at = NOW()
    WHERE id = 12999;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13776 
          AND p.match_id = 8739
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8739 WHERE match_id = 13776;
UPDATE odds_snapshot SET match_id = 8739 WHERE match_id = 13776;
UPDATE surebet_alert SET match_id = 8739 WHERE match_id = 13776;
UPDATE middle_alert SET match_id = 8739 WHERE match_id = 13776;
UPDATE valuebet_alert SET match_id = 8739 WHERE match_id = 13776;
DELETE FROM match_record WHERE id = 13776;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13804 
          AND p.match_id = 8739
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8739 WHERE match_id = 13804;
UPDATE odds_snapshot SET match_id = 8739 WHERE match_id = 13804;
UPDATE surebet_alert SET match_id = 8739 WHERE match_id = 13804;
UPDATE middle_alert SET match_id = 8739 WHERE match_id = 13804;
UPDATE valuebet_alert SET match_id = 8739 WHERE match_id = 13804;
DELETE FROM match_record WHERE id = 13804;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8739)),
        updated_at = NOW()
    WHERE id = 8739;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13568 
          AND p.match_id = 7186
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7186 WHERE match_id = 13568;
UPDATE odds_snapshot SET match_id = 7186 WHERE match_id = 13568;
UPDATE surebet_alert SET match_id = 7186 WHERE match_id = 13568;
UPDATE middle_alert SET match_id = 7186 WHERE match_id = 13568;
UPDATE valuebet_alert SET match_id = 7186 WHERE match_id = 13568;
DELETE FROM match_record WHERE id = 13568;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13595 
          AND p.match_id = 7186
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7186 WHERE match_id = 13595;
UPDATE odds_snapshot SET match_id = 7186 WHERE match_id = 13595;
UPDATE surebet_alert SET match_id = 7186 WHERE match_id = 13595;
UPDATE middle_alert SET match_id = 7186 WHERE match_id = 13595;
UPDATE valuebet_alert SET match_id = 7186 WHERE match_id = 13595;
DELETE FROM match_record WHERE id = 13595;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7186)),
        updated_at = NOW()
    WHERE id = 7186;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13995 
          AND p.match_id = 8129
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8129 WHERE match_id = 13995;
UPDATE odds_snapshot SET match_id = 8129 WHERE match_id = 13995;
UPDATE surebet_alert SET match_id = 8129 WHERE match_id = 13995;
UPDATE middle_alert SET match_id = 8129 WHERE match_id = 13995;
UPDATE valuebet_alert SET match_id = 8129 WHERE match_id = 13995;
DELETE FROM match_record WHERE id = 13995;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8129)),
        updated_at = NOW()
    WHERE id = 8129;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14079 
          AND p.match_id = 8137
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8137 WHERE match_id = 14079;
UPDATE odds_snapshot SET match_id = 8137 WHERE match_id = 14079;
UPDATE surebet_alert SET match_id = 8137 WHERE match_id = 14079;
UPDATE middle_alert SET match_id = 8137 WHERE match_id = 14079;
UPDATE valuebet_alert SET match_id = 8137 WHERE match_id = 14079;
DELETE FROM match_record WHERE id = 14079;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8137)),
        updated_at = NOW()
    WHERE id = 8137;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13948 
          AND p.match_id = 8242
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8242 WHERE match_id = 13948;
UPDATE odds_snapshot SET match_id = 8242 WHERE match_id = 13948;
UPDATE surebet_alert SET match_id = 8242 WHERE match_id = 13948;
UPDATE middle_alert SET match_id = 8242 WHERE match_id = 13948;
UPDATE valuebet_alert SET match_id = 8242 WHERE match_id = 13948;
DELETE FROM match_record WHERE id = 13948;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8242)),
        updated_at = NOW()
    WHERE id = 8242;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13997 
          AND p.match_id = 13307
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13307 WHERE match_id = 13997;
UPDATE odds_snapshot SET match_id = 13307 WHERE match_id = 13997;
UPDATE surebet_alert SET match_id = 13307 WHERE match_id = 13997;
UPDATE middle_alert SET match_id = 13307 WHERE match_id = 13997;
UPDATE valuebet_alert SET match_id = 13307 WHERE match_id = 13997;
DELETE FROM match_record WHERE id = 13997;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13307)),
        updated_at = NOW()
    WHERE id = 13307;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13975 
          AND p.match_id = 13199
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13199 WHERE match_id = 13975;
UPDATE odds_snapshot SET match_id = 13199 WHERE match_id = 13975;
UPDATE surebet_alert SET match_id = 13199 WHERE match_id = 13975;
UPDATE middle_alert SET match_id = 13199 WHERE match_id = 13975;
UPDATE valuebet_alert SET match_id = 13199 WHERE match_id = 13975;
DELETE FROM match_record WHERE id = 13975;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13199)),
        updated_at = NOW()
    WHERE id = 13199;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13972 
          AND p.match_id = 13363
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13363 WHERE match_id = 13972;
UPDATE odds_snapshot SET match_id = 13363 WHERE match_id = 13972;
UPDATE surebet_alert SET match_id = 13363 WHERE match_id = 13972;
UPDATE middle_alert SET match_id = 13363 WHERE match_id = 13972;
UPDATE valuebet_alert SET match_id = 13363 WHERE match_id = 13972;
DELETE FROM match_record WHERE id = 13972;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13363)),
        updated_at = NOW()
    WHERE id = 13363;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13459 
          AND p.match_id = 8199
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8199 WHERE match_id = 13459;
UPDATE odds_snapshot SET match_id = 8199 WHERE match_id = 13459;
UPDATE surebet_alert SET match_id = 8199 WHERE match_id = 13459;
UPDATE middle_alert SET match_id = 8199 WHERE match_id = 13459;
UPDATE valuebet_alert SET match_id = 8199 WHERE match_id = 13459;
DELETE FROM match_record WHERE id = 13459;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13494 
          AND p.match_id = 8199
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8199 WHERE match_id = 13494;
UPDATE odds_snapshot SET match_id = 8199 WHERE match_id = 13494;
UPDATE surebet_alert SET match_id = 8199 WHERE match_id = 13494;
UPDATE middle_alert SET match_id = 8199 WHERE match_id = 13494;
UPDATE valuebet_alert SET match_id = 8199 WHERE match_id = 13494;
DELETE FROM match_record WHERE id = 13494;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8199)),
        updated_at = NOW()
    WHERE id = 8199;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13711 
          AND p.match_id = 13237
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13237 WHERE match_id = 13711;
UPDATE odds_snapshot SET match_id = 13237 WHERE match_id = 13711;
UPDATE surebet_alert SET match_id = 13237 WHERE match_id = 13711;
UPDATE middle_alert SET match_id = 13237 WHERE match_id = 13711;
UPDATE valuebet_alert SET match_id = 13237 WHERE match_id = 13711;
DELETE FROM match_record WHERE id = 13711;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13237)),
        updated_at = NOW()
    WHERE id = 13237;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13722 
          AND p.match_id = 13134
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13134 WHERE match_id = 13722;
UPDATE odds_snapshot SET match_id = 13134 WHERE match_id = 13722;
UPDATE surebet_alert SET match_id = 13134 WHERE match_id = 13722;
UPDATE middle_alert SET match_id = 13134 WHERE match_id = 13722;
UPDATE valuebet_alert SET match_id = 13134 WHERE match_id = 13722;
DELETE FROM match_record WHERE id = 13722;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13731 
          AND p.match_id = 13134
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13134 WHERE match_id = 13731;
UPDATE odds_snapshot SET match_id = 13134 WHERE match_id = 13731;
UPDATE surebet_alert SET match_id = 13134 WHERE match_id = 13731;
UPDATE middle_alert SET match_id = 13134 WHERE match_id = 13731;
UPDATE valuebet_alert SET match_id = 13134 WHERE match_id = 13731;
DELETE FROM match_record WHERE id = 13731;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13134)),
        updated_at = NOW()
    WHERE id = 13134;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13957 
          AND p.match_id = 13413
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13413 WHERE match_id = 13957;
UPDATE odds_snapshot SET match_id = 13413 WHERE match_id = 13957;
UPDATE surebet_alert SET match_id = 13413 WHERE match_id = 13957;
UPDATE middle_alert SET match_id = 13413 WHERE match_id = 13957;
UPDATE valuebet_alert SET match_id = 13413 WHERE match_id = 13957;
DELETE FROM match_record WHERE id = 13957;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13413)),
        updated_at = NOW()
    WHERE id = 13413;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13706 
          AND p.match_id = 13295
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13295 WHERE match_id = 13706;
UPDATE odds_snapshot SET match_id = 13295 WHERE match_id = 13706;
UPDATE surebet_alert SET match_id = 13295 WHERE match_id = 13706;
UPDATE middle_alert SET match_id = 13295 WHERE match_id = 13706;
UPDATE valuebet_alert SET match_id = 13295 WHERE match_id = 13706;
DELETE FROM match_record WHERE id = 13706;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13295)),
        updated_at = NOW()
    WHERE id = 13295;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14007 
          AND p.match_id = 14006
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14006 WHERE match_id = 14007;
UPDATE odds_snapshot SET match_id = 14006 WHERE match_id = 14007;
UPDATE surebet_alert SET match_id = 14006 WHERE match_id = 14007;
UPDATE middle_alert SET match_id = 14006 WHERE match_id = 14007;
UPDATE valuebet_alert SET match_id = 14006 WHERE match_id = 14007;
DELETE FROM match_record WHERE id = 14007;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 8789 
          AND p.match_id = 14006
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14006 WHERE match_id = 8789;
UPDATE odds_snapshot SET match_id = 14006 WHERE match_id = 8789;
UPDATE surebet_alert SET match_id = 14006 WHERE match_id = 8789;
UPDATE middle_alert SET match_id = 14006 WHERE match_id = 8789;
UPDATE valuebet_alert SET match_id = 14006 WHERE match_id = 8789;
DELETE FROM match_record WHERE id = 8789;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14006)),
        updated_at = NOW()
    WHERE id = 14006;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13981 
          AND p.match_id = 13298
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13298 WHERE match_id = 13981;
UPDATE odds_snapshot SET match_id = 13298 WHERE match_id = 13981;
UPDATE surebet_alert SET match_id = 13298 WHERE match_id = 13981;
UPDATE middle_alert SET match_id = 13298 WHERE match_id = 13981;
UPDATE valuebet_alert SET match_id = 13298 WHERE match_id = 13981;
DELETE FROM match_record WHERE id = 13981;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13298)),
        updated_at = NOW()
    WHERE id = 13298;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14066 
          AND p.match_id = 8864
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8864 WHERE match_id = 14066;
UPDATE odds_snapshot SET match_id = 8864 WHERE match_id = 14066;
UPDATE surebet_alert SET match_id = 8864 WHERE match_id = 14066;
UPDATE middle_alert SET match_id = 8864 WHERE match_id = 14066;
UPDATE valuebet_alert SET match_id = 8864 WHERE match_id = 14066;
DELETE FROM match_record WHERE id = 14066;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8864)),
        updated_at = NOW()
    WHERE id = 8864;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13993 
          AND p.match_id = 13281
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13281 WHERE match_id = 13993;
UPDATE odds_snapshot SET match_id = 13281 WHERE match_id = 13993;
UPDATE surebet_alert SET match_id = 13281 WHERE match_id = 13993;
UPDATE middle_alert SET match_id = 13281 WHERE match_id = 13993;
UPDATE valuebet_alert SET match_id = 13281 WHERE match_id = 13993;
DELETE FROM match_record WHERE id = 13993;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13281)),
        updated_at = NOW()
    WHERE id = 13281;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13200 
          AND p.match_id = 13980
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13980 WHERE match_id = 13200;
UPDATE odds_snapshot SET match_id = 13980 WHERE match_id = 13200;
UPDATE surebet_alert SET match_id = 13980 WHERE match_id = 13200;
UPDATE middle_alert SET match_id = 13980 WHERE match_id = 13200;
UPDATE valuebet_alert SET match_id = 13980 WHERE match_id = 13200;
DELETE FROM match_record WHERE id = 13200;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13980)),
        updated_at = NOW()
    WHERE id = 13980;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13952 
          AND p.match_id = 9711
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 9711 WHERE match_id = 13952;
UPDATE odds_snapshot SET match_id = 9711 WHERE match_id = 13952;
UPDATE surebet_alert SET match_id = 9711 WHERE match_id = 13952;
UPDATE middle_alert SET match_id = 9711 WHERE match_id = 13952;
UPDATE valuebet_alert SET match_id = 9711 WHERE match_id = 13952;
DELETE FROM match_record WHERE id = 13952;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 9711)),
        updated_at = NOW()
    WHERE id = 9711;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13710 
          AND p.match_id = 13226
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13226 WHERE match_id = 13710;
UPDATE odds_snapshot SET match_id = 13226 WHERE match_id = 13710;
UPDATE surebet_alert SET match_id = 13226 WHERE match_id = 13710;
UPDATE middle_alert SET match_id = 13226 WHERE match_id = 13710;
UPDATE valuebet_alert SET match_id = 13226 WHERE match_id = 13710;
DELETE FROM match_record WHERE id = 13710;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13226)),
        updated_at = NOW()
    WHERE id = 13226;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13960 
          AND p.match_id = 13192
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13192 WHERE match_id = 13960;
UPDATE odds_snapshot SET match_id = 13192 WHERE match_id = 13960;
UPDATE surebet_alert SET match_id = 13192 WHERE match_id = 13960;
UPDATE middle_alert SET match_id = 13192 WHERE match_id = 13960;
UPDATE valuebet_alert SET match_id = 13192 WHERE match_id = 13960;
DELETE FROM match_record WHERE id = 13960;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13967 
          AND p.match_id = 13192
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13192 WHERE match_id = 13967;
UPDATE odds_snapshot SET match_id = 13192 WHERE match_id = 13967;
UPDATE surebet_alert SET match_id = 13192 WHERE match_id = 13967;
UPDATE middle_alert SET match_id = 13192 WHERE match_id = 13967;
UPDATE valuebet_alert SET match_id = 13192 WHERE match_id = 13967;
DELETE FROM match_record WHERE id = 13967;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13192)),
        updated_at = NOW()
    WHERE id = 13192;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14046 
          AND p.match_id = 9370
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 9370 WHERE match_id = 14046;
UPDATE odds_snapshot SET match_id = 9370 WHERE match_id = 14046;
UPDATE surebet_alert SET match_id = 9370 WHERE match_id = 14046;
UPDATE middle_alert SET match_id = 9370 WHERE match_id = 14046;
UPDATE valuebet_alert SET match_id = 9370 WHERE match_id = 14046;
DELETE FROM match_record WHERE id = 14046;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 9370)),
        updated_at = NOW()
    WHERE id = 9370;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13996 
          AND p.match_id = 13294
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13294 WHERE match_id = 13996;
UPDATE odds_snapshot SET match_id = 13294 WHERE match_id = 13996;
UPDATE surebet_alert SET match_id = 13294 WHERE match_id = 13996;
UPDATE middle_alert SET match_id = 13294 WHERE match_id = 13996;
UPDATE valuebet_alert SET match_id = 13294 WHERE match_id = 13996;
DELETE FROM match_record WHERE id = 13996;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13294)),
        updated_at = NOW()
    WHERE id = 13294;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13704 
          AND p.match_id = 9437
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 9437 WHERE match_id = 13704;
UPDATE odds_snapshot SET match_id = 9437 WHERE match_id = 13704;
UPDATE surebet_alert SET match_id = 9437 WHERE match_id = 13704;
UPDATE middle_alert SET match_id = 9437 WHERE match_id = 13704;
UPDATE valuebet_alert SET match_id = 9437 WHERE match_id = 13704;
DELETE FROM match_record WHERE id = 13704;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 9437)),
        updated_at = NOW()
    WHERE id = 9437;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13680 
          AND p.match_id = 13107
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13107 WHERE match_id = 13680;
UPDATE odds_snapshot SET match_id = 13107 WHERE match_id = 13680;
UPDATE surebet_alert SET match_id = 13107 WHERE match_id = 13680;
UPDATE middle_alert SET match_id = 13107 WHERE match_id = 13680;
UPDATE valuebet_alert SET match_id = 13107 WHERE match_id = 13680;
DELETE FROM match_record WHERE id = 13680;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13694 
          AND p.match_id = 13107
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13107 WHERE match_id = 13694;
UPDATE odds_snapshot SET match_id = 13107 WHERE match_id = 13694;
UPDATE surebet_alert SET match_id = 13107 WHERE match_id = 13694;
UPDATE middle_alert SET match_id = 13107 WHERE match_id = 13694;
UPDATE valuebet_alert SET match_id = 13107 WHERE match_id = 13694;
DELETE FROM match_record WHERE id = 13694;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13107)),
        updated_at = NOW()
    WHERE id = 13107;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13713 
          AND p.match_id = 6106
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6106 WHERE match_id = 13713;
UPDATE odds_snapshot SET match_id = 6106 WHERE match_id = 13713;
UPDATE surebet_alert SET match_id = 6106 WHERE match_id = 13713;
UPDATE middle_alert SET match_id = 6106 WHERE match_id = 13713;
UPDATE valuebet_alert SET match_id = 6106 WHERE match_id = 13713;
DELETE FROM match_record WHERE id = 13713;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6106)),
        updated_at = NOW()
    WHERE id = 6106;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13682 
          AND p.match_id = 13121
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13121 WHERE match_id = 13682;
UPDATE odds_snapshot SET match_id = 13121 WHERE match_id = 13682;
UPDATE surebet_alert SET match_id = 13121 WHERE match_id = 13682;
UPDATE middle_alert SET match_id = 13121 WHERE match_id = 13682;
UPDATE valuebet_alert SET match_id = 13121 WHERE match_id = 13682;
DELETE FROM match_record WHERE id = 13682;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13684 
          AND p.match_id = 13121
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13121 WHERE match_id = 13684;
UPDATE odds_snapshot SET match_id = 13121 WHERE match_id = 13684;
UPDATE surebet_alert SET match_id = 13121 WHERE match_id = 13684;
UPDATE middle_alert SET match_id = 13121 WHERE match_id = 13684;
UPDATE valuebet_alert SET match_id = 13121 WHERE match_id = 13684;
DELETE FROM match_record WHERE id = 13684;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13121)),
        updated_at = NOW()
    WHERE id = 13121;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13697 
          AND p.match_id = 13123
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13123 WHERE match_id = 13697;
UPDATE odds_snapshot SET match_id = 13123 WHERE match_id = 13697;
UPDATE surebet_alert SET match_id = 13123 WHERE match_id = 13697;
UPDATE middle_alert SET match_id = 13123 WHERE match_id = 13697;
UPDATE valuebet_alert SET match_id = 13123 WHERE match_id = 13697;
DELETE FROM match_record WHERE id = 13697;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13123)),
        updated_at = NOW()
    WHERE id = 13123;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13724 
          AND p.match_id = 13131
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13131 WHERE match_id = 13724;
UPDATE odds_snapshot SET match_id = 13131 WHERE match_id = 13724;
UPDATE surebet_alert SET match_id = 13131 WHERE match_id = 13724;
UPDATE middle_alert SET match_id = 13131 WHERE match_id = 13724;
UPDATE valuebet_alert SET match_id = 13131 WHERE match_id = 13724;
DELETE FROM match_record WHERE id = 13724;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13725 
          AND p.match_id = 13131
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13131 WHERE match_id = 13725;
UPDATE odds_snapshot SET match_id = 13131 WHERE match_id = 13725;
UPDATE surebet_alert SET match_id = 13131 WHERE match_id = 13725;
UPDATE middle_alert SET match_id = 13131 WHERE match_id = 13725;
UPDATE valuebet_alert SET match_id = 13131 WHERE match_id = 13725;
DELETE FROM match_record WHERE id = 13725;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13131)),
        updated_at = NOW()
    WHERE id = 13131;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 15818 
          AND p.match_id = 14216
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14216 WHERE match_id = 15818;
UPDATE odds_snapshot SET match_id = 14216 WHERE match_id = 15818;
UPDATE surebet_alert SET match_id = 14216 WHERE match_id = 15818;
UPDATE middle_alert SET match_id = 14216 WHERE match_id = 15818;
UPDATE valuebet_alert SET match_id = 14216 WHERE match_id = 15818;
DELETE FROM match_record WHERE id = 15818;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14216)),
        updated_at = NOW()
    WHERE id = 14216;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14071 
          AND p.match_id = 13202
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13202 WHERE match_id = 14071;
UPDATE odds_snapshot SET match_id = 13202 WHERE match_id = 14071;
UPDATE surebet_alert SET match_id = 13202 WHERE match_id = 14071;
UPDATE middle_alert SET match_id = 13202 WHERE match_id = 14071;
UPDATE valuebet_alert SET match_id = 13202 WHERE match_id = 14071;
DELETE FROM match_record WHERE id = 14071;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13202)),
        updated_at = NOW()
    WHERE id = 13202;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14072 
          AND p.match_id = 13207
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13207 WHERE match_id = 14072;
UPDATE odds_snapshot SET match_id = 13207 WHERE match_id = 14072;
UPDATE surebet_alert SET match_id = 13207 WHERE match_id = 14072;
UPDATE middle_alert SET match_id = 13207 WHERE match_id = 14072;
UPDATE valuebet_alert SET match_id = 13207 WHERE match_id = 14072;
DELETE FROM match_record WHERE id = 14072;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13207)),
        updated_at = NOW()
    WHERE id = 13207;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14074 
          AND p.match_id = 13209
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13209 WHERE match_id = 14074;
UPDATE odds_snapshot SET match_id = 13209 WHERE match_id = 14074;
UPDATE surebet_alert SET match_id = 13209 WHERE match_id = 14074;
UPDATE middle_alert SET match_id = 13209 WHERE match_id = 14074;
UPDATE valuebet_alert SET match_id = 13209 WHERE match_id = 14074;
DELETE FROM match_record WHERE id = 14074;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13209)),
        updated_at = NOW()
    WHERE id = 13209;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13702 
          AND p.match_id = 13216
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13216 WHERE match_id = 13702;
UPDATE odds_snapshot SET match_id = 13216 WHERE match_id = 13702;
UPDATE surebet_alert SET match_id = 13216 WHERE match_id = 13702;
UPDATE middle_alert SET match_id = 13216 WHERE match_id = 13702;
UPDATE valuebet_alert SET match_id = 13216 WHERE match_id = 13702;
DELETE FROM match_record WHERE id = 13702;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13216)),
        updated_at = NOW()
    WHERE id = 13216;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13699 
          AND p.match_id = 13217
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13217 WHERE match_id = 13699;
UPDATE odds_snapshot SET match_id = 13217 WHERE match_id = 13699;
UPDATE surebet_alert SET match_id = 13217 WHERE match_id = 13699;
UPDATE middle_alert SET match_id = 13217 WHERE match_id = 13699;
UPDATE valuebet_alert SET match_id = 13217 WHERE match_id = 13699;
DELETE FROM match_record WHERE id = 13699;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13217)),
        updated_at = NOW()
    WHERE id = 13217;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13723 
          AND p.match_id = 13218
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13218 WHERE match_id = 13723;
UPDATE odds_snapshot SET match_id = 13218 WHERE match_id = 13723;
UPDATE surebet_alert SET match_id = 13218 WHERE match_id = 13723;
UPDATE middle_alert SET match_id = 13218 WHERE match_id = 13723;
UPDATE valuebet_alert SET match_id = 13218 WHERE match_id = 13723;
DELETE FROM match_record WHERE id = 13723;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13218)),
        updated_at = NOW()
    WHERE id = 13218;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13701 
          AND p.match_id = 13222
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13222 WHERE match_id = 13701;
UPDATE odds_snapshot SET match_id = 13222 WHERE match_id = 13701;
UPDATE surebet_alert SET match_id = 13222 WHERE match_id = 13701;
UPDATE middle_alert SET match_id = 13222 WHERE match_id = 13701;
UPDATE valuebet_alert SET match_id = 13222 WHERE match_id = 13701;
DELETE FROM match_record WHERE id = 13701;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13222)),
        updated_at = NOW()
    WHERE id = 13222;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13715 
          AND p.match_id = 13224
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13224 WHERE match_id = 13715;
UPDATE odds_snapshot SET match_id = 13224 WHERE match_id = 13715;
UPDATE surebet_alert SET match_id = 13224 WHERE match_id = 13715;
UPDATE middle_alert SET match_id = 13224 WHERE match_id = 13715;
UPDATE valuebet_alert SET match_id = 13224 WHERE match_id = 13715;
DELETE FROM match_record WHERE id = 13715;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13224)),
        updated_at = NOW()
    WHERE id = 13224;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14080 
          AND p.match_id = 13225
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13225 WHERE match_id = 14080;
UPDATE odds_snapshot SET match_id = 13225 WHERE match_id = 14080;
UPDATE surebet_alert SET match_id = 13225 WHERE match_id = 14080;
UPDATE middle_alert SET match_id = 13225 WHERE match_id = 14080;
UPDATE valuebet_alert SET match_id = 13225 WHERE match_id = 14080;
DELETE FROM match_record WHERE id = 14080;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13225)),
        updated_at = NOW()
    WHERE id = 13225;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13714 
          AND p.match_id = 13227
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13227 WHERE match_id = 13714;
UPDATE odds_snapshot SET match_id = 13227 WHERE match_id = 13714;
UPDATE surebet_alert SET match_id = 13227 WHERE match_id = 13714;
UPDATE middle_alert SET match_id = 13227 WHERE match_id = 13714;
UPDATE valuebet_alert SET match_id = 13227 WHERE match_id = 13714;
DELETE FROM match_record WHERE id = 13714;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13227)),
        updated_at = NOW()
    WHERE id = 13227;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13733 
          AND p.match_id = 13231
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13231 WHERE match_id = 13733;
UPDATE odds_snapshot SET match_id = 13231 WHERE match_id = 13733;
UPDATE surebet_alert SET match_id = 13231 WHERE match_id = 13733;
UPDATE middle_alert SET match_id = 13231 WHERE match_id = 13733;
UPDATE valuebet_alert SET match_id = 13231 WHERE match_id = 13733;
DELETE FROM match_record WHERE id = 13733;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13231)),
        updated_at = NOW()
    WHERE id = 13231;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14062 
          AND p.match_id = 13280
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13280 WHERE match_id = 14062;
UPDATE odds_snapshot SET match_id = 13280 WHERE match_id = 14062;
UPDATE surebet_alert SET match_id = 13280 WHERE match_id = 14062;
UPDATE middle_alert SET match_id = 13280 WHERE match_id = 14062;
UPDATE valuebet_alert SET match_id = 13280 WHERE match_id = 14062;
DELETE FROM match_record WHERE id = 14062;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13280)),
        updated_at = NOW()
    WHERE id = 13280;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13989 
          AND p.match_id = 13284
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13284 WHERE match_id = 13989;
UPDATE odds_snapshot SET match_id = 13284 WHERE match_id = 13989;
UPDATE surebet_alert SET match_id = 13284 WHERE match_id = 13989;
UPDATE middle_alert SET match_id = 13284 WHERE match_id = 13989;
UPDATE valuebet_alert SET match_id = 13284 WHERE match_id = 13989;
DELETE FROM match_record WHERE id = 13989;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13284)),
        updated_at = NOW()
    WHERE id = 13284;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13698 
          AND p.match_id = 13299
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13299 WHERE match_id = 13698;
UPDATE odds_snapshot SET match_id = 13299 WHERE match_id = 13698;
UPDATE surebet_alert SET match_id = 13299 WHERE match_id = 13698;
UPDATE middle_alert SET match_id = 13299 WHERE match_id = 13698;
UPDATE valuebet_alert SET match_id = 13299 WHERE match_id = 13698;
DELETE FROM match_record WHERE id = 13698;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13299)),
        updated_at = NOW()
    WHERE id = 13299;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13988 
          AND p.match_id = 13316
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13316 WHERE match_id = 13988;
UPDATE odds_snapshot SET match_id = 13316 WHERE match_id = 13988;
UPDATE surebet_alert SET match_id = 13316 WHERE match_id = 13988;
UPDATE middle_alert SET match_id = 13316 WHERE match_id = 13988;
UPDATE valuebet_alert SET match_id = 13316 WHERE match_id = 13988;
DELETE FROM match_record WHERE id = 13988;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13316)),
        updated_at = NOW()
    WHERE id = 13316;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14000 
          AND p.match_id = 13319
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13319 WHERE match_id = 14000;
UPDATE odds_snapshot SET match_id = 13319 WHERE match_id = 14000;
UPDATE surebet_alert SET match_id = 13319 WHERE match_id = 14000;
UPDATE middle_alert SET match_id = 13319 WHERE match_id = 14000;
UPDATE valuebet_alert SET match_id = 13319 WHERE match_id = 14000;
DELETE FROM match_record WHERE id = 14000;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14003 
          AND p.match_id = 13319
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13319 WHERE match_id = 14003;
UPDATE odds_snapshot SET match_id = 13319 WHERE match_id = 14003;
UPDATE surebet_alert SET match_id = 13319 WHERE match_id = 14003;
UPDATE middle_alert SET match_id = 13319 WHERE match_id = 14003;
UPDATE valuebet_alert SET match_id = 13319 WHERE match_id = 14003;
DELETE FROM match_record WHERE id = 14003;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13319)),
        updated_at = NOW()
    WHERE id = 13319;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14005 
          AND p.match_id = 13327
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13327 WHERE match_id = 14005;
UPDATE odds_snapshot SET match_id = 13327 WHERE match_id = 14005;
UPDATE surebet_alert SET match_id = 13327 WHERE match_id = 14005;
UPDATE middle_alert SET match_id = 13327 WHERE match_id = 14005;
UPDATE valuebet_alert SET match_id = 13327 WHERE match_id = 14005;
DELETE FROM match_record WHERE id = 14005;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14008 
          AND p.match_id = 13327
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13327 WHERE match_id = 14008;
UPDATE odds_snapshot SET match_id = 13327 WHERE match_id = 14008;
UPDATE surebet_alert SET match_id = 13327 WHERE match_id = 14008;
UPDATE middle_alert SET match_id = 13327 WHERE match_id = 14008;
UPDATE valuebet_alert SET match_id = 13327 WHERE match_id = 14008;
DELETE FROM match_record WHERE id = 14008;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13327)),
        updated_at = NOW()
    WHERE id = 13327;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14048 
          AND p.match_id = 13329
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13329 WHERE match_id = 14048;
UPDATE odds_snapshot SET match_id = 13329 WHERE match_id = 14048;
UPDATE surebet_alert SET match_id = 13329 WHERE match_id = 14048;
UPDATE middle_alert SET match_id = 13329 WHERE match_id = 14048;
UPDATE valuebet_alert SET match_id = 13329 WHERE match_id = 14048;
DELETE FROM match_record WHERE id = 14048;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13329)),
        updated_at = NOW()
    WHERE id = 13329;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14010 
          AND p.match_id = 13335
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13335 WHERE match_id = 14010;
UPDATE odds_snapshot SET match_id = 13335 WHERE match_id = 14010;
UPDATE surebet_alert SET match_id = 13335 WHERE match_id = 14010;
UPDATE middle_alert SET match_id = 13335 WHERE match_id = 14010;
UPDATE valuebet_alert SET match_id = 13335 WHERE match_id = 14010;
DELETE FROM match_record WHERE id = 14010;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14012 
          AND p.match_id = 13335
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13335 WHERE match_id = 14012;
UPDATE odds_snapshot SET match_id = 13335 WHERE match_id = 14012;
UPDATE surebet_alert SET match_id = 13335 WHERE match_id = 14012;
UPDATE middle_alert SET match_id = 13335 WHERE match_id = 14012;
UPDATE valuebet_alert SET match_id = 13335 WHERE match_id = 14012;
DELETE FROM match_record WHERE id = 14012;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13335)),
        updated_at = NOW()
    WHERE id = 13335;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14051 
          AND p.match_id = 13339
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13339 WHERE match_id = 14051;
UPDATE odds_snapshot SET match_id = 13339 WHERE match_id = 14051;
UPDATE surebet_alert SET match_id = 13339 WHERE match_id = 14051;
UPDATE middle_alert SET match_id = 13339 WHERE match_id = 14051;
UPDATE valuebet_alert SET match_id = 13339 WHERE match_id = 14051;
DELETE FROM match_record WHERE id = 14051;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14061 
          AND p.match_id = 13339
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13339 WHERE match_id = 14061;
UPDATE odds_snapshot SET match_id = 13339 WHERE match_id = 14061;
UPDATE surebet_alert SET match_id = 13339 WHERE match_id = 14061;
UPDATE middle_alert SET match_id = 13339 WHERE match_id = 14061;
UPDATE valuebet_alert SET match_id = 13339 WHERE match_id = 14061;
DELETE FROM match_record WHERE id = 14061;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13339)),
        updated_at = NOW()
    WHERE id = 13339;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14082 
          AND p.match_id = 13346
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13346 WHERE match_id = 14082;
UPDATE odds_snapshot SET match_id = 13346 WHERE match_id = 14082;
UPDATE surebet_alert SET match_id = 13346 WHERE match_id = 14082;
UPDATE middle_alert SET match_id = 13346 WHERE match_id = 14082;
UPDATE valuebet_alert SET match_id = 13346 WHERE match_id = 14082;
DELETE FROM match_record WHERE id = 14082;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13346)),
        updated_at = NOW()
    WHERE id = 13346;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14083 
          AND p.match_id = 13355
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13355 WHERE match_id = 14083;
UPDATE odds_snapshot SET match_id = 13355 WHERE match_id = 14083;
UPDATE surebet_alert SET match_id = 13355 WHERE match_id = 14083;
UPDATE middle_alert SET match_id = 13355 WHERE match_id = 14083;
UPDATE valuebet_alert SET match_id = 13355 WHERE match_id = 14083;
DELETE FROM match_record WHERE id = 14083;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13355)),
        updated_at = NOW()
    WHERE id = 13355;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13969 
          AND p.match_id = 13361
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13361 WHERE match_id = 13969;
UPDATE odds_snapshot SET match_id = 13361 WHERE match_id = 13969;
UPDATE surebet_alert SET match_id = 13361 WHERE match_id = 13969;
UPDATE middle_alert SET match_id = 13361 WHERE match_id = 13969;
UPDATE valuebet_alert SET match_id = 13361 WHERE match_id = 13969;
DELETE FROM match_record WHERE id = 13969;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13361)),
        updated_at = NOW()
    WHERE id = 13361;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13983 
          AND p.match_id = 13293
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13293 WHERE match_id = 13983;
UPDATE odds_snapshot SET match_id = 13293 WHERE match_id = 13983;
UPDATE surebet_alert SET match_id = 13293 WHERE match_id = 13983;
UPDATE middle_alert SET match_id = 13293 WHERE match_id = 13983;
UPDATE valuebet_alert SET match_id = 13293 WHERE match_id = 13983;
DELETE FROM match_record WHERE id = 13983;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13293)),
        updated_at = NOW()
    WHERE id = 13293;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13974 
          AND p.match_id = 13364
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13364 WHERE match_id = 13974;
UPDATE odds_snapshot SET match_id = 13364 WHERE match_id = 13974;
UPDATE surebet_alert SET match_id = 13364 WHERE match_id = 13974;
UPDATE middle_alert SET match_id = 13364 WHERE match_id = 13974;
UPDATE valuebet_alert SET match_id = 13364 WHERE match_id = 13974;
DELETE FROM match_record WHERE id = 13974;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13364)),
        updated_at = NOW()
    WHERE id = 13364;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13700 
          AND p.match_id = 13381
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13381 WHERE match_id = 13700;
UPDATE odds_snapshot SET match_id = 13381 WHERE match_id = 13700;
UPDATE surebet_alert SET match_id = 13381 WHERE match_id = 13700;
UPDATE middle_alert SET match_id = 13381 WHERE match_id = 13700;
UPDATE valuebet_alert SET match_id = 13381 WHERE match_id = 13700;
DELETE FROM match_record WHERE id = 13700;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13381)),
        updated_at = NOW()
    WHERE id = 13381;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16922 
          AND p.match_id = 14233
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14233 WHERE match_id = 16922;
UPDATE odds_snapshot SET match_id = 14233 WHERE match_id = 16922;
UPDATE surebet_alert SET match_id = 14233 WHERE match_id = 16922;
UPDATE middle_alert SET match_id = 14233 WHERE match_id = 16922;
UPDATE valuebet_alert SET match_id = 14233 WHERE match_id = 16922;
DELETE FROM match_record WHERE id = 16922;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14233)),
        updated_at = NOW()
    WHERE id = 14233;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13971 
          AND p.match_id = 13365
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13365 WHERE match_id = 13971;
UPDATE odds_snapshot SET match_id = 13365 WHERE match_id = 13971;
UPDATE surebet_alert SET match_id = 13365 WHERE match_id = 13971;
UPDATE middle_alert SET match_id = 13365 WHERE match_id = 13971;
UPDATE valuebet_alert SET match_id = 13365 WHERE match_id = 13971;
DELETE FROM match_record WHERE id = 13971;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13365)),
        updated_at = NOW()
    WHERE id = 13365;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13892 
          AND p.match_id = 13366
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13366 WHERE match_id = 13892;
UPDATE odds_snapshot SET match_id = 13366 WHERE match_id = 13892;
UPDATE surebet_alert SET match_id = 13366 WHERE match_id = 13892;
UPDATE middle_alert SET match_id = 13366 WHERE match_id = 13892;
UPDATE valuebet_alert SET match_id = 13366 WHERE match_id = 13892;
DELETE FROM match_record WHERE id = 13892;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13366)),
        updated_at = NOW()
    WHERE id = 13366;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13903 
          AND p.match_id = 13369
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13369 WHERE match_id = 13903;
UPDATE odds_snapshot SET match_id = 13369 WHERE match_id = 13903;
UPDATE surebet_alert SET match_id = 13369 WHERE match_id = 13903;
UPDATE middle_alert SET match_id = 13369 WHERE match_id = 13903;
UPDATE valuebet_alert SET match_id = 13369 WHERE match_id = 13903;
DELETE FROM match_record WHERE id = 13903;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13369)),
        updated_at = NOW()
    WHERE id = 13369;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13717 
          AND p.match_id = 13376
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13376 WHERE match_id = 13717;
UPDATE odds_snapshot SET match_id = 13376 WHERE match_id = 13717;
UPDATE surebet_alert SET match_id = 13376 WHERE match_id = 13717;
UPDATE middle_alert SET match_id = 13376 WHERE match_id = 13717;
UPDATE valuebet_alert SET match_id = 13376 WHERE match_id = 13717;
DELETE FROM match_record WHERE id = 13717;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13376)),
        updated_at = NOW()
    WHERE id = 13376;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13953 
          AND p.match_id = 13411
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13411 WHERE match_id = 13953;
UPDATE odds_snapshot SET match_id = 13411 WHERE match_id = 13953;
UPDATE surebet_alert SET match_id = 13411 WHERE match_id = 13953;
UPDATE middle_alert SET match_id = 13411 WHERE match_id = 13953;
UPDATE valuebet_alert SET match_id = 13411 WHERE match_id = 13953;
DELETE FROM match_record WHERE id = 13953;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13411)),
        updated_at = NOW()
    WHERE id = 13411;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13739 
          AND p.match_id = 13429
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13429 WHERE match_id = 13739;
UPDATE odds_snapshot SET match_id = 13429 WHERE match_id = 13739;
UPDATE surebet_alert SET match_id = 13429 WHERE match_id = 13739;
UPDATE middle_alert SET match_id = 13429 WHERE match_id = 13739;
UPDATE valuebet_alert SET match_id = 13429 WHERE match_id = 13739;
DELETE FROM match_record WHERE id = 13739;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13429)),
        updated_at = NOW()
    WHERE id = 13429;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13738 
          AND p.match_id = 13430
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13430 WHERE match_id = 13738;
UPDATE odds_snapshot SET match_id = 13430 WHERE match_id = 13738;
UPDATE surebet_alert SET match_id = 13430 WHERE match_id = 13738;
UPDATE middle_alert SET match_id = 13430 WHERE match_id = 13738;
UPDATE valuebet_alert SET match_id = 13430 WHERE match_id = 13738;
DELETE FROM match_record WHERE id = 13738;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13430)),
        updated_at = NOW()
    WHERE id = 13430;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13909 
          AND p.match_id = 13431
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13431 WHERE match_id = 13909;
UPDATE odds_snapshot SET match_id = 13431 WHERE match_id = 13909;
UPDATE surebet_alert SET match_id = 13431 WHERE match_id = 13909;
UPDATE middle_alert SET match_id = 13431 WHERE match_id = 13909;
UPDATE valuebet_alert SET match_id = 13431 WHERE match_id = 13909;
DELETE FROM match_record WHERE id = 13909;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13431)),
        updated_at = NOW()
    WHERE id = 13431;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13737 
          AND p.match_id = 13432
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13432 WHERE match_id = 13737;
UPDATE odds_snapshot SET match_id = 13432 WHERE match_id = 13737;
UPDATE surebet_alert SET match_id = 13432 WHERE match_id = 13737;
UPDATE middle_alert SET match_id = 13432 WHERE match_id = 13737;
UPDATE valuebet_alert SET match_id = 13432 WHERE match_id = 13737;
DELETE FROM match_record WHERE id = 13737;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13432)),
        updated_at = NOW()
    WHERE id = 13432;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16338 
          AND p.match_id = 13869
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13869 WHERE match_id = 16338;
UPDATE odds_snapshot SET match_id = 13869 WHERE match_id = 16338;
UPDATE surebet_alert SET match_id = 13869 WHERE match_id = 16338;
UPDATE middle_alert SET match_id = 13869 WHERE match_id = 16338;
UPDATE valuebet_alert SET match_id = 13869 WHERE match_id = 16338;
DELETE FROM match_record WHERE id = 16338;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13869)),
        updated_at = NOW()
    WHERE id = 13869;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16334 
          AND p.match_id = 13872
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13872 WHERE match_id = 16334;
UPDATE odds_snapshot SET match_id = 13872 WHERE match_id = 16334;
UPDATE surebet_alert SET match_id = 13872 WHERE match_id = 16334;
UPDATE middle_alert SET match_id = 13872 WHERE match_id = 16334;
UPDATE valuebet_alert SET match_id = 13872 WHERE match_id = 16334;
DELETE FROM match_record WHERE id = 16334;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13872)),
        updated_at = NOW()
    WHERE id = 13872;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 15836 
          AND p.match_id = 14333
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14333 WHERE match_id = 15836;
UPDATE odds_snapshot SET match_id = 14333 WHERE match_id = 15836;
UPDATE surebet_alert SET match_id = 14333 WHERE match_id = 15836;
UPDATE middle_alert SET match_id = 14333 WHERE match_id = 15836;
UPDATE valuebet_alert SET match_id = 14333 WHERE match_id = 15836;
DELETE FROM match_record WHERE id = 15836;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14333)),
        updated_at = NOW()
    WHERE id = 14333;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16757 
          AND p.match_id = 14265
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14265 WHERE match_id = 16757;
UPDATE odds_snapshot SET match_id = 14265 WHERE match_id = 16757;
UPDATE surebet_alert SET match_id = 14265 WHERE match_id = 16757;
UPDATE middle_alert SET match_id = 14265 WHERE match_id = 16757;
UPDATE valuebet_alert SET match_id = 14265 WHERE match_id = 16757;
DELETE FROM match_record WHERE id = 16757;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14265)),
        updated_at = NOW()
    WHERE id = 14265;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16764 
          AND p.match_id = 14284
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14284 WHERE match_id = 16764;
UPDATE odds_snapshot SET match_id = 14284 WHERE match_id = 16764;
UPDATE surebet_alert SET match_id = 14284 WHERE match_id = 16764;
UPDATE middle_alert SET match_id = 14284 WHERE match_id = 16764;
UPDATE valuebet_alert SET match_id = 14284 WHERE match_id = 16764;
DELETE FROM match_record WHERE id = 16764;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14284)),
        updated_at = NOW()
    WHERE id = 14284;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16744 
          AND p.match_id = 14304
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14304 WHERE match_id = 16744;
UPDATE odds_snapshot SET match_id = 14304 WHERE match_id = 16744;
UPDATE surebet_alert SET match_id = 14304 WHERE match_id = 16744;
UPDATE middle_alert SET match_id = 14304 WHERE match_id = 16744;
UPDATE valuebet_alert SET match_id = 14304 WHERE match_id = 16744;
DELETE FROM match_record WHERE id = 16744;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14304)),
        updated_at = NOW()
    WHERE id = 14304;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 15838 
          AND p.match_id = 14329
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14329 WHERE match_id = 15838;
UPDATE odds_snapshot SET match_id = 14329 WHERE match_id = 15838;
UPDATE surebet_alert SET match_id = 14329 WHERE match_id = 15838;
UPDATE middle_alert SET match_id = 14329 WHERE match_id = 15838;
UPDATE valuebet_alert SET match_id = 14329 WHERE match_id = 15838;
DELETE FROM match_record WHERE id = 15838;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14329)),
        updated_at = NOW()
    WHERE id = 14329;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 16325 
          AND p.match_id = 14344
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14344 WHERE match_id = 16325;
UPDATE odds_snapshot SET match_id = 14344 WHERE match_id = 16325;
UPDATE surebet_alert SET match_id = 14344 WHERE match_id = 16325;
UPDATE middle_alert SET match_id = 14344 WHERE match_id = 16325;
UPDATE valuebet_alert SET match_id = 14344 WHERE match_id = 16325;
DELETE FROM match_record WHERE id = 16325;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14344)),
        updated_at = NOW()
    WHERE id = 14344;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13718 
          AND p.match_id = 6977
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6977 WHERE match_id = 13718;
UPDATE odds_snapshot SET match_id = 6977 WHERE match_id = 13718;
UPDATE surebet_alert SET match_id = 6977 WHERE match_id = 13718;
UPDATE middle_alert SET match_id = 6977 WHERE match_id = 13718;
UPDATE valuebet_alert SET match_id = 6977 WHERE match_id = 13718;
DELETE FROM match_record WHERE id = 13718;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13732 
          AND p.match_id = 6977
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6977 WHERE match_id = 13732;
UPDATE odds_snapshot SET match_id = 6977 WHERE match_id = 13732;
UPDATE surebet_alert SET match_id = 6977 WHERE match_id = 13732;
UPDATE middle_alert SET match_id = 6977 WHERE match_id = 13732;
UPDATE valuebet_alert SET match_id = 6977 WHERE match_id = 13732;
DELETE FROM match_record WHERE id = 13732;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6977)),
        updated_at = NOW()
    WHERE id = 6977;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 7222 
          AND p.match_id = 7300
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7300 WHERE match_id = 7222;
UPDATE odds_snapshot SET match_id = 7300 WHERE match_id = 7222;
UPDATE surebet_alert SET match_id = 7300 WHERE match_id = 7222;
UPDATE middle_alert SET match_id = 7300 WHERE match_id = 7222;
UPDATE valuebet_alert SET match_id = 7300 WHERE match_id = 7222;
DELETE FROM match_record WHERE id = 7222;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7300)),
        updated_at = NOW()
    WHERE id = 7300;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13679 
          AND p.match_id = 7514
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7514 WHERE match_id = 13679;
UPDATE odds_snapshot SET match_id = 7514 WHERE match_id = 13679;
UPDATE surebet_alert SET match_id = 7514 WHERE match_id = 13679;
UPDATE middle_alert SET match_id = 7514 WHERE match_id = 13679;
UPDATE valuebet_alert SET match_id = 7514 WHERE match_id = 13679;
DELETE FROM match_record WHERE id = 13679;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13685 
          AND p.match_id = 7514
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7514 WHERE match_id = 13685;
UPDATE odds_snapshot SET match_id = 7514 WHERE match_id = 13685;
UPDATE surebet_alert SET match_id = 7514 WHERE match_id = 13685;
UPDATE middle_alert SET match_id = 7514 WHERE match_id = 13685;
UPDATE valuebet_alert SET match_id = 7514 WHERE match_id = 13685;
DELETE FROM match_record WHERE id = 13685;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7514)),
        updated_at = NOW()
    WHERE id = 7514;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13688 
          AND p.match_id = 7714
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7714 WHERE match_id = 13688;
UPDATE odds_snapshot SET match_id = 7714 WHERE match_id = 13688;
UPDATE surebet_alert SET match_id = 7714 WHERE match_id = 13688;
UPDATE middle_alert SET match_id = 7714 WHERE match_id = 13688;
UPDATE valuebet_alert SET match_id = 7714 WHERE match_id = 13688;
DELETE FROM match_record WHERE id = 13688;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7714)),
        updated_at = NOW()
    WHERE id = 7714;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14393 
          AND p.match_id = 7190
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7190 WHERE match_id = 14393;
UPDATE odds_snapshot SET match_id = 7190 WHERE match_id = 14393;
UPDATE surebet_alert SET match_id = 7190 WHERE match_id = 14393;
UPDATE middle_alert SET match_id = 7190 WHERE match_id = 14393;
UPDATE valuebet_alert SET match_id = 7190 WHERE match_id = 14393;
DELETE FROM match_record WHERE id = 14393;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7190)),
        updated_at = NOW()
    WHERE id = 7190;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13712 
          AND p.match_id = 6245
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6245 WHERE match_id = 13712;
UPDATE odds_snapshot SET match_id = 6245 WHERE match_id = 13712;
UPDATE surebet_alert SET match_id = 6245 WHERE match_id = 13712;
UPDATE middle_alert SET match_id = 6245 WHERE match_id = 13712;
UPDATE valuebet_alert SET match_id = 6245 WHERE match_id = 13712;
DELETE FROM match_record WHERE id = 13712;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6245)),
        updated_at = NOW()
    WHERE id = 6245;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14395 
          AND p.match_id = 13239
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13239 WHERE match_id = 14395;
UPDATE odds_snapshot SET match_id = 13239 WHERE match_id = 14395;
UPDATE surebet_alert SET match_id = 13239 WHERE match_id = 14395;
UPDATE middle_alert SET match_id = 13239 WHERE match_id = 14395;
UPDATE valuebet_alert SET match_id = 13239 WHERE match_id = 14395;
DELETE FROM match_record WHERE id = 14395;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13239)),
        updated_at = NOW()
    WHERE id = 13239;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14399 
          AND p.match_id = 13241
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13241 WHERE match_id = 14399;
UPDATE odds_snapshot SET match_id = 13241 WHERE match_id = 14399;
UPDATE surebet_alert SET match_id = 13241 WHERE match_id = 14399;
UPDATE middle_alert SET match_id = 13241 WHERE match_id = 14399;
UPDATE valuebet_alert SET match_id = 13241 WHERE match_id = 14399;
DELETE FROM match_record WHERE id = 14399;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13241)),
        updated_at = NOW()
    WHERE id = 13241;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13683 
          AND p.match_id = 7892
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7892 WHERE match_id = 13683;
UPDATE odds_snapshot SET match_id = 7892 WHERE match_id = 13683;
UPDATE surebet_alert SET match_id = 7892 WHERE match_id = 13683;
UPDATE middle_alert SET match_id = 7892 WHERE match_id = 13683;
UPDATE valuebet_alert SET match_id = 7892 WHERE match_id = 13683;
DELETE FROM match_record WHERE id = 13683;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13693 
          AND p.match_id = 7892
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7892 WHERE match_id = 13693;
UPDATE odds_snapshot SET match_id = 7892 WHERE match_id = 13693;
UPDATE surebet_alert SET match_id = 7892 WHERE match_id = 13693;
UPDATE middle_alert SET match_id = 7892 WHERE match_id = 13693;
UPDATE valuebet_alert SET match_id = 7892 WHERE match_id = 13693;
DELETE FROM match_record WHERE id = 13693;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7892)),
        updated_at = NOW()
    WHERE id = 7892;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14396 
          AND p.match_id = 13251
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13251 WHERE match_id = 14396;
UPDATE odds_snapshot SET match_id = 13251 WHERE match_id = 14396;
UPDATE surebet_alert SET match_id = 13251 WHERE match_id = 14396;
UPDATE middle_alert SET match_id = 13251 WHERE match_id = 14396;
UPDATE valuebet_alert SET match_id = 13251 WHERE match_id = 14396;
DELETE FROM match_record WHERE id = 14396;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13251)),
        updated_at = NOW()
    WHERE id = 13251;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13750 
          AND p.match_id = 5793
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 5793 WHERE match_id = 13750;
UPDATE odds_snapshot SET match_id = 5793 WHERE match_id = 13750;
UPDATE surebet_alert SET match_id = 5793 WHERE match_id = 13750;
UPDATE middle_alert SET match_id = 5793 WHERE match_id = 13750;
UPDATE valuebet_alert SET match_id = 5793 WHERE match_id = 13750;
DELETE FROM match_record WHERE id = 13750;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13763 
          AND p.match_id = 5793
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 5793 WHERE match_id = 13763;
UPDATE odds_snapshot SET match_id = 5793 WHERE match_id = 13763;
UPDATE surebet_alert SET match_id = 5793 WHERE match_id = 13763;
UPDATE middle_alert SET match_id = 5793 WHERE match_id = 13763;
UPDATE valuebet_alert SET match_id = 5793 WHERE match_id = 13763;
DELETE FROM match_record WHERE id = 13763;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 5793)),
        updated_at = NOW()
    WHERE id = 5793;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13753 
          AND p.match_id = 13257
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13257 WHERE match_id = 13753;
UPDATE odds_snapshot SET match_id = 13257 WHERE match_id = 13753;
UPDATE surebet_alert SET match_id = 13257 WHERE match_id = 13753;
UPDATE middle_alert SET match_id = 13257 WHERE match_id = 13753;
UPDATE valuebet_alert SET match_id = 13257 WHERE match_id = 13753;
DELETE FROM match_record WHERE id = 13753;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13766 
          AND p.match_id = 13257
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13257 WHERE match_id = 13766;
UPDATE odds_snapshot SET match_id = 13257 WHERE match_id = 13766;
UPDATE surebet_alert SET match_id = 13257 WHERE match_id = 13766;
UPDATE middle_alert SET match_id = 13257 WHERE match_id = 13766;
UPDATE valuebet_alert SET match_id = 13257 WHERE match_id = 13766;
DELETE FROM match_record WHERE id = 13766;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13257)),
        updated_at = NOW()
    WHERE id = 13257;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14494 
          AND p.match_id = 13338
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13338 WHERE match_id = 14494;
UPDATE odds_snapshot SET match_id = 13338 WHERE match_id = 14494;
UPDATE surebet_alert SET match_id = 13338 WHERE match_id = 14494;
UPDATE middle_alert SET match_id = 13338 WHERE match_id = 14494;
UPDATE valuebet_alert SET match_id = 13338 WHERE match_id = 14494;
DELETE FROM match_record WHERE id = 14494;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13338)),
        updated_at = NOW()
    WHERE id = 13338;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14489 
          AND p.match_id = 13340
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13340 WHERE match_id = 14489;
UPDATE odds_snapshot SET match_id = 13340 WHERE match_id = 14489;
UPDATE surebet_alert SET match_id = 13340 WHERE match_id = 14489;
UPDATE middle_alert SET match_id = 13340 WHERE match_id = 14489;
UPDATE valuebet_alert SET match_id = 13340 WHERE match_id = 14489;
DELETE FROM match_record WHERE id = 14489;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13340)),
        updated_at = NOW()
    WHERE id = 13340;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14499 
          AND p.match_id = 13341
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13341 WHERE match_id = 14499;
UPDATE odds_snapshot SET match_id = 13341 WHERE match_id = 14499;
UPDATE surebet_alert SET match_id = 13341 WHERE match_id = 14499;
UPDATE middle_alert SET match_id = 13341 WHERE match_id = 14499;
UPDATE valuebet_alert SET match_id = 13341 WHERE match_id = 14499;
DELETE FROM match_record WHERE id = 14499;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13341)),
        updated_at = NOW()
    WHERE id = 13341;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13571 
          AND p.match_id = 13422
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13422 WHERE match_id = 13571;
UPDATE odds_snapshot SET match_id = 13422 WHERE match_id = 13571;
UPDATE surebet_alert SET match_id = 13422 WHERE match_id = 13571;
UPDATE middle_alert SET match_id = 13422 WHERE match_id = 13571;
UPDATE valuebet_alert SET match_id = 13422 WHERE match_id = 13571;
DELETE FROM match_record WHERE id = 13571;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13422)),
        updated_at = NOW()
    WHERE id = 13422;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14574 
          AND p.match_id = 6692
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6692 WHERE match_id = 14574;
UPDATE odds_snapshot SET match_id = 6692 WHERE match_id = 14574;
UPDATE surebet_alert SET match_id = 6692 WHERE match_id = 14574;
UPDATE middle_alert SET match_id = 6692 WHERE match_id = 14574;
UPDATE valuebet_alert SET match_id = 6692 WHERE match_id = 14574;
DELETE FROM match_record WHERE id = 14574;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6692)),
        updated_at = NOW()
    WHERE id = 6692;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14577 
          AND p.match_id = 13421
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13421 WHERE match_id = 14577;
UPDATE odds_snapshot SET match_id = 13421 WHERE match_id = 14577;
UPDATE surebet_alert SET match_id = 13421 WHERE match_id = 14577;
UPDATE middle_alert SET match_id = 13421 WHERE match_id = 14577;
UPDATE valuebet_alert SET match_id = 13421 WHERE match_id = 14577;
DELETE FROM match_record WHERE id = 14577;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13421)),
        updated_at = NOW()
    WHERE id = 13421;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14573 
          AND p.match_id = 13425
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13425 WHERE match_id = 14573;
UPDATE odds_snapshot SET match_id = 13425 WHERE match_id = 14573;
UPDATE surebet_alert SET match_id = 13425 WHERE match_id = 14573;
UPDATE middle_alert SET match_id = 13425 WHERE match_id = 14573;
UPDATE valuebet_alert SET match_id = 13425 WHERE match_id = 14573;
DELETE FROM match_record WHERE id = 14573;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13425)),
        updated_at = NOW()
    WHERE id = 13425;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13562 
          AND p.match_id = 13428
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13428 WHERE match_id = 13562;
UPDATE odds_snapshot SET match_id = 13428 WHERE match_id = 13562;
UPDATE surebet_alert SET match_id = 13428 WHERE match_id = 13562;
UPDATE middle_alert SET match_id = 13428 WHERE match_id = 13562;
UPDATE valuebet_alert SET match_id = 13428 WHERE match_id = 13562;
DELETE FROM match_record WHERE id = 13562;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13428)),
        updated_at = NOW()
    WHERE id = 13428;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13741 
          AND p.match_id = 13435
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13435 WHERE match_id = 13741;
UPDATE odds_snapshot SET match_id = 13435 WHERE match_id = 13741;
UPDATE surebet_alert SET match_id = 13435 WHERE match_id = 13741;
UPDATE middle_alert SET match_id = 13435 WHERE match_id = 13741;
UPDATE valuebet_alert SET match_id = 13435 WHERE match_id = 13741;
DELETE FROM match_record WHERE id = 13741;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13435)),
        updated_at = NOW()
    WHERE id = 13435;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 1709 
          AND p.match_id = 9488
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 9488 WHERE match_id = 1709;
UPDATE odds_snapshot SET match_id = 9488 WHERE match_id = 1709;
UPDATE surebet_alert SET match_id = 9488 WHERE match_id = 1709;
UPDATE middle_alert SET match_id = 9488 WHERE match_id = 1709;
UPDATE valuebet_alert SET match_id = 9488 WHERE match_id = 1709;
DELETE FROM match_record WHERE id = 1709;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 9488)),
        updated_at = NOW()
    WHERE id = 9488;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13567 
          AND p.match_id = 7052
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7052 WHERE match_id = 13567;
UPDATE odds_snapshot SET match_id = 7052 WHERE match_id = 13567;
UPDATE surebet_alert SET match_id = 7052 WHERE match_id = 13567;
UPDATE middle_alert SET match_id = 7052 WHERE match_id = 13567;
UPDATE valuebet_alert SET match_id = 7052 WHERE match_id = 13567;
DELETE FROM match_record WHERE id = 13567;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13572 
          AND p.match_id = 7052
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7052 WHERE match_id = 13572;
UPDATE odds_snapshot SET match_id = 7052 WHERE match_id = 13572;
UPDATE surebet_alert SET match_id = 7052 WHERE match_id = 13572;
UPDATE middle_alert SET match_id = 7052 WHERE match_id = 13572;
UPDATE valuebet_alert SET match_id = 7052 WHERE match_id = 13572;
DELETE FROM match_record WHERE id = 13572;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7052)),
        updated_at = NOW()
    WHERE id = 7052;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13564 
          AND p.match_id = 7524
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7524 WHERE match_id = 13564;
UPDATE odds_snapshot SET match_id = 7524 WHERE match_id = 13564;
UPDATE surebet_alert SET match_id = 7524 WHERE match_id = 13564;
UPDATE middle_alert SET match_id = 7524 WHERE match_id = 13564;
UPDATE valuebet_alert SET match_id = 7524 WHERE match_id = 13564;
DELETE FROM match_record WHERE id = 13564;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13565 
          AND p.match_id = 7524
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7524 WHERE match_id = 13565;
UPDATE odds_snapshot SET match_id = 7524 WHERE match_id = 13565;
UPDATE surebet_alert SET match_id = 7524 WHERE match_id = 13565;
UPDATE middle_alert SET match_id = 7524 WHERE match_id = 13565;
UPDATE valuebet_alert SET match_id = 7524 WHERE match_id = 13565;
DELETE FROM match_record WHERE id = 13565;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7524)),
        updated_at = NOW()
    WHERE id = 7524;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13559 
          AND p.match_id = 7853
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7853 WHERE match_id = 13559;
UPDATE odds_snapshot SET match_id = 7853 WHERE match_id = 13559;
UPDATE surebet_alert SET match_id = 7853 WHERE match_id = 13559;
UPDATE middle_alert SET match_id = 7853 WHERE match_id = 13559;
UPDATE valuebet_alert SET match_id = 7853 WHERE match_id = 13559;
DELETE FROM match_record WHERE id = 13559;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7853)),
        updated_at = NOW()
    WHERE id = 7853;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14579 
          AND p.match_id = 9343
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 9343 WHERE match_id = 14579;
UPDATE odds_snapshot SET match_id = 9343 WHERE match_id = 14579;
UPDATE surebet_alert SET match_id = 9343 WHERE match_id = 14579;
UPDATE middle_alert SET match_id = 9343 WHERE match_id = 14579;
UPDATE valuebet_alert SET match_id = 9343 WHERE match_id = 14579;
DELETE FROM match_record WHERE id = 14579;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 9343)),
        updated_at = NOW()
    WHERE id = 9343;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13652 
          AND p.match_id = 12837
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 12837 WHERE match_id = 13652;
UPDATE odds_snapshot SET match_id = 12837 WHERE match_id = 13652;
UPDATE surebet_alert SET match_id = 12837 WHERE match_id = 13652;
UPDATE middle_alert SET match_id = 12837 WHERE match_id = 13652;
UPDATE valuebet_alert SET match_id = 12837 WHERE match_id = 13652;
DELETE FROM match_record WHERE id = 13652;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 12837)),
        updated_at = NOW()
    WHERE id = 12837;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13780 
          AND p.match_id = 7393
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7393 WHERE match_id = 13780;
UPDATE odds_snapshot SET match_id = 7393 WHERE match_id = 13780;
UPDATE surebet_alert SET match_id = 7393 WHERE match_id = 13780;
UPDATE middle_alert SET match_id = 7393 WHERE match_id = 13780;
UPDATE valuebet_alert SET match_id = 7393 WHERE match_id = 13780;
DELETE FROM match_record WHERE id = 13780;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7393)),
        updated_at = NOW()
    WHERE id = 7393;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13709 
          AND p.match_id = 7855
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7855 WHERE match_id = 13709;
UPDATE odds_snapshot SET match_id = 7855 WHERE match_id = 13709;
UPDATE surebet_alert SET match_id = 7855 WHERE match_id = 13709;
UPDATE middle_alert SET match_id = 7855 WHERE match_id = 13709;
UPDATE valuebet_alert SET match_id = 7855 WHERE match_id = 13709;
DELETE FROM match_record WHERE id = 13709;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7855)),
        updated_at = NOW()
    WHERE id = 7855;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13681 
          AND p.match_id = 11569
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 11569 WHERE match_id = 13681;
UPDATE odds_snapshot SET match_id = 11569 WHERE match_id = 13681;
UPDATE surebet_alert SET match_id = 11569 WHERE match_id = 13681;
UPDATE middle_alert SET match_id = 11569 WHERE match_id = 13681;
UPDATE valuebet_alert SET match_id = 11569 WHERE match_id = 13681;
DELETE FROM match_record WHERE id = 13681;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 11569)),
        updated_at = NOW()
    WHERE id = 11569;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13716 
          AND p.match_id = 6249
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6249 WHERE match_id = 13716;
UPDATE odds_snapshot SET match_id = 6249 WHERE match_id = 13716;
UPDATE surebet_alert SET match_id = 6249 WHERE match_id = 13716;
UPDATE middle_alert SET match_id = 6249 WHERE match_id = 13716;
UPDATE valuebet_alert SET match_id = 6249 WHERE match_id = 13716;
DELETE FROM match_record WHERE id = 13716;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6249)),
        updated_at = NOW()
    WHERE id = 6249;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14493 
          AND p.match_id = 6365
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6365 WHERE match_id = 14493;
UPDATE odds_snapshot SET match_id = 6365 WHERE match_id = 14493;
UPDATE surebet_alert SET match_id = 6365 WHERE match_id = 14493;
UPDATE middle_alert SET match_id = 6365 WHERE match_id = 14493;
UPDATE valuebet_alert SET match_id = 6365 WHERE match_id = 14493;
DELETE FROM match_record WHERE id = 14493;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6365)),
        updated_at = NOW()
    WHERE id = 6365;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13735 
          AND p.match_id = 8013
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8013 WHERE match_id = 13735;
UPDATE odds_snapshot SET match_id = 8013 WHERE match_id = 13735;
UPDATE surebet_alert SET match_id = 8013 WHERE match_id = 13735;
UPDATE middle_alert SET match_id = 8013 WHERE match_id = 13735;
UPDATE valuebet_alert SET match_id = 8013 WHERE match_id = 13735;
DELETE FROM match_record WHERE id = 13735;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8013)),
        updated_at = NOW()
    WHERE id = 8013;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13782 
          AND p.match_id = 7663
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7663 WHERE match_id = 13782;
UPDATE odds_snapshot SET match_id = 7663 WHERE match_id = 13782;
UPDATE surebet_alert SET match_id = 7663 WHERE match_id = 13782;
UPDATE middle_alert SET match_id = 7663 WHERE match_id = 13782;
UPDATE valuebet_alert SET match_id = 7663 WHERE match_id = 13782;
DELETE FROM match_record WHERE id = 13782;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13787 
          AND p.match_id = 7663
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7663 WHERE match_id = 13787;
UPDATE odds_snapshot SET match_id = 7663 WHERE match_id = 13787;
UPDATE surebet_alert SET match_id = 7663 WHERE match_id = 13787;
UPDATE middle_alert SET match_id = 7663 WHERE match_id = 13787;
UPDATE valuebet_alert SET match_id = 7663 WHERE match_id = 13787;
DELETE FROM match_record WHERE id = 13787;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7663)),
        updated_at = NOW()
    WHERE id = 7663;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 7677 
          AND p.match_id = 13828
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13828 WHERE match_id = 7677;
UPDATE odds_snapshot SET match_id = 13828 WHERE match_id = 7677;
UPDATE surebet_alert SET match_id = 13828 WHERE match_id = 7677;
UPDATE middle_alert SET match_id = 13828 WHERE match_id = 7677;
UPDATE valuebet_alert SET match_id = 13828 WHERE match_id = 7677;
DELETE FROM match_record WHERE id = 7677;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13828)),
        updated_at = NOW()
    WHERE id = 13828;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 13830 
          AND p.match_id = 8336
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 8336 WHERE match_id = 13830;
UPDATE odds_snapshot SET match_id = 8336 WHERE match_id = 13830;
UPDATE surebet_alert SET match_id = 8336 WHERE match_id = 13830;
UPDATE middle_alert SET match_id = 8336 WHERE match_id = 13830;
UPDATE valuebet_alert SET match_id = 8336 WHERE match_id = 13830;
DELETE FROM match_record WHERE id = 13830;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 8336)),
        updated_at = NOW()
    WHERE id = 8336;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14462 
          AND p.match_id = 14457
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14457 WHERE match_id = 14462;
UPDATE odds_snapshot SET match_id = 14457 WHERE match_id = 14462;
UPDATE surebet_alert SET match_id = 14457 WHERE match_id = 14462;
UPDATE middle_alert SET match_id = 14457 WHERE match_id = 14462;
UPDATE valuebet_alert SET match_id = 14457 WHERE match_id = 14462;
DELETE FROM match_record WHERE id = 14462;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14457)),
        updated_at = NOW()
    WHERE id = 14457;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14468 
          AND p.match_id = 14467
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 14467 WHERE match_id = 14468;
UPDATE odds_snapshot SET match_id = 14467 WHERE match_id = 14468;
UPDATE surebet_alert SET match_id = 14467 WHERE match_id = 14468;
UPDATE middle_alert SET match_id = 14467 WHERE match_id = 14468;
UPDATE valuebet_alert SET match_id = 14467 WHERE match_id = 14468;
DELETE FROM match_record WHERE id = 14468;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14467)),
        updated_at = NOW()
    WHERE id = 14467;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14581 
          AND p.match_id = 7425
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 7425 WHERE match_id = 14581;
UPDATE odds_snapshot SET match_id = 7425 WHERE match_id = 14581;
UPDATE surebet_alert SET match_id = 7425 WHERE match_id = 14581;
UPDATE middle_alert SET match_id = 7425 WHERE match_id = 14581;
UPDATE valuebet_alert SET match_id = 7425 WHERE match_id = 14581;
DELETE FROM match_record WHERE id = 14581;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 7425)),
        updated_at = NOW()
    WHERE id = 7425;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14575 
          AND p.match_id = 6814
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 6814 WHERE match_id = 14575;
UPDATE odds_snapshot SET match_id = 6814 WHERE match_id = 14575;
UPDATE surebet_alert SET match_id = 6814 WHERE match_id = 14575;
UPDATE middle_alert SET match_id = 6814 WHERE match_id = 14575;
UPDATE valuebet_alert SET match_id = 6814 WHERE match_id = 14575;
DELETE FROM match_record WHERE id = 14575;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 6814)),
        updated_at = NOW()
    WHERE id = 6814;
    

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14718 
          AND p.match_id = 13518
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13518 WHERE match_id = 14718;
UPDATE odds_snapshot SET match_id = 13518 WHERE match_id = 14718;
UPDATE surebet_alert SET match_id = 13518 WHERE match_id = 14718;
UPDATE middle_alert SET match_id = 13518 WHERE match_id = 14718;
UPDATE valuebet_alert SET match_id = 13518 WHERE match_id = 14718;
DELETE FROM match_record WHERE id = 14718;

        DELETE FROM odds_actual d
        USING odds_actual p
        WHERE d.match_id = 14722 
          AND p.match_id = 13518
          AND d.bet_source_id = p.bet_source_id
          AND d.odds_type_id = p.odds_type_id
          AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
        
UPDATE odds_actual SET match_id = 13518 WHERE match_id = 14722;
UPDATE odds_snapshot SET match_id = 13518 WHERE match_id = 14722;
UPDATE surebet_alert SET match_id = 13518 WHERE match_id = 14722;
UPDATE middle_alert SET match_id = 13518 WHERE match_id = 14722;
UPDATE valuebet_alert SET match_id = 13518 WHERE match_id = 14722;
DELETE FROM match_record WHERE id = 14722;

    UPDATE match_record 
    SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 13518)),
        updated_at = NOW()
    WHERE id = 13518;
    
COMMIT;