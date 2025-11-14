package co.edu.javeriana.prestamos.service;

import co.edu.javeriana.prestamos.dto.*;
import co.edu.javeriana.prestamos.exception.AuthException;
import co.edu.javeriana.prestamos.exception.ValidationException;
import co.edu.javeriana.prestamos.model.Usuario;
import co.edu.javeriana.prestamos.repository.UsuarioRepository;
import co.edu.javeriana.prestamos.security.CustomUserDetails;
import co.edu.javeriana.prestamos.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) throws ValidationException {
        System.out.println("🔧 AuthService - Registrando usuario: " + request.getUsername());
        
        // Validaciones
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("El username ya está en uso");
        }
        
        if (request.getEmail() != null && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("El email ya está registrado");
        }
        
        if (!request.getContrasena().matches(".*\\d.*")) {
            throw new ValidationException("La contraseña debe contener al menos un número");
        }
        
        if (request.getId_tipo_usuario() < 1 || request.getId_tipo_usuario() > 3) {
            throw new ValidationException("Tipo de usuario inválido");
        }
        
        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        usuario.setId_tipo_usuario(request.getId_tipo_usuario());
        
        // Los demás campos se llenan automáticamente con @PrePersist
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        System.out.println("✅ Usuario guardado con ID: " + usuarioGuardado.getId_usuario());
        
        // Generar token
        CustomUserDetails userDetails = new CustomUserDetails(usuarioGuardado);
        String token = jwtService.generateToken(userDetails);
        
        return AuthResponse.builder()
                .id_usuario(usuarioGuardado.getId_usuario())
                .token(token)
                .mensaje("Registro exitoso")
                .usuario_info(buildUserInfo(usuarioGuardado))
                .permisos(getPermisosArray(usuarioGuardado.getId_tipo_usuario()))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) throws AuthException {
        try {
            Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new AuthException("Credenciales inválidas"));
            
            if (usuario.getIntentos_fallidos() >= 5) {
                throw new AuthException("Cuenta bloqueada por múltiples intentos fallidos");
            }
            
            if (usuario.getId_estado_usuario() != 1) {
                throw new AuthException("Cuenta inactiva");
            }
            
            try {
                Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getContrasena()
                    )
                );
                
                // Resetear intentos fallidos
                usuario.setIntentos_fallidos(0);
                usuarioRepository.save(usuario);
                
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                String token = jwtService.generateToken(userDetails);
                
                return AuthResponse.builder()
                        .id_usuario(usuario.getId_usuario())
                        .token(token)
                        .mensaje("Login exitoso")
                        .usuario_info(buildUserInfo(usuario))
                        .permisos(getPermisosArray(usuario.getId_tipo_usuario()))
                        .build();
                        
            } catch (BadCredentialsException e) {
                usuario.setIntentos_fallidos(usuario.getIntentos_fallidos() + 1);
                usuarioRepository.save(usuario);
                
                if (usuario.getIntentos_fallidos() >= 5) {
                    throw new AuthException("Cuenta bloqueada por múltiples intentos fallidos");
                }
                throw new AuthException("Credenciales inválidas. Intentos restantes: " + 
                        (5 - usuario.getIntentos_fallidos()));
            }
            
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException("Error en el proceso de autenticación");
        }
    }

    private UserInfo buildUserInfo(Usuario usuario) {
        return UserInfo.builder()
                .id_usuario(usuario.getId_usuario())
                .nombre(usuario.getNombre())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .id_tipo_usuario(usuario.getId_tipo_usuario())
                .tipo_usuario(getTipoUsuarioNombre(usuario.getId_tipo_usuario()))
                .build();
    }

    private String getTipoUsuarioNombre(Integer tipoId) {
        switch (tipoId) {
            case 1: return "Estudiante";
            case 2: return "Bibliotecario";
            case 3: return "Admin";
            default: return "Estudiante";
        }
    }

    private String[] getPermisosArray(Integer tipoId) {
        switch (tipoId) {
            case 1: return new String[]{"VER_CATALOGO", "SOLICITAR_PRESTAMO", "VER_MIS_PRESTAMOS"};
            case 2: return new String[]{"VER_CATALOGO", "SOLICITAR_PRESTAMO", "VER_MIS_PRESTAMOS", 
                                       "APROBAR_PRESTAMO", "GESTIONAR_LIBROS", "VER_TODOS_PRESTAMOS"};
            case 3: return new String[]{"VER_CATALOGO", "SOLICITAR_PRESTAMO", "VER_MIS_PRESTAMOS", 
                                       "APROBAR_PRESTAMO", "GESTIONAR_LIBROS", "VER_TODOS_PRESTAMOS", 
                                       "GESTIONAR_USUARIOS", "VER_REPORTES"};
            default: return new String[]{"VER_CATALOGO"};
        }
    }
}