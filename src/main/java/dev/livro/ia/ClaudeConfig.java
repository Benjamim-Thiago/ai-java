package dev.livro.ia;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClaudeConfig {

    @Bean
    AnthropicClient anthropicClient(AnthropicProperties props) {
        String apiKey  = props.apiKey();

        // Falha cedo, com uma mensagem clara, se a chave não foi configurada
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("COLE-SUA-CHAVE-AQUI")) {
            throw new IllegalStateException(
                "Chave da Anthropic não configurada. Copie application-sample.yml para "
                + "application.yml e preencha anthropic.api-key (seção 2.8).");
        }

        return AnthropicOkHttpClient.builder()
            .apiKey(apiKey)
            .maxRetries(props.maxRetries())
            .build();
    }
}