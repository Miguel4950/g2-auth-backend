package co.edu.javeriana.prestamos.service;

import co.edu.javeriana.prestamos.dto.UserInfo;
import co.edu.javeriana.prestamos.dto.UserProfileUpdateRequest;
import co.edu.javeriana.prestamos.exception.ValidationException;
import co.edu.javeriana.prestamos.model.Usuario;
import co.edu.javeriana.prestamos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtener perfil de usuario por ID
     */
    public UserInfo getUserProfile(Integer userId) throws ValidationException {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado"));
        return buildUserInfo(usuario);
    }

    /**
     * Actualizar perfil de usuario
     */
    @Transactional
    public UserInfo updateProfile(Integer userId, UserProfileUpdateRequest request) throws ValidationException {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado"));
        
        // Actualizar campos si están presentes
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            usuario.setNombre(request.getNombre());
        }
        
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Validar que el email no esté en uso por otro usuario
            if (usuarioRepository.existsByEmail(request.getEmail()) && 
                !request.getEmail().equals(usuario.getEmail())) {
                throw new ValidationException("El email ya está en uso");
            }
            usuario.setEmail(request.getEmail());
        }
        
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return buildUserInfo(usuarioActualizado);
    }

    /**
     * Cambiar contraseña
     */
    @Transactional
    public void changePassword(Integer userId, String oldPassword, String newPassword) 
            throws ValidationException {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado"));
        
        // Verificar contraseña actual
        if (!passwordEncoder.matches(oldPassword, usuario.getContrasena())) {
            throw new ValidationException("La contraseña actual es incorrecta");
        }
        
        // Validar nueva contraseña
        if (newPassword.length() < 6) {
            throw new ValidationException("La nueva contraseña debe tener al menos 6 caracteres");
        }
        
        if (!newPassword.matches(".*\\d.*")) {
            throw new ValidationException("La nueva contraseña debe contener al menos un número");
        }
        
        // Actualizar contraseña
        usuario.setContrasena(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }

    /**
     * Iniciar recuperación de contraseña
     */
    @Transactional
    public void initiatePasswordReset(String email) throws ValidationException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("No existe usuario con ese email"));
        
        // En una aplicación real aquí generarías un token de recuperación
        // y lo enviarías por email. Por ahora solo simulamos el proceso
        System.out.println("Email de recuperación enviado a: " + email);
    }

    /**
     * Resetear contraseña con token (simulado)
     */
    @Transactional
    public void resetPassword(String token, String newPassword) throws ValidationException {
        // En una aplicación real aquí validarías el token
        // Por ahora es una implementación simulada
        throw new ValidationException("Funcionalidad no implementada completamente");
    }

    /**
     * Obtener usuario por ID (para bibliotecarios)
     */
    public UserInfo getUserById(Integer userId) throws ValidationException {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado"));
        return buildUserInfo(usuario);
    }

    /**
     * Obtener lista de usuarios con paginación (para bibliotecarios)
     */
    public Page<UserInfo> getAllUsers(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Usuario> usuarios = usuarioRepository.findAll(pageable);
        return usuarios.map(this::buildUserInfo);
    }

    /**
     * Activar usuario (para admin)
     */
    @Transactional
    public void activateUser(Integer userId) throws ValidationException {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado"));
        
        usuario.setId_estado_usuario(1); // 1 = activo
        usuario.setIntentos_fallidos(0); // Resetear intentos fallidos
        usuarioRepository.save(usuario);
    }

    /**
     * Desactivar usuario (para admin)
     */
    @Transactional
    public void deactivateUser(Integer userId) throws ValidationException {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Usuario no encontrado"));
        
        usuario.setId_estado_usuario(0); // 0 = inactivo
        usuarioRepository.save(usuario);
    }

    /**
     * Construir información de usuario
     */
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

    /**
     * Obtener nombre del tipo de usuario
     */
    private String getTipoUsuarioNombre(Integer tipoId) {
        switch (tipoId) {
            case 1:
                return "Estudiante";
            case 2:
                return "Bibliotecario";
            case 3:
                return "Admin";
            default:
                return "Estudiante";
        }
    }
}
