package net.schrodingersinbox.controller;

import net.schrodingersinbox.core.config.SMTPServerConfig;
import net.schrodingersinbox.service.EmailClaimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class EmailControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SMTPServerConfig smtpServerConfig;

    @Autowired
    private EmailClaimService emailClaimService;

    @Test
    public void testGenerateAndSubscribe() throws Exception {
        MvcResult generateResult = mvc.perform(post("/generate"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = generateResult.getResponse().getContentAsString();

        String claimKey = extractFieldFromJson(responseContent, "key");
        assertNotNull(claimKey);

        String tempEmail = emailClaimService.getEmail(claimKey);
        assertNotNull(tempEmail);

        MvcResult subscribeResult = mvc.perform(get("/subscribe/{claimKey}", claimKey)
                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk()).andReturn();

        sendTestEmail(tempEmail);

        String content = subscribeResult.getResponse().getContentAsString();
        assertNotNull(content);
        assertTrue(content.contains("test@localhost.com"));
        assertTrue(content.contains(tempEmail));
        assertTrue(content.contains("Test Subject"));
        assertTrue(content.contains("This is a test email."));
    }

    @Test
    public void testSubscribeWithWrongClaimKey() throws Exception {
        mvc.perform(get("/subscribe/{claimKey}", "invalid-claim-key")
                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isBadRequest()).andReturn();
    }

    private void sendTestEmail(String recipient) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "localhost");
        properties.put("mail.smtp.port", String.valueOf(smtpServerConfig.getSmtpPort()));

        Session session = Session.getDefaultInstance(properties, null);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("test@localhost.com"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        message.setSubject("Test Subject");
        message.setText("This is a test email.");

        Transport.send(message);
    }

    private String extractFieldFromJson(String json, String field) {
        String[] parts = json.split("\"" + field + "\":\"");
        if (parts.length > 1) {
            return parts[1].split("\"")[0];
        }
        return null;
    }

}
