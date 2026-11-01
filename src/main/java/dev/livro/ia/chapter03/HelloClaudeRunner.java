package dev.livro.ia.chapter03;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import dev.livro.ia.config.AnthropicProperties;
import dev.livro.ia.common.MessageText;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("hello")   // roda só com o perfil "hello" ativo
public class HelloClaudeRunner implements CommandLineRunner {

    private final AnthropicClient client;
    private final AnthropicProperties properties;

    public HelloClaudeRunner(AnthropicClient client, AnthropicProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        // 1. Monta o pedido, agora com um system prompt
        MessageCreateParams params = MessageCreateParams.builder()
            .model(properties.model())
            .maxTokens(500L)
            .system("""
                Você é um professor de Java paciente e didático.
                Responda em português, em no máximo 3 frases,
                e termine com um exemplo de código de uma linha.""")
            .addUserMessage("O que é um record em Java?")
            .build();

        // 2. Envia e espera a resposta
        Message response = client.messages().create(params);

        // 3. Imprime o texto e as informações da resposta
        System.out.println(MessageText.of(response));
        System.out.println();
        System.out.println("Motivo da parada: " + response.stopReason().orElse(null));
        System.out.println("Tokens de entrada: " + response.usage().inputTokens());
        System.out.println("Tokens de saída:   " + response.usage().outputTokens());
    }
}
