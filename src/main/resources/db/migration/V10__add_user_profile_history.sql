CREATE TABLE user_profile_history (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    weight_in_kg VARCHAR(255),
    height_in_cm VARCHAR(255),
    body_fat_percentage VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_user_profile_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_profile_history_user_id ON user_profile_history(user_id);
CREATE INDEX idx_user_profile_history_created_at ON user_profile_history(created_at);