package pe.edu.utp.tickets.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.utp.tickets.config.ApiPaths;
import pe.edu.utp.tickets.dto.LoginRequest;
import pe.edu.utp.tickets.dto.TokenResponse;
import pe.edu.utp.tickets.service.AuthService;

@RestController
@RequestMapping(ApiPaths.V1 + "/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}