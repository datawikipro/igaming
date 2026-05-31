-- Insert allowed regions for bookmakers to allow cross-region proxy usage in target_allowed_regions
INSERT INTO target_allowed_regions (target_id, region_code)
SELECT id, 'ru' FROM health_check_target WHERE target_code IN ('baltbet', 'winline', 'fonbet', 'marathonbet', 'betboom', 'betcity', 'zenit', 'olimpbet', 'sportbet', 'leon', 'pari', 'ligastavok', 'bettery', 'olimpbet-kz')
ON CONFLICT DO NOTHING;

INSERT INTO target_allowed_regions (target_id, region_code)
SELECT id, 'us' FROM health_check_target WHERE target_code IN ('baltbet', 'winline', 'fonbet', 'marathonbet', 'betboom', 'betcity', 'zenit', 'olimpbet', 'sportbet', 'leon', 'pari', 'ligastavok', 'bettery')
ON CONFLICT DO NOTHING;

INSERT INTO target_allowed_regions (target_id, region_code)
SELECT id, 'de' FROM health_check_target WHERE target_code IN ('baltbet', 'winline', 'fonbet', 'marathonbet', 'betboom', 'betcity', 'zenit', 'olimpbet', 'sportbet', 'leon', 'pari', 'ligastavok', 'bettery')
ON CONFLICT DO NOTHING;

INSERT INTO target_allowed_regions (target_id, region_code)
SELECT id, 'kz' FROM health_check_target WHERE target_code = 'olimpbet-kz'
ON CONFLICT DO NOTHING;
