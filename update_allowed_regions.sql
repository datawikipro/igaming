-- Update allowed regions for bookmakers to allow cross-region proxy usage
UPDATE health_check_target SET allowed_regions = 'ru,us,de' WHERE target_code IN ('baltbet', 'winline', 'fonbet', 'marathonbet', 'betboom', 'betcity', 'zenit', 'olimpbet', 'sportbet', 'leon', 'pari', 'ligastavok', 'bettery');
UPDATE health_check_target SET allowed_regions = 'kz,ru,us,de' WHERE target_code = 'olimpbet-kz';
