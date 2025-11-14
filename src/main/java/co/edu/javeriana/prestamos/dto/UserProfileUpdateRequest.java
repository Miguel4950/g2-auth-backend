package co.edu.javeriana.prestamos.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {
    
    private String nombre;
    
    @Email(message = "El email debe ser válido")
    private String email;
}
