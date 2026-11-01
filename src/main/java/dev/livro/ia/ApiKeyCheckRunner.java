package dev.livro.ia;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.TextBlock;

import dev.livro.ia.config.AnthropicProperties;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// @Component: o Spring cria esta classe sozinho ao iniciar.
// CommandLineRunner: o Spring chama o método run() logo depois de subir a aplicação.
@Component
@Profile("check")
public class ApiKeyCheckRunner implements CommandLineRunner {

    private final AnthropicClient client;          // o cliente da API, criado no ClaudeConfig
    private final AnthropicProperties properties;  // os valores do application.yml

    // O Spring entrega o cliente e as propriedades pelo construtor (injeção de dependências)
    public ApiKeyCheckRunner(AnthropicClient client, AnthropicProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        // Mostra só o começo da chave, nunca ela inteira
        System.out.println("Chave encontrada: " + properties.apiKey().substring(0, 10) + "...");
        System.out.println("Modelo: " + properties.model());

        // 1. Monta o pedido (padrão builder): modelo, limite da resposta e a pergunta
        MessageCreateParams params = MessageCreateParams.builder()
            .model(properties.model())        // qual modelo vai responder (vem do YAML)
            .maxTokens(100L)                  // tamanho máximo da resposta, em tokens
            .addUserMessage("Responda apenas: o ambiente Java está funcionando!") // a pergunta
            .build();                         // finaliza e valida o pedido

        // 2. Envia o pedido para a API e espera a resposta (esta linha bloqueia até chegar)
        Message response = client.messages().create(params);

        // 3. A resposta é uma lista de blocos (texto, uso de ferramenta etc.).
        //    Filtra só os blocos de texto e imprime cada um.
        response.content().stream()
            .flatMap(block -> block.text().stream())  // mantém só os blocos de texto
            .map(TextBlock::text)                     // de cada bloco, pega a String
            .forEach(System.out::println);            // imprime

        // 4. Quantos tokens a chamada gastou (é isso que você paga)
        System.out.println("Tokens usados: " + response.usage().inputTokens()
            + " de entrada, " + response.usage().outputTokens() + " de saída");
    }
}