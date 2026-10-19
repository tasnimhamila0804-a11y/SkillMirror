CREATE TABLE ai_usage_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NULL,
  purpose VARCHAR(60) NOT NULL,
  provider VARCHAR(60) NULL,
  model VARCHAR(80) NULL,
  mode VARCHAR(20) NOT NULL DEFAULT 'SIMULATED',
  prompt_tokens INT NOT NULL DEFAULT 0,
  completion_tokens INT NOT NULL DEFAULT 0,
  latency_ms BIGINT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
  error_message VARCHAR(500) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_aiusage_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_aiusage_created ON ai_usage_logs (created_at);
CREATE INDEX idx_aiusage_user ON ai_usage_logs (user_id, created_at);
