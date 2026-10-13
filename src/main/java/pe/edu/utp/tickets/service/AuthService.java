package pe.edu.utp.tickets.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import pe.edu.utp.tickets.dto.LoginRequest;
import pe.edu.utp.tickets.dto.TokenResponse;
import pe.edu.utp.tickets.security.JwtService;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // Lanza BadCredentialsException si el usuario no existe o la contraseña no coincide
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        return new TokenResponse(jwtService.generateToken(authentication.getName()));
    }
}
