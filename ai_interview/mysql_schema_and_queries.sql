-- =============================================================
-- MySQL schema and common queries for AI Interview backend
-- =============================================================
-- Usage:
--   mysql -u root -p < mysql_schema_and_queries.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS ai_interview
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ai_interview;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `tool_requirement_answers`;
DROP TABLE IF EXISTS `job_applications`;
DROP TABLE IF EXISTS `internal_job_applications`;
DROP TABLE IF EXISTS `interview_evaluations`;
DROP TABLE IF EXISTS `interview_turns`;
DROP TABLE IF EXISTS `interview_sessions`;
DROP TABLE IF EXISTS `resume_sections`;
DROP TABLE IF EXISTS `resume_versions`;
DROP TABLE IF EXISTS `resume_cloud_files`;
DROP TABLE IF EXISTS `resume_file_assets`;
DROP TABLE IF EXISTS `resumes`;
DROP TABLE IF EXISTS `user_profiles`;
DROP TABLE IF EXISTS `admin_profiles`;
DROP TABLE IF EXISTS `jobs`;
DROP TABLE IF EXISTS `Company`;
DROP TABLE IF EXISTS `Admin`;
DROP TABLE IF EXISTS `Owners`;
DROP TABLE IF EXISTS `Users`;
DROP TABLE IF EXISTS `Notification`;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================
-- 1) Users / Companies / Admins / Owners
-- =============================================================

CREATE TABLE `Users` (
  `s_no` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` VARCHAR(36) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `surname` VARCHAR(255) NOT NULL,
  `email_address` VARCHAR(255) NOT NULL,
  `mobile_number` VARCHAR(20) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `user_created_date` DATE NOT NULL,
  `user_created_time` TIME NOT NULL,
  `share_id` VARCHAR(36) NOT NULL,
  `role` VARCHAR(50) NOT NULL DEFAULT 'USER',
  `resume_scanned` TINYINT(1) NOT NULL DEFAULT 0,
  `resume_file_name` VARCHAR(255) DEFAULT NULL,
  `resume_score` INT DEFAULT NULL,
  `onboarding_domain` VARCHAR(255) DEFAULT NULL,
  `onb_subdomain_mode` VARCHAR(255) DEFAULT NULL,
  `onb_subdomain_single` VARCHAR(255) DEFAULT NULL,
  `onb_subdomain_multi` TEXT DEFAULT NULL,
  `onb_job_titles` TEXT DEFAULT NULL,
  `onb_done` TINYINT(1) NOT NULL DEFAULT 0,
  `profile_full_name` VARCHAR(255) DEFAULT NULL,
  `profile_headline` VARCHAR(255) DEFAULT NULL,
  `profile_location` VARCHAR(255) DEFAULT NULL,
  `profile_summary` TEXT DEFAULT NULL,
  `profile_picture_url` VARCHAR(1000) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`s_no`),
  UNIQUE KEY `uk_users_user_id` (`user_id`),
  UNIQUE KEY `uk_users_email` (`email_address`),
  UNIQUE KEY `uk_users_share_id` (`share_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `Company` (
  `s_no` BIGINT NOT NULL AUTO_INCREMENT,
  `company_id` VARCHAR(36) NOT NULL,
  `company_name` VARCHAR(255) NOT NULL,
  `company_type` VARCHAR(255) NOT NULL,
  `contact_person_name` VARCHAR(255) NOT NULL,
  `email_address` VARCHAR(255) NOT NULL,
  `mobile_number` VARCHAR(10) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `company_created_date` DATE NOT NULL,
  `company_created_time` TIME NOT NULL,
  `share_id` VARCHAR(36) NOT NULL,
  `role` VARCHAR(50) NOT NULL DEFAULT 'COMPANY',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`s_no`),
  UNIQUE KEY `uk_company_id` (`company_id`),
  UNIQUE KEY `uk_company_email` (`email_address`),
  UNIQUE KEY `uk_company_share_id` (`share_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `Admin` (
  `s_no` BIGINT NOT NULL AUTO_INCREMENT,
  `admin_id` VARCHAR(36) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `surname` VARCHAR(255) NOT NULL,
  `email_address` VARCHAR(255) NOT NULL,
  `mobile_number` VARCHAR(10) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `admin_created_date` DATE NOT NULL,
  `admin_created_time` TIME NOT NULL,
  `share_id` VARCHAR(36) NOT NULL,
  `role` VARCHAR(50) NOT NULL DEFAULT 'ADMIN',
  `onboarding_domain` VARCHAR(255) DEFAULT NULL,
  `onboarding_subdomain_mode` VARCHAR(255) DEFAULT NULL,
  `onboarding_subdomain_single` VARCHAR(255) DEFAULT NULL,
  `onboarding_subdomain_multi` TEXT DEFAULT NULL,
  `onboarding_job_titles` TEXT DEFAULT NULL,
  `onboarding_done` TINYINT(1) NOT NULL DEFAULT 0,
  `class_10_marksheet_url` VARCHAR(1000) DEFAULT NULL,
  `class_12_marksheet_url` VARCHAR(1000) DEFAULT NULL,
  `graduation_marksheet_url` VARCHAR(1000) DEFAULT NULL,
  `post_graduation_marksheet_url` VARCHAR(1000) DEFAULT NULL,
  `is_verified` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`s_no`),
  UNIQUE KEY `uk_admin_id` (`admin_id`),
  UNIQUE KEY `uk_admin_email` (`email_address`),
  UNIQUE KEY `uk_admin_share_id` (`share_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `Owners` (
  `s_no` BIGINT NOT NULL AUTO_INCREMENT,
  `owner_id` VARCHAR(36) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `surname` VARCHAR(255) NOT NULL,
  `email_address` VARCHAR(255) NOT NULL,
  `mobile_number` VARCHAR(20) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `owner_created_date` DATE NOT NULL,
  `owner_created_time` TIME NOT NULL,
  `share_id` VARCHAR(36) NOT NULL,
  `role` VARCHAR(50) NOT NULL DEFAULT 'OWNER',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`s_no`),
  UNIQUE KEY `uk_owner_id` (`owner_id`),
  UNIQUE KEY `uk_owner_email` (`email_address`),
  UNIQUE KEY `uk_owner_share_id` (`share_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `Notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `subtitle` VARCHAR(255) NOT NULL,
  `type` VARCHAR(100) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================
-- 2) Profiles and Resume storage
-- =============================================================

CREATE TABLE `user_profiles` (
  `user_profile_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` VARCHAR(36) NOT NULL,
  `full_name` VARCHAR(255) DEFAULT NULL,
  `email` VARCHAR(200) DEFAULT NULL,
  `phone` VARCHAR(50) DEFAULT NULL,
  `headline` VARCHAR(255) DEFAULT NULL,
  `location` VARCHAR(255) DEFAULT NULL,
  `linkedin_url` VARCHAR(1000) DEFAULT NULL,
  `github_url` VARCHAR(1000) DEFAULT NULL,
  `portfolio_url` VARCHAR(1000) DEFAULT NULL,
  `profile_summary` TEXT DEFAULT NULL,
  `current_company` VARCHAR(255) DEFAULT NULL,
  `current_role` VARCHAR(255) DEFAULT NULL,
  `highest_education` VARCHAR(255) DEFAULT NULL,
  `top_skills_json` TEXT DEFAULT NULL,
  `experience_summary_json` TEXT DEFAULT NULL,
  `education_summary_json` TEXT DEFAULT NULL,
  `profile_source_type` VARCHAR(50) DEFAULT 'MANUAL',
  `source_resume_version_id` BIGINT DEFAULT NULL,
  `auto_sync_from_resume` TINYINT(1) NOT NULL DEFAULT 0,
  `allow_resume_overwrite` TINYINT(1) NOT NULL DEFAULT 1,
  `profile_visible_to_admin` TINYINT(1) NOT NULL DEFAULT 1,
  `profile_visible_in_dashboard` TINYINT(1) NOT NULL DEFAULT 1,
  `class_10_marksheet_url` VARCHAR(1000) DEFAULT NULL,
  `class_12_marksheet_url` VARCHAR(1000) DEFAULT NULL,
  `graduation_marksheet_url` VARCHAR(1000) DEFAULT NULL,
  `post_graduation_marksheet_url` VARCHAR(1000) DEFAULT NULL,
  `resume_url` VARCHAR(1000) DEFAULT NULL,
  `experience_years` INT DEFAULT NULL,
  `is_verified` TINYINT(1) NOT NULL DEFAULT 0,
  `profile_picture_url` VARCHAR(1000) DEFAULT NULL,
  `preferred_headline` VARCHAR(255) DEFAULT NULL,
  `preferred_location` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_profile_id`),
  UNIQUE KEY `uk_user_profile_user_id` (`user_id`),
  CONSTRAINT `fk_user_profile_user` FOREIGN KEY (`user_id`) REFERENCES `Users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `admin_profiles` (
  `admin_profile_id` BIGINT NOT NULL AUTO_INCREMENT,
  `admin_id` VARCHAR(36) NOT NULL,
  `full_name` VARCHAR(255) DEFAULT NULL,
  `email` VARCHAR(200) DEFAULT NULL,
  `phone` VARCHAR(50) DEFAULT NULL,
  `headline` VARCHAR(255) DEFAULT NULL,
  `location` VARCHAR(255) DEFAULT NULL,
  `linkedin_url` VARCHAR(1000) DEFAULT NULL,
  `github_url` VARCHAR(1000) DEFAULT NULL,
  `portfolio_url` VARCHAR(1000) DEFAULT NULL,
  `profile_summary` TEXT DEFAULT NULL,
  `top_skills_json` TEXT DEFAULT NULL,
  `profile_source_type` VARCHAR(50) DEFAULT 'MANUAL',
  `source_resume_version_id` BIGINT DEFAULT NULL,
  `auto_sync_from_resume` TINYINT(1) NOT NULL DEFAULT 0,
  `allow_resume_overwrite` TINYINT(1) NOT NULL DEFAULT 1,
  `profile_visible_in_dashboard` TINYINT(1) NOT NULL DEFAULT 1,
  `preferred_headline` VARCHAR(255) DEFAULT NULL,
  `preferred_location` VARCHAR(255) DEFAULT NULL,
  `class_10_marksheet_url` VARCHAR(1000) DEFAULT NULL,
  `class_12_marksheet_url` VARCHAR(1000) DEFAULT NULL,
  `graduation_marksheet_url` VARCHAR(1000) DEFAULT NULL,
  `post_graduation_marksheet_url` VARCHAR(1000) DEFAULT NULL,
  `resume_url` VARCHAR(1000) DEFAULT NULL,
  `is_verified` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`admin_profile_id`),
  UNIQUE KEY `uk_admin_profile_admin_id` (`admin_id`),
  CONSTRAINT `fk_admin_profile_admin` FOREIGN KEY (`admin_id`) REFERENCES `Admin` (`admin_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `resumes` (
  `resume_id` BIGINT NOT NULL AUTO_INCREMENT,
  `resume_code` VARCHAR(36) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `description` VARCHAR(1000) DEFAULT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
  `original_file_name` VARCHAR(255) DEFAULT NULL,
  `original_file_url` VARCHAR(1000) DEFAULT NULL,
  `current_base_version_code` VARCHAR(36) DEFAULT NULL,
  `total_versions` INT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`resume_id`),
  UNIQUE KEY `uk_resume_code` (`resume_code`),
  KEY `idx_resume_user_id` (`user_id`),
  CONSTRAINT `fk_resume_user` FOREIGN KEY (`user_id`) REFERENCES `Users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `resume_versions` (
  `resume_version_id` BIGINT NOT NULL AUTO_INCREMENT,
  `version_code` VARCHAR(36) NOT NULL,
  `version_name` VARCHAR(255) NOT NULL,
  `version_type` VARCHAR(50) NOT NULL,
  `is_base_version` TINYINT(1) NOT NULL DEFAULT 0,
  `file_url` VARCHAR(1000) DEFAULT NULL,
  `preview_url` VARCHAR(1000) DEFAULT NULL,
  `job_application_code` VARCHAR(255) DEFAULT NULL,
  `raw_text` LONGTEXT DEFAULT NULL,
  `structured_content_json` LONGTEXT DEFAULT NULL,
  `profile_snapshot_json` LONGTEXT DEFAULT NULL,
  `format_metadata_json` LONGTEXT DEFAULT NULL,
  `ats_score` INT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `resume_id` BIGINT NOT NULL,
  `parent_version_id` BIGINT DEFAULT NULL,
  PRIMARY KEY (`resume_version_id`),
  UNIQUE KEY `uk_resume_version_code` (`version_code`),
  KEY `idx_resume_version_resume_id` (`resume_id`),
  KEY `idx_resume_version_parent_id` (`parent_version_id`),
  CONSTRAINT `fk_resume_version_resume` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`resume_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_resume_version_parent` FOREIGN KEY (`parent_version_id`) REFERENCES `resume_versions` (`resume_version_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `resume_sections` (
  `resume_section_id` BIGINT NOT NULL AUTO_INCREMENT,
  `resume_version_id` BIGINT NOT NULL,
  `section_type` VARCHAR(50) NOT NULL,
  `section_title` VARCHAR(150) DEFAULT NULL,
  `section_order` INT NOT NULL DEFAULT 0,
  `content_json` LONGTEXT DEFAULT NULL,
  `plain_text` LONGTEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`resume_section_id`),
  KEY `idx_resume_section_version_id` (`resume_version_id`),
  CONSTRAINT `fk_resume_section_version` FOREIGN KEY (`resume_version_id`) REFERENCES `resume_versions` (`resume_version_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `resume_file_assets` (
  `resume_file_asset_id` BIGINT NOT NULL AUTO_INCREMENT,
  `asset_code` VARCHAR(36) NOT NULL,
  `file_name` VARCHAR(255) NOT NULL,
  `stored_file_name` VARCHAR(255) NOT NULL,
  `file_path` VARCHAR(1000) NOT NULL,
  `file_url` VARCHAR(1000) DEFAULT NULL,
  `content_type` VARCHAR(150) DEFAULT NULL,
  `file_size` BIGINT NOT NULL DEFAULT 0,
  `checksum` VARCHAR(255) DEFAULT NULL,
  `asset_type` VARCHAR(50) NOT NULL DEFAULT 'ORIGINAL',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`resume_file_asset_id`),
  UNIQUE KEY `uk_resume_file_asset_code` (`asset_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `resume_cloud_files` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `file_id` VARCHAR(36) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `file_name` VARCHAR(255) NOT NULL,
  `file_size` BIGINT NOT NULL,
  `mime_type` VARCHAR(100) DEFAULT NULL,
  `encrypted_content` LONGBLOB NOT NULL,
  `checksum` VARCHAR(64) NOT NULL,
  `version` INT NOT NULL DEFAULT 1,
  `is_active` TINYINT(1) DEFAULT 1,
  `uploaded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resume_cloud_file_id` (`file_id`),
  KEY `idx_resume_cloud_file_user_id` (`user_id`),
  CONSTRAINT `fk_resume_cloud_file_user` FOREIGN KEY (`user_id`) REFERENCES `Users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================
-- 3) Jobs and Applications
-- =============================================================

CREATE TABLE `jobs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `job_code` VARCHAR(36) NOT NULL,
  `title` VARCHAR(255) NOT NULL DEFAULT 'HR',
  `post` VARCHAR(255) NOT NULL DEFAULT 'HR',
  `hr_type` VARCHAR(255) NOT NULL,
  `other_hr_type` VARCHAR(255) DEFAULT NULL,
  `working_type` VARCHAR(100) NOT NULL,
  `office_location` VARCHAR(255) DEFAULT NULL,
  `start_date_type` VARCHAR(100) NOT NULL,
  `specific_start_date` DATE DEFAULT NULL,
  `salary` VARCHAR(255) NOT NULL,
  `last_date_to_apply` DATE NOT NULL,
  `description` TEXT DEFAULT NULL,
  `skills_required` TEXT DEFAULT NULL,
  `who_can_apply` TEXT DEFAULT NULL,
  `company_id` VARCHAR(36) NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'OPEN',
  `domain` VARCHAR(50) NOT NULL DEFAULT 'TECH',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_code` (`job_code`),
  KEY `idx_jobs_company_id` (`company_id`),
  CONSTRAINT `fk_job_company` FOREIGN KEY (`company_id`) REFERENCES `Company` (`company_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `job_applications` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `application_code` VARCHAR(36) NOT NULL,
  `user_id` VARCHAR(36) NOT NULL,
  `base_resume_version_id` BIGINT DEFAULT NULL,
  `tailored_resume_version_id` BIGINT DEFAULT NULL,
  `company_name` VARCHAR(255) NOT NULL,
  `job_title` VARCHAR(255) NOT NULL,
  `application_source` VARCHAR(255) DEFAULT NULL,
  `job_description` TEXT DEFAULT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'CREATED',
  `ats_score_before` INT DEFAULT NULL,
  `ats_score_after` INT DEFAULT NULL,
  `notes` TEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application_code` (`application_code`),
  KEY `idx_job_application_user_id` (`user_id`),
  KEY `idx_job_application_base_resume_version_id` (`base_resume_version_id`),
  KEY `idx_job_application_tailored_resume_version_id` (`tailored_resume_version_id`),
  CONSTRAINT `fk_job_application_user` FOREIGN KEY (`user_id`) REFERENCES `Users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_job_application_base_resume_version` FOREIGN KEY (`base_resume_version_id`) REFERENCES `resume_versions` (`resume_version_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_job_application_tailored_resume_version` FOREIGN KEY (`tailored_resume_version_id`) REFERENCES `resume_versions` (`resume_version_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `internal_job_applications` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `job_id` BIGINT NOT NULL,
  `admin_id` VARCHAR(36) NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  `resume_type` VARCHAR(50) NOT NULL DEFAULT 'UPLOADED',
  `resume_file_id` BIGINT DEFAULT NULL,
  `applied_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_internal_job_application_job_id` (`job_id`),
  KEY `idx_internal_job_application_admin_id` (`admin_id`),
  CONSTRAINT `fk_internal_job_application_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_internal_job_application_admin` FOREIGN KEY (`admin_id`) REFERENCES `Admin` (`admin_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_internal_job_application_resume_file` FOREIGN KEY (`resume_file_id`) REFERENCES `resume_file_assets` (`resume_file_asset_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `tool_requirement_answers` (
  `tool_requirement_answer_id` BIGINT NOT NULL AUTO_INCREMENT,
  `job_application_id` BIGINT NOT NULL,
  `tool_name` VARCHAR(150) NOT NULL,
  `required_flag` TINYINT(1) NOT NULL DEFAULT 0,
  `user_knows_tool` TINYINT(1) NOT NULL DEFAULT 0,
  `user_experience_level` VARCHAR(50) DEFAULT NULL,
  `notes` TEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`tool_requirement_answer_id`),
  KEY `idx_tool_requirement_answer_job_application_id` (`job_application_id`),
  CONSTRAINT `fk_tool_requirement_answer_job_application` FOREIGN KEY (`job_application_id`) REFERENCES `job_applications` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================
-- 4) Interview flow tables
-- =============================================================

CREATE TABLE `interview_sessions` (
  `interview_session_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `admin_id` BIGINT DEFAULT NULL,
  `interview_token` VARCHAR(128) DEFAULT NULL,
  `resume_id` BIGINT DEFAULT NULL,
  `resume_version_id` BIGINT DEFAULT NULL,
  `interview_type` VARCHAR(50) NOT NULL,
  `interview_mode` VARCHAR(50) NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'CREATED',
  `role` VARCHAR(255) DEFAULT NULL,
  `domain` VARCHAR(255) DEFAULT NULL,
  `difficulty` INT NOT NULL DEFAULT 3,
  `total_questions` INT NOT NULL DEFAULT 10,
  `current_question_index` INT NOT NULL DEFAULT 0,
  `duration_minutes` INT DEFAULT NULL,
  `allow_hints` TINYINT(1) NOT NULL DEFAULT 1,
  `include_behavioral` TINYINT(1) NOT NULL DEFAULT 1,
  `include_technical` TINYINT(1) NOT NULL DEFAULT 1,
  `resume_based` TINYINT(1) NOT NULL DEFAULT 0,
  `github_based` TINYINT(1) NOT NULL DEFAULT 0,
  `job_description_based` TINYINT(1) NOT NULL DEFAULT 0,
  `preferred_language` VARCHAR(50) DEFAULT NULL,
  `job_description` TEXT DEFAULT NULL,
  `github_urls` TEXT DEFAULT NULL,
  `feedback_summary` TEXT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`interview_session_id`),
  UNIQUE KEY `uk_interview_token` (`interview_token`),
  KEY `idx_interview_session_user_id` (`user_id`),
  KEY `idx_interview_session_admin_id` (`admin_id`),
  KEY `idx_interview_session_status` (`status`),
  KEY `idx_interview_session_type` (`interview_type`),
  KEY `idx_interview_session_mode` (`interview_mode`),
  KEY `idx_interview_session_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `interview_turns` (
  `interview_turn_id` BIGINT NOT NULL AUTO_INCREMENT,
  `interview_session_id` BIGINT NOT NULL,
  `question_index` INT NOT NULL,
  `question_type` VARCHAR(50) DEFAULT NULL,
  `category` VARCHAR(255) DEFAULT NULL,
  `difficulty` INT DEFAULT NULL,
  `question` TEXT NOT NULL,
  `source_summary` VARCHAR(1000) DEFAULT NULL,
  `resume_based` TINYINT(1) NOT NULL DEFAULT 0,
  `github_based` TINYINT(1) NOT NULL DEFAULT 0,
  `job_description_based` TINYINT(1) NOT NULL DEFAULT 0,
  `answer` TEXT DEFAULT NULL,
  `transcript` TEXT DEFAULT NULL,
  `answer_language` VARCHAR(50) DEFAULT NULL,
  `speech_based` TINYINT(1) NOT NULL DEFAULT 0,
  `skipped` TINYINT(1) NOT NULL DEFAULT 0,
  `hint_used` TINYINT(1) NOT NULL DEFAULT 0,
  `sample_answer_used` TINYINT(1) NOT NULL DEFAULT 0,
  `evaluated` TINYINT(1) NOT NULL DEFAULT 0,
  `duration_seconds` INT DEFAULT NULL,
  `score` INT DEFAULT NULL,
  `feedback_summary` TEXT DEFAULT NULL,
  `hint_text` TEXT DEFAULT NULL,
  `sample_answer` TEXT DEFAULT NULL,
  `follow_up_question` TEXT DEFAULT NULL,
  `client_timestamp` VARCHAR(100) DEFAULT NULL,
  `asked_at` DATETIME DEFAULT NULL,
  `answered_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`interview_turn_id`),
  KEY `idx_interview_turn_session_id` (`interview_session_id`),
  KEY `idx_interview_turn_question_index` (`question_index`),
  KEY `idx_interview_turn_created_at` (`created_at`),
  KEY `idx_interview_turn_question_type` (`question_type`),
  CONSTRAINT `fk_interview_turn_session` FOREIGN KEY (`interview_session_id`) REFERENCES `interview_sessions` (`interview_session_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `interview_evaluations` (
  `interview_evaluation_id` BIGINT NOT NULL AUTO_INCREMENT,
  `interview_session_id` BIGINT NOT NULL,
  `interview_turn_id` BIGINT DEFAULT NULL,
  `evaluation_type` VARCHAR(50) NOT NULL DEFAULT 'TURN',
  `evaluation_mode` VARCHAR(50) DEFAULT NULL,
  `forced_reevaluation` TINYINT(1) NOT NULL DEFAULT 0,
  `overall_score` INT DEFAULT NULL,
  `confidence_score` INT DEFAULT NULL,
  `knowledge_score` INT DEFAULT NULL,
  `communication_score` INT DEFAULT NULL,
  `clarity_score` INT DEFAULT NULL,
  `relevance_score` INT DEFAULT NULL,
  `emotional_composure_score` INT DEFAULT NULL,
  `technical_depth_score` INT DEFAULT NULL,
  `problem_solving_score` INT DEFAULT NULL,
  `professionalism_score` INT DEFAULT NULL,
  `presence_score` INT DEFAULT NULL,
  `summary` TEXT DEFAULT NULL,
  `feedback` TEXT DEFAULT NULL,
  `explanation` TEXT DEFAULT NULL,
  `strengths` TEXT DEFAULT NULL,
  `weaknesses` TEXT DEFAULT NULL,
  `improvement_suggestions` TEXT DEFAULT NULL,
  `detected_skills` TEXT DEFAULT NULL,
  `missing_concepts` TEXT DEFAULT NULL,
  `rubric_notes` TEXT DEFAULT NULL,
  `next_step_suggestion` TEXT DEFAULT NULL,
  `follow_up_question` TEXT DEFAULT NULL,
  `ready_for_next_question` TINYINT(1) DEFAULT NULL,
  `grade` VARCHAR(50) DEFAULT NULL,
  `recommendation` VARCHAR(100) DEFAULT NULL,
  `evaluation_source` VARCHAR(50) NOT NULL DEFAULT 'AI_ENGINE',
  `latency_ms` INT DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`interview_evaluation_id`),
  KEY `idx_interview_eval_session_id` (`interview_session_id`),
  KEY `idx_interview_eval_turn_id` (`interview_turn_id`),
  KEY `idx_interview_eval_type` (`evaluation_type`),
  KEY `idx_interview_eval_created_at` (`created_at`),
  CONSTRAINT `fk_interview_evaluation_session` FOREIGN KEY (`interview_session_id`) REFERENCES `interview_sessions` (`interview_session_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_interview_evaluation_turn` FOREIGN KEY (`interview_turn_id`) REFERENCES `interview_turns` (`interview_turn_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================================
-- 5) Common database queries
-- =============================================================

-- Create a user
-- INSERT INTO `Users` (user_id, name, surname, email_address, mobile_number, password, user_created_date, user_created_time, share_id, role)
-- VALUES (UUID(), 'John', 'Doe', 'john@example.com', '9876543210', 'hashed_password', CURDATE(), CURTIME(), UUID(), 'USER');

-- Get user by email
SELECT * FROM `Users` WHERE `email_address` = 'john@example.com';

-- Get all open jobs
SELECT * FROM `jobs` WHERE `status` = 'OPEN' ORDER BY `created_at` DESC;

-- Get jobs for a company
SELECT * FROM `jobs` WHERE `company_id` = 'company-id-here' ORDER BY `created_at` DESC;

-- Create a job application
-- INSERT INTO `job_applications` (application_code, user_id, company_name, job_title, application_source, job_description, status)
-- VALUES (UUID(), 'user-id-here', 'Microsoft', 'Software Engineer', 'LinkedIn', 'Backend Java role', 'CREATED');

-- Get applications by user
SELECT * FROM `job_applications` WHERE `user_id` = 'user-id-here' ORDER BY `created_at` DESC;

-- Get resume versions for a resume
SELECT * FROM `resume_versions` WHERE `resume_id` = 1 ORDER BY `created_at` DESC;

-- Get interview sessions for a user
SELECT * FROM `interview_sessions` WHERE `user_id` = 1 ORDER BY `created_at` DESC;

-- Get turns for an interview session
SELECT * FROM `interview_turns` WHERE `interview_session_id` = 1 ORDER BY `question_index` ASC;

-- Get evaluations for a session
SELECT * FROM `interview_evaluations` WHERE `interview_session_id` = 1 ORDER BY `created_at` DESC;

-- Update a user's profile summary
-- UPDATE `user_profiles` SET `profile_summary` = 'Experienced backend engineer', `updated_at` = NOW() WHERE `user_id` = 'user-id-here';

-- Mark a notification as read
-- UPDATE `Notification` SET `is_read` = 1 WHERE `id` = 1;

-- Delete an old interview session
-- DELETE FROM `interview_sessions` WHERE `interview_session_id` = 1;
