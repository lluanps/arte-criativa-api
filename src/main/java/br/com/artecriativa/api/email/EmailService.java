package br.com.artecriativa.api.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Envio de e-mail via API HTTP do Resend (sem SMTP). Se {@code RESEND_API_KEY} não
 * estiver configurada (ex: ambiente local), o envio vira um no-op com log de aviso — não
 * quebra o fluxo que chamou (registro de usuário, recuperação de senha, etc). Falhas na
 * chamada HTTP também só logam erro, nunca propagam: um e-mail que não saiu não deve
 * derrubar a operação de negócio que o disparou.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestClient restClient;
    private final String remetente;
    private final boolean habilitado;

    public EmailService(@Value("${app.email.resend-api-key:}") String apiKey,
                         @Value("${app.email.remetente}") String remetente) {
        this.remetente = remetente;
        this.habilitado = apiKey != null && !apiKey.isBlank();
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public void enviar(String destinatario, String assunto, String htmlCorpo) {
        if (!habilitado) {
            log.warn("RESEND_API_KEY não configurada — e-mail '{}' para {} não foi enviado.", assunto, destinatario);
            return;
        }
        try {
            restClient.post()
                    .uri("/emails")
                    .body(new EnvioResend(remetente, List.of(destinatario), assunto, htmlCorpo))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Falha ao enviar e-mail '{}' para {}: {}", assunto, destinatario, e.getMessage());
        }
    }

    private record EnvioResend(String from, List<String> to, String subject, String html) {
    }
}
