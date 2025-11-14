package co.edu.javeriana.prestamos.repository;

import co.edu.javeriana.prestamos.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Libro l WHERE l.id_libro = :id")
    Optional<Libro> findByIdForUpdate(@Param("id") Integer id);
}
