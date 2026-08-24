-- Task 4 only: triage persistence schema for Supabase PostgreSQL.
-- Apply through the Supabase SQL Editor or Supabase CLI before starting the app.

create schema if not exists task4;

create table if not exists task4.triage_assessments (
    id uuid primary key default gen_random_uuid(),
    breathing boolean not null,
    pulse_rate integer not null check (pulse_rate between 0 and 300),
    avpu varchar(16) not null check (avpu in ('ALERT', 'VOICE', 'PAIN', 'UNRESPONSIVE')),
    oxygen_saturation integer not null check (oxygen_saturation between 0 and 100),
    systolic_bp integer not null check (systolic_bp between 0 and 300),
    pain_score integer not null check (pain_score between 0 and 10),
    temperature double precision not null check (temperature between 20 and 45),
    age integer not null check (age between 0 and 130),
    hazard_present boolean not null,
    assigned_category varchar(16) not null
        check (assigned_category in ('RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE')),
    tie_breaker_score double precision not null check (tie_breaker_score >= 0),
    created_at timestamp without time zone not null default (current_timestamp at time zone 'UTC'),
    resolved boolean not null default false,
    severity_rank integer not null check (severity_rank between 1 and 5)
);

create table if not exists task4.triage_assessment_symptoms (
    assessment_id uuid not null
        references task4.triage_assessments(id) on delete cascade,
    symptom_order integer not null,
    symptom varchar(255) not null,
    primary key (assessment_id, symptom_order)
);

create index if not exists idx_triage_active_queue
    on task4.triage_assessments (
        resolved,
        severity_rank desc,
        tie_breaker_score desc,
        created_at asc
    );

create index if not exists idx_triage_symptoms_assessment
    on task4.triage_assessment_symptoms (assessment_id);

-- The Spring backend connects as the database user. Keep Task 4 tables private
-- from Supabase's browser-facing anon/authenticated Data API roles.
revoke all on schema task4 from anon, authenticated;
revoke all on all tables in schema task4 from anon, authenticated;

alter table task4.triage_assessments enable row level security;
alter table task4.triage_assessment_symptoms enable row level security;
