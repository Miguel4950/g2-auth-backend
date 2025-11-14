package co.edu.javeriana.prestamos.repository;

import co.edu.javeriana.prestamos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    // Buscar usuario por username
    Optional<Usuario> findByUsername(String username);
    
    // Buscar usuario por email
    Optional<Usuario> findByEmail(String email);
    
    // Verificar si existe un usuario con username
    boolean existsByUsername(String username);
    
    // Verificar si existe un usuario con email
    boolean existsByEmail(String email);
    
    // Buscar usuarios por tipo
    @Query("SELECT u FROM Usuario u WHERE u.id_tipo_usuario = :tipo")
    java.util.List<Usuario> findByTipoUsuario(@Param("tipo") Integer tipo);
    
    // Buscar usuarios activos
    @Query("SELECT u FROM Usuario u WHERE u.id_estado_usuario = 1")
    java.util.List<Usuario> findUsuariosActivos();
}
