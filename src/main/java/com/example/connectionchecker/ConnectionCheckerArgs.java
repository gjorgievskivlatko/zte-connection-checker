package com.example.connectionchecker;

import com.beust.jcommander.Parameter;

import static com.example.connectionchecker.ConnectionCheckerApplication.*;

public class ConnectionCheckerArgs {

    @Parameter(names = "-cli")
    private boolean cli;

    @Parameter(names = "-interval")
    private int interval = DEFAULT_INTERVAL;

    @Parameter(names = "-domain")
    private String domain = DEFAULT_DOMAIN;

    @Parameter(names = "-password")
    private String password = DEFAULT_PASSWORD;

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
