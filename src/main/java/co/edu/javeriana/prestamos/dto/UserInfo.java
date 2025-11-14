package co.edu.javeriana.prestamos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private Integer id_usuario;
    private String nombre;
    private String username;
    private String email;
    private Integer id_tipo_usuario;
    private String tipo_usuario; // "Estudiante", "Bibliotecario", "Admin"
}
