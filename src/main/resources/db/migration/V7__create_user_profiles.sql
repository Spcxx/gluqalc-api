CREATE TABLE user_profiles (
    user_id UUID PRIMARY KEY,

    gender VARCHAR(255),
    weight_in_kg VARCHAR(255),
    height_in_cm VARCHAR(255),
    birth_date VARCHAR(255),
    physical_activity_level VARCHAR(255),
    kcal_goal_difference VARCHAR(255),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_user_profiles_user
       FOREIGN KEY (user_id)
           REFERENCES users(id)
);
