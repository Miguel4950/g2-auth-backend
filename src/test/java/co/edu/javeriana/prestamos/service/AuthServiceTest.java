package co.edu.javeriana.prestamos.service;

import co.edu.javeriana.prestamos.dto.AuthResponse;
import co.edu.javeriana.prestamos.dto.LoginRequest;
import co.edu.javeriana.prestamos.dto.RegisterRequest;
import co.edu.javeriana.prestamos.exception.AuthException;
import co.edu.javeriana.prestamos.exception.ValidationException;
import co.edu.javeriana.prestamos.model.Usuario;
import co.edu.javeriana.prestamos.repository.UsuarioRepository;
import co.edu.javeriana.prestamos.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setNombre("Test User");
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@test.com");
        registerRequest.setContrasena("password123");
        registerRequest.setId_tipo_usuario(1);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setContrasena("password123");

        usuario = new Usuario();
        usuario.setId_usuario(1);
        usuario.setNombre("Test User");
        usuario.setUsername("testuser");
        usuario.setEmail("test@test.com");
        usuario.setContrasena("encodedPassword");
        usuario.setId_tipo_usuario(1);
        usuario.setId_estado_usuario(1);
        usuario.setIntentos_fallidos(0);
    }

    @Test
    void testRegisterSuccess() throws ValidationException {
        // Arrange
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken(any())).thenReturn("test-jwt-token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getId_usuario());
        assertEquals("test-jwt-token", response.getToken());
        assertEquals("Registro exitoso", response.getMensaje());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testRegisterUsernameAlreadyExists() {
        // Arrange
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            authService.register(registerRequest);
        });
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void testRegisterInvalidPassword() {
        // Arrange
        registerRequest.setContrasena("noNumbers");
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            authService.register(registerRequest);
        });
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
