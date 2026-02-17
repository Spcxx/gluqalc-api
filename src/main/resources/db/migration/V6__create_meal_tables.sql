CREATE TABLE meal_categories (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
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
    energy_kcal NUMERIC(10, 1),
    carbohydrates NUMERIC(10, 1),
    sugars NUMERIC(10, 1),
    fat NUMERIC(10, 1),
    saturated_fat NUMERIC(10, 1),
    protein NUMERIC(10, 1),
    fiber NUMERIC(10, 1),
    salt NUMERIC(10, 2),
    glycemic_index INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_meal_entries_category
        FOREIGN KEY (meal_category_id)
            REFERENCES meal_categories(id)
);

CREATE INDEX idx_meal_entries_user_id ON meal_entries(user_id);
CREATE INDEX idx_meal_entries_date ON meal_entries(consumed_at);
CREATE INDEX idx_meal_entries_category_id ON meal_entries(meal_category_id);