-- SQL Script to seed 1xBet in bet_source table
INSERT INTO public.bet_source (id, is_active, created_at, display_name, is_trusted) 
VALUES ('1xbet', true, NOW(), '1xBet', true)
ON CONFLICT (id) DO NOTHING;
