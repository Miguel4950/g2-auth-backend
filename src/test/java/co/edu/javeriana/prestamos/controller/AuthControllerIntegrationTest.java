package co.edu.javeriana.prestamos.controller;

import co.edu.javeriana.prestamos.dto.LoginRequest;
import co.edu.javeriana.prestamos.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterEndpoint() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setNombre("Test Integration");
        request.setUsername("testintegration");
        request.setEmail("integration@test.com");
        request.setContrasena("contrasena123");
        request.setId_tipo_usuario(1);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.mensaje").value("Registro exitoso"));
    }

    @Test
    void testLoginEndpoint() throws Exception {
        // Primero registramos un usuario
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNombre("Test Login");
        registerRequest.setUsername("testlogin");
        registerRequest.setEmail("login@test.com");
        registerRequest.setContrasena("contrasena123");
        registerRequest.setId_tipo_usuario(1);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Luego hacemos login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testlogin");
        loginRequest.setContrasena("contrasena123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.usuario_info.username").value("testlogin"));
    }

    @Test
    void testInvalidLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent");
        loginRequest.setContrasena("wrongcontrasena");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }
}
