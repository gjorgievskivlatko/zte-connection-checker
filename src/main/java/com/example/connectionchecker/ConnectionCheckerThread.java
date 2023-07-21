package com.example.connectionchecker;

import com.example.connectionchecker.commands.*;
import com.example.connectionchecker.dto.FirmwareVersionResultDto;
import com.example.connectionchecker.dto.LoginResultDto;
import com.example.connectionchecker.dto.RDDto;
import com.example.connectionchecker.dto.ResultDto;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.TimeUnit;

public class ConnectionCheckerThread extends Thread {

    private final int interval;
    private final String domain;
    private final String password;
    private final FetchRDCommand fetchRDCommand;
    private final LoginCommand loginCommand;
    private final RebootCommand rebootCommand;
    private final FirmwareVersionCommand firmwareVersionCommand;
    private final CheckConnectionCommand checkConnectionCommand;
    private final JTextArea statusLbl;

    private final Logger logger = LoggerFactory.getLogger(ConnectionCheckerThread.class);

    public ConnectionCheckerThread(int interval, String domain, String password,
                                   FetchRDCommand fetchRDCommand,
                                   LoginCommand loginCommand, RebootCommand rebootCommand,
                                   FirmwareVersionCommand firmwareVersionCommand,
                                   CheckConnectionCommand checkConnectionCommand,
                                   JTextArea statusLbl) {
        this.interval = interval;
        this.domain = domain;
        this.password = password;
        this.fetchRDCommand = fetchRDCommand;
        this.loginCommand = loginCommand;
        this.rebootCommand = rebootCommand;
        this.firmwareVersionCommand = firmwareVersionCommand;
        this.checkConnectionCommand = checkConnectionCommand;
        this.statusLbl = statusLbl;
    }

    @Override
    public void run() {
        try {
            while (true) {
                boolean restart = false;
                try {
                    checkConnectionCommand.execute(null);
                    setInfoStatus("connection success");
                } catch (HttpTimeoutException e) {
                    restart = true;
                } catch (Exception e) {
                    restart = true;
                    setErrorStatus("Unexpected exception during connection check", e);
                }

                if (restart) {
                    try {
                        setErrorStatus("connection failed, restarting...", null);
                        FirmwareVersionResultDto firmwareVersionResultDto = firmwareVersionCommand.execute(new FirmwareVersionCommand.FirmwareVersionCommandContext(domain));
                        setInfoStatus("fetched firmware version: %s".formatted(firmwareVersionResultDto.getFirmwareVersion()));
                        String a = DigestUtils.md5Hex(firmwareVersionResultDto.getFirmwareVersion());
                        setInfoStatus("fw=%s, md5=%s".formatted(firmwareVersionResultDto.getFirmwareVersion(), a));
                        RDDto rdDto = fetchRDCommand.execute(new FetchRDCommand.FetchRDCommandContext(domain));
                        setInfoStatus("fetched RD=%s".formatted(rdDto.getRD()));
                        LoginResultDto loginResultDto = loginCommand.execute(new LoginCommand.LoginCommandContext(domain, password));
                        setInfoStatus("login result=%s, cookie=%s".formatted(loginResultDto.getResultDto().getResult(), loginResultDto.getCookie()));
                        String ad = DigestUtils.md5Hex(a + rdDto.getRD());
                        ResultDto rebootResult = rebootCommand.execute(new RebootCommand.RebootCommandContext(domain, loginResultDto.getCookie(), ad));
                        setInfoStatus("reboot finished, result=%s".formatted(rebootResult.getResult()));
                        // wait for a device startup
                        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
                    } catch (ConnectException e) {
                        setWarnStatus("Can not connect to the router", e);
                    } catch (InterruptedException e) {
                        throw e;
                    } catch (Exception e) {
                        setErrorStatus("Error", e);
                    }
                }
                Thread.sleep(interval);
            }
        } catch (InterruptedException e) {
            setErrorStatus("Connection checking interrupted", e);
        }
    }

    private void setInfoStatus(String txt) {
        logger.info(txt);
        if (statusLbl != null) {
            statusLbl.setText(txt);
            statusLbl.setForeground(new Color(17, 106, 72, 255));
        }
    }

    private void setWarnStatus(String txt, Exception e) {
        if (e != null) {
            logger.warn(txt, e);
        } else {
            logger.warn(txt);
        }
        if (statusLbl != null) {
            statusLbl.setText(txt);
            statusLbl.setForeground(new Color(246, 201, 6));
        }
    }

    private void setErrorStatus(String txt, Exception e) {
        if (e != null) {
            logger.error(txt, e);
        } else {
            logger.error(txt);
        }
        if (statusLbl != null) {
            statusLbl.setText(txt);
            statusLbl.setForeground(Color.RED);
        }
    }
}
