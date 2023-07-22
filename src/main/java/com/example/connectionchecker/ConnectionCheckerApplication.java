package com.example.connectionchecker;

import com.beust.jcommander.JCommander;
import com.example.connectionchecker.commands.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

import static javax.swing.JOptionPane.ERROR_MESSAGE;

@SpringBootApplication
public class ConnectionCheckerApplication implements CommandLineRunner {

    public static final int DEFAULT_INTERVAL = 10_000;
    public static final String DEFAULT_DOMAIN = "192.168.1.1";
    public static final String DEFAULT_PASSWORD = "b42wkpVZ";

    private static final String DEFAULT_FIRMWARE_VERSION_VALUE = "Firmware version: n/a ";

    private final FetchRDCommand fetchRDCommand;
    private final LoginCommand loginCommand;
    private final RebootCommand rebootCommand;
    private final FirmwareVersionCommand firmwareVersionCommand;
    private final CheckConnectionCommand checkConnectionCommand;

    public ConnectionCheckerApplication(FetchRDCommand fetchRDCommand, LoginCommand loginCommand,
                                        RebootCommand rebootCommand,
                                        FirmwareVersionCommand firmwareVersionCommand,
                                        CheckConnectionCommand checkConnectionCommand) {
        this.fetchRDCommand = fetchRDCommand;
        this.loginCommand = loginCommand;
        this.rebootCommand = rebootCommand;
        this.firmwareVersionCommand = firmwareVersionCommand;
        this.checkConnectionCommand = checkConnectionCommand;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(ConnectionCheckerApplication.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .run(args);
    }

    @Override
    public void run(String... args) {
        ConnectionCheckerArgs connectionCheckerArgs = new ConnectionCheckerArgs();
        JCommander connectionCheckCmd = JCommander.newBuilder()
                .addObject(connectionCheckerArgs)
                .build();
        connectionCheckCmd.parse(args);
        if (connectionCheckerArgs.isCli()) {
            ConnectionCheckerThread connectionCheckerThread = new ConnectionCheckerThread(connectionCheckerArgs.getInterval(),
                    connectionCheckerArgs.getDomain(), connectionCheckerArgs.getPassword(),
                    fetchRDCommand, loginCommand, rebootCommand, firmwareVersionCommand, checkConnectionCommand, null, null);
            connectionCheckerThread.start();
        } else {
            SwingUtilities.invokeLater(() -> {

                JPanel mainLayout = new JPanel();
                mainLayout.setLayout(new BoxLayout(mainLayout, BoxLayout.Y_AXIS));

                // Create the layout
                JPanel layoutPnl = new JPanel(new GridLayout(5, 2));
                layoutPnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                // Create the input fields
                JLabel intervalLbl = new JLabel("Interval:");
                JTextField intervalTxt = new JTextField(String.valueOf(DEFAULT_INTERVAL), 10);
                JLabel domainLbl = new JLabel("Domain:");
                JTextField domainTxt = new JTextField(DEFAULT_DOMAIN, 30);
                JLabel passwordLbl = new JLabel("Password:");
                JTextField passwordTxt = new JTextField(DEFAULT_PASSWORD, 30);
                JTextArea statusLbl = new JTextArea("connection checker status");
                statusLbl.setEditable(false);
                statusLbl.setLineWrap(true);
                statusLbl.setWrapStyleWord(true);
                statusLbl.setFont(statusLbl.getFont().deriveFont(statusLbl.getFont().getStyle() | Font.BOLD));

                JLabel fwLbl = new JLabel(DEFAULT_FIRMWARE_VERSION_VALUE);
                fwLbl.setFont(fwLbl.getFont().deriveFont(fwLbl.getFont().getStyle() | Font.BOLD));

                AtomicReference<ConnectionCheckerThread> connectionCheckerThread = new AtomicReference<>(null);

                JButton startBtn = new JButton("Start");
                JButton stopBtn = new JButton("Stop");
                stopBtn.setEnabled(false);
                startBtn.addActionListener(e -> {
                    int intervalValue;
                    try {
                        intervalValue = Integer.parseInt(intervalTxt.getText());
                    } catch (NumberFormatException ignored) {
                        JOptionPane.showMessageDialog(layoutPnl, "Invalid interval value", "Error", ERROR_MESSAGE);
                        return;
                    }
                    startBtn.setEnabled(false);
                    intervalTxt.setEnabled(false);
                    domainTxt.setEnabled(false);
                    passwordTxt.setEnabled(false);
                    stopBtn.setEnabled(true);
                    connectionCheckerThread.set(new ConnectionCheckerThread(intervalValue, domainTxt.getText(), passwordTxt.getText(),
                            fetchRDCommand, loginCommand, rebootCommand, firmwareVersionCommand, checkConnectionCommand, statusLbl, fwLbl));
                    connectionCheckerThread.get().start();
                });

                stopBtn.addActionListener(e -> {
                    stopBtn.setEnabled(false);
                    intervalTxt.setEnabled(true);
                    domainTxt.setEnabled(true);
                    passwordTxt.setEnabled(true);
                    startBtn.setEnabled(true);
                    fwLbl.setText(DEFAULT_FIRMWARE_VERSION_VALUE);
                    if (connectionCheckerThread.get() != null && !connectionCheckerThread.get().isInterrupted()) {
                        connectionCheckerThread.get().interrupt();
                    }
                });

                layoutPnl.add(intervalLbl);
                layoutPnl.add(intervalTxt);
                layoutPnl.add(domainLbl);
                layoutPnl.add(domainTxt);
                layoutPnl.add(passwordLbl);
                layoutPnl.add(passwordTxt);
                layoutPnl.add(startBtn);
                layoutPnl.add(stopBtn);

                JPanel fwPnl = new JPanel(new BorderLayout());
                fwPnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                fwPnl.setPreferredSize(new Dimension(fwPnl.getWidth(), 30));
                fwPnl.add(fwLbl);

                JPanel statusPnl = new JPanel(new BorderLayout());
                statusPnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                statusPnl.setPreferredSize(new Dimension(statusPnl.getWidth(), 100));
                statusPnl.add(statusLbl);

                mainLayout.add(layoutPnl);
                mainLayout.add(fwPnl);
                mainLayout.add(statusPnl);

                // Create the frame
                JFrame frame = new JFrame("Connection checker");
                frame.setContentPane(mainLayout);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            });
        }
    }
}
