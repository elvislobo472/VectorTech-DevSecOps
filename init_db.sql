-- VectorTech Database Initialization Script
-- This script is run automatically when PostgreSQL container starts

-- Ensure schema exists
CREATE SCHEMA IF NOT EXISTS public;

-- Set schema permissions
ALTER SCHEMA public OWNER TO admin;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Optionally load initial data
-- \i /docker-entrypoint-initdb.d/data.sql
