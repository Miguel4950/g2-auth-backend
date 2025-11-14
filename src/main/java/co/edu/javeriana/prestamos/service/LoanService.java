package co.edu.javeriana.prestamos.service;

import co.edu.javeriana.prestamos.exception.BusinessException;
import co.edu.javeriana.prestamos.model.Libro;
import co.edu.javeriana.prestamos.model.Prestamo;
import co.edu.javeriana.prestamos.repository.LibroRepository;
import co.edu.javeriana.prestamos.repository.PrestamoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class LoanService {

    public static final int ESTADO_SOLICITADO = 1;
    public static final int ESTADO_ACTIVO = 2;
    public static final int ESTADO_DEVUELTO = 3;
    public static final int ESTADO_VENCIDO = 4;

    private static final Set<Integer> ESTADOS_ACTIVOS = Set.of(ESTADO_SOLICITADO, ESTADO_ACTIVO);
    private static final Set<Integer> ESTADOS_VISIBLES = Set.of(ESTADO_SOLICITADO, ESTADO_ACTIVO, ESTADO_DEVUELTO, ESTADO_VENCIDO);

    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    public LoanService(LibroRepository libroRepository, PrestamoRepository prestamoRepository) {
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
    }

    @Transactional
    public Prestamo solicitarPrestamo(Integer usuarioId, Integer libroId) {
        Libro libro = libroRepository.findByIdForUpdate(libroId)
                .orElseThrow(() -> new BusinessException("Libro no encontrado"));

        if (libro.getCantidad_disponible() <= 0) {
            throw new BusinessException("El libro no tiene ejemplares disponibles");
        }

        List<Prestamo> prestamosDelUsuario = prestamoRepository.findByUsuarioAndEstados(
                usuarioId, Set.of(ESTADO_SOLICITADO, ESTADO_ACTIVO, ESTADO_VENCIDO));

        boolean tieneVencidos = prestamosDelUsuario.stream()
                .anyMatch(p -> p.getId_estado_prestamo() == ESTADO_VENCIDO);
        if (tieneVencidos) {
            throw new BusinessException("Usuario con pr�stamos vencidos");
        }

        long activos = prestamosDelUsuario.stream()
                .filter(p -> ESTADOS_ACTIVOS.contains(p.getId_estado_prestamo()))
                .count();
        if (activos >= 3) {
            throw new BusinessException("L�mite de 3 pr�stamos simult�neos alcanzado");
        }

        libro.setCantidad_disponible(libro.getCantidad_disponible() - 1);
        libroRepository.save(libro);

        Prestamo nuevoPrestamo = new Prestamo();
        nuevoPrestamo.setId_usuario(usuarioId);
        nuevoPrestamo.setId_libro(libroId);
        nuevoPrestamo.setId_estado_prestamo(ESTADO_SOLICITADO);
        nuevoPrestamo.setFecha_prestamo(LocalDateTime.now());
        nuevoPrestamo.setFecha_devolucion_esperada(LocalDateTime.now().plusDays(14));

        return prestamoRepository.save(nuevoPrestamo);
    }

    public List<Prestamo> getMisPrestamos(Integer usuarioId) {
        return prestamoRepository.findByUsuarioAndEstados(usuarioId, ESTADOS_VISIBLES);
    }
}
