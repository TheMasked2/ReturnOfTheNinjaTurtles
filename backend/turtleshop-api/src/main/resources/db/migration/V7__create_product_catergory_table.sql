CREATE TABLE IF NOT EXISTS PRODUCT_CATEGORY (
    product_id INT,
    category_id INT,
    PRIMARY KEY (product_id, category_id),
    CONSTRAINT fk_pc_product FOREIGN KEY (product_id) REFERENCES PRODUCT(product_id),
    CONSTRAINT fk_pc_category FOREIGN KEY (category_id) REFERENCES CATEGORY(category_id)
);