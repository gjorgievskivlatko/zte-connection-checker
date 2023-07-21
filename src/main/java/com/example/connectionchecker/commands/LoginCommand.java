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
import java.time.Duration;
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

    private final ObjectMapper objectMapper;

    public LoginCommand(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public LoginResultDto execute(LoginCommandContext context) throws IOException, InterruptedException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("isTest", "false");
        parameters.put("goformId", "LOGIN");
        parameters.put("password", context.password());

        String form = parameters.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder()
                .timeout(Duration.ofSeconds(10))
                .uri(URI.create(COMMAND_URI.formatted(context.domain())))
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .header("Referer", REFERER_HEADER_FORMAT.formatted(context.domain()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ResultDto resultDto = objectMapper.readValue(response.body(), ResultDto.class);

        if (!resultDto.getResult().equals("0")) {
            throw new RuntimeException("Wrong result during login, resultDto:" + resultDto);
        }

        String cookie = response.headers().firstValue("set-cookie").orElse("");

        return new LoginResultDto(resultDto, cookie);
    }

    public record LoginCommandContext(String domain, String password) {
    }
}
