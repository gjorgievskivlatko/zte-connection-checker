package com.example.connectionchecker.commands;

import com.example.connectionchecker.dto.FirmwareVersionResultDto;
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
 -d 'isTest=false&cmd=Language%2Ccr_version%2Cwa_inner_version&multi_data=1' \
 http://192.168.1.1/goform/goform_get_cmd_process
 */
@Component
public class FirmwareVersionCommand implements HttpCommand<FirmwareVersionCommand.FirmwareVersionCommandContext, FirmwareVersionResultDto> {

    private static final String REFERER_HEADER_FORMAT = "http://%s/index.html";
    private static final String COMMAND_URI = "http://%s/goform/goform_get_cmd_process?isTest=false&cmd=Language%%2Ccr_version%%2Cwa_inner_version&multi_data=1&_=%s";

    private final ObjectMapper objectMapper;

    public FirmwareVersionCommand(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public FirmwareVersionResultDto execute(FirmwareVersionCommand.FirmwareVersionCommandContext context) throws IOException, InterruptedException {
        String domain = context.domain();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder()
                .timeout(Duration.ofSeconds(10))
                .uri(URI.create(COMMAND_URI.formatted(domain, System.currentTimeMillis())))
                .GET()
                .header("Referer", REFERER_HEADER_FORMAT.formatted(domain))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        FirmwareVersionResultDto firmwareVersionResultDto = objectMapper.readValue(response.body(), FirmwareVersionResultDto.class);
        return firmwareVersionResultDto;
    }

    public record FirmwareVersionCommandContext(String domain) {
    }
}
