package com.example.connectionchecker.commands;

import java.net.http.HttpResponse;

public interface HttpCommand<C, R> {

    R execute(C context) throws Exception;

    default void checkStatusCode(HttpResponse<String> response) {
        if (response.statusCode() / 100 != 2) {
            throw new HttpResponseException(response.statusCode(), response.body());
        }
    }
}
