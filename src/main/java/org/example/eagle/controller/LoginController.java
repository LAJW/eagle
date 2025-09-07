package org.example.eagle.controller;

import org.example.eagle.dto.LoginRequest;
import org.example.eagle.dto.LoginResponse;
import org.example.eagle.repository.UserRepository;
import org.example.eagle.service.Auth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/login")
public class LoginController {
    private final UserRepository userRepository;
    private final Auth authService;

    public LoginController(UserRepository userRepository, Auth authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        var user = userRepository.findByUsername(request.username());
        if (user.isEmpty() || !authService.verifyPassword(request.password(), user.get().getSalt(), user.get().getHash())) {
            return ResponseEntity.status(401).body(new LoginResponse(null, "Invalid credentials"));
        }
        String jwt = authService.generateJwt(user.get().getId());
        return ResponseEntity.ok(new LoginResponse(jwt, null));
    }
}

