package com.tinnova.veiculos.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        userDetails = User.builder()
                .username("admin")
                .password("123456")
                .roles("ADMIN")
                .build();
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido")
    void shouldGenerateValidToken() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("Deve extrair o username corretamente do token")
    void shouldExtractUsernameFromToken() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("admin", username);
    }

    @Test
    @DisplayName("Deve validar um token válido")
    void shouldValidateValidToken() {
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Deve retornar false para token inválido")
    void shouldReturnFalseForInvalidToken() {
        String invalidToken = "token.invalido.aqui";

        boolean isValid = jwtService.isTokenValid(invalidToken);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar extrair username de token inválido")
    void shouldThrowExceptionWhenExtractingFromInvalidToken() {
        String invalidToken = "token.invalido";

        assertThrows(JwtException.class, () ->
                jwtService.extractUsername(invalidToken)
        );
    }
}
