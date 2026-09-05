ALTER TABLE product_names
    ADD CONSTRAINT fk_product_names_user
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

