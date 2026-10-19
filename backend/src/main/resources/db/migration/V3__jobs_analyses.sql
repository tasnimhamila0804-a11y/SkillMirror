CREATE TABLE job_offers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(190) NOT NULL,
  company VARCHAR(190) NULL,
  description ${text_type} NOT NULL,
  experience_level VARCHAR(40) NULL,
  education_requirement VARCHAR(190) NULL,
  extraction_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_job_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_job_user ON job_offers (user_id);

CREATE TABLE job_skills (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  job_offer_id BIGINT NOT NULL,
  skill_id BIGINT NULL,
  skill_name VARCHAR(120) NOT NULL,
  normalized_name VARCHAR(140) NOT NULL,
  category VARCHAR(40) NOT NULL,
  requirement VARCHAR(20) NOT NULL DEFAULT 'REQUIRED',
  weight INT NOT NULL DEFAULT 2,
  required_level VARCHAR(30) NULL,
  source VARCHAR(20) NOT NULL DEFAULT 'AI',
  confirmed BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_jobskill_job FOREIGN KEY (job_offer_id) REFERENCES job_offers (id) ON DELETE CASCADE,
  CONSTRAINT fk_jobskill_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE SET NULL
);

CREATE INDEX idx_jobskill_offer ON job_skills (job_offer_id);

CREATE TABLE analyses (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  job_offer_id BIGINT NOT NULL,
  score INT NOT NULL DEFAULT 0,
  summary VARCHAR(2000) NULL,
  explanation ${text_type} NULL,
  simulated_ai BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_analysis_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_analysis_job FOREIGN KEY (job_offer_id) REFERENCES job_offers (id) ON DELETE CASCADE
);

CREATE INDEX idx_analysis_user ON analyses (user_id, created_at);

CREATE TABLE analysis_details (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  analysis_id BIGINT NOT NULL,
  user_skill_id BIGINT NULL,
  skill_name VARCHAR(120) NOT NULL,
  normalized_name VARCHAR(140) NOT NULL,
  category VARCHAR(40) NOT NULL,
  match_category VARCHAR(40) NOT NULL,
  required_level VARCHAR(30) NULL,
  user_level VARCHAR(30) NULL,
  weight INT NOT NULL DEFAULT 2,
  mastery_score DECIMAL(5,2) NOT NULL DEFAULT 0,
  priority INT NOT NULL DEFAULT 0,
  note VARCHAR(1000) NULL,
  CONSTRAINT fk_detail_analysis FOREIGN KEY (analysis_id) REFERENCES analyses (id) ON DELETE CASCADE,
  CONSTRAINT fk_detail_userskill FOREIGN KEY (user_skill_id) REFERENCES user_skills (id) ON DELETE SET NULL
);

CREATE INDEX idx_detail_analysis ON analysis_details (analysis_id);
