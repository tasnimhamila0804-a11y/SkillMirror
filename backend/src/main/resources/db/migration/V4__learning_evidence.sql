CREATE TABLE learning_paths (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  analysis_id BIGINT NULL,
  analysis_detail_id BIGINT NULL,
  skill_id BIGINT NULL,
  skill_name VARCHAR(120) NOT NULL,
  title VARCHAR(220) NOT NULL,
  description ${text_type} NULL,
  difficulty VARCHAR(30) NOT NULL DEFAULT 'INTERMEDIATE',
  objectives ${text_type} NULL,
  tools ${text_type} NULL,
  steps ${text_type} NULL,
  evaluation_criteria ${text_type} NULL,
  expected_result VARCHAR(2000) NULL,
  proof_required VARCHAR(1000) NULL,
  status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
  ai_generated BOOLEAN NOT NULL DEFAULT FALSE,
  simulated_ai BOOLEAN NOT NULL DEFAULT FALSE,
  validation_note VARCHAR(2000) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at TIMESTAMP NULL,
  CONSTRAINT fk_path_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_path_analysis FOREIGN KEY (analysis_id) REFERENCES analyses (id) ON DELETE SET NULL,
  CONSTRAINT fk_path_detail FOREIGN KEY (analysis_detail_id) REFERENCES analysis_details (id) ON DELETE SET NULL,
  CONSTRAINT fk_path_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE SET NULL
);

CREATE INDEX idx_path_user ON learning_paths (user_id, status);

CREATE TABLE learning_tasks (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  learning_path_id BIGINT NOT NULL,
  title VARCHAR(300) NOT NULL,
  description VARCHAR(1000) NULL,
  position INT NOT NULL DEFAULT 0,
  done BOOLEAN NOT NULL DEFAULT FALSE,
  note VARCHAR(2000) NULL,
  done_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_task_path FOREIGN KEY (learning_path_id) REFERENCES learning_paths (id) ON DELETE CASCADE
);

CREATE INDEX idx_task_path ON learning_tasks (learning_path_id, position);

CREATE TABLE skill_evidence (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  skill_id BIGINT NULL,
  skill_name VARCHAR(120) NOT NULL,
  learning_path_id BIGINT NULL,
  project_id BIGINT NULL,
  type VARCHAR(20) NOT NULL DEFAULT 'LINK',
  url VARCHAR(600) NULL,
  file_name VARCHAR(255) NULL,
  file_path VARCHAR(600) NULL,
  description VARCHAR(4000) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
  review_note VARCHAR(1000) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_evidence_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_evidence_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE SET NULL,
  CONSTRAINT fk_evidence_path FOREIGN KEY (learning_path_id) REFERENCES learning_paths (id) ON DELETE SET NULL,
  CONSTRAINT fk_evidence_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE SET NULL
);

CREATE INDEX idx_evidence_user ON skill_evidence (user_id);
CREATE INDEX idx_evidence_path ON skill_evidence (learning_path_id);
