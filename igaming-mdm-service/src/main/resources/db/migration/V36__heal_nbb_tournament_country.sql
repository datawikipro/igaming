-- V36: Fix Novo Basquete Brasil (NBB) tournament country from Israel (499) to Brazil (176)
UPDATE mdm_tournament SET country_id = 176 WHERE id = 19684 AND country_id = 499;
