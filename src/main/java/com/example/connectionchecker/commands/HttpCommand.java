package com.example.connectionchecker.commands;

import com.example.connectionchecker.config.SettingsConstants;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public abstract class HttpCommand<C, R> {

    abstract R execute(C context) throws Exception;

    HttpRequest.Builder httpRequestBuilder() {
        return HttpRequest
            .newBuilder()
            .timeout(Duration.ofSeconds(SettingsConstants.CONNECTION_TIMEOUT));
    }

    void checkStatusCode(HttpResponse<String> response) {
        if (response.statusCode() / 100 != 2) {
            throw new HttpResponseException(response.statusCode(), response.body());
        }
    }
}
