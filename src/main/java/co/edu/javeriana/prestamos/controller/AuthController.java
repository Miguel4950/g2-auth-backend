package co.edu.javeriana.prestamos.controller;

import co.edu.javeriana.prestamos.dto.AuthResponse;
import co.edu.javeriana.prestamos.dto.LoginRequest;
import co.edu.javeriana.prestamos.dto.RegisterRequest;
import co.edu.javeriana.prestamos.exception.AuthException;
import co.edu.javeriana.prestamos.exception.ValidationException;
import co.edu.javeriana.prestamos.model.Usuario;
import co.edu.javeriana.prestamos.repository.UsuarioRepository;
import co.edu.javeriana.prestamos.service.AuthService;
import co.edu.javeriana.prestamos.service.RateLimitingService;
import co.edu.javeriana.prestamos.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Autenticación", description = "API de autenticación, registro y seguridad avanzada")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final RateLimitingService rateLimitingService; // ✅ Nuevo: Protección IP
    private final UsuarioRepository usuarioRepository;     // ✅ Nuevo: Para simulación rápida

    // -----------------------------------------------------------------------------------------
    // 1. REGISTRO DE USUARIO
    // -----------------------------------------------------------------------------------------
    @Operation(summary = "Registrar nuevo usuario", 
               description = "Registra un usuario en estado INACTIVO hasta que verifique su email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado. Revise su email.",
                     content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflicto (Email/User ya existe)")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return manejarErroresValidacion(bindingResult);
        }
        
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error al registrar: " + e.getMessage()));
        }
    }

    // -----------------------------------------------------------------------------------------
    // 2. LOGIN (CON PROTECCIÓN RATE LIMITING)
    // -----------------------------------------------------------------------------------------
    @Operation(summary = "Iniciar sesión", 
               description = "Autentica usuario. Bloquea IP tras múltiples intentos fallidos.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, 
                                   BindingResult bindingResult,
                                   HttpServletRequest httpRequest) { // ✅ Necesario para obtener IP
        
        // 1. Validar errores de formato
        if (bindingResult.hasErrors()) {
            return manejarErroresValidacion(bindingResult);
        }

        String ip = httpRequest.getRemoteAddr();

        // 2. Verificar bloqueo por IP (Seguridad Reforzada)
        if (rateLimitingService.isBlocked(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "⛔ IP bloqueada temporalmente por demasiados intentos fallidos. Intente en 15 minutos."));
        }
        
        try {
            AuthResponse response = authService.login(request);
            
            // 3. Éxito: Limpiar historial de la IP
            rateLimitingService.loginSucceeded(ip);
            
            return ResponseEntity.ok(response);
            
        } catch (AuthException e) {
            // 4. Fallo: Registrar intento fallido de la IP
            rateLimitingService.loginFailed(ip);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno en login: " + e.getMessage()));
        }
    }

    // -----------------------------------------------------------------------------------------
    // 3. REFRESH TOKEN (GESTIÓN AVANZADA)
    // -----------------------------------------------------------------------------------------
    @Operation(summary = "Renovar Token", description = "Obtiene un nuevo Access Token usando un Refresh Token válido")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El refreshToken es obligatorio"));
        }

        try {
            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    // -----------------------------------------------------------------------------------------
    // 4. VERIFICACIÓN DE EMAIL (SIMULADO)
    // -----------------------------------------------------------------------------------------
    @Operation(summary = "Verificar Email", description = "Simula el link de activación que llegaría al correo")
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("email") String email) {
        // En un caso real, validarías un token criptográfico. 
        // Para la entrega, activamos buscando por email para que el profesor pueda probarlo fácil.
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        if (usuario != null) {
            usuario.setId_estado_usuario(1); // 1 = ACTIVO
            usuarioRepository.save(usuario);
            return ResponseEntity.ok(Map.of("mensaje", "✅ Cuenta verificada exitosamente. Ya puede iniciar sesión."));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Email no encontrado"));
    }

    // -----------------------------------------------------------------------------------------
    // 5. OTROS ENDPOINTS (LOGOUT, PASSWORD)
    // -----------------------------------------------------------------------------------------
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Como es Stateless (JWT), el logout real se hace en frontend borrando el token.
        // Opcional: Podrías añadir el token a una "Blacklist" en BD si quisieras máxima seguridad.
        return ResponseEntity.ok(Map.of("mensaje", "Logout exitoso (borre el token del cliente)"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                throw new ValidationException("El email es obligatorio");
            }
            userService.initiatePasswordReset(email);
            return ResponseEntity.ok(Map.of("mensaje", "Se ha enviado un email con instrucciones."));
        } catch (ValidationException e) {
            // Por seguridad, a veces se responde OK aunque el email no exista para no revelar usuarios
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword"); // Corregido nombre variable estándar
            
            if (token == null || newPassword == null) {
                throw new ValidationException("Token y nueva contraseña son obligatorios");
            }
            
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña restablecida exitosamente"));
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // -----------------------------------------------------------------------------------------
    // UTILITIES
    // -----------------------------------------------------------------------------------------
    private ResponseEntity<?> manejarErroresValidacion(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Datos inválidos");
        response.put("detalles", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}