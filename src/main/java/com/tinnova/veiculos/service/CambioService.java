package com.tinnova.veiculos.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Service
public class CambioService {

    private final WebClient webClient;

    public CambioService(WebClient.Builder webClientBuilder) {
        // Usa WebClient para chamadas reativas e modernas
        this.webClient = webClientBuilder.baseUrl("https://economia.awesomeapi.com.br").build();
    }

    @Cacheable(value = "cotacaoDolar", key = "'latest'", unless = "#result == null") // Cache por 10 minutos
    public BigDecimal getCotacaoDolarBRL() {
        try {
            // API Principal
            Mono<Map[]> response = webClient.get()
                    .uri("/json/last/USD-BRL")
                    .retrieve()
                    .bodyToMono(Map[].class);

            Map<String, Object> usdBrL = (Map<String, Object>) Objects.requireNonNull(response.block())[0];
            return new BigDecimal((String) usdBrL.get("bid"));

        } catch (Exception e) {
            // Fallback API
            return getCotacaoDolarBRLFallback();
        }
    }

    // Método de fallback sem cache para evitar loop infinito
    public BigDecimal getCotacaoDolarBRLFallback() {
        WebClient fallbackWebClient = WebClient.builder().baseUrl("https://api.frankfurter.app").build();
        Mono<Map> response = fallbackWebClient.get()
                .uri("/latest?from=USD&to=BRL")
                .retrieve()
                .bodyToMono(Map.class);
        Map<String, Object> rates = (Map<String, Object>) response.block().get("rates");
        return new BigDecimal(rates.get("BRL").toString());
    }
}
