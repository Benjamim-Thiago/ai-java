package dev.livro.ia.chapter03;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import dev.livro.ia.config.AnthropicProperties;
import dev.livro.ia.common.MessageText;
import java.util.Scanner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("chat")
public class ChatRunner implements CommandLineRunner {

    private final AnthropicClient client;
    private final AnthropicProperties properties;

    public ChatRunner(AnthropicClient client, AnthropicProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        // O builder guarda a configuração fixa e, ao longo do laço, acumula o histórico
        MessageCreateParams.Builder conversation = MessageCreateParams.builder()
            .model(properties.model())
            .maxTokens(1000L)
            .system("Você é um assistente de programação Java. Seja direto e responda em português.");

        Scanner keyboard = new Scanner(System.in);
        System.out.println("Chat iniciado. Digite 'sair' para terminar.");

        while (true) {
            // 1. Lê a pergunta do usuário
            System.out.print("\nVocê: ");
            if (!keyboard.hasNextLine()) break;        // entrada encerrada (Ctrl+D / Ctrl+Z)
            String question = keyboard.nextLine().trim();
            if (question.equalsIgnoreCase("sair")) break;
            if (question.isEmpty()) continue;          // ignora linhas vazias

            // 2. Acrescenta a pergunta ao histórico
            conversation.addUserMessage(question);

            // 3. Envia o histórico inteiro e recebe a resposta
            Message response = client.messages().create(conversation.build());
            String answer = MessageText.of(response);
            System.out.println("\nClaude: " + answer);

            // 4. Acrescenta a resposta ao histórico, para a próxima volta
            conversation.addAssistantMessage(answer);

            // 5. Mostra quanto o histórico já custa por chamada
            System.out.println("(tokens de entrada nesta chamada: "
                + response.usage().inputTokens() + ")");
        }
        System.out.println("Até a próxima!");
    }
}