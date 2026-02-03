package com.tinnova.veiculos.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CambioServiceTest {

    // =========================
    // API PRINCIPAL
    // =========================
    @Test
    void shouldReturnDollarRateFromMainApi() {
        // Arrange
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);

        String jsonResponse = """
            [{
              "bid": "5.25"
            }]
            """;

        ClientResponse clientResponse =
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(jsonResponse)
                        .build();

        when(exchangeFunction.exchange(any()))
                .thenReturn(Mono.just(clientResponse));

        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        CambioService service = new CambioService(webClient);

        // Act
        BigDecimal result = service.getCotacaoDolarBRL();

        // Assert
        assertEquals(new BigDecimal("5.25"), result);
    }

    // =========================
    // FALLBACK
    // =========================
    @Test
    void shouldUseFallbackWhenMainApiFails() {
        // Arrange
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);

        when(exchangeFunction.exchange(any()))
                .thenReturn(Mono.error(new RuntimeException("API down")));

        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        CambioService service = new CambioService(webClient){
                    @Override
                    public BigDecimal getCotacaoDolarBRLFallback() {
                        return new BigDecimal("5.50");
                    }
                };

        // Act
        BigDecimal result = service.getCotacaoDolarBRL();

        // Assert
        assertEquals(new BigDecimal("5.50"), result);
    }
}
