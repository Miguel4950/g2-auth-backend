package co.edu.javeriana.prestamos.service;

import co.edu.javeriana.prestamos.model.PasswordResetToken;
import co.edu.javeriana.prestamos.model.RefreshToken;
import co.edu.javeriana.prestamos.model.Usuario;
import co.edu.javeriana.prestamos.repository.PasswordResetTokenRepository;
import co.edu.javeriana.prestamos.repository.RefreshTokenRepository;
import co.edu.javeriana.prestamos.repository.UsuarioRepository;
import co.edu.javeriana.prestamos.security.CustomUserDetails;
import co.edu.javeriana.prestamos.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class TokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    public RefreshToken createRefreshToken(Usuario usuario) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUsuario(usuario);
        rt.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        rt.setRevoked(false);
        return refreshTokenRepository.save(rt);
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token).orElse(null);
    }

    public void revokeRefreshToken(RefreshToken rt) {
        if (rt == null) return;
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);
    }

    public PasswordResetToken createPasswordResetToken(Usuario usuario) {
        PasswordResetToken pr = new PasswordResetToken();
        pr.setToken(UUID.randomUUID().toString());
        pr.setUsuario(usuario);
        pr.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        pr.setUsed(false);
        return passwordResetTokenRepository.save(pr);
    }

    public PasswordResetToken findPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token).orElse(null);
    }

    public void markPasswordResetUsed(PasswordResetToken pr) {
        pr.setUsed(true);
        passwordResetTokenRepository.save(pr);
    }

    public String generateAccessTokenForUser(Usuario usuario) {
        CustomUserDetails cud = new CustomUserDetails(usuario);
        return jwtService.generateToken(cud);
    }
}
