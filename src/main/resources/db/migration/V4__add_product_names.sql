CREATE TABLE product_names (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    type VARCHAR(30) NOT NULL,
    source VARCHAR(30) NOT NULL,
    approved BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_product_names_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uq_product_names_product_language_name
        UNIQUE (product_id, language_code, name)
);

CREATE INDEX idx_product_names_product_id ON product_names(product_id);
CREATE INDEX idx_product_names_language_code ON product_names(language_code);
CREATE INDEX idx_product_names_name ON product_names(name);
