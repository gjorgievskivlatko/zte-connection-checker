package com.example.connectionchecker.commands;

import com.example.connectionchecker.dto.RDDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/*
curl -s -v --header "Referer: http://192.168.1.1/index.html" \
 -d 'isTest=false&cmd=RD' http://192.168.1.1/goform/goform_get_cmd_process
 */
@Component
public class FetchRDCommand implements HttpCommand<FetchRDCommand.FetchRDCommandContext, RDDto> {

    private static final String REFERER_HEADER_FORMAT = "http://%s/index.html";
    private static final String COMMAND_URI = "http://%s/goform/goform_get_cmd_process?isTest=%s&cmd=RD&_=%s";

    private final ObjectMapper objectMapper;

    public FetchRDCommand(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RDDto execute(FetchRDCommand.FetchRDCommandContext context) throws IOException, InterruptedException {
        String domain = context.domain();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder()
                .timeout(Duration.ofSeconds(10))
                .uri(URI.create(COMMAND_URI.formatted(domain, false, System.currentTimeMillis())))
                .GET()
                .header("Referer", REFERER_HEADER_FORMAT.formatted(domain))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        RDDto rdDto = objectMapper.readValue(response.body(), RDDto.class);
        return rdDto;
    }

    public record FetchRDCommandContext(String domain) {
    }
}
