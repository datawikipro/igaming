-- Migration: add k3s and tailscale per-account fields
-- Run this on the admin_db database

ALTER TABLE cloud_accounts
    ADD COLUMN IF NOT EXISTS k3s_server_url  VARCHAR(255),
    ADD COLUMN IF NOT EXISTS k3s_token       TEXT,
    ADD COLUMN IF NOT EXISTS tailscale_auth_key VARCHAR(255);

-- Update betting_hacker (priority=1, europe-north2)
UPDATE cloud_accounts
SET k3s_server_url    = 'https://master-vm:6443',
    k3s_token         = 'K10c7e23826108f187591e9533b0f508af002ac6670d728c57e0b4919cac2725b15::server:84cc0f96d1b83f1620ba81127b42d739',
    tailscale_auth_key = 'tskey-auth-kZotQnypWz11CNTRL-tZfx6Wiowm2GwL6euwP5n2Ti4487uRHab'
WHERE name = 'betting_hacker';

-- Update test1 (priority=2, europe-north1)
-- Uses the same k3s cluster (master reachable via Tailscale magic DNS master-vm)
UPDATE cloud_accounts
SET k3s_server_url    = 'https://master-vm:6443',
    k3s_token         = 'K10c7e23826108f187591e9533b0f508af002ac6670d728c57e0b4919cac2725b15::server:84cc0f96d1b83f1620ba81127b42d739',
    tailscale_auth_key = 'tskey-auth-kZotQnypWz11CNTRL-tZfx6Wiowm2GwL6euwP5n2Ti4487uRHab'
WHERE name = 'test1';
