package co.edu.javeriana.prestamos.service;

import co.edu.javeriana.prestamos.exception.BusinessException;
import co.edu.javeriana.prestamos.model.Libro;
import co.edu.javeriana.prestamos.model.Prestamo;
import co.edu.javeriana.prestamos.repository.LibroRepository;
import co.edu.javeriana.prestamos.repository.PrestamoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private LoanService loanService;

    private Libro libroDisponible;

    @BeforeEach
    void setUp() {
        libroDisponible = new Libro();
        libroDisponible.setId_libro(1);
        libroDisponible.setCantidad_total(5);
        libroDisponible.setCantidad_disponible(3);
    }

    @Test
    void solicitarPrestamo_restaDisponibilidad_y_creaPrestamo() {
        when(libroRepository.findByIdForUpdate(1)).thenReturn(Optional.of(libroDisponible));
        when(prestamoRepository.findByUsuarioAndEstados(eq(100), any())).thenReturn(Collections.emptyList());
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        Prestamo prestamo = loanService.solicitarPrestamo(100, 1);

        assertThat(prestamo.getId_usuario()).isEqualTo(100);
        assertThat(prestamo.getId_libro()).isEqualTo(1);
        assertThat(prestamo.getId_estado_prestamo()).isEqualTo(LoanService.ESTADO_SOLICITADO);

        ArgumentCaptor<Libro> libroCaptor = ArgumentCaptor.forClass(Libro.class);
        verify(libroRepository).save(libroCaptor.capture());
        assertThat(libroCaptor.getValue().getCantidad_disponible()).isEqualTo(2);
    }

    @Test
    void solicitarPrestamo_conVencidos_lanzaBusinessException() {
        Prestamo vencido = new Prestamo();
        vencido.setId_estado_prestamo(LoanService.ESTADO_VENCIDO);

        when(libroRepository.findByIdForUpdate(1)).thenReturn(Optional.of(libroDisponible));
        when(prestamoRepository.findByUsuarioAndEstados(eq(100), any()))
                .thenReturn(List.of(vencido));

        assertThatThrownBy(() -> loanService.solicitarPrestamo(100, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vencidos");

        verify(prestamoRepository, never()).save(any());
        verify(libroRepository, never()).save(any());
    }

    @Test
    void solicitarPrestamo_conMasDeTresActivos_lanzaBusinessException() {
        when(libroRepository.findByIdForUpdate(1)).thenReturn(Optional.of(libroDisponible));
        Prestamo activo = new Prestamo();
        activo.setId_estado_prestamo(LoanService.ESTADO_ACTIVO);
        when(prestamoRepository.findByUsuarioAndEstados(eq(55), any()))
                .thenReturn(List.of(activo, activo, activo));

        assertThatThrownBy(() -> loanService.solicitarPrestamo(55, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("3 préstamos");

        verify(prestamoRepository, never()).save(any());
    }

    @Test
    void solicitarPrestamo_sinDisponibilidad_lanzaBusinessException() {
        libroDisponible.setCantidad_disponible(0);
        when(libroRepository.findByIdForUpdate(1)).thenReturn(Optional.of(libroDisponible));

        assertThatThrownBy(() -> loanService.solicitarPrestamo(10, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no tiene ejemplares");
    }
}
