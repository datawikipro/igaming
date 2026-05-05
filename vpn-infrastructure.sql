-- VPN Pool Configuration - Initial Data Migration
-- Imports existing WireGuard configs from static YAML into the vpn_pool_config table.

-- Note: Table creation is handled by JPA/Hibernate ddl-auto=update.
-- This script only seeds the initial data.

-- Note: User configures VPN configs via UI, no static seeds needed here.
-- VPN Validation Targets вЂ” Bookmaker target URLs for proactive VPN health checks
-- These entries define which URLs to test each VPN proxy against every 20 minutes.

INSERT INTO vpn_validation_target (source_name, bookmaker_code, target_url, blocked_status_codes, timeout_seconds, is_active, created_at) VALUES
('igaming-source-winline', 'winline', 'https://api.winline.ru', '403', 15, true, NOW()),
('igaming-source-fonbet', 'fonbet', 'https://fon.bet', '403', 15, true, NOW()),
('igaming-source-marathonbet', 'marathonbet', 'https://www.marathonbet.ru', '403', 15, true, NOW()),
('igaming-source-betboom', 'betboom', 'https://siteapi.betboom.ru', '403', 15, true, NOW()),
('igaming-source-betcity', 'betcity', 'https://ad.betcity.ru', '403', 15, true, NOW()),
('igaming-source-baltbet', 'baltbet', 'https://events.baltbet.ru', '403', 15, true, NOW()),
('igaming-source-zenit', 'zenit', 'https://zenit.win', '403', 15, true, NOW()),
('igaming-source-olimpbet', 'olimpbet', 'https://www.olimp.bet', '403', 15, true, NOW()),
('igaming-source-sportbet', 'sportbet', 'https://sportbet.ru', '403', 15, true, NOW()),
('igaming-source-leon', 'leon', 'https://leon.ru', '403', 15, true, NOW()),
('igaming-source-pari', 'pari', 'https://pari.ru', '403', 15, true, NOW()),
('igaming-source-ligastavok', 'ligastavok', 'https://www.ligastavok.ru', '403', 15, true, NOW()),
('igaming-source-bettery', 'bettery', 'https://bettery.ru', '403', 15, true, NOW()),
('igaming-source-olimpbet-kz', 'olimpbet-kz', 'https://olimpbet.kz', '403', 15, true, NOW())
ON CONFLICT (source_name) DO NOTHING;
