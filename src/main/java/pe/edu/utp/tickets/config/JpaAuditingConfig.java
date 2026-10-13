package pe.edu.utp.tickets.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Activa @CreatedDate y @LastModifiedDate en las entidades que extienden AuditableEntity
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}