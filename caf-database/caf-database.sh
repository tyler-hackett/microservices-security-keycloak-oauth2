#!/bin/bash
set -e
psql -v ON_ERROR_STOP=1 -v password="$DATABASE_PASSWORD" \
  --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER cafuser PASSWORD :'password';
    CREATE DATABASE caf WITH OWNER cafuser;
    GRANT ALL PRIVILEGES ON DATABASE caf TO cafuser;
    \c caf postgres
    GRANT ALL ON SCHEMA public TO cafuser;
EOSQL