-- Safe migration for duplicate/cloned Supabase tables
-- Goal: preserve data and consolidate onto the canonical tables used by the Spring Boot entities.
-- This script does NOT drop any table until all data has been copied and references are verified.

BEGIN;

-- 1) Ensure canonical Users table has the profile columns expected by the Spring Boot entity.
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS linkedin_url VARCHAR(1000);
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS github_url VARCHAR(1000);
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS portfolio_url VARCHAR(1000);
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS current_company VARCHAR(255);
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS current_job_role VARCHAR(255);
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS highest_education VARCHAR(255);
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS top_skills_json TEXT;
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS experience_summary_json TEXT;
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS education_summary_json TEXT;
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS profile_source_type VARCHAR(50) DEFAULT 'MANUAL';
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS source_resume_version_id BIGINT;
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS profile_created BOOLEAN DEFAULT FALSE;
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS class_10_marksheet_url VARCHAR(1000);
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS class_12_marksheet_url VARCHAR(1000);
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS graduation_marksheet_url VARCHAR(1000);
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS post_graduation_marksheet_url VARCHAR(1000);
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS experience_years INTEGER;
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE public."Users" ADD COLUMN IF NOT EXISTS profile_picture_url VARCHAR(1000);

-- 2) Merge user data from lowercase users -> canonical Users
INSERT INTO public."Users" (
  s_no,
  user_id,
  name,
  surname,
  email_address,
  mobile_number,
  password,
  user_created_date,
  user_created_time,
  share_id,
  role,
  resume_scanned,
  resume_file_name,
  resume_score,
  onboarding_domain,
  onb_subdomain_mode,
  onb_subdomain_single,
  onb_subdomain_multi,
  onb_job_titles,
  onb_done,
  profile_full_name,
  profile_headline,
  profile_location,
  profile_summary,
  linkedin_url,
  github_url,
  portfolio_url,
  current_company,
  current_job_role,
  highest_education,
  top_skills_json,
  experience_summary_json,
  education_summary_json,
  profile_source_type,
  source_resume_version_id,
  profile_created,
  class_10_marksheet_url,
  class_12_marksheet_url,
  graduation_marksheet_url,
  post_graduation_marksheet_url,
  experience_years,
  is_verified,
  profile_picture_url,
  created_at,
  updated_at
)
SELECT
  u.s_no,
  u.user_id,
  COALESCE(NULLIF(u.name, ''), 'Integration') AS name,
  COALESCE(NULLIF(u.surname, ''), 'User') AS surname,
  u.email_address,
  u.mobile_number,
  u.password,
  u.user_created_date,
  u.user_created_time,
  u.share_id,
  COALESCE(NULLIF(u.role, ''), 'USER') AS role,
  COALESCE(u.resume_scanned, FALSE),
  u.resume_file_name,
  u.resume_score,
  u.onboarding_domain,
  u.onb_subdomain_mode,
  u.onb_subdomain_single,
  u.onb_subdomain_multi,
  u.onb_job_titles,
  COALESCE(u.onb_done, FALSE),
  u.profile_full_name,
  u.profile_headline,
  u.profile_location,
  u.profile_summary,
  u.github_url,
  u.github_url,
  u.portfolio_url,
  u.current_company,
  u.current_job_role,
  u.highest_education,
  u.top_skills_json,
  u.experience_summary_json,
  u.education_summary_json,
  u.profile_source_type,
  u.source_resume_version_id,
  COALESCE(u.profile_created, FALSE),
  u.class_10_marksheet_url,
  u.class_12_marksheet_url,
  u.graduation_marksheet_url,
  u.post_graduation_marksheet_url,
  u.experience_years,
  COALESCE(u.is_verified, FALSE),
  u.profile_picture_url,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM public."users" u
WHERE NOT EXISTS (
  SELECT 1 FROM public."Users" cu WHERE cu.user_id = u.user_id
);

-- 2) Merge admin data from lowercase admin -> canonical Admin
INSERT INTO public."Admin" (
  s_no,
  admin_id,
  name,
  surname,
  email_address,
  mobile_number,
  password,
  admin_created_date,
  admin_created_time,
  share_id,
  role,
  onboarding_domain,
  onboarding_subdomain_mode,
  onboarding_subdomain_single,
  onboarding_subdomain_multi,
  onboarding_job_titles,
  onboarding_done,
  class_10_marksheet_url,
  class_12_marksheet_url,
  graduation_marksheet_url,
  post_graduation_marksheet_url,
  is_verified,
  created_at,
  updated_at
)
SELECT
  a.s_no,
  a.admin_id,
  a.name,
  a.surname,
  a.email_address,
  a.mobile_number,
  a.password,
  a.admin_created_date,
  a.admin_created_time,
  a.share_id,
  COALESCE(NULLIF(a.role, ''), 'ADMIN') AS role,
  a.onboarding_domain,
  a.onboarding_subdomain_mode,
  a.onboarding_subdomain_single,
  a.onboarding_subdomain_multi,
  a.onboarding_job_titles,
  COALESCE(a.onboarding_done, FALSE),
  a.class_10_marksheet_url,
  a.class_12_marksheet_url,
  a.graduation_marksheet_url,
  a.post_graduation_marksheet_url,
  COALESCE(a.is_verified, FALSE),
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM public."admin" a
WHERE NOT EXISTS (
  SELECT 1 FROM public."Admin" ca WHERE ca.admin_id = a.admin_id
);

-- 3) Merge owner data from lowercase owners -> canonical Owners
INSERT INTO public."Owners" (
  s_no,
  owner_id,
  name,
  surname,
  email_address,
  mobile_number,
  password,
  owner_created_date,
  owner_created_time,
  share_id,
  role,
  created_at,
  updated_at
)
SELECT
  o.s_no,
  o.owner_id,
  o.name,
  o.surname,
  o.email_address,
  o.mobile_number,
  o.password,
  o.owner_created_date,
  o.owner_created_time,
  o.share_id,
  COALESCE(NULLIF(o.role, ''), 'OWNER') AS role,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM public."owners" o
WHERE NOT EXISTS (
  SELECT 1 FROM public."Owners" co WHERE co.owner_id = o.owner_id
);

-- 4) Merge notification data from lowercase notification -> canonical Notification
INSERT INTO public."Notification" (
  id,
  title,
  subtitle,
  type,
  created_at,
  is_read
)
SELECT
  n.id,
  n.title,
  n.subtitle,
  n.type,
  COALESCE(n.created_at, CURRENT_TIMESTAMP),
  COALESCE(n.is_read, FALSE)
FROM public."notification" n
WHERE NOT EXISTS (
  SELECT 1 FROM public."Notification" cn WHERE cn.id = n.id
);

-- 5) Merge company data from lowercase company -> canonical Company
INSERT INTO public."Company" (
  s_no,
  company_id,
  company_name,
  company_type,
  contact_person_name,
  email_address,
  mobile_number,
  password,
  company_created_date,
  company_created_time,
  share_id,
  role,
  created_at,
  updated_at
)
SELECT
  c.s_no,
  c.company_id,
  c.company_name,
  c.company_type,
  c.contact_person_name,
  c.email_address,
  c.mobile_number,
  c.password,
  c.company_created_date,
  c.company_created_time,
  c.share_id,
  COALESCE(NULLIF(c.role, ''), 'COMPANY') AS role,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM public."company" c
WHERE NOT EXISTS (
  SELECT 1 FROM public."Company" cc WHERE cc.company_id = c.company_id
);

COMMIT;

-- The duplicate lowercase tables are intentionally left in place for manual verification.
-- They can be dropped after the application has been validated and no references remain.
