package com.example.connectionchecker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RDDto {

    @JsonProperty("RD")
    private String rd;

    public String getRD() {
        return rd;
    }

    public void setRd(String rd) {
        this.rd = rd;
    }

    @Override
    public String toString() {
        return "RDDto{" +
                "rd='" + rd + '\'' +
                '}';
    }
}
