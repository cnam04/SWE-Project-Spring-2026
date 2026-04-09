-- init.sql — runs automatically on first container start
-- this contains test data and test tables for now. 

CREATE TABLE IF NOT EXISTS test_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

INSERT INTO test_items (name) VALUES
    ('The'),
    ('Database'),
    ('is'),
    ('working!'),
    ('Nice :)');


