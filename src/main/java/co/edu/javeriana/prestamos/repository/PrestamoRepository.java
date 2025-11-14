package co.edu.javeriana.prestamos.repository;

import co.edu.javeriana.prestamos.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PrestamoRepository extends JpaRepository<Prestamo, Integer> {

    @Query("SELECT p FROM Prestamo p WHERE p.id_usuario = :userId AND p.id_estado_prestamo IN :estados")
    List<Prestamo> findByUsuarioAndEstados(@Param("userId") Integer userId,
                                           @Param("estados") Set<Integer> estados);

}
