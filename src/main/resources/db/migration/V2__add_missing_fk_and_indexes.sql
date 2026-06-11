-- Missing FK: user_products.product_id had no FK to products(id).
-- Audit finding SEC-5.
ALTER TABLE user_products
    ADD CONSTRAINT fk_user_products_product_id
    FOREIGN KEY (product_id) REFERENCES products(id);

-- Performance indexes for the two most common filter queries.
CREATE INDEX idx_exams_provider_level        ON exams(provider_id, level_id);
CREATE INDEX idx_exam_details_provider_level ON exam_details(provider_id, level_id);

-- Speeds up the per-user token lookup on every refresh call.
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
