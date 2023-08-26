package com.example.connectionchecker.commands;

import com.example.connectionchecker.dto.FirmwareVersionResultDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/*
curl -s -v --header "Referer: http://192.168.1.1/index.html" \
 -d 'isTest=false&cmd=Language%2Ccr_version%2Cwa_inner_version&multi_data=1' \
 http://192.168.1.1/goform/goform_get_cmd_process
 */
@Component
public class FirmwareVersionCommand implements HttpCommand<FirmwareVersionCommand.FirmwareVersionCommandContext, FirmwareVersionResultDto> {

    private static final String REFERER_HEADER_FORMAT = "http://%s/index.html";
    private static final String COMMAND_URI = "http://%s/goform/goform_get_cmd_process?isTest=false&cmd=Language%%2Ccr_version%%2Cwa_inner_version&multi_data=1&_=%s";

    private final HttpClient httpClient;
    private final HttpRequest.Builder httpRequestBuilder;
    private final ObjectMapper objectMapper;

    public FirmwareVersionCommand(HttpClient httpClient, HttpRequest.Builder httpRequestBuilder, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.httpRequestBuilder = httpRequestBuilder;
        this.objectMapper = objectMapper;
    }

    public FirmwareVersionResultDto execute(FirmwareVersionCommand.FirmwareVersionCommandContext context) throws IOException, InterruptedException {
        String domain = context.domain();
        HttpRequest request = httpRequestBuilder
                .uri(URI.create(String.format(COMMAND_URI, domain, System.currentTimeMillis())))
                .GET()
                .header("Referer", String.format(REFERER_HEADER_FORMAT, domain))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        checkStatusCode(response);

        FirmwareVersionResultDto firmwareVersionResultDto = objectMapper.readValue(response.body(), FirmwareVersionResultDto.class);
        return firmwareVersionResultDto;
    }

    public static final class FirmwareVersionCommandContext {

        private final String domain;

        public FirmwareVersionCommandContext(String domain) {
            this.domain = domain;
        }

        public String domain() {
            return domain;
        }

    }
}
