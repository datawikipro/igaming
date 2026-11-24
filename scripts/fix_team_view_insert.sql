CREATE OR REPLACE FUNCTION public.fn_team_insert()
RETURNS trigger AS $func$
DECLARE
    v_sport_id INT;
    v_new_id BIGINT;
    v_canonical VARCHAR(255);
    v_slug VARCHAR(191);
    v_sig VARCHAR(255);
BEGIN
    -- 1. Fetch next sequence ID from igaming_mdm
    SELECT next_id INTO v_new_id 
    FROM dblink('dbname=igaming_mdm user=postgres password=postgres', 'SELECT nextval(''mdm_team_id_seq'')') 
    AS t(next_id bigint);

    -- 2. Resolve sport integer id
    SELECT id INTO v_sport_id FROM mdm_sport WHERE domain_key = NEW.sport_id LIMIT 1;
    IF v_sport_id IS NULL THEN
        BEGIN
            v_sport_id := NEW.sport_id::INT;
        EXCEPTION WHEN OTHERS THEN
            v_sport_id := 1;
        END;
    END IF;

    v_canonical := COALESCE(NEW.default_display_name, NEW.name_english, NEW.name_local, NEW.name_bookmaker, 'Unknown Team');
    v_slug := lower(regexp_replace(v_canonical, '[^a-zA-Z0-9]+', '-', 'g')) || '-' || floor(random() * 1000000)::text;
    v_sig := upper(regexp_replace(v_canonical, '[^a-zA-Z0-9]+', '', 'g'));

    -- 3. Insert into foreign table with explicit ID
    INSERT INTO mdm_team (
        id, canonical_name, sport_id, slug, normalized_signature, is_national, is_individual, gender, age_category,
        popularity_rank, source_origin, created_at, updated_at
    ) VALUES (
        v_new_id, v_canonical, v_sport_id, v_slug, v_sig, false, false, 'UNKNOWN', 'SENIOR',
        1000, 'NATIVE_LOCAL', now(), now()
    );

    NEW.id := v_new_id;
    RETURN NEW;
END;
$func$ LANGUAGE plpgsql;

DROP RULE IF EXISTS team_ins ON public.team;

DROP TRIGGER IF EXISTS trg_team_ins ON public.team;
CREATE TRIGGER trg_team_ins
    INSTEAD OF INSERT ON public.team
    FOR EACH ROW EXECUTE FUNCTION public.fn_team_insert();
