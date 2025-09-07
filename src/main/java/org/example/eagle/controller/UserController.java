package org.example.eagle.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.eagle.dto.CreateOrUpdateUserRequest;
import org.example.eagle.entity.User;
import org.example.eagle.entity.User.Address;
import org.example.eagle.repository.UserRepository;
import org.example.eagle.service.Auth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/users")
public class UserController {
    private final UserRepository userRepository;
    private final Auth auth;

    public UserController(UserRepository userRepository, Auth auth) {
        this.userRepository = userRepository;
        this.auth = auth;
    }

    private org.example.eagle.dto.UserResponse toResponse(User user) {
        return new org.example.eagle.dto.UserResponse(
            user.getName(),
            user.getAddress() == null ? null : new org.example.eagle.dto.AddressDTO(
                user.getAddress().getLine1(),
                user.getAddress().getLine2(),
                user.getAddress().getLine3(),
                user.getAddress().getTown(),
                user.getAddress().getCounty(),
                user.getAddress().getPostcode()
            ),
            user.getPhoneNumber(),
            user.getEmail()
        );
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateOrUpdateUserRequest request) {
        if (request.email() == null || request.password() == null || request.name() == null ||
            request.address() == null || request.address().line1() == null) {
            return ResponseEntity.status(400).body("Missing required fields: email, password, name, and address line1 must not be null");
        }
        User user = new User();
        user.setEmail(request.email());
        String salt = auth.generateSalt();
        user.setSalt(salt);
        String hash = auth.hashPassword(request.password(), salt);
        user.setHash(hash);
        user.setName(request.name());
        user.setPhoneNumber(request.phoneNumber());
        user.setAddress(new Address(
                request.address().line1(),
                request.address().line2(),
                request.address().line3(),
                request.address().town(),
                request.address().county(),
                request.address().postcode()
        ));
        userRepository.save(user);
        var userResponse = toResponse(user);
        return ResponseEntity.status(201).body(userResponse);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> fetchUserByID(@PathVariable Long userId, HttpServletRequest request) {
        String currentUserId = String.valueOf(request.getAttribute("userId"));
        if (!currentUserId.equals(String.valueOf(userId))) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "You are not allowed to access this user");
        }
        var user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        var userResponse = toResponse(user.get());
        return ResponseEntity.ok(userResponse);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody CreateOrUpdateUserRequest request) {
        var maybeUser = userRepository.findById(userId);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        var user = maybeUser.get();
        if (request.name() != null) user.setEmail(request.name());
        if (request.phoneNumber() != null) user.setPhoneNumber(request.phoneNumber());
        if (request.address() != null) {
            user.setAddress(new Address(
                    request.address().line1(),
                    request.address().line2(),
                    request.address().line3(),
                    request.address().town(),
                    request.address().county(),
                    request.address().postcode()
            ));
        }
        userRepository.save(user);
        var userResponse = toResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        var user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        userRepository.delete(user.get());
        return ResponseEntity.noContent().build();
    }
}
