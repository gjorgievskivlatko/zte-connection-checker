package com.example.connectionchecker.commands;

import com.example.connectionchecker.dto.RDDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class FetchRDCommand implements HttpCommand<FetchRDCommand.FetchRDCommandContext, RDDto> {

    private static final String REFERER_HEADER_FORMAT = "http://%s/index.html";
    private static final String COMMAND_URI = "http://%s/goform/goform_get_cmd_process?isTest=%s&cmd=RD&_=%s";

    private final HttpClient httpClient;
    private final HttpRequest.Builder httpRequestBuilder;
    private final ObjectMapper objectMapper;

    public FetchRDCommand(HttpClient httpClient, HttpRequest.Builder httpRequestBuilder, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.httpRequestBuilder = httpRequestBuilder;
        this.objectMapper = objectMapper;
    }

    public RDDto execute(FetchRDCommand.FetchRDCommandContext context) throws IOException, InterruptedException {
        String domain = context.domain();
        HttpRequest request = httpRequestBuilder
                .uri(URI.create(String.format(COMMAND_URI, domain, false, System.currentTimeMillis())))
                .GET()
                .header("Referer", String.format(REFERER_HEADER_FORMAT, domain))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        RDDto rdDto = objectMapper.readValue(response.body(), RDDto.class);
        return rdDto;
    }

    public static final class FetchRDCommandContext {

        private final String domain;

        public FetchRDCommandContext(String domain) {
            this.domain = domain;
        }

        public String domain() {
            return domain;
        }

    }
}
