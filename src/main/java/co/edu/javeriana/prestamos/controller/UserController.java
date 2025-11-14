package co.edu.javeriana.prestamos.controller;

import co.edu.javeriana.prestamos.dto.UserInfo;
import co.edu.javeriana.prestamos.dto.UserProfileUpdateRequest;
import co.edu.javeriana.prestamos.exception.ValidationException;
import co.edu.javeriana.prestamos.security.CustomUserDetails;
import co.edu.javeriana.prestamos.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "API de gestión de usuarios")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Obtener perfil del usuario autenticado",
               description = "Retorna la información del perfil del usuario actual")
    @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente",
                 content = @Content(schema = @Schema(implementation = UserInfo.class)))
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            // Obtener usuario actual desde el contexto de seguridad
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            UserInfo userInfo = userService.getUserProfile(userDetails.getUserId());
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener perfil");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Actualizar perfil del usuario",
               description = "Permite al usuario actualizar su información personal")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente",
                     content = @Content(schema = @Schema(implementation = UserInfo.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "Email ya está en uso")
    })
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            UserInfo updatedInfo = userService.updateProfile(userDetails.getUserId(), request);
            return ResponseEntity.ok(updatedInfo);
        } catch (ValidationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar perfil");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Cambiar contraseña",
               description = "Permite al usuario cambiar su contraseña")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contraseña cambiada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta o nueva contraseña inválida")
    })
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                throw new ValidationException("Se requieren la contraseña actual y la nueva");
            }
            
            userService.changePassword(userDetails.getUserId(), oldPassword, newPassword);
            
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Contraseña cambiada exitosamente");
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @Operation(summary = "Obtener usuario por ID",
               description = "Obtiene la información de un usuario específico (Solo bibliotecarios)")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                 content = @Content(schema = @Schema(implementation = UserInfo.class)))
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @Parameter(description = "ID del usuario") @PathVariable Integer id) {
        try {
            UserInfo userInfo = userService.getUserById(id);
            return ResponseEntity.ok(userInfo);
        } catch (ValidationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Listar todos los usuarios",
               description = "Obtiene lista paginada de usuarios (Solo bibliotecarios)")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "id_usuario") String sortBy) {
        try {
            Page<UserInfo> users = userService.getAllUsers(page, size, sortBy);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener usuarios");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Activar usuario",
               description = "Activa un usuario desactivado (Solo admin)")
    @ApiResponse(responseCode = "200", description = "Usuario activado exitosamente")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(
            @Parameter(description = "ID del usuario") @PathVariable Integer id) {
        try {
            userService.activateUser(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario activado exitosamente");
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @Operation(summary = "Desactivar usuario",
               description = "Desactiva un usuario activo (Solo admin)")
    @ApiResponse(responseCode = "200", description = "Usuario desactivado exitosamente")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(
            @Parameter(description = "ID del usuario") @PathVariable Integer id) {
        try {
            userService.deactivateUser(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario desactivado exitosamente");
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
