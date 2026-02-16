CREATE TABLE products (
      id UUID PRIMARY KEY,
      barcode VARCHAR(255),
      name VARCHAR(255) NOT NULL,
      brand VARCHAR(255),
      energy_kcal NUMERIC(10, 1) NOT NULL,
      carbohydrates NUMERIC(10, 1) NOT NULL,
      sugars NUMERIC(10, 1),
      fat NUMERIC(10, 1),
      saturated_fat NUMERIC(10, 1),
      protein NUMERIC(10, 1),
      fiber NUMERIC(10, 1),
      salt NUMERIC(10, 2),
      glycemic_index INTEGER,
      published BOOLEAN NOT NULL DEFAULT FALSE,
      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
      updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX idx_products_barcode ON products(barcode);
CREATE INDEX idx_products_name ON products(name);

CREATE TABLE product_portions (
      id UUID PRIMARY KEY,
      product_id UUID NOT NULL,
      name VARCHAR(255) NOT NULL,
      weight_in_grams NUMERIC(10, 1) NOT NULL,
      published BOOLEAN NOT NULL DEFAULT FALSE,
      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
      updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

      CONSTRAINT fk_portions_product
          FOREIGN KEY (product_id)
              REFERENCES products(id)
              ON DELETE CASCADE
);

CREATE INDEX idx_product_portions_product_id ON product_portions(product_id);