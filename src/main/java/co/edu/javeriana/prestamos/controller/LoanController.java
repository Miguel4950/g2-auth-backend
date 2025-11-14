package co.edu.javeriana.prestamos.controller;

import co.edu.javeriana.prestamos.exception.BusinessException;
import co.edu.javeriana.prestamos.model.Prestamo;
import co.edu.javeriana.prestamos.security.CustomUserDetails;
import co.edu.javeriana.prestamos.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/loans")
    public ResponseEntity<?> solicitarPrestamo(@AuthenticationPrincipal CustomUserDetails user,
                                               @RequestBody LoanRequest request) {
        try {
            Prestamo nuevoPrestamo = loanService.solicitarPrestamo(user.getUserId(), request.getLibro_id());
            return ResponseEntity.ok(new LoanResponse(nuevoPrestamo));
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(be.getMessage());
        }
    }

    @GetMapping("/loans/my-loans")
    public ResponseEntity<?> getMisPrestamos(@AuthenticationPrincipal CustomUserDetails user) {
        List<Prestamo> prestamos = loanService.getMisPrestamos(user.getUserId());
        return ResponseEntity.ok(new MyLoansResponse(prestamos));
    }
}
