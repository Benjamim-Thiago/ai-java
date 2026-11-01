package dev.livro.ia.chapter03;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.errors.UnauthorizedException;
import com.anthropic.models.messages.MessageCreateParams;
import dev.livro.ia.common.MessageText;
import dev.livro.ia.config.AnthropicProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("errors")   // roda só com o perfil "errors" ativo
public class SafeCallRunner implements CommandLineRunner {

    private final AnthropicClient client;
    private final AnthropicProperties properties;

    public SafeCallRunner(AnthropicClient client, AnthropicProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        MessageCreateParams params = MessageCreateParams.builder()
            .model(properties.model())
            .maxTokens(100L)
            .addUserMessage("Diga olá em uma frase.")
            .build();

        // Em vez de chamar o client direto, chama o método protegido
        System.out.println(askSafely(params));
    }

    // Faz a chamada e, se der erro, devolve uma explicação em vez de lançar exceção
    private String askSafely(MessageCreateParams params) {
        try {
            return MessageText.of(client.messages().create(params));

        } catch (UnauthorizedException e) {
            return "[Erro] Chave de API inválida. Confira o application.yml.";

        } catch (RateLimitException e) {
            return "[Erro] Muitas requisições. Espere alguns segundos e tente de novo.";

        } catch (AnthropicServiceException e) {
            // Qualquer outro erro HTTP: mostra o código para facilitar a investigação
            return "[Erro] A API respondeu com o código " + e.statusCode() + ": " + e.getMessage();

        } catch (AnthropicIoException e) {
            return "[Erro] Falha de rede: " + e.getMessage();
        }
    }
}