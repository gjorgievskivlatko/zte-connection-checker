package com.example.connectionchecker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FirmwareVersionResultDto {

    @JsonProperty("Language")
    private String language;

    @JsonProperty("cr_version")
    private String crVersion;

    @JsonProperty("wa_inner_version")
    private String firmwareVersion;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCrVersion() {
        return crVersion;
    }

    public void setCrVersion(String crVersion) {
        this.crVersion = crVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    @Override
    public String toString() {
        return "FirmwareVersionResultDto{" +
                "language='" + language + '\'' +
                ", crVersion='" + crVersion + '\'' +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                '}';
    }
}
