package pe.edu.utp.tickets.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.edu.utp.tickets.dto.UserRequest;
import pe.edu.utp.tickets.dto.UserResponse;
import pe.edu.utp.tickets.exception.DuplicateResourceException;
import pe.edu.utp.tickets.model.Role;
import pe.edu.utp.tickets.model.User;
import pe.edu.utp.tickets.repository.RoleRepository;
import pe.edu.utp.tickets.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Ya existe el usuario " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email " + request.email());
        }
        String authority = request.role().authority();
        Role role = roleRepository.findByName(authority)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + authority + " en la tabla roles"));

        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.fullName(),
                request.email());
        user.addRole(role);

        return UserResponse.from(userRepository.save(user));
    }
}