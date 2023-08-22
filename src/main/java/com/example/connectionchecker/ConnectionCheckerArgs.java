package com.example.connectionchecker;

import com.beust.jcommander.Parameter;
import org.springframework.beans.factory.annotation.Value;

public class ConnectionCheckerArgs {

    @Value("${interval}")
    private int intervalProp;

    @Value("${domain}")
    private String domainProp;

    @Value("${password}")
    private String passwordProp;

    @Parameter(names = "-cli")
    private boolean cli;

    @Parameter(names = "-interval")
    private int interval = intervalProp;

    @Parameter(names = "-domain")
    private String domain = domainProp;

    @Parameter(names = "-password")
    private String password = passwordProp;

    public boolean isCli() {
        return cli;
    }

    public int getInterval() {
        return interval;
    }

    public String getDomain() {
        return domain;
    }

    public String getPassword() {
        return password;
    }
}
