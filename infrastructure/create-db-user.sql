-- Run this as the postgres master user after RDS is created
-- psql -h <RDS_ENDPOINT> -U postgres -d actiontracker -f infrastructure/create-db-user.sql

CREATE USER actiontracker_app WITH PASSWORD 'REPLACE_WITH_APP_DB_PASSWORD';

GRANT CONNECT ON DATABASE actiontracker TO actiontracker_app;
GRANT ALL PRIVILEGES ON DATABASE actiontracker TO actiontracker_app;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO actiontracker_app;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO actiontracker_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO actiontracker_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO actiontracker_app;
