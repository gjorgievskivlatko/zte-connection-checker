package com.example.connectionchecker.commands;

public class HttpResponseException extends RuntimeException {

    private final int status;
    private final String body;

    public HttpResponseException(int status, String body) {
        super(String.format("Unexpected response, status: %d, body: %s", status, body));
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }
}
