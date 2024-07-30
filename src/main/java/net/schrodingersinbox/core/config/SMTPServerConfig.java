package net.schrodingersinbox.core.config;

import net.schrodingersinbox.controller.EmailController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

@Component
public class SMTPServerConfig {

    private SMTPServer smtpServer;

    @Value("${smtp.port}")
    private int smtpPort;

    @Value("${email.max.size}")
    private int maxEmailSize;

    @Autowired
    private EmailController emailController;

    @PostConstruct
    public void startServer() throws Exception {
        if (smtpPort == 0) {
            ServerSocket socket = new ServerSocket(0);
            smtpPort = socket.getLocalPort();
            socket.close();
        }

        smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(new SimpleMessageListener() {
            @Override
            public boolean accept(String from, String recipient) {
                return true;
            }

            @Override
            public void deliver(String from, String recipient, InputStream inputStream) {
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    int totalRead = 0;

                    while ((len = inputStream.read(buffer)) != -1) {
                        totalRead += len;
                        if (totalRead > maxEmailSize) {
                            return;
                        }

                        outputStream.write(buffer, 0, len);
                    }
                    String data = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
                    emailController.sendReceivedEmailToClient(recipient, data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
        smtpServer.setPort(smtpPort);
        smtpServer.start();
    }

    @PreDestroy
    public void stopServer() {
        if (smtpServer != null) {
            smtpServer.stop();
        }
    }

    public int getSmtpPort() {
        return smtpPort;
    }

}
