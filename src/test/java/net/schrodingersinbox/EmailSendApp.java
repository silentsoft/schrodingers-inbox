package net.schrodingersinbox;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class EmailSendApp {

    private static final String smtpPort = "";
    private static final String recipient = "";

    public static void main(String[] args) throws Exception {
        for (int i=0; i<3; i++) {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", "localhost");
            properties.put("mail.smtp.port", smtpPort);

            Session session = Session.getDefaultInstance(properties, null);

            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress("test@localhost.com"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject("Test Subject " + i);

            Multipart multipart = new MimeMultipart();

            BodyPart textPart = new MimeBodyPart();
            textPart.setText("This is the text part of the email.");

            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent("<h1>This is the HTML part of the email</h1>", "text/html");

            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);

            Transport.send(message);
        }
    }

}
