package dev.livro.ia.chapter03;

import com.anthropic.client.AnthropicClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.helpers.MessageAccumulator;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.RawMessageStreamEvent;
import dev.livro.ia.config.AnthropicProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("stream")
public class StreamingRunner implements CommandLineRunner {

    private final AnthropicClient client;
    private final AnthropicProperties properties;

    public StreamingRunner(AnthropicClient client, AnthropicProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        MessageCreateParams params = MessageCreateParams.builder()
            .model(properties.model())
            .maxTokens(1500L)
            .addUserMessage("Explique em 4 parágrafos como funciona o garbage collector do Java.")
            .build();

        // O acumulador vai remontando a mensagem completa a partir dos eventos
        MessageAccumulator accumulator = MessageAccumulator.create();

        // createStreaming abre uma conexão que entrega os eventos aos poucos.
        // O try-with-resources garante que a conexão será fechada no final.
        try (StreamResponse<RawMessageStreamEvent> stream =
                 client.messages().createStreaming(params)) {

            stream.stream()
                .peek(accumulator::accumulate)                      // 1. entrega cada evento ao acumulador
                .flatMap(event -> event.contentBlockDelta().stream()) // 2. fica só com os eventos de texto novo
                .flatMap(delta -> delta.delta().text().stream())      // 3. pega o pedaço de texto do evento
                .forEach(piece -> System.out.print(piece.text()));    // 4. imprime sem pular linha
        }

        // Terminado o stream, o acumulador tem a mensagem completa
        Message complete = accumulator.message();
        System.out.println("\n\nTokens de saída: " + complete.usage().outputTokens());
    }
}