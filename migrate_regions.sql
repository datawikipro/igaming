-- Migrate allowed regions from CSV column to separate join table
DO $$
DECLARE
    target_rec RECORD;
    region_code TEXT;
BEGIN
    FOR target_rec IN SELECT id, allowed_regions FROM health_check_target WHERE allowed_regions IS NOT NULL LOOP
        FOR region_code IN SELECT unnest(string_to_array(target_rec.allowed_regions, ',')) LOOP
            INSERT INTO target_allowed_regions (target_id, region_code)
            VALUES (target_rec.id, trim(region_code))
            ON CONFLICT DO NOTHING;
        END LOOP;
    END LOOP;
END $$;

-- Optional: drop the old column if you are sure
-- ALTER TABLE health_check_target DROP COLUMN allowed_regions;
