package pe.edu.utp.tickets.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Agrupa las propiedades jwt.* de application.properties
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, Duration expiration) {
}