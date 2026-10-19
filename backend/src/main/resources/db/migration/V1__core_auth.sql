CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  profile_completed BOOLEAN NOT NULL DEFAULT FALSE,
  last_login_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_users_email ON users (email);

CREATE TABLE profiles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  situation VARCHAR(60) NULL,
  domain VARCHAR(160) NULL,
  education_level VARCHAR(120) NULL,
  experience_summary VARCHAR(2000) NULL,
  target_job VARCHAR(160) NULL,
  career_goal VARCHAR(60) NULL,
  target_company VARCHAR(160) NULL,
  position_type VARCHAR(40) NULL,
  hours_per_week INT NULL,
  learning_preference VARCHAR(40) NULL,
  preferred_difficulty VARCHAR(30) NULL,
  deadline VARCHAR(60) NULL,
  headline VARCHAR(220) NULL,
  bio VARCHAR(4000) NULL,
  github_url VARCHAR(300) NULL,
  portfolio_url VARCHAR(300) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_profiles_user ON profiles (user_id);

CREATE TABLE questionnaire_responses (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  current_step INT NOT NULL DEFAULT 1,
  completed BOOLEAN NOT NULL DEFAULT FALSE,
  payload ${text_type} NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_questionnaire_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_questionnaire_user ON questionnaire_responses (user_id);

CREATE TABLE error_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  level VARCHAR(20) NOT NULL,
  message VARCHAR(1000) NOT NULL,
  path VARCHAR(300) NULL,
  method VARCHAR(10) NULL,
  user_id BIGINT NULL,
  details ${text_type} NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_error_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_error_logs_created ON error_logs (created_at);

CREATE TABLE app_settings (
  setting_key VARCHAR(80) PRIMARY KEY,
  setting_value VARCHAR(500) NOT NULL,
  description VARCHAR(300) NULL,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
