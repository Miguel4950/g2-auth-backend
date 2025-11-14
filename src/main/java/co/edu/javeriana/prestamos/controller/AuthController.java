package co.edu.javeriana.prestamos.controller;

import co.edu.javeriana.prestamos.dto.AuthResponse;
import co.edu.javeriana.prestamos.dto.LoginRequest;
import co.edu.javeriana.prestamos.dto.RegisterRequest;
import co.edu.javeriana.prestamos.exception.AuthException;
import co.edu.javeriana.prestamos.exception.ValidationException;
import co.edu.javeriana.prestamos.service.AuthService;
import co.edu.javeriana.prestamos.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Autenticación", description = "API de autenticación y registro de usuarios")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "Registrar nuevo usuario", 
               description = "Permite registrar un nuevo usuario en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
                     content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de registro inválidos"),
        @ApiResponse(responseCode = "409", description = "Username o email ya existe")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        System.out.println("RECIBIENDO PETICIÓN REGISTER: " + request.getUsername());
        System.out.println("DATOS RECIBIDOS:");
        System.out.println("   - username: " + request.getUsername());
        System.out.println("   - nombre: " + request.getNombre());
        System.out.println("   - email: " + request.getEmail());
        // LÍNEA CORREGIDA: Cambiado 'getcontrasena()' a 'getContrasena()'
        System.out.println("   - contrasena: " + (request.getContrasena() != null ? "[PRESENTE]" : "null"));
        System.out.println("   - id_tipo_usuario: " + request.getId_tipo_usuario());


        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
            
            System.out.println("ERRORES DE VALIDACIÓN: " + errorMessage);
            
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Datos de registro inválidos");
            response.put("errores", errors);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        try {
            System.out.println("PROCESANDO REGISTRO PARA: " + request.getEmail());
            AuthResponse response = authService.register(request);
            System.out.println("REGISTRO EXITOSO: " + request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ValidationException e) {
            System.out.println("ERROR VALIDACIÓN: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            System.out.println("ERROR INESPERADO EN REGISTER: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al registrar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Iniciar sesión", 
               description = "Autentica un usuario y retorna un token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login exitoso",
                     content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        System.out.println("RECIBIENDO PETICIÓN LOGIN: " + request.getUsername());
        
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
            
            System.out.println("ERRORES DE VALIDACIÓN EN LOGIN: " + errorMessage);
            
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Datos de login inválidos");
            response.put("errores", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        try {
            AuthResponse response = authService.login(request);
            System.out.println("LOGIN EXITOSO: " + request.getUsername());
            return ResponseEntity.ok(response);
        } catch (AuthException e) {
            System.out.println("ERROR AUTENTICACIÓN: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            System.out.println("ERROR INESPERADO EN LOGIN: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error en el proceso de autenticación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/register-debug")
    public ResponseEntity<?> registerDebug(@RequestBody Map<String, Object> requestData) {
        System.out.println("🔧 REGISTER-DEBUG - DATOS RECIBIDOS: " + requestData);
        
        try {
            // Crear RegisterRequest manualmente
            RegisterRequest request = new RegisterRequest();
            request.setUsername((String) requestData.get("username"));
            request.setNombre((String) requestData.get("nombre"));
            request.setEmail((String) requestData.get("email"));
            // LÍNEA CORREGIDA: Cambiado 'setcontrasena()' a 'setContrasena()'
            request.setContrasena((String) requestData.get("contrasena"));
            
            // Manejar tipo de usuario
            Object tipoUsuario = requestData.get("id_tipo_usuario");
            if (tipoUsuario instanceof Integer) {
                request.setId_tipo_usuario((Integer) tipoUsuario);
            } else if (tipoUsuario instanceof String) {
                try {
                    request.setId_tipo_usuario(Integer.parseInt((String) tipoUsuario));
                } catch (NumberFormatException e) {
                    request.setId_tipo_usuario(1); // Por defecto
                }
            } else {
                request.setId_tipo_usuario(1); // Por defecto estudiante
            }
            
            System.out.println("PROCESANDO REGISTRO DEBUG...");
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            System.out.println("ERROR EN REGISTER-DEBUG: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Los demás métodos permanecen igual...
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Logout exitoso");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-contrasena")
    public ResponseEntity<?> forgotcPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                throw new ValidationException("El email es obligatorio");
            }
            
            userService.initiatePasswordReset(email);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Se ha enviado un email con instrucciones para recuperar tu contraseña");
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/reset-contrasena")
    public ResponseEntity<?> resetcontrasena(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newcontrasena = request.get("newcontrasena");
            
            if (token == null || newcontrasena == null) {
                throw new ValidationException("Token y nueva contraseña son obligatorios");
            }
            
            userService.resetPassword(token, newcontrasena);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Contraseña reseteada exitosamente");
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        System.out.println("ENDPOINT /api/auth/test ACCEDIDO CORRECTAMENTE");
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "El endpoint de autenticación está funcionando");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
