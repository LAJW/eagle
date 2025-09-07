DROP TABLE IF EXISTS transaction;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS users;

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(32),
    address JSONB
);

-- Accounts table
CREATE TABLE accounts (
    account_number SERIAL PRIMARY KEY,
    sort_code VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    account_type VARCHAR(64) NOT NULL,
    balance DOUBLE PRECISION NOT NULL,
    currency VARCHAR(8) NOT NULL,
    created_timestamp TIMESTAMP NOT NULL,
    updated_timestamp TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id)
);

-- Transactions table
CREATE TABLE transaction (
    id BIGSERIAL PRIMARY KEY,
    account_id INTEGER NOT NULL REFERENCES accounts(account_number),
    amount DOUBLE PRECISION NOT NULL,
    currency VARCHAR(8) NOT NULL,
    type VARCHAR(32) NOT NULL,
    reference VARCHAR(255),
    created_timestamp TIMESTAMP NOT NULL,
    user_id VARCHAR(255) NOT NULL
);
