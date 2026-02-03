package com.tinnova.veiculos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("cambioWebClient")
    public WebClient cambioWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://economia.awesomeapi.com.br")
                .build();
    }
}
