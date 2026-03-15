CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,

    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    barcode VARCHAR(255) UNIQUE,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(255),
    energy_kcal NUMERIC(10, 2) NOT NULL,
    carbohydrates NUMERIC(10, 2) NOT NULL,
    sugars NUMERIC(10, 2),
    fat NUMERIC(10, 2),
    saturated_fat NUMERIC(10, 2),
    protein NUMERIC(10, 2),
    fiber NUMERIC(10, 2),
    salt NUMERIC(10, 4),
    glycemic_index INTEGER,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_products_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_products_name ON products(name);

CREATE TABLE product_portions (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    weight_in_grams NUMERIC(10, 2) NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_portions_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_portions_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_product_portions_product_id ON product_portions(product_id);

CREATE TABLE product_changes (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    user_id UUID NOT NULL,
    name VARCHAR(255),
    brand VARCHAR(255),
    barcode VARCHAR(255),
    energy_kcal NUMERIC(10, 2),
    carbohydrates NUMERIC(10, 2),
    sugars NUMERIC(10, 2),
    fat NUMERIC(10, 2),
    saturated_fat NUMERIC(10, 2),
    protein NUMERIC(10, 2),
    fiber NUMERIC(10, 2),
    salt NUMERIC(10, 4),
    glycemic_index INTEGER,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_product_changes_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uq_product_user_change UNIQUE (product_id, user_id),
    CONSTRAINT fk_product_changes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE portion_changes (
    id UUID PRIMARY KEY,
    portion_id UUID NOT NULL,
    user_id UUID NOT NULL,
    name VARCHAR(255),
    weight_in_grams NUMERIC(10, 1),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_portion_changes_portion FOREIGN KEY (portion_id) REFERENCES product_portions(id) ON DELETE CASCADE,
    CONSTRAINT uq_portion_user_change UNIQUE (portion_id, user_id),
    CONSTRAINT fk_portion_changes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE meal_categories (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_meal_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_meal_categories_user_id ON meal_categories(user_id);

CREATE TABLE meal_entries (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    meal_category_id UUID NOT NULL,
    consumed_at DATE NOT NULL,
    product_id UUID,
    product_name VARCHAR(255),
    brand VARCHAR(255),
    barcode VARCHAR(255),
    portion_id UUID,
    portion_name VARCHAR(255),
    weight_in_grams NUMERIC(10, 2) NOT NULL,
    portion_unit_weight NUMERIC(10, 2),
    portion_quantity NUMERIC(10, 2),
    energy_kcal NUMERIC(10, 2),
    carbohydrates NUMERIC(10, 2),
    sugars NUMERIC(10, 2),
    fat NUMERIC(10, 2),
    saturated_fat NUMERIC(10, 2),
    protein NUMERIC(10, 2),
    fiber NUMERIC(10, 2),
    salt NUMERIC(10, 4),
    glycemic_index INTEGER,
    consumed_at_time TIME NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_meal_entries_category FOREIGN KEY (meal_category_id) REFERENCES meal_categories(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_entries_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_meal_entries_user_id ON meal_entries(user_id);
CREATE INDEX idx_meal_entries_date ON meal_entries(consumed_at);
CREATE INDEX idx_meal_entries_category_id ON meal_entries(meal_category_id);

CREATE TABLE user_profiles (
    user_id UUID PRIMARY KEY,

    gender VARCHAR(255),
    weight_in_kg VARCHAR(255),
    height_in_cm VARCHAR(255),
    birth_date VARCHAR(255),
    physical_activity_level VARCHAR(255),
    kcal_goal_difference VARCHAR(255),
    weekly_kcal_distribution VARCHAR(1000),
    body_fat_percentage VARCHAR(255),
    bmr_method VARCHAR(255),
    macro_strategy VARCHAR(512),
    insulin_sensitivity_factor VARCHAR(255),
    insulin_fat_protein_ratio VARCHAR(255),
    hourly_carb_ratio VARCHAR(2000),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE device_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    refresh_token VARCHAR(512) NOT NULL UNIQUE,
    user_agent VARCHAR(512),
    ip_address VARCHAR(255),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_accessed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_device_sessions_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_user_device UNIQUE (user_id, device_id)
);

CREATE INDEX idx_device_sessions_user_id ON device_sessions(user_id);

CREATE TABLE user_providers (
    user_id UUID NOT NULL,
    provider VARCHAR(50) NOT NULL,

    CONSTRAINT fk_user_providers_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, provider)
);

CREATE TABLE consent_definitions (
    id UUID NOT NULL,
    code VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_consent_definitions PRIMARY KEY (id)
);

CREATE TABLE user_consents (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    consent_definition_id UUID NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ip_address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_user_consents PRIMARY KEY (id),
    CONSTRAINT fk_user_consents_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_consents_definition FOREIGN KEY (consent_definition_id) REFERENCES consent_definitions (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_consents_user_id ON user_consents (user_id);
CREATE INDEX idx_user_consents_definition_id ON user_consents (consent_definition_id);