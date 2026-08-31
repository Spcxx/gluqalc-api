ALTER TABLE products DROP CONSTRAINT IF EXISTS products_barcode_key;
CREATE UNIQUE INDEX idx_products_barcode_active ON products(barcode) WHERE deleted = false;