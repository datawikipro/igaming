CREATE OR REPLACE FUNCTION public.fn_gatekeeper_lineup_guard()
 RETURNS trigger
 LANGUAGE plpgsql
AS $$
DECLARE
    v_start bigint;
    v_now bigint;
    v_sport varchar;
BEGIN
    IF NEW.match_start_time IS NOT NULL THEN
        v_start := (EXTRACT(EPOCH FROM NEW.match_start_time) * 1000)::bigint;
    END IF;

    v_sport := NEW.sport_name;

    IF v_sport IS NULL THEN
        v_sport := 'FOOTBALL';
    END IF;

    v_now := (EXTRACT(EPOCH FROM now()) * 1000)::bigint;

    IF v_start IS NOT NULL THEN
        IF (v_now - v_start) > (30 * 60 * 1000) THEN
            RAISE EXCEPTION 'GATEKEEPER VETO: Match #% started more than 30 minutes ago (v_start=%, now=%)!', NEW.match_id, v_start, v_now;
        END IF;

        IF (v_start - v_now) > (72 * 60 * 60 * 1000) THEN
            RAISE EXCEPTION 'GATEKEEPER VETO: Match #% is >72h away from start!', NEW.match_id;
        END IF;
    END IF;

    IF NEW.lineup_status IS NULL THEN
        NEW.lineup_status := 'ESTIMATED';
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_gatekeeper_lineup_guard ON public.match_ai_recommendation;
CREATE TRIGGER trg_gatekeeper_lineup_guard BEFORE INSERT ON public.match_ai_recommendation FOR EACH ROW EXECUTE FUNCTION fn_gatekeeper_lineup_guard();
