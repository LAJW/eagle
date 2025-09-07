package org.example.eagle.dto;

public record CreateOrUpdateUserRequest(
        String name,
        AddressDTO address,
        String phoneNumber,
        String email,
        String password
) {
}
