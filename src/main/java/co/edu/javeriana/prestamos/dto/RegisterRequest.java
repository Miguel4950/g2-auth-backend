package co.edu.javeriana.prestamos.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*$",
        message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial"
    )
    private String contrasena; 
    
    @NotNull(message = "El tipo de usuario es obligatorio")
    @Min(value = 1, message = "El tipo de usuario debe ser 1 (Estudiante), 2 (Bibliotecario) o 3 (Admin)")
    @Max(value = 3, message = "El tipo de usuario debe ser 1 (Estudiante), 2 (Bibliotecario) o 3 (Admin)")
    private Integer id_tipo_usuario; 
}
