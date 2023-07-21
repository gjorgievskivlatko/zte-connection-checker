package com.example.connectionchecker.dto;

public class LoginResultDto {

    private final ResultDto resultDto;
    private final String cookie;

    public LoginResultDto(ResultDto resultDto, String cookie) {
        this.resultDto = resultDto;
        this.cookie = cookie;
    }

    public ResultDto getResultDto() {
        return resultDto;
    }

    public String getCookie() {
        return cookie;
    }

    @Override
    public String toString() {
        return "LoginResultDto{" +
                "resultDto=" + resultDto +
                ", cookie='" + cookie + '\'' +
                '}';
    }
}
