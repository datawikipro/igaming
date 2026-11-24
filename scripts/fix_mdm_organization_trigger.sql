CREATE OR REPLACE FUNCTION public.trg_mdm_team_auto_organization()
RETURNS trigger AS $func$
DECLARE
    v_org_id BIGINT;
    v_base_slug VARCHAR(191);
    v_base_name VARCHAR(255);
BEGIN
    IF NEW.organization_id IS NULL THEN
        v_base_slug := regexp_replace(NEW.slug, '-(?:u1[5-9]|u2[0-3]|youth|women|fem)$', '', 'i');
        v_base_name := regexp_replace(NEW.canonical_name, '\s*\((?:U1[5-9]|U2[0-3]|Youth|Women|W)\)$', '', 'i');

        SELECT id INTO v_org_id
        FROM public.mdm_organization
        WHERE slug = v_base_slug
           OR lower(canonical_name) = lower(v_base_name)
        LIMIT 1;

        IF v_org_id IS NULL THEN
            INSERT INTO public.mdm_organization (canonical_name, slug, country_id, country_name, logo_url)
            VALUES (v_base_name, v_base_slug, NEW.country_id, NEW.country_name, NEW.logo_url)
            RETURNING id INTO v_org_id;
        END IF;

        NEW.organization_id := v_org_id;
    END IF;

    RETURN NEW;
END;
$func$ LANGUAGE plpgsql;
