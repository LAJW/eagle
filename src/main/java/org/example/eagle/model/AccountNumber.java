package org.example.eagle.model;

import com.fasterxml.jackson.annotation.JsonValue;

public final class AccountNumber {
    private final int value;

    public AccountNumber(int value) {
        if (value <= 0 || value > 99999999) {
            throw new IllegalArgumentException("Account number must be positive and at most 8 digits");
        }
        this.value = value;
    }

    public AccountNumber(String value) {
        if (value == null || value.length() != 8 || !value.matches("\\d{8}")) {
            throw new IllegalArgumentException("Account number string must be 8 digits");
        }
        int intValue = Integer.parseInt(value);
        if (intValue <= 0) {
            throw new IllegalArgumentException("Account number must be positive");
        }
        this.value = intValue;
    }

    public int asInt() {
        return value;
    }

    @JsonValue
    @Override
    public String toString() {
        return String.format("%08d", value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountNumber that = (AccountNumber) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}

