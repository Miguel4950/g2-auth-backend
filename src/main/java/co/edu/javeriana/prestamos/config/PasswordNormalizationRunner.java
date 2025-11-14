package co.edu.javeriana.prestamos.config;

import co.edu.javeriana.prestamos.model.Usuario;
import co.edu.javeriana.prestamos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PasswordNormalizationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordNormalizationRunner.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getContrasena() != null && !usuario.getContrasena().startsWith("$2"))
                .forEach(usuario -> {
                    String raw = usuario.getContrasena();
                    usuario.setContrasena(passwordEncoder.encode(raw));
                    usuarioRepository.save(usuario);
                    log.info("Normalizada contrase�a para usuario {}", usuario.getUsername());
                });
    }
}
