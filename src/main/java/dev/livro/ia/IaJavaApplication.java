package dev.livro.ia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan   // encontra o AnthropicProperties e preenche com o YAML
public class IaJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(IaJavaApplication.class, args);
	}

}
