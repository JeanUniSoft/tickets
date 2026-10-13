package pe.edu.utp.tickets;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import pe.edu.utp.tickets.config.ApiPaths;
import pe.edu.utp.tickets.model.Category;
import pe.edu.utp.tickets.model.Priority;
import pe.edu.utp.tickets.model.Role;
import pe.edu.utp.tickets.model.Ticket;
import pe.edu.utp.tickets.model.User;
import pe.edu.utp.tickets.repository.CategoryRepository;
import pe.edu.utp.tickets.repository.CommentRepository;
import pe.edu.utp.tickets.repository.RoleRepository;
import pe.edu.utp.tickets.repository.TicketRepository;
import pe.edu.utp.tickets.repository.UserRepository;
import pe.edu.utp.tickets.security.JwtService;

/**
 * Base de las pruebas de integración: levanta la aplicación completa contra H2 y, antes de
 * cada prueba, deja la base con los mismos datos conocidos:
 * usuarios admin (ROLE_ADMIN) y ana (ROLE_USER), la categoría Hardware y un ticket de ana.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class BaseIntegrationTest {

    protected static final String API = ApiPaths.V1;
    protected static final long NON_EXISTENT_ID = 999_999L;

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected RoleRepository roleRepository;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected CategoryRepository categoryRepository;
    @Autowired
    protected TicketRepository ticketRepository;
    @Autowired
    protected CommentRepository commentRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;
    @Autowired
    protected JwtService jwtService;

    protected User ana;
    protected Category hardware;
    protected Ticket ticket;

    @BeforeEach
    void setUpData() {
        // Se borra en orden: primero lo que apunta a otras tablas
        commentRepository.deleteAll();
        ticketRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role adminRole = roleRepository.save(new Role("ROLE_ADMIN"));
        Role userRole = roleRepository.save(new Role("ROLE_USER"));
        saveUser("admin", "admin123", "Administrador", adminRole);
        ana = saveUser("ana", "ana123", "Ana Torres", userRole);

        hardware = categoryRepository.save(new Category("Hardware", "Equipos y periféricos"));
        ticket = ticketRepository.save(new Ticket("La PC no enciende",
                "Presiono el botón y no pasa nada", Priority.HIGH, hardware, ana));
    }

    private User saveUser(String username, String password, String fullName, Role role) {
        User user = new User(username, passwordEncoder.encode(password), fullName, username + "@tickets.pe");
        user.addRole(role);
        return userRepository.save(user);
    }

    /** Valor de la cabecera Authorization con un token válido de ese usuario. */
    protected String bearer(String username) {
        return "Bearer " + jwtService.generateToken(username);
    }

    /** Los ids son Long en Java, pero en el JSON de respuesta se leen como enteros. */
    protected int asInt(Long id) {
        return id.intValue();
    }
}
