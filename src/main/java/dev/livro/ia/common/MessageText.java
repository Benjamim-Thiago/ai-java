package dev.livro.ia.common;

import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.TextBlock;
import java.util.stream.Collectors;

// Utilitário: junta todos os blocos de texto de uma resposta numa única String.
public final class MessageText {

    private MessageText() {} // classe utilitária: ninguém precisa criar instâncias

    public static String of(Message message) {
        return message.content().stream()
            .flatMap(block -> block.text().stream())   // mantém só os blocos de texto
            .map(TextBlock::text)                      // pega a String de cada bloco
            .collect(Collectors.joining());            // junta tudo numa String só
    }
}
