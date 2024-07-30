package net.schrodingersinbox.controller;

import net.schrodingersinbox.service.EmailClaimService;
import net.schrodingersinbox.service.EmailGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RestController
public class EmailController {

    @Autowired
    private EmailGeneratorService emailGeneratorService;

    @Autowired
    private EmailClaimService emailClaimService;

    @Value("${email.expiration.minutes}")
    private int expirationMinutes;

    private Map<String, SseEmitter> emitters;
    private Map<String, List<Map<String, Object>>> emailBuffer;
    private Map<String, ScheduledFuture<?>> scheduledTasks;
    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        emitters = new ConcurrentHashMap<>();
        emailBuffer = new ConcurrentHashMap<>();
        scheduledTasks = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateEmail() {
        try {
            String tempEmail = emailGeneratorService.generate();
            String claimKey = emailClaimService.generateClaimKey(tempEmail);

            Map<String, String> response = new HashMap<>();
            response.put("key", claimKey);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/subscribe/{claimKey}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@PathVariable String claimKey) {
        String email;
        try {
            email = emailClaimService.getEmail(claimKey);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        SseEmitter emitter = new SseEmitter();
        emitters.put(email, emitter);

        emitter.onCompletion(() -> emitters.remove(email));
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> emitters.remove(email));

        try {
            emitter.send(SseEmitter.event().name("email").data(email));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        List<Map<String, Object>> bufferedEmails = emailBuffer.getOrDefault(email, new ArrayList<>());
        for (Map<String, Object> bufferedEmail : bufferedEmails) {
            try {
                emitter.send(SseEmitter.event().name("email:received").data(bufferedEmail));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(email);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        emailBuffer.remove(email);

        scheduleEmailExpiration(email);

        return ResponseEntity.ok(emitter);
    }

    public void sendReceivedEmailToClient(String email, String data) {
        try {
            if (scheduledTasks.containsKey(email)) {
                SseEmitter emitter = emitters.get(email);
                Map<String, Object> parsedEmail = parseMimeMessage(data);
                if (emitter != null) {
                    try {
                        emitter.send(SseEmitter.event().name("email:received").data(parsedEmail));
                    } catch (IOException e) {
                        emailBuffer.computeIfAbsent(email, k -> new ArrayList<>()).add(parsedEmail);
                        emitter.completeWithError(e);
                    }
                } else {
                    emailBuffer.computeIfAbsent(email, k -> new ArrayList<>()).add(parsedEmail);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> parseMimeMessage(String rawMessage) throws Exception {
        Map<String, Object> email = new HashMap<>();
        ByteArrayInputStream is = new ByteArrayInputStream(rawMessage.getBytes());
        MimeMessage mimeMessage = new MimeMessage(null, is);

        email.put("subject", mimeMessage.getSubject() == null ? "" : mimeMessage.getSubject());

        Date sentDate = mimeMessage.getSentDate();
        if (sentDate == null) {
            sentDate = new Date();
        }
        email.put("date", sentDate.getTime());

        Object content = mimeMessage.getContent();
        if (content instanceof String) {
            email.put("body", content.toString());
        } else if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            StringBuilder body = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.getContent() instanceof String) {
                    body.append(bodyPart.getContent().toString());
                }
            }
            email.put("body", body.toString());
        }

        email.put("from", MimeUtility.decodeText(mimeMessage.getFrom()[0].toString()));

        Address[] toAddresses = mimeMessage.getRecipients(MimeMessage.RecipientType.TO);
        String to = Arrays.stream(toAddresses)
                .map(Address::toString)
                .collect(Collectors.joining(", "));
        email.put("to", MimeUtility.decodeText(to));

        Address[] ccAddresses = mimeMessage.getRecipients(MimeMessage.RecipientType.CC);
        if (ccAddresses != null) {
            String cc = Arrays.stream(ccAddresses)
                    .map(Address::toString)
                    .collect(Collectors.joining(", "));
            email.put("cc", MimeUtility.decodeText(cc));
        }

        return email;
    }

    private void scheduleEmailExpiration(String email) {
        if (!scheduledTasks.containsKey(email)) {
            ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> {
                scheduledTasks.remove(email);

                notifyEmailExpiration(email);
                emitters.remove(email);
                emailBuffer.remove(email);
            }, expirationMinutes, TimeUnit.MINUTES);

            scheduledTasks.put(email, scheduledTask);
        }
    }

    private void notifyEmailExpiration(String email) {
        SseEmitter emitter = emitters.get(email);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("email:expired").data("Email has expired"));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }

}
