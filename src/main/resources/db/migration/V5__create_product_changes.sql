ALTER TABLE products
    ADD COLUMN created_by UUID;

ALTER TABLE product_portions
    ADD COLUMN created_by UUID;

CREATE INDEX idx_products_created_by ON products(created_by);
CREATE INDEX idx_product_portions_created_by ON product_portions(created_by);


CREATE TABLE product_changes (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    user_id UUID NOT NULL,
    name VARCHAR(255),
    brand VARCHAR(255),
    barcode VARCHAR(255),
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
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_product_changes_product
     FOREIGN KEY (product_id)
         REFERENCES products(id)
         ON DELETE CASCADE,

    CONSTRAINT uq_product_user_change UNIQUE (product_id, user_id)
);

CREATE INDEX idx_product_changes_lookup ON product_changes(product_id, user_id);

CREATE TABLE portion_changes (
    id UUID PRIMARY KEY,
    portion_id UUID NOT NULL,
    user_id UUID NOT NULL,
    name VARCHAR(255),
    weight_in_grams NUMERIC(10, 1),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_portion_changes_portion
     FOREIGN KEY (portion_id)
         REFERENCES product_portions(id)
         ON DELETE CASCADE,

    CONSTRAINT uq_portion_user_change UNIQUE (portion_id, user_id)
);

CREATE INDEX idx_portion_changes_lookup ON portion_changes(portion_id, user_id);
CREATE INDEX idx_product_changes_deleted ON product_changes(deleted);
CREATE INDEX idx_portion_changes_deleted ON portion_changes(deleted);