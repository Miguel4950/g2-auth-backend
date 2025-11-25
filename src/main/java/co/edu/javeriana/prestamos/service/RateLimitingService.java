package co.edu.javeriana.prestamos.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitingService {
    // Mapa: IP -> Número de intentos fallidos
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    // Mapa: IP -> Tiempo de desbloqueo (timestamp)
    private final Map<String, Long> blockCache = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 5; // Bloqueo al 5to intento fallido
    private static final long BLOCK_DURATION = TimeUnit.MINUTES.toMillis(15); // 15 minutos de castigo

    public boolean isBlocked(String ip) {
        if (blockCache.containsKey(ip)) {
            // Verificar si ya pasó el tiempo de castigo
            if (System.currentTimeMillis() > blockCache.get(ip)) {
                blockCache.remove(ip); // Desbloquear
                attemptsCache.remove(ip);
                return false;
            }
            return true; // Sigue bloqueado
        }
        return false;
    }

    public void loginFailed(String ip) {
        int attempts = attemptsCache.getOrDefault(ip, 0);
        attempts++;
        attemptsCache.put(ip, attempts);
        
        if (attempts >= MAX_ATTEMPTS) {
            blockCache.put(ip, System.currentTimeMillis() + BLOCK_DURATION);
        }
    }

    public void loginSucceeded(String ip) {
        attemptsCache.remove(ip);
        blockCache.remove(ip);
    }
}