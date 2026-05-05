#!/bin/bash
set -e

# Create user and databases
sudo -u postgres psql <<'EOSQL'
CREATE USER igaming WITH PASSWORD 'igaming_pass_2026' CREATEDB;
CREATE DATABASE igaming_db OWNER igaming;
CREATE DATABASE smartbet_db OWNER igaming;
GRANT ALL PRIVILEGES ON DATABASE igaming_db TO igaming;
GRANT ALL PRIVILEGES ON DATABASE smartbet_db TO igaming;
\l
EOSQL

# Configure PostgreSQL to accept connections from internal network (10.226.x.x)
PG_CONF="/var/lib/pgsql/data/postgresql.conf"
HBA_CONF="/var/lib/pgsql/data/pg_hba.conf"

# Listen on all interfaces (VPC internal only — protected by GCP firewall)
sudo sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '10.226.0.3'/" $PG_CONF

# Allow connections from VPC subnet
echo "host    all             igaming         10.226.0.0/24           scram-sha-256" | sudo tee -a $HBA_CONF

# Restart PostgreSQL
sudo systemctl restart postgresql
sudo systemctl status postgresql --no-pager -l

echo "POSTGRES_SETUP_DONE"
