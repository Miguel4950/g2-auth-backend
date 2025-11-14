package co.edu.javeriana.prestamos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Integer id_usuario;
    private String token;
    private String mensaje;
    private UserInfo usuario_info;
    private String[] permisos;
}
