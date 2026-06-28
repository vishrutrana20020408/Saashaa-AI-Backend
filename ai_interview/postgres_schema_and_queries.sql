-- =============================================================
-- PostgreSQL schema and common queries for AI Interview backend
-- =============================================================
-- Usage for Supabase SQL editor:
--   Run this script directly in the SQL editor of your Supabase project.
--   It does not require psql meta-commands or database creation.
-- =============================================================

-- Drop existing tables if they exist
DROP TABLE IF EXISTS tool_requirement_answers CASCADE;
DROP TABLE IF EXISTS job_applications CASCADE;
DROP TABLE IF EXISTS internal_job_applications CASCADE;
DROP TABLE IF EXISTS interview_evaluations CASCADE;
DROP TABLE IF EXISTS interview_turns CASCADE;
DROP TABLE IF EXISTS interview_sessions CASCADE;
DROP TABLE IF EXISTS resume_sections CASCADE;
DROP TABLE IF EXISTS resume_versions CASCADE;
DROP TABLE IF EXISTS resume_cloud_files CASCADE;
DROP TABLE IF EXISTS resume_file_assets CASCADE;
DROP TABLE IF EXISTS resumes CASCADE;
DROP TABLE IF EXISTS user_profiles CASCADE;
DROP TABLE IF EXISTS admin_profiles CASCADE;
DROP TABLE IF EXISTS jobs CASCADE;
DROP TABLE IF EXISTS "Company" CASCADE;
DROP TABLE IF EXISTS "Admin" CASCADE;
DROP TABLE IF EXISTS "Owners" CASCADE;
DROP TABLE IF EXISTS "Users" CASCADE;
DROP TABLE IF EXISTS "Notification" CASCADE;

-- =============================================================
-- 1) Users / Companies / Admins / Owners
-- =============================================================

CREATE TABLE "Users" (
  s_no BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  surname VARCHAR(255) NOT NULL,
  email_address VARCHAR(255) NOT NULL UNIQUE,
  mobile_number VARCHAR(20) NOT NULL,
  password VARCHAR(255) NOT NULL,
  user_created_date DATE NOT NULL,
  user_created_time TIME NOT NULL,
  share_id VARCHAR(36) NOT NULL UNIQUE,
  role VARCHAR(50) NOT NULL DEFAULT 'USER',
  resume_scanned BOOLEAN NOT NULL DEFAULT FALSE,
  resume_file_name VARCHAR(255) DEFAULT NULL,
  resume_score INTEGER DEFAULT NULL,
  onboarding_domain VARCHAR(255) DEFAULT NULL,
  onb_subdomain_mode VARCHAR(255) DEFAULT NULL,
  onb_subdomain_single VARCHAR(255) DEFAULT NULL,
  onb_subdomain_multi TEXT DEFAULT NULL,
  onb_job_titles TEXT DEFAULT NULL,
  onb_done BOOLEAN NOT NULL DEFAULT FALSE,
  profile_full_name VARCHAR(255) DEFAULT NULL,
  profile_headline VARCHAR(255) DEFAULT NULL,
  profile_location VARCHAR(255) DEFAULT NULL,
  profile_summary TEXT DEFAULT NULL,
  profile_picture_url VARCHAR(1000) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON "Users"(email_address);
CREATE INDEX idx_users_user_id ON "Users"(user_id);

CREATE TABLE "Company" (
  s_no BIGSERIAL PRIMARY KEY,
  company_id VARCHAR(36) NOT NULL UNIQUE,
  company_name VARCHAR(255) NOT NULL,
  company_type VARCHAR(255) NOT NULL,
  contact_person_name VARCHAR(255) NOT NULL,
  email_address VARCHAR(255) NOT NULL UNIQUE,
  mobile_number VARCHAR(10) NOT NULL,
  password VARCHAR(255) NOT NULL,
  company_created_date DATE NOT NULL,
  company_created_time TIME NOT NULL,
  share_id VARCHAR(36) NOT NULL UNIQUE,
  role VARCHAR(50) NOT NULL DEFAULT 'COMPANY',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_company_email ON "Company"(email_address);
CREATE INDEX idx_company_id ON "Company"(company_id);

CREATE TABLE "Admin" (
  s_no BIGSERIAL PRIMARY KEY,
  admin_id VARCHAR(36) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  surname VARCHAR(255) NOT NULL,
  email_address VARCHAR(255) NOT NULL UNIQUE,
  mobile_number VARCHAR(10) NOT NULL,
  password VARCHAR(255) NOT NULL,
  admin_created_date DATE NOT NULL,
  admin_created_time TIME NOT NULL,
  share_id VARCHAR(36) NOT NULL UNIQUE,
  role VARCHAR(50) NOT NULL DEFAULT 'ADMIN',
  onboarding_domain VARCHAR(255) DEFAULT NULL,
  onboarding_subdomain_mode VARCHAR(255) DEFAULT NULL,
  onboarding_subdomain_single VARCHAR(255) DEFAULT NULL,
  onboarding_subdomain_multi TEXT DEFAULT NULL,
  onboarding_job_titles TEXT DEFAULT NULL,
  onboarding_done BOOLEAN NOT NULL DEFAULT FALSE,
  class_10_marksheet_url VARCHAR(1000) DEFAULT NULL,
  class_12_marksheet_url VARCHAR(1000) DEFAULT NULL,
  graduation_marksheet_url VARCHAR(1000) DEFAULT NULL,
  post_graduation_marksheet_url VARCHAR(1000) DEFAULT NULL,
  is_verified BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_admin_email ON "Admin"(email_address);
CREATE INDEX idx_admin_id ON "Admin"(admin_id);

CREATE TABLE "Owners" (
  s_no BIGSERIAL PRIMARY KEY,
  owner_id VARCHAR(36) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  surname VARCHAR(255) NOT NULL,
  email_address VARCHAR(255) NOT NULL UNIQUE,
  mobile_number VARCHAR(20) NOT NULL,
  password VARCHAR(255) NOT NULL,
  owner_created_date DATE NOT NULL,
  owner_created_time TIME NOT NULL,
  share_id VARCHAR(36) NOT NULL UNIQUE,
  role VARCHAR(50) NOT NULL DEFAULT 'OWNER',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_owner_email ON "Owners"(email_address);
CREATE INDEX idx_owner_id ON "Owners"(owner_id);

CREATE TABLE "Notification" (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  subtitle VARCHAR(255) NOT NULL,
  type VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_read BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_notification_type ON "Notification"(type);

-- =============================================================
-- 2) Profiles and Resume storage
-- =============================================================

CREATE TABLE user_profiles (
  user_profile_id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL UNIQUE,
  full_name VARCHAR(255) DEFAULT NULL,
  email VARCHAR(200) DEFAULT NULL,
  phone VARCHAR(50) DEFAULT NULL,
  headline VARCHAR(255) DEFAULT NULL,
  location VARCHAR(255) DEFAULT NULL,
  linkedin_url VARCHAR(1000) DEFAULT NULL,
  github_url VARCHAR(1000) DEFAULT NULL,
  portfolio_url VARCHAR(1000) DEFAULT NULL,
  profile_summary TEXT DEFAULT NULL,
  current_company VARCHAR(255) DEFAULT NULL,
  current_job_role VARCHAR(255) DEFAULT NULL,
  highest_education VARCHAR(255) DEFAULT NULL,
  top_skills_json TEXT DEFAULT NULL,
  experience_summary_json TEXT DEFAULT NULL,
  education_summary_json TEXT DEFAULT NULL,
  profile_source_type VARCHAR(50) DEFAULT 'MANUAL',
  source_resume_version_id BIGINT DEFAULT NULL,
  auto_sync_from_resume BOOLEAN NOT NULL DEFAULT FALSE,
  allow_resume_overwrite BOOLEAN NOT NULL DEFAULT TRUE,
  profile_visible_to_admin BOOLEAN NOT NULL DEFAULT TRUE,
  profile_visible_in_dashboard BOOLEAN NOT NULL DEFAULT TRUE,
  class_10_marksheet_url VARCHAR(1000) DEFAULT NULL,
  class_12_marksheet_url VARCHAR(1000) DEFAULT NULL,
  graduation_marksheet_url VARCHAR(1000) DEFAULT NULL,
  post_graduation_marksheet_url VARCHAR(1000) DEFAULT NULL,
  resume_url VARCHAR(1000) DEFAULT NULL,
  experience_years INTEGER DEFAULT NULL,
  is_verified BOOLEAN NOT NULL DEFAULT FALSE,
  profile_picture_url VARCHAR(1000) DEFAULT NULL,
  preferred_headline VARCHAR(255) DEFAULT NULL,
  preferred_location VARCHAR(255) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES "Users"(user_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_user_profile_user_id ON user_profiles(user_id);

CREATE TABLE admin_profiles (
  admin_profile_id BIGSERIAL PRIMARY KEY,
  admin_id VARCHAR(36) NOT NULL UNIQUE,
  full_name VARCHAR(255) DEFAULT NULL,
  email VARCHAR(200) DEFAULT NULL,
  phone VARCHAR(50) DEFAULT NULL,
  headline VARCHAR(255) DEFAULT NULL,
  location VARCHAR(255) DEFAULT NULL,
  linkedin_url VARCHAR(1000) DEFAULT NULL,
  github_url VARCHAR(1000) DEFAULT NULL,
  portfolio_url VARCHAR(1000) DEFAULT NULL,
  profile_summary TEXT DEFAULT NULL,
  top_skills_json TEXT DEFAULT NULL,
  profile_source_type VARCHAR(50) DEFAULT 'MANUAL',
  source_resume_version_id BIGINT DEFAULT NULL,
  auto_sync_from_resume BOOLEAN NOT NULL DEFAULT FALSE,
  allow_resume_overwrite BOOLEAN NOT NULL DEFAULT TRUE,
  profile_visible_in_dashboard BOOLEAN NOT NULL DEFAULT TRUE,
  preferred_headline VARCHAR(255) DEFAULT NULL,
  preferred_location VARCHAR(255) DEFAULT NULL,
  class_10_marksheet_url VARCHAR(1000) DEFAULT NULL,
  class_12_marksheet_url VARCHAR(1000) DEFAULT NULL,
  graduation_marksheet_url VARCHAR(1000) DEFAULT NULL,
  post_graduation_marksheet_url VARCHAR(1000) DEFAULT NULL,
  resume_url VARCHAR(1000) DEFAULT NULL,
  is_verified BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_admin_profile_admin FOREIGN KEY (admin_id) REFERENCES "Admin"(admin_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_admin_profile_admin_id ON admin_profiles(admin_id);

CREATE TABLE resumes (
  resume_id BIGSERIAL PRIMARY KEY,
  resume_code VARCHAR(36) NOT NULL UNIQUE,
  user_id VARCHAR(36) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description VARCHAR(1000) DEFAULT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  original_file_name VARCHAR(255) DEFAULT NULL,
  original_file_url VARCHAR(1000) DEFAULT NULL,
  current_base_version_code VARCHAR(36) DEFAULT NULL,
  total_versions INTEGER NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_resume_user FOREIGN KEY (user_id) REFERENCES "Users"(user_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_resume_user_id ON resumes(user_id);
CREATE INDEX idx_resume_code ON resumes(resume_code);

CREATE TABLE resume_versions (
  resume_version_id BIGSERIAL PRIMARY KEY,
  version_code VARCHAR(36) NOT NULL UNIQUE,
  version_name VARCHAR(255) NOT NULL,
  version_type VARCHAR(50) NOT NULL,
  is_base_version BOOLEAN NOT NULL DEFAULT FALSE,
  file_url VARCHAR(1000) DEFAULT NULL,
  preview_url VARCHAR(1000) DEFAULT NULL,
  job_application_code VARCHAR(255) DEFAULT NULL,
  raw_text TEXT DEFAULT NULL,
  structured_content_json TEXT DEFAULT NULL,
  profile_snapshot_json TEXT DEFAULT NULL,
  format_metadata_json TEXT DEFAULT NULL,
  ats_score INTEGER DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resume_id BIGINT NOT NULL,
  parent_version_id BIGINT DEFAULT NULL,
  CONSTRAINT fk_resume_version_resume FOREIGN KEY (resume_id) REFERENCES resumes(resume_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_resume_version_parent FOREIGN KEY (parent_version_id) REFERENCES resume_versions(resume_version_id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX idx_resume_version_resume_id ON resume_versions(resume_id);
CREATE INDEX idx_resume_version_parent_id ON resume_versions(parent_version_id);
CREATE INDEX idx_resume_version_code ON resume_versions(version_code);

CREATE TABLE resume_sections (
  resume_section_id BIGSERIAL PRIMARY KEY,
  resume_version_id BIGINT NOT NULL,
  section_type VARCHAR(50) NOT NULL,
  section_title VARCHAR(150) DEFAULT NULL,
  section_order INTEGER NOT NULL DEFAULT 0,
  content_json TEXT DEFAULT NULL,
  plain_text TEXT DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_resume_section_version FOREIGN KEY (resume_version_id) REFERENCES resume_versions(resume_version_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_resume_section_version_id ON resume_sections(resume_version_id);

CREATE TABLE resume_file_assets (
  resume_file_asset_id BIGSERIAL PRIMARY KEY,
  asset_code VARCHAR(36) NOT NULL UNIQUE,
  file_name VARCHAR(255) NOT NULL,
  stored_file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(1000) NOT NULL,
  file_url VARCHAR(1000) DEFAULT NULL,
  content_type VARCHAR(150) DEFAULT NULL,
  file_size BIGINT NOT NULL DEFAULT 0,
  checksum VARCHAR(255) DEFAULT NULL,
  asset_type VARCHAR(50) NOT NULL DEFAULT 'ORIGINAL',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_resume_file_asset_code ON resume_file_assets(asset_code);

CREATE TABLE resume_cloud_files (
  id BIGSERIAL PRIMARY KEY,
  file_id VARCHAR(36) NOT NULL UNIQUE,
  user_id VARCHAR(36) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_size BIGINT NOT NULL,
  mime_type VARCHAR(100) DEFAULT NULL,
  encrypted_content BYTEA NOT NULL,
  checksum VARCHAR(64) NOT NULL,
  version INTEGER NOT NULL DEFAULT 1,
  is_active BOOLEAN DEFAULT TRUE,
  uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_at TIMESTAMP DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_resume_cloud_file_user FOREIGN KEY (user_id) REFERENCES "Users"(user_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_resume_cloud_file_id ON resume_cloud_files(file_id);
CREATE INDEX idx_resume_cloud_file_user_id ON resume_cloud_files(user_id);

-- =============================================================
-- 3) Jobs and Applications
-- =============================================================

CREATE TABLE jobs (
  id BIGSERIAL PRIMARY KEY,
  job_code VARCHAR(36) NOT NULL UNIQUE,
  title VARCHAR(255) NOT NULL DEFAULT 'HR',
  post VARCHAR(255) NOT NULL DEFAULT 'HR',
  hr_type VARCHAR(255) NOT NULL,
  other_hr_type VARCHAR(255) DEFAULT NULL,
  working_type VARCHAR(100) NOT NULL,
  office_location VARCHAR(255) DEFAULT NULL,
  start_date_type VARCHAR(100) NOT NULL,
  specific_start_date DATE DEFAULT NULL,
  salary VARCHAR(255) NOT NULL,
  last_date_to_apply DATE NOT NULL,
  description TEXT DEFAULT NULL,
  skills_required TEXT DEFAULT NULL,
  who_can_apply TEXT DEFAULT NULL,
  company_id VARCHAR(36) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
  domain VARCHAR(50) NOT NULL DEFAULT 'TECH',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_job_company FOREIGN KEY (company_id) REFERENCES "Company"(company_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE INDEX idx_jobs_company_id ON jobs(company_id);
CREATE INDEX idx_jobs_code ON jobs(job_code);
CREATE INDEX idx_jobs_status ON jobs(status);

CREATE TABLE job_applications (
  id BIGSERIAL PRIMARY KEY,
  application_code VARCHAR(36) NOT NULL UNIQUE,
  user_id VARCHAR(36) NOT NULL,
  base_resume_version_id BIGINT DEFAULT NULL,
  tailored_resume_version_id BIGINT DEFAULT NULL,
  company_name VARCHAR(255) NOT NULL,
  job_title VARCHAR(255) NOT NULL,
  application_source VARCHAR(255) DEFAULT NULL,
  job_description TEXT DEFAULT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
  ats_score_before INTEGER DEFAULT NULL,
  ats_score_after INTEGER DEFAULT NULL,
  notes TEXT DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_job_application_user FOREIGN KEY (user_id) REFERENCES "Users"(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_job_application_base_resume_version FOREIGN KEY (base_resume_version_id) REFERENCES resume_versions(resume_version_id) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT fk_job_application_tailored_resume_version FOREIGN KEY (tailored_resume_version_id) REFERENCES resume_versions(resume_version_id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX idx_job_application_user_id ON job_applications(user_id);
CREATE INDEX idx_job_application_base_resume_version_id ON job_applications(base_resume_version_id);
CREATE INDEX idx_job_application_tailored_resume_version_id ON job_applications(tailored_resume_version_id);
CREATE INDEX idx_job_application_code ON job_applications(application_code);

CREATE TABLE internal_job_applications (
  id BIGSERIAL PRIMARY KEY,
  job_id BIGINT NOT NULL,
  admin_id VARCHAR(36) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  resume_type VARCHAR(50) NOT NULL DEFAULT 'UPLOADED',
  resume_file_id BIGINT DEFAULT NULL,
  applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_internal_job_application_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_internal_job_application_admin FOREIGN KEY (admin_id) REFERENCES "Admin"(admin_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_internal_job_application_resume_file FOREIGN KEY (resume_file_id) REFERENCES resume_file_assets(resume_file_asset_id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX idx_internal_job_application_job_id ON internal_job_applications(job_id);
CREATE INDEX idx_internal_job_application_admin_id ON internal_job_applications(admin_id);

CREATE TABLE tool_requirement_answers (
  tool_requirement_answer_id BIGSERIAL PRIMARY KEY,
  job_application_id BIGINT NOT NULL,
  tool_name VARCHAR(150) NOT NULL,
  required_flag BOOLEAN NOT NULL DEFAULT FALSE,
  user_knows_tool BOOLEAN NOT NULL DEFAULT FALSE,
  user_experience_level VARCHAR(50) DEFAULT NULL,
  notes TEXT DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tool_requirement_answer_job_application FOREIGN KEY (job_application_id) REFERENCES job_applications(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_tool_requirement_answer_job_application_id ON tool_requirement_answers(job_application_id);

-- =============================================================
-- 4) Interview flow tables
-- =============================================================

CREATE TABLE interview_sessions (
  interview_session_id BIGSERIAL PRIMARY KEY,
  user_id BIGINT DEFAULT NULL,
  admin_id BIGINT DEFAULT NULL,
  interview_token VARCHAR(128) DEFAULT NULL UNIQUE,
  resume_id BIGINT DEFAULT NULL,
  resume_version_id BIGINT DEFAULT NULL,
  interview_type VARCHAR(50) NOT NULL,
  interview_mode VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
  role VARCHAR(255) DEFAULT NULL,
  domain VARCHAR(255) DEFAULT NULL,
  difficulty INTEGER NOT NULL DEFAULT 3,
  total_questions INTEGER NOT NULL DEFAULT 10,
  current_question_index INTEGER NOT NULL DEFAULT 0,
  duration_minutes INTEGER DEFAULT NULL,
  allow_hints BOOLEAN NOT NULL DEFAULT TRUE,
  include_behavioral BOOLEAN NOT NULL DEFAULT TRUE,
  include_technical BOOLEAN NOT NULL DEFAULT TRUE,
  resume_based BOOLEAN NOT NULL DEFAULT FALSE,
  github_based BOOLEAN NOT NULL DEFAULT FALSE,
  job_description_based BOOLEAN NOT NULL DEFAULT FALSE,
  preferred_language VARCHAR(50) DEFAULT NULL,
  job_description TEXT DEFAULT NULL,
  github_urls TEXT DEFAULT NULL,
  feedback_summary TEXT DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_interview_session_user_id ON interview_sessions(user_id);
CREATE INDEX idx_interview_session_admin_id ON interview_sessions(admin_id);
CREATE INDEX idx_interview_session_status ON interview_sessions(status);
CREATE INDEX idx_interview_session_type ON interview_sessions(interview_type);
CREATE INDEX idx_interview_session_mode ON interview_sessions(interview_mode);
CREATE INDEX idx_interview_session_created_at ON interview_sessions(created_at);

CREATE TABLE interview_turns (
  interview_turn_id BIGSERIAL PRIMARY KEY,
  interview_session_id BIGINT NOT NULL,
  question_index INTEGER NOT NULL,
  question_type VARCHAR(50) DEFAULT NULL,
  category VARCHAR(255) DEFAULT NULL,
  difficulty INTEGER DEFAULT NULL,
  question TEXT NOT NULL,
  source_summary VARCHAR(1000) DEFAULT NULL,
  resume_based BOOLEAN NOT NULL DEFAULT FALSE,
  github_based BOOLEAN NOT NULL DEFAULT FALSE,
  job_description_based BOOLEAN NOT NULL DEFAULT FALSE,
  answer TEXT DEFAULT NULL,
  transcript TEXT DEFAULT NULL,
  answer_language VARCHAR(50) DEFAULT NULL,
  speech_based BOOLEAN NOT NULL DEFAULT FALSE,
  skipped BOOLEAN NOT NULL DEFAULT FALSE,
  hint_used BOOLEAN NOT NULL DEFAULT FALSE,
  sample_answer_used BOOLEAN NOT NULL DEFAULT FALSE,
  evaluated BOOLEAN NOT NULL DEFAULT FALSE,
  duration_seconds INTEGER DEFAULT NULL,
  score INTEGER DEFAULT NULL,
  feedback_summary TEXT DEFAULT NULL,
  hint_text TEXT DEFAULT NULL,
  sample_answer TEXT DEFAULT NULL,
  follow_up_question TEXT DEFAULT NULL,
  client_timestamp VARCHAR(100) DEFAULT NULL,
  asked_at TIMESTAMP DEFAULT NULL,
  answered_at TIMESTAMP DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_interview_turn_session FOREIGN KEY (interview_session_id) REFERENCES interview_sessions(interview_session_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX idx_interview_turn_session_id ON interview_turns(interview_session_id);
CREATE INDEX idx_interview_turn_question_index ON interview_turns(question_index);
CREATE INDEX idx_interview_turn_created_at ON interview_turns(created_at);
CREATE INDEX idx_interview_turn_question_type ON interview_turns(question_type);

CREATE TABLE interview_evaluations (
  interview_evaluation_id BIGSERIAL PRIMARY KEY,
  interview_session_id BIGINT NOT NULL,
  interview_turn_id BIGINT DEFAULT NULL,
  evaluation_type VARCHAR(50) NOT NULL DEFAULT 'TURN',
  evaluation_mode VARCHAR(50) DEFAULT NULL,
  forced_reevaluation BOOLEAN NOT NULL DEFAULT FALSE,
  overall_score INTEGER DEFAULT NULL,
  confidence_score INTEGER DEFAULT NULL,
  knowledge_score INTEGER DEFAULT NULL,
  communication_score INTEGER DEFAULT NULL,
  clarity_score INTEGER DEFAULT NULL,
  relevance_score INTEGER DEFAULT NULL,
  emotional_composure_score INTEGER DEFAULT NULL,
  technical_depth_score INTEGER DEFAULT NULL,
  problem_solving_score INTEGER DEFAULT NULL,
  professionalism_score INTEGER DEFAULT NULL,
  presence_score INTEGER DEFAULT NULL,
  summary TEXT DEFAULT NULL,
  feedback TEXT DEFAULT NULL,
  explanation TEXT DEFAULT NULL,
  strengths TEXT DEFAULT NULL,
  weaknesses TEXT DEFAULT NULL,
  improvement_suggestions TEXT DEFAULT NULL,
  detected_skills TEXT DEFAULT NULL,
  missing_concepts TEXT DEFAULT NULL,
  rubric_notes TEXT DEFAULT NULL,
  next_step_suggestion TEXT DEFAULT NULL,
  follow_up_question TEXT DEFAULT NULL,
  ready_for_next_question BOOLEAN DEFAULT NULL,
  grade VARCHAR(50) DEFAULT NULL,
  recommendation VARCHAR(100) DEFAULT NULL,
  evaluation_source VARCHAR(50) NOT NULL DEFAULT 'AI_ENGINE',
  latency_ms INTEGER DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_interview_evaluation_session FOREIGN KEY (interview_session_id) REFERENCES interview_sessions(interview_session_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_interview_evaluation_turn FOREIGN KEY (interview_turn_id) REFERENCES interview_turns(interview_turn_id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE INDEX idx_interview_eval_session_id ON interview_evaluations(interview_session_id);
CREATE INDEX idx_interview_eval_turn_id ON interview_evaluations(interview_turn_id);
CREATE INDEX idx_interview_eval_type ON interview_evaluations(evaluation_type);
CREATE INDEX idx_interview_eval_created_at ON interview_evaluations(created_at);

-- =============================================================
-- 5) Common database queries
-- =============================================================

-- Create a user
-- INSERT INTO "Users" (user_id, name, surname, email_address, mobile_number, password, user_created_date, user_created_time, share_id, role)
-- VALUES (gen_random_uuid()::text, 'John', 'Doe', 'john@example.com', '9876543210', 'hashed_password', CURRENT_DATE, CURRENT_TIME, gen_random_uuid()::text, 'USER');

-- Get user by email
SELECT * FROM "Users" WHERE email_address = 'john@example.com';

-- Get all open jobs
SELECT * FROM jobs WHERE status = 'OPEN' ORDER BY created_at DESC;

-- Get jobs for a company
SELECT * FROM jobs WHERE company_id = 'company-id-here' ORDER BY created_at DESC;

-- Create a job application
-- INSERT INTO job_applications (application_code, user_id, company_name, job_title, application_source, job_description, status)
-- VALUES (gen_random_uuid()::text, 'user-id-here', 'Microsoft', 'Software Engineer', 'LinkedIn', 'Backend Java role', 'CREATED');

-- Get applications by user
SELECT * FROM job_applications WHERE user_id = 'user-id-here' ORDER BY created_at DESC;

-- Get resume versions for a resume
SELECT * FROM resume_versions WHERE resume_id = 1 ORDER BY created_at DESC;

-- Get interview sessions for a user
SELECT * FROM interview_sessions WHERE user_id = 1 ORDER BY created_at DESC;

-- Get turns for an interview session
SELECT * FROM interview_turns WHERE interview_session_id = 1 ORDER BY question_index ASC;

-- Get evaluations for a session
SELECT * FROM interview_evaluations WHERE interview_session_id = 1 ORDER BY created_at DESC;

-- Update a user's profile summary
-- UPDATE user_profiles SET profile_summary = 'Experienced backend engineer', updated_at = CURRENT_TIMESTAMP WHERE user_id = 'user-id-here';

-- Mark a notification as read
-- UPDATE "Notification" SET is_read = TRUE WHERE id = 1;

-- Delete an old interview session
-- DELETE FROM interview_sessions WHERE interview_session_id = 1;
