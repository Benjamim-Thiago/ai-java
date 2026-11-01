package dev.livro.ia.chapter03;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.helpers.MessageAccumulator;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.RawMessageStreamEvent;
import dev.livro.ia.config.AnthropicProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("review")
public class CodeReviewRunner implements ApplicationRunner {

    // As instruções do revisor. Quanto mais específicas, mais útil a revisão.
    private static final String SYSTEM_PROMPT = """
        Você é um revisor de código Java experiente e didático.
        Analise o código enviado dentro das tags <code> e procure:
        - bugs e erros de lógica;
        - exceções engolidas e recursos que não são fechados;
        - nomes pouco claros;
        - problemas de concorrência ou de desempenho.

        Para cada problema encontrado, informe:
        1. a severidade: ALTA, MÉDIA ou BAIXA;
        2. a linha aproximada;
        3. o problema, em uma frase;
        4. como corrigir.

        Se o código estiver bom, diga isso. Não invente problemas.
        Responda em português.
        """;

    private final AnthropicClient client;
    private final AnthropicProperties properties;

    public CodeReviewRunner(AnthropicClient client, AnthropicProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 1. Pega os argumentos comuns (sem as opções --spring...)
        List<String> files = args.getNonOptionArgs();
        if (files.isEmpty()) {
            System.out.println("Uso: java -jar ... --spring.profiles.active=review <arquivo.java>");
            return;
        }

        // 2. Lê o arquivo inteiro como texto
        Path file = Path.of(files.get(0));
        String code = Files.readString(file);

        // 3. Monta o pedido. O código vai entre tags <code> para separar dado de instrução.
        MessageCreateParams params = MessageCreateParams.builder()
            .model(properties.model())
            .maxTokens(2000L)
            .system(SYSTEM_PROMPT)
            .addUserMessage("Revise o arquivo " + file.getFileName() + ":\n<code>\n" + code + "\n</code>")
            .build();

        // 4. Envia em streaming e imprime a revisão conforme chega
        System.out.println("Revisando " + file + "...\n");
        MessageAccumulator accumulator = MessageAccumulator.create();
        try (StreamResponse<RawMessageStreamEvent> stream = client.messages().createStreaming(params)) {
            stream.stream()
                .peek(accumulator::accumulate)
                .flatMap(event -> event.contentBlockDelta().stream())
                .flatMap(delta -> delta.delta().text().stream())
                .forEach(piece -> System.out.print(piece.text()));
        }

        // 5. Mostra o consumo de tokens
        Message complete = accumulator.message();
        System.out.println("\n\nTokens: " + complete.usage().inputTokens() + " de entrada, "
            + complete.usage().outputTokens() + " de saída");
    }
}