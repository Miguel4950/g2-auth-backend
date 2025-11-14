package co.edu.javeriana.prestamos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_usuario;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(unique = true)
    private String email;
    
    @Column(name = "contrasena", nullable = false)
    private String contrasena;
    
    @Column(nullable = false)
    private Integer id_tipo_usuario;
    
    private Integer id_estado_usuario;
    private Integer intentos_fallidos;

    @Column(name = "requiere_cambio_pass", nullable = false)
    private Boolean requiereCambioPass = false; 

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha_registro")
    private Date fecha_registro;

    // Constructor para pruebas
    public Usuario(Integer id, String username, String nombre, int tipo) {
        this.id_usuario = id;
        this.username = username;
        this.nombre = nombre;
        this.id_tipo_usuario = tipo;
        this.id_estado_usuario = 1;
        this.intentos_fallidos = 0;
        this.fecha_registro = new Date();
        // Inicializar el campo faltante en el constructor también.
        this.requiereCambioPass = false; 
    }
    
    @PrePersist
    protected void onCreate() {
        if (fecha_registro == null) {
            fecha_registro = new Date();
        }
        if (id_estado_usuario == null) {
            id_estado_usuario = 1; // Activo por defecto
        }
        if (intentos_fallidos == null) {
            intentos_fallidos = 0;
        }
    }
}
