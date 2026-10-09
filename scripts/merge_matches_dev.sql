BEGIN;
-- Temporary table for conflict-free odds merging

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 61658 
              AND p.match_id = 60354
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 60354 WHERE match_id = 61658;
UPDATE odds_snapshot SET match_id = 60354 WHERE match_id = 61658;
UPDATE surebet_alert SET match_id = 60354 WHERE match_id = 61658;
UPDATE middle_alert SET match_id = 60354 WHERE match_id = 61658;
UPDATE valuebet_alert SET match_id = 60354 WHERE match_id = 61658;
DELETE FROM match_record WHERE id = 61658;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 60354)),
            updated_at = NOW()
        WHERE id = 60354;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 40264 
              AND p.match_id = 41030
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 41030 WHERE match_id = 40264;
UPDATE odds_snapshot SET match_id = 41030 WHERE match_id = 40264;
UPDATE surebet_alert SET match_id = 41030 WHERE match_id = 40264;
UPDATE middle_alert SET match_id = 41030 WHERE match_id = 40264;
UPDATE valuebet_alert SET match_id = 41030 WHERE match_id = 40264;
DELETE FROM match_record WHERE id = 40264;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 41030)),
            updated_at = NOW()
        WHERE id = 41030;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 40125 
              AND p.match_id = 33562
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 33562 WHERE match_id = 40125;
UPDATE odds_snapshot SET match_id = 33562 WHERE match_id = 40125;
UPDATE surebet_alert SET match_id = 33562 WHERE match_id = 40125;
UPDATE middle_alert SET match_id = 33562 WHERE match_id = 40125;
UPDATE valuebet_alert SET match_id = 33562 WHERE match_id = 40125;
DELETE FROM match_record WHERE id = 40125;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 33562)),
            updated_at = NOW()
        WHERE id = 33562;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 40356 
              AND p.match_id = 416
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 416 WHERE match_id = 40356;
UPDATE odds_snapshot SET match_id = 416 WHERE match_id = 40356;
UPDATE surebet_alert SET match_id = 416 WHERE match_id = 40356;
UPDATE middle_alert SET match_id = 416 WHERE match_id = 40356;
UPDATE valuebet_alert SET match_id = 416 WHERE match_id = 40356;
DELETE FROM match_record WHERE id = 40356;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 416)),
            updated_at = NOW()
        WHERE id = 416;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 66542 
              AND p.match_id = 66479
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 66479 WHERE match_id = 66542;
UPDATE odds_snapshot SET match_id = 66479 WHERE match_id = 66542;
UPDATE surebet_alert SET match_id = 66479 WHERE match_id = 66542;
UPDATE middle_alert SET match_id = 66479 WHERE match_id = 66542;
UPDATE valuebet_alert SET match_id = 66479 WHERE match_id = 66542;
DELETE FROM match_record WHERE id = 66542;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 66479)),
            updated_at = NOW()
        WHERE id = 66479;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 56553 
              AND p.match_id = 56521
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 56521 WHERE match_id = 56553;
UPDATE odds_snapshot SET match_id = 56521 WHERE match_id = 56553;
UPDATE surebet_alert SET match_id = 56521 WHERE match_id = 56553;
UPDATE middle_alert SET match_id = 56521 WHERE match_id = 56553;
UPDATE valuebet_alert SET match_id = 56521 WHERE match_id = 56553;
DELETE FROM match_record WHERE id = 56553;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 56521)),
            updated_at = NOW()
        WHERE id = 56521;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 63455 
              AND p.match_id = 63434
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 63434 WHERE match_id = 63455;
UPDATE odds_snapshot SET match_id = 63434 WHERE match_id = 63455;
UPDATE surebet_alert SET match_id = 63434 WHERE match_id = 63455;
UPDATE middle_alert SET match_id = 63434 WHERE match_id = 63455;
UPDATE valuebet_alert SET match_id = 63434 WHERE match_id = 63455;
DELETE FROM match_record WHERE id = 63455;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 63434)),
            updated_at = NOW()
        WHERE id = 63434;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 60250 
              AND p.match_id = 60041
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 60041 WHERE match_id = 60250;
UPDATE odds_snapshot SET match_id = 60041 WHERE match_id = 60250;
UPDATE surebet_alert SET match_id = 60041 WHERE match_id = 60250;
UPDATE middle_alert SET match_id = 60041 WHERE match_id = 60250;
UPDATE valuebet_alert SET match_id = 60041 WHERE match_id = 60250;
DELETE FROM match_record WHERE id = 60250;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 60041)),
            updated_at = NOW()
        WHERE id = 60041;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59593 
              AND p.match_id = 59317
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 59317 WHERE match_id = 59593;
UPDATE odds_snapshot SET match_id = 59317 WHERE match_id = 59593;
UPDATE surebet_alert SET match_id = 59317 WHERE match_id = 59593;
UPDATE middle_alert SET match_id = 59317 WHERE match_id = 59593;
UPDATE valuebet_alert SET match_id = 59317 WHERE match_id = 59593;
DELETE FROM match_record WHERE id = 59593;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 59317)),
            updated_at = NOW()
        WHERE id = 59317;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 48542 
              AND p.match_id = 66451
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 66451 WHERE match_id = 48542;
UPDATE odds_snapshot SET match_id = 66451 WHERE match_id = 48542;
UPDATE surebet_alert SET match_id = 66451 WHERE match_id = 48542;
UPDATE middle_alert SET match_id = 66451 WHERE match_id = 48542;
UPDATE valuebet_alert SET match_id = 66451 WHERE match_id = 48542;
DELETE FROM match_record WHERE id = 48542;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 66451)),
            updated_at = NOW()
        WHERE id = 66451;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59828 
              AND p.match_id = 59785
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 59785 WHERE match_id = 59828;
UPDATE odds_snapshot SET match_id = 59785 WHERE match_id = 59828;
UPDATE surebet_alert SET match_id = 59785 WHERE match_id = 59828;
UPDATE middle_alert SET match_id = 59785 WHERE match_id = 59828;
UPDATE valuebet_alert SET match_id = 59785 WHERE match_id = 59828;
DELETE FROM match_record WHERE id = 59828;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 59785)),
            updated_at = NOW()
        WHERE id = 59785;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59891 
              AND p.match_id = 59773
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 59773 WHERE match_id = 59891;
UPDATE odds_snapshot SET match_id = 59773 WHERE match_id = 59891;
UPDATE surebet_alert SET match_id = 59773 WHERE match_id = 59891;
UPDATE middle_alert SET match_id = 59773 WHERE match_id = 59891;
UPDATE valuebet_alert SET match_id = 59773 WHERE match_id = 59891;
DELETE FROM match_record WHERE id = 59891;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 59773)),
            updated_at = NOW()
        WHERE id = 59773;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 78411 
              AND p.match_id = 78383
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 78383 WHERE match_id = 78411;
UPDATE odds_snapshot SET match_id = 78383 WHERE match_id = 78411;
UPDATE surebet_alert SET match_id = 78383 WHERE match_id = 78411;
UPDATE middle_alert SET match_id = 78383 WHERE match_id = 78411;
UPDATE valuebet_alert SET match_id = 78383 WHERE match_id = 78411;
DELETE FROM match_record WHERE id = 78411;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 78383)),
            updated_at = NOW()
        WHERE id = 78383;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70578 
              AND p.match_id = 70576
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 70576 WHERE match_id = 70578;
UPDATE odds_snapshot SET match_id = 70576 WHERE match_id = 70578;
UPDATE surebet_alert SET match_id = 70576 WHERE match_id = 70578;
UPDATE middle_alert SET match_id = 70576 WHERE match_id = 70578;
UPDATE valuebet_alert SET match_id = 70576 WHERE match_id = 70578;
DELETE FROM match_record WHERE id = 70578;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 70576)),
            updated_at = NOW()
        WHERE id = 70576;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59360 
              AND p.match_id = 60252
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 60252 WHERE match_id = 59360;
UPDATE odds_snapshot SET match_id = 60252 WHERE match_id = 59360;
UPDATE surebet_alert SET match_id = 60252 WHERE match_id = 59360;
UPDATE middle_alert SET match_id = 60252 WHERE match_id = 59360;
UPDATE valuebet_alert SET match_id = 60252 WHERE match_id = 59360;
DELETE FROM match_record WHERE id = 59360;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 60252)),
            updated_at = NOW()
        WHERE id = 60252;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 69144 
              AND p.match_id = 68836
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 68836 WHERE match_id = 69144;
UPDATE odds_snapshot SET match_id = 68836 WHERE match_id = 69144;
UPDATE surebet_alert SET match_id = 68836 WHERE match_id = 69144;
UPDATE middle_alert SET match_id = 68836 WHERE match_id = 69144;
UPDATE valuebet_alert SET match_id = 68836 WHERE match_id = 69144;
DELETE FROM match_record WHERE id = 69144;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 68836)),
            updated_at = NOW()
        WHERE id = 68836;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 56989 
              AND p.match_id = 56666
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 56666 WHERE match_id = 56989;
UPDATE odds_snapshot SET match_id = 56666 WHERE match_id = 56989;
UPDATE surebet_alert SET match_id = 56666 WHERE match_id = 56989;
UPDATE middle_alert SET match_id = 56666 WHERE match_id = 56989;
UPDATE valuebet_alert SET match_id = 56666 WHERE match_id = 56989;
DELETE FROM match_record WHERE id = 56989;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 56666)),
            updated_at = NOW()
        WHERE id = 56666;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 69605 
              AND p.match_id = 69228
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69228 WHERE match_id = 69605;
UPDATE odds_snapshot SET match_id = 69228 WHERE match_id = 69605;
UPDATE surebet_alert SET match_id = 69228 WHERE match_id = 69605;
UPDATE middle_alert SET match_id = 69228 WHERE match_id = 69605;
UPDATE valuebet_alert SET match_id = 69228 WHERE match_id = 69605;
DELETE FROM match_record WHERE id = 69605;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69228)),
            updated_at = NOW()
        WHERE id = 69228;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 56988 
              AND p.match_id = 56664
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 56664 WHERE match_id = 56988;
UPDATE odds_snapshot SET match_id = 56664 WHERE match_id = 56988;
UPDATE surebet_alert SET match_id = 56664 WHERE match_id = 56988;
UPDATE middle_alert SET match_id = 56664 WHERE match_id = 56988;
UPDATE valuebet_alert SET match_id = 56664 WHERE match_id = 56988;
DELETE FROM match_record WHERE id = 56988;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 56664)),
            updated_at = NOW()
        WHERE id = 56664;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 56552 
              AND p.match_id = 56520
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 56520 WHERE match_id = 56552;
UPDATE odds_snapshot SET match_id = 56520 WHERE match_id = 56552;
UPDATE surebet_alert SET match_id = 56520 WHERE match_id = 56552;
UPDATE middle_alert SET match_id = 56520 WHERE match_id = 56552;
UPDATE valuebet_alert SET match_id = 56520 WHERE match_id = 56552;
DELETE FROM match_record WHERE id = 56552;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 56520)),
            updated_at = NOW()
        WHERE id = 56520;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 57568 
              AND p.match_id = 56665
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 56665 WHERE match_id = 57568;
UPDATE odds_snapshot SET match_id = 56665 WHERE match_id = 57568;
UPDATE surebet_alert SET match_id = 56665 WHERE match_id = 57568;
UPDATE middle_alert SET match_id = 56665 WHERE match_id = 57568;
UPDATE valuebet_alert SET match_id = 56665 WHERE match_id = 57568;
DELETE FROM match_record WHERE id = 57568;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 56665)),
            updated_at = NOW()
        WHERE id = 56665;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 57109 
              AND p.match_id = 56803
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 56803 WHERE match_id = 57109;
UPDATE odds_snapshot SET match_id = 56803 WHERE match_id = 57109;
UPDATE surebet_alert SET match_id = 56803 WHERE match_id = 57109;
UPDATE middle_alert SET match_id = 56803 WHERE match_id = 57109;
UPDATE valuebet_alert SET match_id = 56803 WHERE match_id = 57109;
DELETE FROM match_record WHERE id = 57109;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 56803)),
            updated_at = NOW()
        WHERE id = 56803;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70545 
              AND p.match_id = 69956
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69956 WHERE match_id = 70545;
UPDATE odds_snapshot SET match_id = 69956 WHERE match_id = 70545;
UPDATE surebet_alert SET match_id = 69956 WHERE match_id = 70545;
UPDATE middle_alert SET match_id = 69956 WHERE match_id = 70545;
UPDATE valuebet_alert SET match_id = 69956 WHERE match_id = 70545;
DELETE FROM match_record WHERE id = 70545;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69956)),
            updated_at = NOW()
        WHERE id = 69956;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 69143 
              AND p.match_id = 68832
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 68832 WHERE match_id = 69143;
UPDATE odds_snapshot SET match_id = 68832 WHERE match_id = 69143;
UPDATE surebet_alert SET match_id = 68832 WHERE match_id = 69143;
UPDATE middle_alert SET match_id = 68832 WHERE match_id = 69143;
UPDATE valuebet_alert SET match_id = 68832 WHERE match_id = 69143;
DELETE FROM match_record WHERE id = 69143;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 68832)),
            updated_at = NOW()
        WHERE id = 68832;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 28926 
              AND p.match_id = 418
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 418 WHERE match_id = 28926;
UPDATE odds_snapshot SET match_id = 418 WHERE match_id = 28926;
UPDATE surebet_alert SET match_id = 418 WHERE match_id = 28926;
UPDATE middle_alert SET match_id = 418 WHERE match_id = 28926;
UPDATE valuebet_alert SET match_id = 418 WHERE match_id = 28926;
DELETE FROM match_record WHERE id = 28926;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 418)),
            updated_at = NOW()
        WHERE id = 418;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59155 
              AND p.match_id = 41284
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 41284 WHERE match_id = 59155;
UPDATE odds_snapshot SET match_id = 41284 WHERE match_id = 59155;
UPDATE surebet_alert SET match_id = 41284 WHERE match_id = 59155;
UPDATE middle_alert SET match_id = 41284 WHERE match_id = 59155;
UPDATE valuebet_alert SET match_id = 41284 WHERE match_id = 59155;
DELETE FROM match_record WHERE id = 59155;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 41284)),
            updated_at = NOW()
        WHERE id = 41284;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 28931 
              AND p.match_id = 473
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 473 WHERE match_id = 28931;
UPDATE odds_snapshot SET match_id = 473 WHERE match_id = 28931;
UPDATE surebet_alert SET match_id = 473 WHERE match_id = 28931;
UPDATE middle_alert SET match_id = 473 WHERE match_id = 28931;
UPDATE valuebet_alert SET match_id = 473 WHERE match_id = 28931;
DELETE FROM match_record WHERE id = 28931;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 473)),
            updated_at = NOW()
        WHERE id = 473;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59224 
              AND p.match_id = 41279
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 41279 WHERE match_id = 59224;
UPDATE odds_snapshot SET match_id = 41279 WHERE match_id = 59224;
UPDATE surebet_alert SET match_id = 41279 WHERE match_id = 59224;
UPDATE middle_alert SET match_id = 41279 WHERE match_id = 59224;
UPDATE valuebet_alert SET match_id = 41279 WHERE match_id = 59224;
DELETE FROM match_record WHERE id = 59224;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 41279)),
            updated_at = NOW()
        WHERE id = 41279;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59220 
              AND p.match_id = 41315
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 41315 WHERE match_id = 59220;
UPDATE odds_snapshot SET match_id = 41315 WHERE match_id = 59220;
UPDATE surebet_alert SET match_id = 41315 WHERE match_id = 59220;
UPDATE middle_alert SET match_id = 41315 WHERE match_id = 59220;
UPDATE valuebet_alert SET match_id = 41315 WHERE match_id = 59220;
DELETE FROM match_record WHERE id = 59220;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 41315)),
            updated_at = NOW()
        WHERE id = 41315;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59245 
              AND p.match_id = 477
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 477 WHERE match_id = 59245;
UPDATE odds_snapshot SET match_id = 477 WHERE match_id = 59245;
UPDATE surebet_alert SET match_id = 477 WHERE match_id = 59245;
UPDATE middle_alert SET match_id = 477 WHERE match_id = 59245;
UPDATE valuebet_alert SET match_id = 477 WHERE match_id = 59245;
DELETE FROM match_record WHERE id = 59245;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 477)),
            updated_at = NOW()
        WHERE id = 477;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 73427 
              AND p.match_id = 76151
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 76151 WHERE match_id = 73427;
UPDATE odds_snapshot SET match_id = 76151 WHERE match_id = 73427;
UPDATE surebet_alert SET match_id = 76151 WHERE match_id = 73427;
UPDATE middle_alert SET match_id = 76151 WHERE match_id = 73427;
UPDATE valuebet_alert SET match_id = 76151 WHERE match_id = 73427;
DELETE FROM match_record WHERE id = 73427;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 76151)),
            updated_at = NOW()
        WHERE id = 76151;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59225 
              AND p.match_id = 490
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 490 WHERE match_id = 59225;
UPDATE odds_snapshot SET match_id = 490 WHERE match_id = 59225;
UPDATE surebet_alert SET match_id = 490 WHERE match_id = 59225;
UPDATE middle_alert SET match_id = 490 WHERE match_id = 59225;
UPDATE valuebet_alert SET match_id = 490 WHERE match_id = 59225;
DELETE FROM match_record WHERE id = 59225;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 490)),
            updated_at = NOW()
        WHERE id = 490;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 40830 
              AND p.match_id = 465
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 465 WHERE match_id = 40830;
UPDATE odds_snapshot SET match_id = 465 WHERE match_id = 40830;
UPDATE surebet_alert SET match_id = 465 WHERE match_id = 40830;
UPDATE middle_alert SET match_id = 465 WHERE match_id = 40830;
UPDATE valuebet_alert SET match_id = 465 WHERE match_id = 40830;
DELETE FROM match_record WHERE id = 40830;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 465)),
            updated_at = NOW()
        WHERE id = 465;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 63421 
              AND p.match_id = 63376
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 63376 WHERE match_id = 63421;
UPDATE odds_snapshot SET match_id = 63376 WHERE match_id = 63421;
UPDATE surebet_alert SET match_id = 63376 WHERE match_id = 63421;
UPDATE middle_alert SET match_id = 63376 WHERE match_id = 63421;
UPDATE valuebet_alert SET match_id = 63376 WHERE match_id = 63421;
DELETE FROM match_record WHERE id = 63421;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 63376)),
            updated_at = NOW()
        WHERE id = 63376;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 72444 
              AND p.match_id = 55418
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 55418 WHERE match_id = 72444;
UPDATE odds_snapshot SET match_id = 55418 WHERE match_id = 72444;
UPDATE surebet_alert SET match_id = 55418 WHERE match_id = 72444;
UPDATE middle_alert SET match_id = 55418 WHERE match_id = 72444;
UPDATE valuebet_alert SET match_id = 55418 WHERE match_id = 72444;
DELETE FROM match_record WHERE id = 72444;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 55418)),
            updated_at = NOW()
        WHERE id = 55418;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 72443 
              AND p.match_id = 41381
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 41381 WHERE match_id = 72443;
UPDATE odds_snapshot SET match_id = 41381 WHERE match_id = 72443;
UPDATE surebet_alert SET match_id = 41381 WHERE match_id = 72443;
UPDATE middle_alert SET match_id = 41381 WHERE match_id = 72443;
UPDATE valuebet_alert SET match_id = 41381 WHERE match_id = 72443;
DELETE FROM match_record WHERE id = 72443;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 41381)),
            updated_at = NOW()
        WHERE id = 41381;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 57514 
              AND p.match_id = 35774
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 35774 WHERE match_id = 57514;
UPDATE odds_snapshot SET match_id = 35774 WHERE match_id = 57514;
UPDATE surebet_alert SET match_id = 35774 WHERE match_id = 57514;
UPDATE middle_alert SET match_id = 35774 WHERE match_id = 57514;
UPDATE valuebet_alert SET match_id = 35774 WHERE match_id = 57514;
DELETE FROM match_record WHERE id = 57514;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 35774)),
            updated_at = NOW()
        WHERE id = 35774;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 62981 
              AND p.match_id = 63425
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 63425 WHERE match_id = 62981;
UPDATE odds_snapshot SET match_id = 63425 WHERE match_id = 62981;
UPDATE surebet_alert SET match_id = 63425 WHERE match_id = 62981;
UPDATE middle_alert SET match_id = 63425 WHERE match_id = 62981;
UPDATE valuebet_alert SET match_id = 63425 WHERE match_id = 62981;
DELETE FROM match_record WHERE id = 62981;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 63425)),
            updated_at = NOW()
        WHERE id = 63425;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 58026 
              AND p.match_id = 35473
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 35473 WHERE match_id = 58026;
UPDATE odds_snapshot SET match_id = 35473 WHERE match_id = 58026;
UPDATE surebet_alert SET match_id = 35473 WHERE match_id = 58026;
UPDATE middle_alert SET match_id = 35473 WHERE match_id = 58026;
UPDATE valuebet_alert SET match_id = 35473 WHERE match_id = 58026;
DELETE FROM match_record WHERE id = 58026;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 35473)),
            updated_at = NOW()
        WHERE id = 35473;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 74530 
              AND p.match_id = 76152
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 76152 WHERE match_id = 74530;
UPDATE odds_snapshot SET match_id = 76152 WHERE match_id = 74530;
UPDATE surebet_alert SET match_id = 76152 WHERE match_id = 74530;
UPDATE middle_alert SET match_id = 76152 WHERE match_id = 74530;
UPDATE valuebet_alert SET match_id = 76152 WHERE match_id = 74530;
DELETE FROM match_record WHERE id = 74530;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 76152)),
            updated_at = NOW()
        WHERE id = 76152;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 29114 
              AND p.match_id = 471
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 471 WHERE match_id = 29114;
UPDATE odds_snapshot SET match_id = 471 WHERE match_id = 29114;
UPDATE surebet_alert SET match_id = 471 WHERE match_id = 29114;
UPDATE middle_alert SET match_id = 471 WHERE match_id = 29114;
UPDATE valuebet_alert SET match_id = 471 WHERE match_id = 29114;
DELETE FROM match_record WHERE id = 29114;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 471)),
            updated_at = NOW()
        WHERE id = 471;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59206 
              AND p.match_id = 472
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 472 WHERE match_id = 59206;
UPDATE odds_snapshot SET match_id = 472 WHERE match_id = 59206;
UPDATE surebet_alert SET match_id = 472 WHERE match_id = 59206;
UPDATE middle_alert SET match_id = 472 WHERE match_id = 59206;
UPDATE valuebet_alert SET match_id = 472 WHERE match_id = 59206;
DELETE FROM match_record WHERE id = 59206;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 472)),
            updated_at = NOW()
        WHERE id = 472;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59204 
              AND p.match_id = 41320
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 41320 WHERE match_id = 59204;
UPDATE odds_snapshot SET match_id = 41320 WHERE match_id = 59204;
UPDATE surebet_alert SET match_id = 41320 WHERE match_id = 59204;
UPDATE middle_alert SET match_id = 41320 WHERE match_id = 59204;
UPDATE valuebet_alert SET match_id = 41320 WHERE match_id = 59204;
DELETE FROM match_record WHERE id = 59204;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 41320)),
            updated_at = NOW()
        WHERE id = 41320;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 47317 
              AND p.match_id = 411
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 411 WHERE match_id = 47317;
UPDATE odds_snapshot SET match_id = 411 WHERE match_id = 47317;
UPDATE surebet_alert SET match_id = 411 WHERE match_id = 47317;
UPDATE middle_alert SET match_id = 411 WHERE match_id = 47317;
UPDATE valuebet_alert SET match_id = 411 WHERE match_id = 47317;
DELETE FROM match_record WHERE id = 47317;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 411)),
            updated_at = NOW()
        WHERE id = 411;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59233 
              AND p.match_id = 41318
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 41318 WHERE match_id = 59233;
UPDATE odds_snapshot SET match_id = 41318 WHERE match_id = 59233;
UPDATE surebet_alert SET match_id = 41318 WHERE match_id = 59233;
UPDATE middle_alert SET match_id = 41318 WHERE match_id = 59233;
UPDATE valuebet_alert SET match_id = 41318 WHERE match_id = 59233;
DELETE FROM match_record WHERE id = 59233;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 41318)),
            updated_at = NOW()
        WHERE id = 41318;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 29186 
              AND p.match_id = 406
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 406 WHERE match_id = 29186;
UPDATE odds_snapshot SET match_id = 406 WHERE match_id = 29186;
UPDATE surebet_alert SET match_id = 406 WHERE match_id = 29186;
UPDATE middle_alert SET match_id = 406 WHERE match_id = 29186;
UPDATE valuebet_alert SET match_id = 406 WHERE match_id = 29186;
DELETE FROM match_record WHERE id = 29186;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 406)),
            updated_at = NOW()
        WHERE id = 406;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 74531 
              AND p.match_id = 75308
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 75308 WHERE match_id = 74531;
UPDATE odds_snapshot SET match_id = 75308 WHERE match_id = 74531;
UPDATE surebet_alert SET match_id = 75308 WHERE match_id = 74531;
UPDATE middle_alert SET match_id = 75308 WHERE match_id = 74531;
UPDATE valuebet_alert SET match_id = 75308 WHERE match_id = 74531;
DELETE FROM match_record WHERE id = 74531;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 75308)),
            updated_at = NOW()
        WHERE id = 75308;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 72484 
              AND p.match_id = 72342
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 72342 WHERE match_id = 72484;
UPDATE odds_snapshot SET match_id = 72342 WHERE match_id = 72484;
UPDATE surebet_alert SET match_id = 72342 WHERE match_id = 72484;
UPDATE middle_alert SET match_id = 72342 WHERE match_id = 72484;
UPDATE valuebet_alert SET match_id = 72342 WHERE match_id = 72484;
DELETE FROM match_record WHERE id = 72484;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 72342)),
            updated_at = NOW()
        WHERE id = 72342;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59243 
              AND p.match_id = 41319
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 41319 WHERE match_id = 59243;
UPDATE odds_snapshot SET match_id = 41319 WHERE match_id = 59243;
UPDATE surebet_alert SET match_id = 41319 WHERE match_id = 59243;
UPDATE middle_alert SET match_id = 41319 WHERE match_id = 59243;
UPDATE valuebet_alert SET match_id = 41319 WHERE match_id = 59243;
DELETE FROM match_record WHERE id = 59243;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 41319)),
            updated_at = NOW()
        WHERE id = 41319;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 58078 
              AND p.match_id = 412
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 412 WHERE match_id = 58078;
UPDATE odds_snapshot SET match_id = 412 WHERE match_id = 58078;
UPDATE surebet_alert SET match_id = 412 WHERE match_id = 58078;
UPDATE middle_alert SET match_id = 412 WHERE match_id = 58078;
UPDATE valuebet_alert SET match_id = 412 WHERE match_id = 58078;
DELETE FROM match_record WHERE id = 58078;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 412)),
            updated_at = NOW()
        WHERE id = 412;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 29181 
              AND p.match_id = 422
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 422 WHERE match_id = 29181;
UPDATE odds_snapshot SET match_id = 422 WHERE match_id = 29181;
UPDATE surebet_alert SET match_id = 422 WHERE match_id = 29181;
UPDATE middle_alert SET match_id = 422 WHERE match_id = 29181;
UPDATE valuebet_alert SET match_id = 422 WHERE match_id = 29181;
DELETE FROM match_record WHERE id = 29181;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 422)),
            updated_at = NOW()
        WHERE id = 422;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59218 
              AND p.match_id = 41313
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 41313 WHERE match_id = 59218;
UPDATE odds_snapshot SET match_id = 41313 WHERE match_id = 59218;
UPDATE surebet_alert SET match_id = 41313 WHERE match_id = 59218;
UPDATE middle_alert SET match_id = 41313 WHERE match_id = 59218;
UPDATE valuebet_alert SET match_id = 41313 WHERE match_id = 59218;
DELETE FROM match_record WHERE id = 59218;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 41313)),
            updated_at = NOW()
        WHERE id = 41313;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59145 
              AND p.match_id = 419
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 419 WHERE match_id = 59145;
UPDATE odds_snapshot SET match_id = 419 WHERE match_id = 59145;
UPDATE surebet_alert SET match_id = 419 WHERE match_id = 59145;
UPDATE middle_alert SET match_id = 419 WHERE match_id = 59145;
UPDATE valuebet_alert SET match_id = 419 WHERE match_id = 59145;
DELETE FROM match_record WHERE id = 59145;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 419)),
            updated_at = NOW()
        WHERE id = 419;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 58025 
              AND p.match_id = 408
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 408 WHERE match_id = 58025;
UPDATE odds_snapshot SET match_id = 408 WHERE match_id = 58025;
UPDATE surebet_alert SET match_id = 408 WHERE match_id = 58025;
UPDATE middle_alert SET match_id = 408 WHERE match_id = 58025;
UPDATE valuebet_alert SET match_id = 408 WHERE match_id = 58025;
DELETE FROM match_record WHERE id = 58025;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 408)),
            updated_at = NOW()
        WHERE id = 408;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 29135 
              AND p.match_id = 414
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 414 WHERE match_id = 29135;
UPDATE odds_snapshot SET match_id = 414 WHERE match_id = 29135;
UPDATE surebet_alert SET match_id = 414 WHERE match_id = 29135;
UPDATE middle_alert SET match_id = 414 WHERE match_id = 29135;
UPDATE valuebet_alert SET match_id = 414 WHERE match_id = 29135;
DELETE FROM match_record WHERE id = 29135;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 414)),
            updated_at = NOW()
        WHERE id = 414;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 74529 
              AND p.match_id = 76153
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 76153 WHERE match_id = 74529;
UPDATE odds_snapshot SET match_id = 76153 WHERE match_id = 74529;
UPDATE surebet_alert SET match_id = 76153 WHERE match_id = 74529;
UPDATE middle_alert SET match_id = 76153 WHERE match_id = 74529;
UPDATE valuebet_alert SET match_id = 76153 WHERE match_id = 74529;
DELETE FROM match_record WHERE id = 74529;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 76153)),
            updated_at = NOW()
        WHERE id = 76153;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 46923 
              AND p.match_id = 14827
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 14827 WHERE match_id = 46923;
UPDATE odds_snapshot SET match_id = 14827 WHERE match_id = 46923;
UPDATE surebet_alert SET match_id = 14827 WHERE match_id = 46923;
UPDATE middle_alert SET match_id = 14827 WHERE match_id = 46923;
UPDATE valuebet_alert SET match_id = 14827 WHERE match_id = 46923;
DELETE FROM match_record WHERE id = 46923;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14827)),
            updated_at = NOW()
        WHERE id = 14827;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 44347 
              AND p.match_id = 474
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 474 WHERE match_id = 44347;
UPDATE odds_snapshot SET match_id = 474 WHERE match_id = 44347;
UPDATE surebet_alert SET match_id = 474 WHERE match_id = 44347;
UPDATE middle_alert SET match_id = 474 WHERE match_id = 44347;
UPDATE valuebet_alert SET match_id = 474 WHERE match_id = 44347;
DELETE FROM match_record WHERE id = 44347;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 474)),
            updated_at = NOW()
        WHERE id = 474;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 57569 
              AND p.match_id = 56662
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 56662 WHERE match_id = 57569;
UPDATE odds_snapshot SET match_id = 56662 WHERE match_id = 57569;
UPDATE surebet_alert SET match_id = 56662 WHERE match_id = 57569;
UPDATE middle_alert SET match_id = 56662 WHERE match_id = 57569;
UPDATE valuebet_alert SET match_id = 56662 WHERE match_id = 57569;
DELETE FROM match_record WHERE id = 57569;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 56662)),
            updated_at = NOW()
        WHERE id = 56662;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 47276 
              AND p.match_id = 14828
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 14828 WHERE match_id = 47276;
UPDATE odds_snapshot SET match_id = 14828 WHERE match_id = 47276;
UPDATE surebet_alert SET match_id = 14828 WHERE match_id = 47276;
UPDATE middle_alert SET match_id = 14828 WHERE match_id = 47276;
UPDATE valuebet_alert SET match_id = 14828 WHERE match_id = 47276;
DELETE FROM match_record WHERE id = 47276;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14828)),
            updated_at = NOW()
        WHERE id = 14828;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 38371 
              AND p.match_id = 470
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 470 WHERE match_id = 38371;
UPDATE odds_snapshot SET match_id = 470 WHERE match_id = 38371;
UPDATE surebet_alert SET match_id = 470 WHERE match_id = 38371;
UPDATE middle_alert SET match_id = 470 WHERE match_id = 38371;
UPDATE valuebet_alert SET match_id = 470 WHERE match_id = 38371;
DELETE FROM match_record WHERE id = 38371;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 470)),
            updated_at = NOW()
        WHERE id = 470;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 74532 
              AND p.match_id = 76149
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 76149 WHERE match_id = 74532;
UPDATE odds_snapshot SET match_id = 76149 WHERE match_id = 74532;
UPDATE surebet_alert SET match_id = 76149 WHERE match_id = 74532;
UPDATE middle_alert SET match_id = 76149 WHERE match_id = 74532;
UPDATE valuebet_alert SET match_id = 76149 WHERE match_id = 74532;
DELETE FROM match_record WHERE id = 74532;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 76149)),
            updated_at = NOW()
        WHERE id = 76149;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 39930 
              AND p.match_id = 476
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 476 WHERE match_id = 39930;
UPDATE odds_snapshot SET match_id = 476 WHERE match_id = 39930;
UPDATE surebet_alert SET match_id = 476 WHERE match_id = 39930;
UPDATE middle_alert SET match_id = 476 WHERE match_id = 39930;
UPDATE valuebet_alert SET match_id = 476 WHERE match_id = 39930;
DELETE FROM match_record WHERE id = 39930;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 476)),
            updated_at = NOW()
        WHERE id = 476;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 73921 
              AND p.match_id = 76146
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 76146 WHERE match_id = 73921;
UPDATE odds_snapshot SET match_id = 76146 WHERE match_id = 73921;
UPDATE surebet_alert SET match_id = 76146 WHERE match_id = 73921;
UPDATE middle_alert SET match_id = 76146 WHERE match_id = 73921;
UPDATE valuebet_alert SET match_id = 76146 WHERE match_id = 73921;
DELETE FROM match_record WHERE id = 73921;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 76146)),
            updated_at = NOW()
        WHERE id = 76146;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 48352 
              AND p.match_id = 40993
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 40993 WHERE match_id = 48352;
UPDATE odds_snapshot SET match_id = 40993 WHERE match_id = 48352;
UPDATE surebet_alert SET match_id = 40993 WHERE match_id = 48352;
UPDATE middle_alert SET match_id = 40993 WHERE match_id = 48352;
UPDATE valuebet_alert SET match_id = 40993 WHERE match_id = 48352;
DELETE FROM match_record WHERE id = 48352;

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 35472 
              AND p.match_id = 40993
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 40993 WHERE match_id = 35472;
UPDATE odds_snapshot SET match_id = 40993 WHERE match_id = 35472;
UPDATE surebet_alert SET match_id = 40993 WHERE match_id = 35472;
UPDATE middle_alert SET match_id = 40993 WHERE match_id = 35472;
UPDATE valuebet_alert SET match_id = 40993 WHERE match_id = 35472;
DELETE FROM match_record WHERE id = 35472;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 40993)),
            updated_at = NOW()
        WHERE id = 40993;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 58020 
              AND p.match_id = 478
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 478 WHERE match_id = 58020;
UPDATE odds_snapshot SET match_id = 478 WHERE match_id = 58020;
UPDATE surebet_alert SET match_id = 478 WHERE match_id = 58020;
UPDATE middle_alert SET match_id = 478 WHERE match_id = 58020;
UPDATE valuebet_alert SET match_id = 478 WHERE match_id = 58020;
DELETE FROM match_record WHERE id = 58020;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 478)),
            updated_at = NOW()
        WHERE id = 478;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 57562 
              AND p.match_id = 67719
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 67719 WHERE match_id = 57562;
UPDATE odds_snapshot SET match_id = 67719 WHERE match_id = 57562;
UPDATE surebet_alert SET match_id = 67719 WHERE match_id = 57562;
UPDATE middle_alert SET match_id = 67719 WHERE match_id = 57562;
UPDATE valuebet_alert SET match_id = 67719 WHERE match_id = 57562;
DELETE FROM match_record WHERE id = 57562;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 67719)),
            updated_at = NOW()
        WHERE id = 67719;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 57560 
              AND p.match_id = 41034
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 41034 WHERE match_id = 57560;
UPDATE odds_snapshot SET match_id = 41034 WHERE match_id = 57560;
UPDATE surebet_alert SET match_id = 41034 WHERE match_id = 57560;
UPDATE middle_alert SET match_id = 41034 WHERE match_id = 57560;
UPDATE valuebet_alert SET match_id = 41034 WHERE match_id = 57560;
DELETE FROM match_record WHERE id = 57560;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 41034)),
            updated_at = NOW()
        WHERE id = 41034;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 80398 
              AND p.match_id = 80342
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 80342 WHERE match_id = 80398;
UPDATE odds_snapshot SET match_id = 80342 WHERE match_id = 80398;
UPDATE surebet_alert SET match_id = 80342 WHERE match_id = 80398;
UPDATE middle_alert SET match_id = 80342 WHERE match_id = 80398;
UPDATE valuebet_alert SET match_id = 80342 WHERE match_id = 80398;
DELETE FROM match_record WHERE id = 80398;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 80342)),
            updated_at = NOW()
        WHERE id = 80342;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 58115 
              AND p.match_id = 57883
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 57883 WHERE match_id = 58115;
UPDATE odds_snapshot SET match_id = 57883 WHERE match_id = 58115;
UPDATE surebet_alert SET match_id = 57883 WHERE match_id = 58115;
UPDATE middle_alert SET match_id = 57883 WHERE match_id = 58115;
UPDATE valuebet_alert SET match_id = 57883 WHERE match_id = 58115;
DELETE FROM match_record WHERE id = 58115;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 57883)),
            updated_at = NOW()
        WHERE id = 57883;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 40124 
              AND p.match_id = 41037
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 41037 WHERE match_id = 40124;
UPDATE odds_snapshot SET match_id = 41037 WHERE match_id = 40124;
UPDATE surebet_alert SET match_id = 41037 WHERE match_id = 40124;
UPDATE middle_alert SET match_id = 41037 WHERE match_id = 40124;
UPDATE valuebet_alert SET match_id = 41037 WHERE match_id = 40124;
DELETE FROM match_record WHERE id = 40124;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 41037)),
            updated_at = NOW()
        WHERE id = 41037;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 71300 
              AND p.match_id = 71706
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 71706 WHERE match_id = 71300;
UPDATE odds_snapshot SET match_id = 71706 WHERE match_id = 71300;
UPDATE surebet_alert SET match_id = 71706 WHERE match_id = 71300;
UPDATE middle_alert SET match_id = 71706 WHERE match_id = 71300;
UPDATE valuebet_alert SET match_id = 71706 WHERE match_id = 71300;
DELETE FROM match_record WHERE id = 71300;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 71706)),
            updated_at = NOW()
        WHERE id = 71706;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 58083 
              AND p.match_id = 57884
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 57884 WHERE match_id = 58083;
UPDATE odds_snapshot SET match_id = 57884 WHERE match_id = 58083;
UPDATE surebet_alert SET match_id = 57884 WHERE match_id = 58083;
UPDATE middle_alert SET match_id = 57884 WHERE match_id = 58083;
UPDATE valuebet_alert SET match_id = 57884 WHERE match_id = 58083;
DELETE FROM match_record WHERE id = 58083;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 57884)),
            updated_at = NOW()
        WHERE id = 57884;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 74928 
              AND p.match_id = 75167
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 75167 WHERE match_id = 74928;
UPDATE odds_snapshot SET match_id = 75167 WHERE match_id = 74928;
UPDATE surebet_alert SET match_id = 75167 WHERE match_id = 74928;
UPDATE middle_alert SET match_id = 75167 WHERE match_id = 74928;
UPDATE valuebet_alert SET match_id = 75167 WHERE match_id = 74928;
DELETE FROM match_record WHERE id = 74928;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 75167)),
            updated_at = NOW()
        WHERE id = 75167;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 40357 
              AND p.match_id = 475
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 475 WHERE match_id = 40357;
UPDATE odds_snapshot SET match_id = 475 WHERE match_id = 40357;
UPDATE surebet_alert SET match_id = 475 WHERE match_id = 40357;
UPDATE middle_alert SET match_id = 475 WHERE match_id = 40357;
UPDATE valuebet_alert SET match_id = 475 WHERE match_id = 40357;
DELETE FROM match_record WHERE id = 40357;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 475)),
            updated_at = NOW()
        WHERE id = 475;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 40518 
              AND p.match_id = 42117
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 42117 WHERE match_id = 40518;
UPDATE odds_snapshot SET match_id = 42117 WHERE match_id = 40518;
UPDATE surebet_alert SET match_id = 42117 WHERE match_id = 40518;
UPDATE middle_alert SET match_id = 42117 WHERE match_id = 40518;
UPDATE valuebet_alert SET match_id = 42117 WHERE match_id = 40518;
DELETE FROM match_record WHERE id = 40518;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 42117)),
            updated_at = NOW()
        WHERE id = 42117;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70558 
              AND p.match_id = 64668
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 64668 WHERE match_id = 70558;
UPDATE odds_snapshot SET match_id = 64668 WHERE match_id = 70558;
UPDATE surebet_alert SET match_id = 64668 WHERE match_id = 70558;
UPDATE middle_alert SET match_id = 64668 WHERE match_id = 70558;
UPDATE valuebet_alert SET match_id = 64668 WHERE match_id = 70558;
DELETE FROM match_record WHERE id = 70558;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 64668)),
            updated_at = NOW()
        WHERE id = 64668;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 71712 
              AND p.match_id = 69631
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69631 WHERE match_id = 71712;
UPDATE odds_snapshot SET match_id = 69631 WHERE match_id = 71712;
UPDATE surebet_alert SET match_id = 69631 WHERE match_id = 71712;
UPDATE middle_alert SET match_id = 69631 WHERE match_id = 71712;
UPDATE valuebet_alert SET match_id = 69631 WHERE match_id = 71712;
DELETE FROM match_record WHERE id = 71712;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69631)),
            updated_at = NOW()
        WHERE id = 69631;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 58414 
              AND p.match_id = 33492
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 33492 WHERE match_id = 58414;
UPDATE odds_snapshot SET match_id = 33492 WHERE match_id = 58414;
UPDATE surebet_alert SET match_id = 33492 WHERE match_id = 58414;
UPDATE middle_alert SET match_id = 33492 WHERE match_id = 58414;
UPDATE valuebet_alert SET match_id = 33492 WHERE match_id = 58414;
DELETE FROM match_record WHERE id = 58414;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 33492)),
            updated_at = NOW()
        WHERE id = 33492;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 66778 
              AND p.match_id = 66655
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 66655 WHERE match_id = 66778;
UPDATE odds_snapshot SET match_id = 66655 WHERE match_id = 66778;
UPDATE surebet_alert SET match_id = 66655 WHERE match_id = 66778;
UPDATE middle_alert SET match_id = 66655 WHERE match_id = 66778;
UPDATE valuebet_alert SET match_id = 66655 WHERE match_id = 66778;
DELETE FROM match_record WHERE id = 66778;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 66655)),
            updated_at = NOW()
        WHERE id = 66655;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 66779 
              AND p.match_id = 66656
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 66656 WHERE match_id = 66779;
UPDATE odds_snapshot SET match_id = 66656 WHERE match_id = 66779;
UPDATE surebet_alert SET match_id = 66656 WHERE match_id = 66779;
UPDATE middle_alert SET match_id = 66656 WHERE match_id = 66779;
UPDATE valuebet_alert SET match_id = 66656 WHERE match_id = 66779;
DELETE FROM match_record WHERE id = 66779;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 66656)),
            updated_at = NOW()
        WHERE id = 66656;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 45008 
              AND p.match_id = 14782
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 14782 WHERE match_id = 45008;
UPDATE odds_snapshot SET match_id = 14782 WHERE match_id = 45008;
UPDATE surebet_alert SET match_id = 14782 WHERE match_id = 45008;
UPDATE middle_alert SET match_id = 14782 WHERE match_id = 45008;
UPDATE valuebet_alert SET match_id = 14782 WHERE match_id = 45008;
DELETE FROM match_record WHERE id = 45008;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 14782)),
            updated_at = NOW()
        WHERE id = 14782;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 68707 
              AND p.match_id = 66357
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 66357 WHERE match_id = 68707;
UPDATE odds_snapshot SET match_id = 66357 WHERE match_id = 68707;
UPDATE surebet_alert SET match_id = 66357 WHERE match_id = 68707;
UPDATE middle_alert SET match_id = 66357 WHERE match_id = 68707;
UPDATE valuebet_alert SET match_id = 66357 WHERE match_id = 68707;
DELETE FROM match_record WHERE id = 68707;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 66357)),
            updated_at = NOW()
        WHERE id = 66357;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 61938 
              AND p.match_id = 62062
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 62062 WHERE match_id = 61938;
UPDATE odds_snapshot SET match_id = 62062 WHERE match_id = 61938;
UPDATE surebet_alert SET match_id = 62062 WHERE match_id = 61938;
UPDATE middle_alert SET match_id = 62062 WHERE match_id = 61938;
UPDATE valuebet_alert SET match_id = 62062 WHERE match_id = 61938;
DELETE FROM match_record WHERE id = 61938;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 62062)),
            updated_at = NOW()
        WHERE id = 62062;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 77596 
              AND p.match_id = 67040
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 67040 WHERE match_id = 77596;
UPDATE odds_snapshot SET match_id = 67040 WHERE match_id = 77596;
UPDATE surebet_alert SET match_id = 67040 WHERE match_id = 77596;
UPDATE middle_alert SET match_id = 67040 WHERE match_id = 77596;
UPDATE valuebet_alert SET match_id = 67040 WHERE match_id = 77596;
DELETE FROM match_record WHERE id = 77596;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 67040)),
            updated_at = NOW()
        WHERE id = 67040;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 68708 
              AND p.match_id = 63616
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 63616 WHERE match_id = 68708;
UPDATE odds_snapshot SET match_id = 63616 WHERE match_id = 68708;
UPDATE surebet_alert SET match_id = 63616 WHERE match_id = 68708;
UPDATE middle_alert SET match_id = 63616 WHERE match_id = 68708;
UPDATE valuebet_alert SET match_id = 63616 WHERE match_id = 68708;
DELETE FROM match_record WHERE id = 68708;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 63616)),
            updated_at = NOW()
        WHERE id = 63616;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 58412 
              AND p.match_id = 35059
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 35059 WHERE match_id = 58412;
UPDATE odds_snapshot SET match_id = 35059 WHERE match_id = 58412;
UPDATE surebet_alert SET match_id = 35059 WHERE match_id = 58412;
UPDATE middle_alert SET match_id = 35059 WHERE match_id = 58412;
UPDATE valuebet_alert SET match_id = 35059 WHERE match_id = 58412;
DELETE FROM match_record WHERE id = 58412;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 35059)),
            updated_at = NOW()
        WHERE id = 35059;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70135 
              AND p.match_id = 56283
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 56283 WHERE match_id = 70135;
UPDATE odds_snapshot SET match_id = 56283 WHERE match_id = 70135;
UPDATE surebet_alert SET match_id = 56283 WHERE match_id = 70135;
UPDATE middle_alert SET match_id = 56283 WHERE match_id = 70135;
UPDATE valuebet_alert SET match_id = 56283 WHERE match_id = 70135;
DELETE FROM match_record WHERE id = 70135;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 56283)),
            updated_at = NOW()
        WHERE id = 56283;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 71313 
              AND p.match_id = 71318
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 71318 WHERE match_id = 71313;
UPDATE odds_snapshot SET match_id = 71318 WHERE match_id = 71313;
UPDATE surebet_alert SET match_id = 71318 WHERE match_id = 71313;
UPDATE middle_alert SET match_id = 71318 WHERE match_id = 71313;
UPDATE valuebet_alert SET match_id = 71318 WHERE match_id = 71313;
DELETE FROM match_record WHERE id = 71313;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 71318)),
            updated_at = NOW()
        WHERE id = 71318;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 68151 
              AND p.match_id = 67492
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 67492 WHERE match_id = 68151;
UPDATE odds_snapshot SET match_id = 67492 WHERE match_id = 68151;
UPDATE surebet_alert SET match_id = 67492 WHERE match_id = 68151;
UPDATE middle_alert SET match_id = 67492 WHERE match_id = 68151;
UPDATE valuebet_alert SET match_id = 67492 WHERE match_id = 68151;
DELETE FROM match_record WHERE id = 68151;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 67492)),
            updated_at = NOW()
        WHERE id = 67492;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 73980 
              AND p.match_id = 73964
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 73964 WHERE match_id = 73980;
UPDATE odds_snapshot SET match_id = 73964 WHERE match_id = 73980;
UPDATE surebet_alert SET match_id = 73964 WHERE match_id = 73980;
UPDATE middle_alert SET match_id = 73964 WHERE match_id = 73980;
UPDATE valuebet_alert SET match_id = 73964 WHERE match_id = 73980;
DELETE FROM match_record WHERE id = 73980;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 73964)),
            updated_at = NOW()
        WHERE id = 73964;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 61550 
              AND p.match_id = 61270
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 61270 WHERE match_id = 61550;
UPDATE odds_snapshot SET match_id = 61270 WHERE match_id = 61550;
UPDATE surebet_alert SET match_id = 61270 WHERE match_id = 61550;
UPDATE middle_alert SET match_id = 61270 WHERE match_id = 61550;
UPDATE valuebet_alert SET match_id = 61270 WHERE match_id = 61550;
DELETE FROM match_record WHERE id = 61550;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 61270)),
            updated_at = NOW()
        WHERE id = 61270;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70871 
              AND p.match_id = 69712
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69712 WHERE match_id = 70871;
UPDATE odds_snapshot SET match_id = 69712 WHERE match_id = 70871;
UPDATE surebet_alert SET match_id = 69712 WHERE match_id = 70871;
UPDATE middle_alert SET match_id = 69712 WHERE match_id = 70871;
UPDATE valuebet_alert SET match_id = 69712 WHERE match_id = 70871;
DELETE FROM match_record WHERE id = 70871;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69712)),
            updated_at = NOW()
        WHERE id = 69712;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 56614 
              AND p.match_id = 56589
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 56589 WHERE match_id = 56614;
UPDATE odds_snapshot SET match_id = 56589 WHERE match_id = 56614;
UPDATE surebet_alert SET match_id = 56589 WHERE match_id = 56614;
UPDATE middle_alert SET match_id = 56589 WHERE match_id = 56614;
UPDATE valuebet_alert SET match_id = 56589 WHERE match_id = 56614;
DELETE FROM match_record WHERE id = 56614;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 56589)),
            updated_at = NOW()
        WHERE id = 56589;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 56615 
              AND p.match_id = 56588
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 56588 WHERE match_id = 56615;
UPDATE odds_snapshot SET match_id = 56588 WHERE match_id = 56615;
UPDATE surebet_alert SET match_id = 56588 WHERE match_id = 56615;
UPDATE middle_alert SET match_id = 56588 WHERE match_id = 56615;
UPDATE valuebet_alert SET match_id = 56588 WHERE match_id = 56615;
DELETE FROM match_record WHERE id = 56615;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 56588)),
            updated_at = NOW()
        WHERE id = 56588;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 69453 
              AND p.match_id = 69174
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69174 WHERE match_id = 69453;
UPDATE odds_snapshot SET match_id = 69174 WHERE match_id = 69453;
UPDATE surebet_alert SET match_id = 69174 WHERE match_id = 69453;
UPDATE middle_alert SET match_id = 69174 WHERE match_id = 69453;
UPDATE valuebet_alert SET match_id = 69174 WHERE match_id = 69453;
DELETE FROM match_record WHERE id = 69453;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69174)),
            updated_at = NOW()
        WHERE id = 69174;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 69632 
              AND p.match_id = 69661
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69661 WHERE match_id = 69632;
UPDATE odds_snapshot SET match_id = 69661 WHERE match_id = 69632;
UPDATE surebet_alert SET match_id = 69661 WHERE match_id = 69632;
UPDATE middle_alert SET match_id = 69661 WHERE match_id = 69632;
UPDATE valuebet_alert SET match_id = 69661 WHERE match_id = 69632;
DELETE FROM match_record WHERE id = 69632;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69661)),
            updated_at = NOW()
        WHERE id = 69661;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 69187 
              AND p.match_id = 69197
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69197 WHERE match_id = 69187;
UPDATE odds_snapshot SET match_id = 69197 WHERE match_id = 69187;
UPDATE surebet_alert SET match_id = 69197 WHERE match_id = 69187;
UPDATE middle_alert SET match_id = 69197 WHERE match_id = 69187;
UPDATE valuebet_alert SET match_id = 69197 WHERE match_id = 69187;
DELETE FROM match_record WHERE id = 69187;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69197)),
            updated_at = NOW()
        WHERE id = 69197;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 71158 
              AND p.match_id = 71322
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 71322 WHERE match_id = 71158;
UPDATE odds_snapshot SET match_id = 71322 WHERE match_id = 71158;
UPDATE surebet_alert SET match_id = 71322 WHERE match_id = 71158;
UPDATE middle_alert SET match_id = 71322 WHERE match_id = 71158;
UPDATE valuebet_alert SET match_id = 71322 WHERE match_id = 71158;
DELETE FROM match_record WHERE id = 71158;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 71322)),
            updated_at = NOW()
        WHERE id = 71322;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 79823 
              AND p.match_id = 79816
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 79816 WHERE match_id = 79823;
UPDATE odds_snapshot SET match_id = 79816 WHERE match_id = 79823;
UPDATE surebet_alert SET match_id = 79816 WHERE match_id = 79823;
UPDATE middle_alert SET match_id = 79816 WHERE match_id = 79823;
UPDATE valuebet_alert SET match_id = 79816 WHERE match_id = 79823;
DELETE FROM match_record WHERE id = 79823;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 79816)),
            updated_at = NOW()
        WHERE id = 79816;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70380 
              AND p.match_id = 70339
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 70339 WHERE match_id = 70380;
UPDATE odds_snapshot SET match_id = 70339 WHERE match_id = 70380;
UPDATE surebet_alert SET match_id = 70339 WHERE match_id = 70380;
UPDATE middle_alert SET match_id = 70339 WHERE match_id = 70380;
UPDATE valuebet_alert SET match_id = 70339 WHERE match_id = 70380;
DELETE FROM match_record WHERE id = 70380;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 70339)),
            updated_at = NOW()
        WHERE id = 70339;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 71314 
              AND p.match_id = 71319
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 71319 WHERE match_id = 71314;
UPDATE odds_snapshot SET match_id = 71319 WHERE match_id = 71314;
UPDATE surebet_alert SET match_id = 71319 WHERE match_id = 71314;
UPDATE middle_alert SET match_id = 71319 WHERE match_id = 71314;
UPDATE valuebet_alert SET match_id = 71319 WHERE match_id = 71314;
DELETE FROM match_record WHERE id = 71314;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 71319)),
            updated_at = NOW()
        WHERE id = 71319;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 68006 
              AND p.match_id = 67945
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 67945 WHERE match_id = 68006;
UPDATE odds_snapshot SET match_id = 67945 WHERE match_id = 68006;
UPDATE surebet_alert SET match_id = 67945 WHERE match_id = 68006;
UPDATE middle_alert SET match_id = 67945 WHERE match_id = 68006;
UPDATE valuebet_alert SET match_id = 67945 WHERE match_id = 68006;
DELETE FROM match_record WHERE id = 68006;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 67945)),
            updated_at = NOW()
        WHERE id = 67945;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 68415 
              AND p.match_id = 68394
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 68394 WHERE match_id = 68415;
UPDATE odds_snapshot SET match_id = 68394 WHERE match_id = 68415;
UPDATE surebet_alert SET match_id = 68394 WHERE match_id = 68415;
UPDATE middle_alert SET match_id = 68394 WHERE match_id = 68415;
UPDATE valuebet_alert SET match_id = 68394 WHERE match_id = 68415;
DELETE FROM match_record WHERE id = 68415;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 68394)),
            updated_at = NOW()
        WHERE id = 68394;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 67609 
              AND p.match_id = 67750
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 67750 WHERE match_id = 67609;
UPDATE odds_snapshot SET match_id = 67750 WHERE match_id = 67609;
UPDATE surebet_alert SET match_id = 67750 WHERE match_id = 67609;
UPDATE middle_alert SET match_id = 67750 WHERE match_id = 67609;
UPDATE valuebet_alert SET match_id = 67750 WHERE match_id = 67609;
DELETE FROM match_record WHERE id = 67609;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 67750)),
            updated_at = NOW()
        WHERE id = 67750;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 61625 
              AND p.match_id = 60254
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 60254 WHERE match_id = 61625;
UPDATE odds_snapshot SET match_id = 60254 WHERE match_id = 61625;
UPDATE surebet_alert SET match_id = 60254 WHERE match_id = 61625;
UPDATE middle_alert SET match_id = 60254 WHERE match_id = 61625;
UPDATE valuebet_alert SET match_id = 60254 WHERE match_id = 61625;
DELETE FROM match_record WHERE id = 61625;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 60254)),
            updated_at = NOW()
        WHERE id = 60254;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70580 
              AND p.match_id = 69715
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69715 WHERE match_id = 70580;
UPDATE odds_snapshot SET match_id = 69715 WHERE match_id = 70580;
UPDATE surebet_alert SET match_id = 69715 WHERE match_id = 70580;
UPDATE middle_alert SET match_id = 69715 WHERE match_id = 70580;
UPDATE valuebet_alert SET match_id = 69715 WHERE match_id = 70580;
DELETE FROM match_record WHERE id = 70580;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69715)),
            updated_at = NOW()
        WHERE id = 69715;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 78261 
              AND p.match_id = 78045
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 78045 WHERE match_id = 78261;
UPDATE odds_snapshot SET match_id = 78045 WHERE match_id = 78261;
UPDATE surebet_alert SET match_id = 78045 WHERE match_id = 78261;
UPDATE middle_alert SET match_id = 78045 WHERE match_id = 78261;
UPDATE valuebet_alert SET match_id = 78045 WHERE match_id = 78261;
DELETE FROM match_record WHERE id = 78261;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 78045)),
            updated_at = NOW()
        WHERE id = 78045;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 77664 
              AND p.match_id = 77561
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 77561 WHERE match_id = 77664;
UPDATE odds_snapshot SET match_id = 77561 WHERE match_id = 77664;
UPDATE surebet_alert SET match_id = 77561 WHERE match_id = 77664;
UPDATE middle_alert SET match_id = 77561 WHERE match_id = 77664;
UPDATE valuebet_alert SET match_id = 77561 WHERE match_id = 77664;
DELETE FROM match_record WHERE id = 77664;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 77561)),
            updated_at = NOW()
        WHERE id = 77561;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 78642 
              AND p.match_id = 78339
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 78339 WHERE match_id = 78642;
UPDATE odds_snapshot SET match_id = 78339 WHERE match_id = 78642;
UPDATE surebet_alert SET match_id = 78339 WHERE match_id = 78642;
UPDATE middle_alert SET match_id = 78339 WHERE match_id = 78642;
UPDATE valuebet_alert SET match_id = 78339 WHERE match_id = 78642;
DELETE FROM match_record WHERE id = 78642;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 78339)),
            updated_at = NOW()
        WHERE id = 78339;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 74874 
              AND p.match_id = 74854
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 74854 WHERE match_id = 74874;
UPDATE odds_snapshot SET match_id = 74854 WHERE match_id = 74874;
UPDATE surebet_alert SET match_id = 74854 WHERE match_id = 74874;
UPDATE middle_alert SET match_id = 74854 WHERE match_id = 74874;
UPDATE valuebet_alert SET match_id = 74854 WHERE match_id = 74874;
DELETE FROM match_record WHERE id = 74874;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 74854)),
            updated_at = NOW()
        WHERE id = 74854;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 73978 
              AND p.match_id = 73963
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 73963 WHERE match_id = 73978;
UPDATE odds_snapshot SET match_id = 73963 WHERE match_id = 73978;
UPDATE surebet_alert SET match_id = 73963 WHERE match_id = 73978;
UPDATE middle_alert SET match_id = 73963 WHERE match_id = 73978;
UPDATE valuebet_alert SET match_id = 73963 WHERE match_id = 73978;
DELETE FROM match_record WHERE id = 73978;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 73963)),
            updated_at = NOW()
        WHERE id = 73963;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 69186 
              AND p.match_id = 69196
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69196 WHERE match_id = 69186;
UPDATE odds_snapshot SET match_id = 69196 WHERE match_id = 69186;
UPDATE surebet_alert SET match_id = 69196 WHERE match_id = 69186;
UPDATE middle_alert SET match_id = 69196 WHERE match_id = 69186;
UPDATE valuebet_alert SET match_id = 69196 WHERE match_id = 69186;
DELETE FROM match_record WHERE id = 69186;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69196)),
            updated_at = NOW()
        WHERE id = 69196;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 68150 
              AND p.match_id = 67494
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 67494 WHERE match_id = 68150;
UPDATE odds_snapshot SET match_id = 67494 WHERE match_id = 68150;
UPDATE surebet_alert SET match_id = 67494 WHERE match_id = 68150;
UPDATE middle_alert SET match_id = 67494 WHERE match_id = 68150;
UPDATE valuebet_alert SET match_id = 67494 WHERE match_id = 68150;
DELETE FROM match_record WHERE id = 68150;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 67494)),
            updated_at = NOW()
        WHERE id = 67494;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 67946 
              AND p.match_id = 67493
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 67493 WHERE match_id = 67946;
UPDATE odds_snapshot SET match_id = 67493 WHERE match_id = 67946;
UPDATE surebet_alert SET match_id = 67493 WHERE match_id = 67946;
UPDATE middle_alert SET match_id = 67493 WHERE match_id = 67946;
UPDATE valuebet_alert SET match_id = 67493 WHERE match_id = 67946;
DELETE FROM match_record WHERE id = 67946;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 67493)),
            updated_at = NOW()
        WHERE id = 67493;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 68414 
              AND p.match_id = 68395
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 68395 WHERE match_id = 68414;
UPDATE odds_snapshot SET match_id = 68395 WHERE match_id = 68414;
UPDATE surebet_alert SET match_id = 68395 WHERE match_id = 68414;
UPDATE middle_alert SET match_id = 68395 WHERE match_id = 68414;
UPDATE valuebet_alert SET match_id = 68395 WHERE match_id = 68414;
DELETE FROM match_record WHERE id = 68414;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 68395)),
            updated_at = NOW()
        WHERE id = 68395;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 67611 
              AND p.match_id = 67751
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 67751 WHERE match_id = 67611;
UPDATE odds_snapshot SET match_id = 67751 WHERE match_id = 67611;
UPDATE surebet_alert SET match_id = 67751 WHERE match_id = 67611;
UPDATE middle_alert SET match_id = 67751 WHERE match_id = 67611;
UPDATE valuebet_alert SET match_id = 67751 WHERE match_id = 67611;
DELETE FROM match_record WHERE id = 67611;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 67751)),
            updated_at = NOW()
        WHERE id = 67751;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 68197 
              AND p.match_id = 67467
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 67467 WHERE match_id = 68197;
UPDATE odds_snapshot SET match_id = 67467 WHERE match_id = 68197;
UPDATE surebet_alert SET match_id = 67467 WHERE match_id = 68197;
UPDATE middle_alert SET match_id = 67467 WHERE match_id = 68197;
UPDATE valuebet_alert SET match_id = 67467 WHERE match_id = 68197;
DELETE FROM match_record WHERE id = 68197;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 67467)),
            updated_at = NOW()
        WHERE id = 67467;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 67930 
              AND p.match_id = 67465
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 67465 WHERE match_id = 67930;
UPDATE odds_snapshot SET match_id = 67465 WHERE match_id = 67930;
UPDATE surebet_alert SET match_id = 67465 WHERE match_id = 67930;
UPDATE middle_alert SET match_id = 67465 WHERE match_id = 67930;
UPDATE valuebet_alert SET match_id = 67465 WHERE match_id = 67930;
DELETE FROM match_record WHERE id = 67930;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 67465)),
            updated_at = NOW()
        WHERE id = 67465;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 77599 
              AND p.match_id = 77668
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 77668 WHERE match_id = 77599;
UPDATE odds_snapshot SET match_id = 77668 WHERE match_id = 77599;
UPDATE surebet_alert SET match_id = 77668 WHERE match_id = 77599;
UPDATE middle_alert SET match_id = 77668 WHERE match_id = 77599;
UPDATE valuebet_alert SET match_id = 77668 WHERE match_id = 77599;
DELETE FROM match_record WHERE id = 77599;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 77668)),
            updated_at = NOW()
        WHERE id = 77668;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 79713 
              AND p.match_id = 79600
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 79600 WHERE match_id = 79713;
UPDATE odds_snapshot SET match_id = 79600 WHERE match_id = 79713;
UPDATE surebet_alert SET match_id = 79600 WHERE match_id = 79713;
UPDATE middle_alert SET match_id = 79600 WHERE match_id = 79713;
UPDATE valuebet_alert SET match_id = 79600 WHERE match_id = 79713;
DELETE FROM match_record WHERE id = 79713;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 79600)),
            updated_at = NOW()
        WHERE id = 79600;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 58520 
              AND p.match_id = 51314
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 51314 WHERE match_id = 58520;
UPDATE odds_snapshot SET match_id = 51314 WHERE match_id = 58520;
UPDATE surebet_alert SET match_id = 51314 WHERE match_id = 58520;
UPDATE middle_alert SET match_id = 51314 WHERE match_id = 58520;
UPDATE valuebet_alert SET match_id = 51314 WHERE match_id = 58520;
DELETE FROM match_record WHERE id = 58520;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 51314)),
            updated_at = NOW()
        WHERE id = 51314;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 47460 
              AND p.match_id = 34306
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 34306 WHERE match_id = 47460;
UPDATE odds_snapshot SET match_id = 34306 WHERE match_id = 47460;
UPDATE surebet_alert SET match_id = 34306 WHERE match_id = 47460;
UPDATE middle_alert SET match_id = 34306 WHERE match_id = 47460;
UPDATE valuebet_alert SET match_id = 34306 WHERE match_id = 47460;
DELETE FROM match_record WHERE id = 47460;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 34306)),
            updated_at = NOW()
        WHERE id = 34306;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70306 
              AND p.match_id = 70381
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 70381 WHERE match_id = 70306;
UPDATE odds_snapshot SET match_id = 70381 WHERE match_id = 70306;
UPDATE surebet_alert SET match_id = 70381 WHERE match_id = 70306;
UPDATE middle_alert SET match_id = 70381 WHERE match_id = 70306;
UPDATE valuebet_alert SET match_id = 70381 WHERE match_id = 70306;
DELETE FROM match_record WHERE id = 70306;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 70381)),
            updated_at = NOW()
        WHERE id = 70381;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70295 
              AND p.match_id = 69986
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69986 WHERE match_id = 70295;
UPDATE odds_snapshot SET match_id = 69986 WHERE match_id = 70295;
UPDATE surebet_alert SET match_id = 69986 WHERE match_id = 70295;
UPDATE middle_alert SET match_id = 69986 WHERE match_id = 70295;
UPDATE valuebet_alert SET match_id = 69986 WHERE match_id = 70295;
DELETE FROM match_record WHERE id = 70295;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69986)),
            updated_at = NOW()
        WHERE id = 69986;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 69198 
              AND p.match_id = 69184
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69184 WHERE match_id = 69198;
UPDATE odds_snapshot SET match_id = 69184 WHERE match_id = 69198;
UPDATE surebet_alert SET match_id = 69184 WHERE match_id = 69198;
UPDATE middle_alert SET match_id = 69184 WHERE match_id = 69198;
UPDATE valuebet_alert SET match_id = 69184 WHERE match_id = 69198;
DELETE FROM match_record WHERE id = 69198;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69184)),
            updated_at = NOW()
        WHERE id = 69184;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70337 
              AND p.match_id = 70382
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 70382 WHERE match_id = 70337;
UPDATE odds_snapshot SET match_id = 70382 WHERE match_id = 70337;
UPDATE surebet_alert SET match_id = 70382 WHERE match_id = 70337;
UPDATE middle_alert SET match_id = 70382 WHERE match_id = 70337;
UPDATE valuebet_alert SET match_id = 70382 WHERE match_id = 70337;
DELETE FROM match_record WHERE id = 70337;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 70382)),
            updated_at = NOW()
        WHERE id = 70382;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 69829 
              AND p.match_id = 69797
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69797 WHERE match_id = 69829;
UPDATE odds_snapshot SET match_id = 69797 WHERE match_id = 69829;
UPDATE surebet_alert SET match_id = 69797 WHERE match_id = 69829;
UPDATE middle_alert SET match_id = 69797 WHERE match_id = 69829;
UPDATE valuebet_alert SET match_id = 69797 WHERE match_id = 69829;
DELETE FROM match_record WHERE id = 69829;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69797)),
            updated_at = NOW()
        WHERE id = 69797;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70826 
              AND p.match_id = 70816
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 70816 WHERE match_id = 70826;
UPDATE odds_snapshot SET match_id = 70816 WHERE match_id = 70826;
UPDATE surebet_alert SET match_id = 70816 WHERE match_id = 70826;
UPDATE middle_alert SET match_id = 70816 WHERE match_id = 70826;
UPDATE valuebet_alert SET match_id = 70816 WHERE match_id = 70826;
DELETE FROM match_record WHERE id = 70826;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 70816)),
            updated_at = NOW()
        WHERE id = 70816;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 60070 
              AND p.match_id = 59865
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 59865 WHERE match_id = 60070;
UPDATE odds_snapshot SET match_id = 59865 WHERE match_id = 60070;
UPDATE surebet_alert SET match_id = 59865 WHERE match_id = 60070;
UPDATE middle_alert SET match_id = 59865 WHERE match_id = 60070;
UPDATE valuebet_alert SET match_id = 59865 WHERE match_id = 60070;
DELETE FROM match_record WHERE id = 60070;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 59865)),
            updated_at = NOW()
        WHERE id = 59865;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70205 
              AND p.match_id = 69719
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69719 WHERE match_id = 70205;
UPDATE odds_snapshot SET match_id = 69719 WHERE match_id = 70205;
UPDATE surebet_alert SET match_id = 69719 WHERE match_id = 70205;
UPDATE middle_alert SET match_id = 69719 WHERE match_id = 70205;
UPDATE valuebet_alert SET match_id = 69719 WHERE match_id = 70205;
DELETE FROM match_record WHERE id = 70205;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69719)),
            updated_at = NOW()
        WHERE id = 69719;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 38372 
              AND p.match_id = 461
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 461 WHERE match_id = 38372;
UPDATE odds_snapshot SET match_id = 461 WHERE match_id = 38372;
UPDATE surebet_alert SET match_id = 461 WHERE match_id = 38372;
UPDATE middle_alert SET match_id = 461 WHERE match_id = 38372;
UPDATE valuebet_alert SET match_id = 461 WHERE match_id = 38372;
DELETE FROM match_record WHERE id = 38372;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 461)),
            updated_at = NOW()
        WHERE id = 461;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 47319 
              AND p.match_id = 417
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 417 WHERE match_id = 47319;
UPDATE odds_snapshot SET match_id = 417 WHERE match_id = 47319;
UPDATE surebet_alert SET match_id = 417 WHERE match_id = 47319;
UPDATE middle_alert SET match_id = 417 WHERE match_id = 47319;
UPDATE valuebet_alert SET match_id = 417 WHERE match_id = 47319;
DELETE FROM match_record WHERE id = 47319;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 417)),
            updated_at = NOW()
        WHERE id = 417;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 47316 
              AND p.match_id = 462
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 462 WHERE match_id = 47316;
UPDATE odds_snapshot SET match_id = 462 WHERE match_id = 47316;
UPDATE surebet_alert SET match_id = 462 WHERE match_id = 47316;
UPDATE middle_alert SET match_id = 462 WHERE match_id = 47316;
UPDATE valuebet_alert SET match_id = 462 WHERE match_id = 47316;
DELETE FROM match_record WHERE id = 47316;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 462)),
            updated_at = NOW()
        WHERE id = 462;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 67929 
              AND p.match_id = 67948
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 67948 WHERE match_id = 67929;
UPDATE odds_snapshot SET match_id = 67948 WHERE match_id = 67929;
UPDATE surebet_alert SET match_id = 67948 WHERE match_id = 67929;
UPDATE middle_alert SET match_id = 67948 WHERE match_id = 67929;
UPDATE valuebet_alert SET match_id = 67948 WHERE match_id = 67929;
DELETE FROM match_record WHERE id = 67929;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 67948)),
            updated_at = NOW()
        WHERE id = 67948;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 74876 
              AND p.match_id = 74855
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 74855 WHERE match_id = 74876;
UPDATE odds_snapshot SET match_id = 74855 WHERE match_id = 74876;
UPDATE surebet_alert SET match_id = 74855 WHERE match_id = 74876;
UPDATE middle_alert SET match_id = 74855 WHERE match_id = 74876;
UPDATE valuebet_alert SET match_id = 74855 WHERE match_id = 74876;
DELETE FROM match_record WHERE id = 74876;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 74855)),
            updated_at = NOW()
        WHERE id = 74855;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 79707 
              AND p.match_id = 79598
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 79598 WHERE match_id = 79707;
UPDATE odds_snapshot SET match_id = 79598 WHERE match_id = 79707;
UPDATE surebet_alert SET match_id = 79598 WHERE match_id = 79707;
UPDATE middle_alert SET match_id = 79598 WHERE match_id = 79707;
UPDATE valuebet_alert SET match_id = 79598 WHERE match_id = 79707;
DELETE FROM match_record WHERE id = 79707;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 79598)),
            updated_at = NOW()
        WHERE id = 79598;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 80428 
              AND p.match_id = 80416
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 80416 WHERE match_id = 80428;
UPDATE odds_snapshot SET match_id = 80416 WHERE match_id = 80428;
UPDATE surebet_alert SET match_id = 80416 WHERE match_id = 80428;
UPDATE middle_alert SET match_id = 80416 WHERE match_id = 80428;
UPDATE valuebet_alert SET match_id = 80416 WHERE match_id = 80428;
DELETE FROM match_record WHERE id = 80428;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 80416)),
            updated_at = NOW()
        WHERE id = 80416;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 79710 
              AND p.match_id = 79599
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 79599 WHERE match_id = 79710;
UPDATE odds_snapshot SET match_id = 79599 WHERE match_id = 79710;
UPDATE surebet_alert SET match_id = 79599 WHERE match_id = 79710;
UPDATE middle_alert SET match_id = 79599 WHERE match_id = 79710;
UPDATE valuebet_alert SET match_id = 79599 WHERE match_id = 79710;
DELETE FROM match_record WHERE id = 79710;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 79599)),
            updated_at = NOW()
        WHERE id = 79599;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 48368 
              AND p.match_id = 35772
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 35772 WHERE match_id = 48368;
UPDATE odds_snapshot SET match_id = 35772 WHERE match_id = 48368;
UPDATE surebet_alert SET match_id = 35772 WHERE match_id = 48368;
UPDATE middle_alert SET match_id = 35772 WHERE match_id = 48368;
UPDATE valuebet_alert SET match_id = 35772 WHERE match_id = 48368;
DELETE FROM match_record WHERE id = 48368;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 35772)),
            updated_at = NOW()
        WHERE id = 35772;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 60228 
              AND p.match_id = 60017
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 60017 WHERE match_id = 60228;
UPDATE odds_snapshot SET match_id = 60017 WHERE match_id = 60228;
UPDATE surebet_alert SET match_id = 60017 WHERE match_id = 60228;
UPDATE middle_alert SET match_id = 60017 WHERE match_id = 60228;
UPDATE valuebet_alert SET match_id = 60017 WHERE match_id = 60228;
DELETE FROM match_record WHERE id = 60228;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 60017)),
            updated_at = NOW()
        WHERE id = 60017;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 69201 
              AND p.match_id = 69188
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69188 WHERE match_id = 69201;
UPDATE odds_snapshot SET match_id = 69188 WHERE match_id = 69201;
UPDATE surebet_alert SET match_id = 69188 WHERE match_id = 69201;
UPDATE middle_alert SET match_id = 69188 WHERE match_id = 69201;
UPDATE valuebet_alert SET match_id = 69188 WHERE match_id = 69201;
DELETE FROM match_record WHERE id = 69201;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69188)),
            updated_at = NOW()
        WHERE id = 69188;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59409 
              AND p.match_id = 59422
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 59422 WHERE match_id = 59409;
UPDATE odds_snapshot SET match_id = 59422 WHERE match_id = 59409;
UPDATE surebet_alert SET match_id = 59422 WHERE match_id = 59409;
UPDATE middle_alert SET match_id = 59422 WHERE match_id = 59409;
UPDATE valuebet_alert SET match_id = 59422 WHERE match_id = 59409;
DELETE FROM match_record WHERE id = 59409;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 59422)),
            updated_at = NOW()
        WHERE id = 59422;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 79715 
              AND p.match_id = 79597
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 79597 WHERE match_id = 79715;
UPDATE odds_snapshot SET match_id = 79597 WHERE match_id = 79715;
UPDATE surebet_alert SET match_id = 79597 WHERE match_id = 79715;
UPDATE middle_alert SET match_id = 79597 WHERE match_id = 79715;
UPDATE valuebet_alert SET match_id = 79597 WHERE match_id = 79715;
DELETE FROM match_record WHERE id = 79715;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 79597)),
            updated_at = NOW()
        WHERE id = 79597;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 69200 
              AND p.match_id = 69185
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69185 WHERE match_id = 69200;
UPDATE odds_snapshot SET match_id = 69185 WHERE match_id = 69200;
UPDATE surebet_alert SET match_id = 69185 WHERE match_id = 69200;
UPDATE middle_alert SET match_id = 69185 WHERE match_id = 69200;
UPDATE valuebet_alert SET match_id = 69185 WHERE match_id = 69200;
DELETE FROM match_record WHERE id = 69200;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69185)),
            updated_at = NOW()
        WHERE id = 69185;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 78641 
              AND p.match_id = 78338
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 78338 WHERE match_id = 78641;
UPDATE odds_snapshot SET match_id = 78338 WHERE match_id = 78641;
UPDATE surebet_alert SET match_id = 78338 WHERE match_id = 78641;
UPDATE middle_alert SET match_id = 78338 WHERE match_id = 78641;
UPDATE valuebet_alert SET match_id = 78338 WHERE match_id = 78641;
DELETE FROM match_record WHERE id = 78641;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 78338)),
            updated_at = NOW()
        WHERE id = 78338;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 61391 
              AND p.match_id = 61386
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 61386 WHERE match_id = 61391;
UPDATE odds_snapshot SET match_id = 61386 WHERE match_id = 61391;
UPDATE surebet_alert SET match_id = 61386 WHERE match_id = 61391;
UPDATE middle_alert SET match_id = 61386 WHERE match_id = 61391;
UPDATE valuebet_alert SET match_id = 61386 WHERE match_id = 61391;
DELETE FROM match_record WHERE id = 61391;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 61386)),
            updated_at = NOW()
        WHERE id = 61386;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 74907 
              AND p.match_id = 74938
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 74938 WHERE match_id = 74907;
UPDATE odds_snapshot SET match_id = 74938 WHERE match_id = 74907;
UPDATE surebet_alert SET match_id = 74938 WHERE match_id = 74907;
UPDATE middle_alert SET match_id = 74938 WHERE match_id = 74907;
UPDATE valuebet_alert SET match_id = 74938 WHERE match_id = 74907;
DELETE FROM match_record WHERE id = 74907;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 74938)),
            updated_at = NOW()
        WHERE id = 74938;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 68578 
              AND p.match_id = 68786
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 68786 WHERE match_id = 68578;
UPDATE odds_snapshot SET match_id = 68786 WHERE match_id = 68578;
UPDATE surebet_alert SET match_id = 68786 WHERE match_id = 68578;
UPDATE middle_alert SET match_id = 68786 WHERE match_id = 68578;
UPDATE valuebet_alert SET match_id = 68786 WHERE match_id = 68578;
DELETE FROM match_record WHERE id = 68578;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 68786)),
            updated_at = NOW()
        WHERE id = 68786;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 80427 
              AND p.match_id = 80415
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 80415 WHERE match_id = 80427;
UPDATE odds_snapshot SET match_id = 80415 WHERE match_id = 80427;
UPDATE surebet_alert SET match_id = 80415 WHERE match_id = 80427;
UPDATE middle_alert SET match_id = 80415 WHERE match_id = 80427;
UPDATE valuebet_alert SET match_id = 80415 WHERE match_id = 80427;
DELETE FROM match_record WHERE id = 80427;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 80415)),
            updated_at = NOW()
        WHERE id = 80415;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70752 
              AND p.match_id = 70874
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 70874 WHERE match_id = 70752;
UPDATE odds_snapshot SET match_id = 70874 WHERE match_id = 70752;
UPDATE surebet_alert SET match_id = 70874 WHERE match_id = 70752;
UPDATE middle_alert SET match_id = 70874 WHERE match_id = 70752;
UPDATE valuebet_alert SET match_id = 70874 WHERE match_id = 70752;
DELETE FROM match_record WHERE id = 70752;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 70874)),
            updated_at = NOW()
        WHERE id = 70874;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 69830 
              AND p.match_id = 69796
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 69796 WHERE match_id = 69830;
UPDATE odds_snapshot SET match_id = 69796 WHERE match_id = 69830;
UPDATE surebet_alert SET match_id = 69796 WHERE match_id = 69830;
UPDATE middle_alert SET match_id = 69796 WHERE match_id = 69830;
UPDATE valuebet_alert SET match_id = 69796 WHERE match_id = 69830;
DELETE FROM match_record WHERE id = 69830;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 69796)),
            updated_at = NOW()
        WHERE id = 69796;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 76159 
              AND p.match_id = 41278
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 41278 WHERE match_id = 76159;
UPDATE odds_snapshot SET match_id = 41278 WHERE match_id = 76159;
UPDATE surebet_alert SET match_id = 41278 WHERE match_id = 76159;
UPDATE middle_alert SET match_id = 41278 WHERE match_id = 76159;
UPDATE valuebet_alert SET match_id = 41278 WHERE match_id = 76159;
DELETE FROM match_record WHERE id = 76159;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 41278)),
            updated_at = NOW()
        WHERE id = 41278;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 78130 
              AND p.match_id = 77663
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 77663 WHERE match_id = 78130;
UPDATE odds_snapshot SET match_id = 77663 WHERE match_id = 78130;
UPDATE surebet_alert SET match_id = 77663 WHERE match_id = 78130;
UPDATE middle_alert SET match_id = 77663 WHERE match_id = 78130;
UPDATE valuebet_alert SET match_id = 77663 WHERE match_id = 78130;
DELETE FROM match_record WHERE id = 78130;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 77663)),
            updated_at = NOW()
        WHERE id = 77663;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 57110 
              AND p.match_id = 56802
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 56802 WHERE match_id = 57110;
UPDATE odds_snapshot SET match_id = 56802 WHERE match_id = 57110;
UPDATE surebet_alert SET match_id = 56802 WHERE match_id = 57110;
UPDATE middle_alert SET match_id = 56802 WHERE match_id = 57110;
UPDATE valuebet_alert SET match_id = 56802 WHERE match_id = 57110;
DELETE FROM match_record WHERE id = 57110;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 56802)),
            updated_at = NOW()
        WHERE id = 56802;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 28928 
              AND p.match_id = 463
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 463 WHERE match_id = 28928;
UPDATE odds_snapshot SET match_id = 463 WHERE match_id = 28928;
UPDATE surebet_alert SET match_id = 463 WHERE match_id = 28928;
UPDATE middle_alert SET match_id = 463 WHERE match_id = 28928;
UPDATE valuebet_alert SET match_id = 463 WHERE match_id = 28928;
DELETE FROM match_record WHERE id = 28928;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 463)),
            updated_at = NOW()
        WHERE id = 463;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 59239 
              AND p.match_id = 491
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 491 WHERE match_id = 59239;
UPDATE odds_snapshot SET match_id = 491 WHERE match_id = 59239;
UPDATE surebet_alert SET match_id = 491 WHERE match_id = 59239;
UPDATE middle_alert SET match_id = 491 WHERE match_id = 59239;
UPDATE valuebet_alert SET match_id = 491 WHERE match_id = 59239;
DELETE FROM match_record WHERE id = 59239;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 491)),
            updated_at = NOW()
        WHERE id = 491;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 74943 
              AND p.match_id = 74995
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 74995 WHERE match_id = 74943;
UPDATE odds_snapshot SET match_id = 74995 WHERE match_id = 74943;
UPDATE surebet_alert SET match_id = 74995 WHERE match_id = 74943;
UPDATE middle_alert SET match_id = 74995 WHERE match_id = 74943;
UPDATE valuebet_alert SET match_id = 74995 WHERE match_id = 74943;
DELETE FROM match_record WHERE id = 74943;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 74995)),
            updated_at = NOW()
        WHERE id = 74995;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 74947 
              AND p.match_id = 74991
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 74991 WHERE match_id = 74947;
UPDATE odds_snapshot SET match_id = 74991 WHERE match_id = 74947;
UPDATE surebet_alert SET match_id = 74991 WHERE match_id = 74947;
UPDATE middle_alert SET match_id = 74991 WHERE match_id = 74947;
UPDATE valuebet_alert SET match_id = 74991 WHERE match_id = 74947;
DELETE FROM match_record WHERE id = 74947;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 74991)),
            updated_at = NOW()
        WHERE id = 74991;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 70827 
              AND p.match_id = 70815
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 70815 WHERE match_id = 70827;
UPDATE odds_snapshot SET match_id = 70815 WHERE match_id = 70827;
UPDATE surebet_alert SET match_id = 70815 WHERE match_id = 70827;
UPDATE middle_alert SET match_id = 70815 WHERE match_id = 70827;
UPDATE valuebet_alert SET match_id = 70815 WHERE match_id = 70827;
DELETE FROM match_record WHERE id = 70827;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 70815)),
            updated_at = NOW()
        WHERE id = 70815;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 72483 
              AND p.match_id = 78845
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 78845 WHERE match_id = 72483;
UPDATE odds_snapshot SET match_id = 78845 WHERE match_id = 72483;
UPDATE surebet_alert SET match_id = 78845 WHERE match_id = 72483;
UPDATE middle_alert SET match_id = 78845 WHERE match_id = 72483;
UPDATE valuebet_alert SET match_id = 78845 WHERE match_id = 72483;
DELETE FROM match_record WHERE id = 72483;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 78845)),
            updated_at = NOW()
        WHERE id = 78845;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 73217 
              AND p.match_id = 73083
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 73083 WHERE match_id = 73217;
UPDATE odds_snapshot SET match_id = 73083 WHERE match_id = 73217;
UPDATE surebet_alert SET match_id = 73083 WHERE match_id = 73217;
UPDATE middle_alert SET match_id = 73083 WHERE match_id = 73217;
UPDATE valuebet_alert SET match_id = 73083 WHERE match_id = 73217;
DELETE FROM match_record WHERE id = 73217;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 73083)),
            updated_at = NOW()
        WHERE id = 73083;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 77665 
              AND p.match_id = 77255
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 77255 WHERE match_id = 77665;
UPDATE odds_snapshot SET match_id = 77255 WHERE match_id = 77665;
UPDATE surebet_alert SET match_id = 77255 WHERE match_id = 77665;
UPDATE middle_alert SET match_id = 77255 WHERE match_id = 77665;
UPDATE valuebet_alert SET match_id = 77255 WHERE match_id = 77665;
DELETE FROM match_record WHERE id = 77665;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 77255)),
            updated_at = NOW()
        WHERE id = 77255;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 73821 
              AND p.match_id = 73777
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 73777 WHERE match_id = 73821;
UPDATE odds_snapshot SET match_id = 73777 WHERE match_id = 73821;
UPDATE surebet_alert SET match_id = 73777 WHERE match_id = 73821;
UPDATE middle_alert SET match_id = 73777 WHERE match_id = 73821;
UPDATE valuebet_alert SET match_id = 73777 WHERE match_id = 73821;
DELETE FROM match_record WHERE id = 73821;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 73777)),
            updated_at = NOW()
        WHERE id = 73777;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 72445 
              AND p.match_id = 41377
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 41377 WHERE match_id = 72445;
UPDATE odds_snapshot SET match_id = 41377 WHERE match_id = 72445;
UPDATE surebet_alert SET match_id = 41377 WHERE match_id = 72445;
UPDATE middle_alert SET match_id = 41377 WHERE match_id = 72445;
UPDATE valuebet_alert SET match_id = 41377 WHERE match_id = 72445;
DELETE FROM match_record WHERE id = 72445;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 41377)),
            updated_at = NOW()
        WHERE id = 41377;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 44792 
              AND p.match_id = 409
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 409 WHERE match_id = 44792;
UPDATE odds_snapshot SET match_id = 409 WHERE match_id = 44792;
UPDATE surebet_alert SET match_id = 409 WHERE match_id = 44792;
UPDATE middle_alert SET match_id = 409 WHERE match_id = 44792;
UPDATE valuebet_alert SET match_id = 409 WHERE match_id = 44792;
DELETE FROM match_record WHERE id = 44792;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 409)),
            updated_at = NOW()
        WHERE id = 409;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 80194 
              AND p.match_id = 80177
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 80177 WHERE match_id = 80194;
UPDATE odds_snapshot SET match_id = 80177 WHERE match_id = 80194;
UPDATE surebet_alert SET match_id = 80177 WHERE match_id = 80194;
UPDATE middle_alert SET match_id = 80177 WHERE match_id = 80194;
UPDATE valuebet_alert SET match_id = 80177 WHERE match_id = 80194;
DELETE FROM match_record WHERE id = 80194;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 80177)),
            updated_at = NOW()
        WHERE id = 80177;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 80243 
              AND p.match_id = 80235
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 80235 WHERE match_id = 80243;
UPDATE odds_snapshot SET match_id = 80235 WHERE match_id = 80243;
UPDATE surebet_alert SET match_id = 80235 WHERE match_id = 80243;
UPDATE middle_alert SET match_id = 80235 WHERE match_id = 80243;
UPDATE valuebet_alert SET match_id = 80235 WHERE match_id = 80243;
DELETE FROM match_record WHERE id = 80243;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 80235)),
            updated_at = NOW()
        WHERE id = 80235;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 80244 
              AND p.match_id = 80232
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 80232 WHERE match_id = 80244;
UPDATE odds_snapshot SET match_id = 80232 WHERE match_id = 80244;
UPDATE surebet_alert SET match_id = 80232 WHERE match_id = 80244;
UPDATE middle_alert SET match_id = 80232 WHERE match_id = 80244;
UPDATE valuebet_alert SET match_id = 80232 WHERE match_id = 80244;
DELETE FROM match_record WHERE id = 80244;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 80232)),
            updated_at = NOW()
        WHERE id = 80232;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 80246 
              AND p.match_id = 80234
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 80234 WHERE match_id = 80246;
UPDATE odds_snapshot SET match_id = 80234 WHERE match_id = 80246;
UPDATE surebet_alert SET match_id = 80234 WHERE match_id = 80246;
UPDATE middle_alert SET match_id = 80234 WHERE match_id = 80246;
UPDATE valuebet_alert SET match_id = 80234 WHERE match_id = 80246;
DELETE FROM match_record WHERE id = 80246;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 80234)),
            updated_at = NOW()
        WHERE id = 80234;
        

            DELETE FROM odds_actual d
            USING odds_actual p
            WHERE d.match_id = 80247 
              AND p.match_id = 80237
              AND d.bet_source_id = p.bet_source_id
              AND d.odds_type_id = p.odds_type_id
              AND COALESCE(d.param, -10000) = COALESCE(p.param, -10000);
            
UPDATE odds_actual SET match_id = 80237 WHERE match_id = 80247;
UPDATE odds_snapshot SET match_id = 80237 WHERE match_id = 80247;
UPDATE surebet_alert SET match_id = 80237 WHERE match_id = 80247;
UPDATE middle_alert SET match_id = 80237 WHERE match_id = 80247;
UPDATE valuebet_alert SET match_id = 80237 WHERE match_id = 80247;
DELETE FROM match_record WHERE id = 80247;

        UPDATE match_record 
        SET source_count = GREATEST(1, (SELECT count(DISTINCT bet_source_id) FROM odds_actual WHERE match_id = 80237)),
            updated_at = NOW()
        WHERE id = 80237;
        
COMMIT;