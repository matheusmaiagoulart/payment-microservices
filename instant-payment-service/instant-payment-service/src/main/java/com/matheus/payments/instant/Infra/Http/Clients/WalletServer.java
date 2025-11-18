package com.matheus.payments.instant.Infra.Http.Clients;


import com.matheus.payments.instant.Application.DTOs.Response.PaymentProcessorResponse;
import com.matheus.payments.instant.Infra.Exceptions.Custom.FailedToSentException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class WalletServer {


    public HttpResponse<String> instantPaymentRequest(String jsonPayload) throws IOException, InterruptedException {


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/wallet/instant-payment"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpClient responseClient = HttpClient.newHttpClient();
        return responseClient.send(request, HttpResponse.BodyHandlers.ofString());

    }
}
