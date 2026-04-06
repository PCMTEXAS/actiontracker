#!/usr/bin/env bash
# =============================================================================
# ActionTracker — Supabase -> RDS Data Migration
#
# Prerequisites: pg_dump and psql installed (brew install postgresql)
# Run AFTER RDS is provisioned and actiontracker_app user is created.
#
# Usage:
#   chmod +x infrastructure/migrate-data.sh
#   ./infrastructure/migrate-data.sh
# =============================================================================

set -euo pipefail

SUPABASE_HOST="db.dhpvkiguitysrdrccilt.supabase.co"
SUPABASE_USER="actiontracker_app"
SUPABASE_DB="postgres"

RDS_HOST="REPLACE_WITH_RDS_ENDPOINT"   # e.g. actiontracker-db.abc123.us-east-1.rds.amazonaws.com
RDS_USER="postgres"
RDS_DB="actiontracker"

DUMP_FILE="/tmp/actiontracker_export.sql"

echo "==> Exporting from Supabase..."
echo "    (You will be prompted for the Supabase DB password)"
PGPASSWORD="" pg_dump \
  --host="$SUPABASE_HOST" \
  --port=5432 \
  --username="$SUPABASE_USER" \
  --dbname="$SUPABASE_DB" \
  --no-owner \
  --no-acl \
  --schema=public \
  --format=plain \
  --file="$DUMP_FILE"

echo "    Export complete: $DUMP_FILE"
echo "    Size: $(du -sh $DUMP_FILE | cut -f1)"

echo ""
echo "==> Previewing tables in export..."
grep "^CREATE TABLE" "$DUMP_FILE" || echo "    (no CREATE TABLE statements found — schema may already exist in RDS)"

echo ""
echo "==> Importing to RDS..."
echo "    (You will be prompted for the RDS postgres master password)"
echo "    NOTE: RDS must be reachable — run this from an EC2 bastion in the same VPC,"
echo "          or temporarily enable public access on the RDS instance."
PGPASSWORD="" psql \
  --host="$RDS_HOST" \
  --port=5432 \
  --username="$RDS_USER" \
  --dbname="$RDS_DB" \
  --file="$DUMP_FILE"

echo ""
echo "==> Verifying row counts..."
PGPASSWORD="" psql \
  --host="$RDS_HOST" \
  --port=5432 \
  --username="$RDS_USER" \
  --dbname="$RDS_DB" \
  --command="SELECT schemaname, tablename, n_live_tup FROM pg_stat_user_tables ORDER BY tablename;"

echo ""
echo "==> Migration complete. Compare row counts against Supabase before cutover."
