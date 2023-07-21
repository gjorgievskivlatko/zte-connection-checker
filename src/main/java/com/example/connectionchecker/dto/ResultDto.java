package com.example.connectionchecker.dto;

public class ResultDto {

    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ResultDto{" +
                "result='" + result + '\'' +
                '}';
    }
}
