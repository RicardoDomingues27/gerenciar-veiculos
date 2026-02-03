package com.tinnova.veiculos.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // =========================
    // VALIDATION EXCEPTION
    // =========================
    @Test
    @DisplayName("Deve retornar 409 para ValidationException")
    void shouldReturn409ForValidationException() throws Exception {
        mockMvc.perform(get("/test/validation"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Erro de validação"));
    }

    // =========================
    // NOT FOUND EXCEPTION
    // =========================
    @Test
    @DisplayName("Deve retornar 404 para ResourceNotFoundException")
    void shouldReturn404ForResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"));
    }

    // =========================
    // CONTROLLER DE TESTE REAL
    // =========================
    @RestController
    static class TestController {

        @GetMapping("/test/validation")
        public void validation() {
            throw new ValidationException("Erro de validação");
        }

        @GetMapping("/test/not-found")
        public void notFound() {
            throw new ResourceNotFoundException("Recurso não encontrado");
        }
    }
}
