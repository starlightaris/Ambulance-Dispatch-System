# Task 4 Supabase database

Task 4 stores triage assessments in the private `task4` PostgreSQL schema. No
tables belonging to Tasks 1, 2, 3, or 5 are created or changed by this setup.

## Setup

1. In the Supabase SQL Editor, run
   `supabase/migrations/20260822133000_create_task4_triage_schema.sql`.
2. Copy `application-local.properties.example` to
   `application-local.properties` in the repository root.
3. In Supabase, open **Connect**, select **Session pooler**, then the **JDBC** tab.
4. Put the JDBC URL in the `SUPABASE_DB_URL` environment variable. Use port
   `5432` and ensure the URL contains `sslmode=require`.
5. Start the API with `./mvnw spring-boot:run`.

Do not use the transaction pooler on port `6543` for Hibernate and do not commit
the database URL or password.

## Task 4 tables

- `task4.triage_assessments` stores patient inputs, the assigned MTS category,
  weighted score, queue severity, creation time, and resolution state.
- `task4.triage_assessment_symptoms` stores the ordered symptoms for each
  assessment and deletes them automatically when the assessment is deleted.

The active-queue index follows Task 4's dispatch order: unresolved first, then
severity descending, weighted score descending, and creation time ascending.
