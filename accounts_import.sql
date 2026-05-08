INSERT INTO cloud_accounts (name, provider, project_id, default_region, default_zone, credentials_json, is_active, priority, created_at)
VALUES 
('betting_hacker', 'GCP', 'project-cd5288a2-f8f8-4142-923', 'europe-north2', 'europe-north2-b', '{"account": "", "client_id": "32555940559.apps.googleusercontent.com", "client_secret": "ZmssLNjJy2998hD4CTg2ejr2", "refresh_token": "MASKED", "type": "authorized_user", "universe_domain": "googleapis.com"}', true, 1, NOW()),
('test1', 'GCP', 'project-ec074516-4738-4d28-ad5', 'europe-north1', 'europe-north1-b', '{"account": "", "client_id": "32555940559.apps.googleusercontent.com", "client_secret": "ZmssLNjJy2998hD4CTg2ejr2", "refresh_token": "MASKED", "type": "authorized_user", "universe_domain": "googleapis.com"}', true, 2, NOW());
