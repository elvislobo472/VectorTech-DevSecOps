#!/bin/sh
set -e

: "${DB_HOST:?DB_HOST is required}"
: "${DB_PORT:?DB_PORT is required}"

until nc -z "$DB_HOST" "$DB_PORT"; do
  echo "Waiting for database at $DB_HOST:$DB_PORT..."
  sleep 2
done

exec java $JAVA_OPTS -jar /app/app.jar
