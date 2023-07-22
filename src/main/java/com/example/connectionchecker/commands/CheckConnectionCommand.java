package com.example.connectionchecker.commands;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/*
curl -s -v --header "Referer: http://192.168.1.1/index.html" \
 -d 'isTest=false&cmd=RD' http://192.168.1.1/goform/goform_get_cmd_process
 */
@Component
public class CheckConnectionCommand implements HttpCommand<Void, Void> {

    private static final String PING_URI = "https://google.com";

    private final HttpClient httpClient;
    private final HttpRequest.Builder httpRequestBuilder;

    public CheckConnectionCommand(HttpClient httpClient, HttpRequest.Builder httpRequestBuilder) {
        this.httpClient = httpClient;
        this.httpRequestBuilder = httpRequestBuilder;
    }

    public Void execute(Void context) throws IOException, InterruptedException {
        HttpRequest request = httpRequestBuilder
                .uri(URI.create(PING_URI))
                .GET()
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return null;
    }
}
