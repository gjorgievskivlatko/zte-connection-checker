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
    private final JLabel fwLbl;

    private final Logger logger = LoggerFactory.getLogger(ConnectionCheckerThread.class);

    private int connectionFailures = 0;
    private int successfullRestarts = 0;
    private int failedRestarts = 0;
    private boolean restarting = false;

    public ConnectionCheckerThread(int interval, String domain, String password,
                                   FetchRDCommand fetchRDCommand,
                                   LoginCommand loginCommand, RebootCommand rebootCommand,
                                   FirmwareVersionCommand firmwareVersionCommand,
                                   CheckConnectionCommand checkConnectionCommand,
                                   JTextArea statusLbl, JLabel fwLbl) {
        this.interval = interval;
        this.domain = domain;
        this.password = password;
        this.fetchRDCommand = fetchRDCommand;
        this.loginCommand = loginCommand;
        this.rebootCommand = rebootCommand;
        this.firmwareVersionCommand = firmwareVersionCommand;
        this.checkConnectionCommand = checkConnectionCommand;
        this.statusLbl = statusLbl;
        this.fwLbl = fwLbl;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (!isMF971Online()) {
                    Thread.sleep(10_000);
                    continue;
                }

                boolean restart = false;
                try {
                    checkConnectionCommand.execute(null);
                    setInfoStatus("connection success");
                } catch (ConnectException e) {
                    setWarnStatus("Can not connect to the router during connection check", e);
                } catch (InterruptedException e) {
                    throw e;
                } catch (HttpTimeoutException e) {
                    restart = true;
                    setWarnStatus("HttpTimeout during connection check", e);
                } catch (Exception e) {
                    restart = true;
                    setErrorStatus("Unexpected exception during connection check", e);
                }

                if (restart) {
                    try {
                        restarting = true;
                        connectionFailures++;
                        setErrorStatus("connection failed, restarting...", null);
                        FirmwareVersionResultDto firmwareVersionResultDto = firmwareVersionCommand.execute(new FirmwareVersionCommand.FirmwareVersionCommandContext(domain));
                        setInfoStatus(String.format("Fetched firmware version: %s", firmwareVersionResultDto.getFirmwareVersion()));
                        String a = DigestUtils.md5Hex(firmwareVersionResultDto.getFirmwareVersion());
                        setInfoStatus(String.format("fw=%s, md5=%s", firmwareVersionResultDto.getFirmwareVersion(), a));
                        RDDto rdDto = fetchRDCommand.execute(new FetchRDCommand.FetchRDCommandContext(domain));
                        setInfoStatus(String.format("fetched RD=%s", rdDto.getRD()));
                        LoginResultDto loginResultDto = loginCommand.execute(new LoginCommand.LoginCommandContext(domain, password));
                        setInfoStatus(String.format("login result=%s, cookie=%s", loginResultDto.getResultDto().getResult(), loginResultDto.getCookie()));
                        String ad = DigestUtils.md5Hex(a + rdDto.getRD());
                        ResultDto rebootResult = rebootCommand.execute(new RebootCommand.RebootCommandContext(domain, loginResultDto.getCookie(), ad));
                        if ("success".equals(rebootResult.getResult())) {
                            successfullRestarts++;
                        } else {
                            failedRestarts++;
                        }
                        setInfoStatus(String.format("reboot commands executed, result=%s", rebootResult.getResult()));
                        // wait for a device startup
                        do {
                            Thread.sleep(10_000);
                            setInfoStatus("waiting for a device/router startup");
                        } while (!isMF971Online());
                        restarting = false;
                        setInfoStatus("reboot finished");
                    } catch (ConnectException e) {
                        setWarnStatus("Can not connect to the router", e);
                    } catch (InterruptedException e) {
                        throw e;
                    } catch (Exception e) {
                        setErrorStatus("Error", e);
                    } finally {
                        restarting = false;
                    }
                }
                Thread.sleep(interval);
            }
        } catch (InterruptedException e) {
            setInfoStatus("connection checking stopped");
        }
    }

    private boolean isMF971Online() throws InterruptedException {
        boolean isOnline = false;
        try {
            FirmwareVersionResultDto firmwareVersionResultDto = firmwareVersionCommand.execute(new FirmwareVersionCommand.FirmwareVersionCommandContext(domain));
            if (isValidFirmwareVersion(firmwareVersionResultDto.getFirmwareVersion())) {
                isOnline = true;
                setInfoFwVersion(firmwareVersionResultDto.getFirmwareVersion());
            } else {
                setErrorFwVersion(firmwareVersionResultDto.getFirmwareVersion());
            }
        } catch (ConnectException e) {
            setWarnStatus("Can not connect to the router during online check", e);
        } catch (InterruptedException e) {
            throw e;
        } catch (HttpTimeoutException e) {
            setWarnStatus("HttpTimeout during online check", e);
        } catch (Exception e) {
            setErrorStatus("Unexpected exception during online check", e);
        }

        return isOnline;
    }

    private boolean isValidFirmwareVersion(String firmwareVersion) {
        if (firmwareVersion == null) {
            return false;
        }

        return firmwareVersion.contains("MF971");
    }

    private void setInfoStatus(String txt) {
        logger.info(txt);
        if (statusLbl != null) {
            String msg = getCountersLabel() + txt;
            statusLbl.setText(msg);
            statusLbl.setForeground(new Color(17, 106, 72, 255));
        }
    }

    private void setInfoFwVersion(String version) {
        logger.info(String.format("fetched valid firmware version: %s", version));
        if (fwLbl != null) {
            fwLbl.setText(String.format("Firmware version: %s", version));
            fwLbl.setForeground(new Color(17, 106, 72, 255));
        }
    }

    private void setErrorFwVersion(String version) {
        logger.info(String.format("fetched invalid firmware version: %s, probably not a MF971 router", version));
        if (fwLbl != null) {
            fwLbl.setText(String.format("Firmware version: %s", version));
            fwLbl.setForeground(new Color(17, 106, 72, 255));
        }
    }

    private void setWarnStatus(String txt, Exception e) {
        if (e != null) {
            logger.warn(txt, e);
        } else {
            logger.warn(txt);
        }
        if (statusLbl != null) {
            String msg = getCountersLabel() + txt;
            statusLbl.setText(msg);
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
            String msg = getCountersLabel() + txt;
            statusLbl.setText(msg);
            statusLbl.setForeground(Color.RED);
        }
    }

    private String getCountersLabel() {
        return String.format("connectionFailures: %s, successfullRestarts: %s, failedRestarts: %s, restarting: %s%n----------------------------------------------------------------------%n%n", connectionFailures, successfullRestarts, failedRestarts, restarting);
    }
}
