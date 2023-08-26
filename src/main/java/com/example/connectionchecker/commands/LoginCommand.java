package com.example.connectionchecker.commands;

import com.example.connectionchecker.dto.LoginResultDto;
import com.example.connectionchecker.dto.ResultDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/*
curl -s -v --header "Referer: http://192.168.1.1/index.html" \
 -d 'isTest=false&goformId=LOGIN&password=YjQyd2twVlo=' \
 http://192.168.1.1/goform/goform_set_cmd_process
 */
@Component
public class LoginCommand implements HttpCommand<LoginCommand.LoginCommandContext, LoginResultDto> {

    private static final String REFERER_HEADER_FORMAT = "http://%s/index.html";
    private static final String COMMAND_URI = "http://%s/goform/goform_set_cmd_process";

    private final HttpClient httpClient;
    private final HttpRequest.Builder httpRequestBuilder;
    private final ObjectMapper objectMapper;

    public LoginCommand(HttpClient httpClient, HttpRequest.Builder httpRequestBuilder, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.httpRequestBuilder = httpRequestBuilder;
        this.objectMapper = objectMapper;
    }

    public LoginResultDto execute(LoginCommandContext context) throws IOException, InterruptedException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("isTest", "false");
        parameters.put("goformId", "LOGIN");
        parameters.put("password", Base64.getEncoder().encodeToString(context.password().getBytes(StandardCharsets.UTF_8)));

        String form = parameters.entrySet()
            .stream()
            .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));

        HttpRequest request = httpRequestBuilder
            .uri(URI.create(String.format(COMMAND_URI, context.domain())))
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .header("Referer", String.format(REFERER_HEADER_FORMAT, context.domain()))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        checkStatusCode(response);

        ResultDto resultDto = objectMapper.readValue(response.body(), ResultDto.class);

        if (!resultDto.getResult().equals("0")) {
            throw new RuntimeException("Wrong result during login, resultDto:" + resultDto);
        }

        String cookie = response.headers().firstValue("set-cookie").orElse("");

        return new LoginResultDto(resultDto, cookie);
    }

    public static final class LoginCommandContext {

        private final String domain;
        private final String password;

        public LoginCommandContext(String domain, String password) {
            this.domain = domain;
            this.password = password;
        }

        public String domain() {
            return domain;
        }

        public String password() {
            return password;
        }

    }
}
