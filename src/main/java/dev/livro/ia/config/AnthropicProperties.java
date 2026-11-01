package dev.livro.ia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Liga o bloco "anthropic:" do application.yml a este record.
// O Spring converte os nomes: api-key -> apiKey, max-retries -> maxRetries.
@ConfigurationProperties(prefix = "anthropic")
public record AnthropicProperties(String apiKey, String model, int maxRetries) {

}
