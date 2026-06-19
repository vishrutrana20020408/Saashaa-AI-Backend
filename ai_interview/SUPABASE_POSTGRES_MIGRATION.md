# Supabase PostgreSQL Migration Instructions

## PostgreSQL equivalents for MySQL schema

- `INT AUTO_INCREMENT PRIMARY KEY` → `BIGSERIAL PRIMARY KEY`
- `INT UNSIGNED` → `INTEGER` or `BIGINT`
- `VARCHAR(255)` → `VARCHAR(255)` or `TEXT` for AI resume storage
- `LONGTEXT` / `TEXT` → `TEXT`
- `DATETIME` → `TIMESTAMPTZ`
- `TIMESTAMP DEFAULT CURRENT_TIMESTAMP` → `TIMESTAMPTZ DEFAULT now()`
- `TINYINT(1)` → `BOOLEAN`
- `ENUM('x','y')` → `VARCHAR(...) CHECK (...)` or `TEXT` with application validation
- `BLOB` / `LONGBLOB` → `BYTEA`
- `CHARSET utf8mb4` → PostgreSQL uses UTF-8 by default
- `ENGINE=InnoDB` → omit engine specification

## Recommended AI resume storage types

- `resume_text TEXT`
- `ai_prompt TEXT`
- `resume_metadata JSONB`
- `generated_summary TEXT`
- `created_at TIMESTAMPTZ DEFAULT now()`
- `updated_at TIMESTAMPTZ DEFAULT now()`

## Example Supabase table

```sql
CREATE TABLE interview_session (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(128) NOT NULL,
  candidate_email VARCHAR(255) NOT NULL,
  status VARCHAR(64) NOT NULL,
  resume_text TEXT,
  ai_prompt TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

## Supabase connection variables

- `POSTGRES_HOST`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

> The frontend must not connect directly to Supabase. All access must go through Spring Boot REST controllers.
