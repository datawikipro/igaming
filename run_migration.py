import psycopg2
import sys

def migrate():
    dsn = "host=localhost port=5432 dbname=igaming_auth user=igaming_user password=igaming_password"
    print("Connecting to database...")
    try:
        conn = psycopg2.connect(dsn)
        conn.autocommit = True
        cur = conn.cursor()
        
        queries = [
            "ALTER TABLE users ADD COLUMN IF NOT EXISTS region VARCHAR(50) DEFAULT 'RU';",
            "ALTER TABLE users ADD COLUMN IF NOT EXISTS virtual_wallet_enabled BOOLEAN DEFAULT TRUE;",
            "ALTER TABLE users ADD COLUMN IF NOT EXISTS preselected_bookmakers TEXT;",
            "ALTER TABLE users ADD COLUMN IF NOT EXISTS sport_filters TEXT;",
            "ALTER TABLE users ADD COLUMN IF NOT EXISTS coefficient_types TEXT;",
            "ALTER TABLE users ADD COLUMN IF NOT EXISTS custom_subscription_enabled BOOLEAN DEFAULT FALSE;",
            "ALTER TABLE users ADD COLUMN IF NOT EXISTS custom_subscription_min_profit NUMERIC(5, 2) DEFAULT 0.00;",
            "ALTER TABLE users ADD COLUMN IF NOT EXISTS custom_subscription_sports TEXT;",
            "ALTER TABLE users ADD COLUMN IF NOT EXISTS custom_subscription_outcomes TEXT;",
            "ALTER TABLE users ADD COLUMN IF NOT EXISTS custom_subscription_bookmakers TEXT;"
        ]
        
        for q in queries:
            print(f"Executing: {q}")
            cur.execute(q)
            
        print("Migration complete!")
        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error executing migration: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == '__main__':
    migrate()
