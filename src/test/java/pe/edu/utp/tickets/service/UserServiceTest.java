package pe.edu.utp.tickets.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import pe.edu.utp.tickets.dto.UserRequest;
import pe.edu.utp.tickets.dto.UserResponse;
import pe.edu.utp.tickets.exception.DuplicateResourceException;
import pe.edu.utp.tickets.model.Role;
import pe.edu.utp.tickets.model.RoleName;
import pe.edu.utp.tickets.model.User;
import pe.edu.utp.tickets.repository.RoleRepository;
import pe.edu.utp.tickets.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final UserRequest LUIS =
            new UserRequest("luis", "luis123", "Luis Ramos", "luis@tickets.pe", RoleName.USER);

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("UT-03 create guarda el hash de la contraseña, nunca la contraseña")
    void create_whenUserIsNew_storesPasswordHash() {
        when(userRepository.existsByUsername("luis")).thenReturn(false);
        when(userRepository.existsByEmail("luis@tickets.pe")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role("ROLE_USER")));
        when(passwordEncoder.encode("luis123")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.create(LUIS);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("HASH");
        assertThat(response.username()).isEqualTo("luis");
        assertThat(response.roles()).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("UT-04 create lanza DuplicateResourceException cuando el username ya existe")
    void create_whenUsernameExists_throwsDuplicate() {
        when(userRepository.existsByUsername("luis")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(LUIS))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Ya existe el usuario luis");

        verify(userRepository, never()).save(any(User.class));
    }
}
