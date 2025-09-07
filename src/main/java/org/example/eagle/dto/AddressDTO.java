package org.example.eagle.dto;

public record AddressDTO(
        String line1,
        String line2,
        String line3,
        String town,
        String county,
        String postcode
) {
}
