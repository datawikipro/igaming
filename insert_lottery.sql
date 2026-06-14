INSERT INTO sport (id, display_name, is_three_way_possible, priority, is_temporary) VALUES ('LOTTERY', 'ЛОТЕРЕИ', true, 0, false) ON CONFLICT DO NOTHING;
