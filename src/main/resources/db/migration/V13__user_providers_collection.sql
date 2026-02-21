CREATE TABLE user_providers (
    user_id UUID NOT NULL,
    provider VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_providers_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, provider)
);

INSERT INTO user_providers (user_id, provider)
SELECT id, provider FROM users WHERE provider IS NOT NULL;

ALTER TABLE users DROP COLUMN provider;