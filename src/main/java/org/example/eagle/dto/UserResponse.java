package org.example.eagle.dto;

public record UserResponse(
        String name,
        AddressDTO address,
        String phoneNumber,
        String email
) {
}
