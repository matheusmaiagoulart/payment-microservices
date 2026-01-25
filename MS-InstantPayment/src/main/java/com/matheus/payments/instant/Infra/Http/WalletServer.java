package com.matheus.payments.instant.Infra.Http;


import com.matheus.payments.instant.Application.Audit.CorrelationId;
import org.springframework.core.io.ClassPathResource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Class responsible for making HTTPS request to Wallet Service for instant payment processing.
 *
 * @author Matheus Maia Goularet
 */

@Service
public class WalletServer {

    private final HttpClient responseClient;

    public WalletServer() throws Exception {
        // Carrega o truststore
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (InputStream trustStream = new ClassPathResource("Certifications/truststore.jks").getInputStream()) {
            trustStore.load(trustStream, "changeit".toCharArray());
        }

        // Cria o SSLContext que confia nesse truststore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        // Cria o HttpClient com SSL customizado
        this.responseClient = HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(5)) // Setting a connection timeout
                .build();
    }

    @Retryable(retryFor = {IOException.class, InterruptedException.class}, maxAttempts = 4, backoff = @Backoff (delay = 1000, multiplier = 1.5))
    public HttpResponse<String> instantPaymentRequest(String jsonPayload) throws IOException, InterruptedException, TimeoutException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:8081/wallets/instant-payment"))
                .header("Content-Type", "application/json")
                .header("X-Correlation-Id", CorrelationId.get())
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(30)) // Setting a request timeout
                .build();

        return responseClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
