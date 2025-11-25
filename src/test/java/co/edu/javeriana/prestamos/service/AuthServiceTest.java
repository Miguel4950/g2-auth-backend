package co.edu.javeriana.prestamos.service;

import co.edu.javeriana.prestamos.dto.AuthResponse;
import co.edu.javeriana.prestamos.dto.LoginRequest;
import co.edu.javeriana.prestamos.model.Usuario;
import co.edu.javeriana.prestamos.repository.UsuarioRepository;
import co.edu.javeriana.prestamos.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authManager;
    @Mock TokenService tokenService; // Mockear el nuevo servicio
    
    @InjectMocks AuthService authService;

    @Test
    void loginExitoso_DebeRetornarTokens() throws Exception {
        // Arrange
        Usuario mockUser = new Usuario();
        mockUser.setId_usuario(1);
        mockUser.setUsername("test");
        mockUser.setId_tipo_usuario(1);
        mockUser.setId_estado_usuario(1); // Activo
        
        when(usuarioRepository.findByUsername("test")).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(any())).thenReturn("token_falso");
        // Importante: Simular el refresh token también
        when(tokenService.createRefreshToken(any())).thenReturn(new co.edu.javeriana.prestamos.model.RefreshToken());

        // Act
        LoginRequest request = new LoginRequest("test", "12345");
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response.getToken());
        // assertNotNull(response.getRefreshToken()); // Descomenta si usas el getter
    }
}