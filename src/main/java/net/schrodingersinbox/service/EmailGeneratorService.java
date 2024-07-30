package net.schrodingersinbox.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@Service
public class EmailGeneratorService {

    @Value("${email.prefix}")
    private String emailPrefix;

    @Value("${email.magic.yymmdd}")
    private String emailMagicYYMMDD;

    @Value("${email.random.characters}")
    private String emailRandomCharacters;

    @Value("${email.random.length}")
    private int emailRandomLength;

    @Value("${email.domain}")
    private String emailDomain;

    public String generate() {
        return emailPrefix + generateDatePart() + generateRandomPart() + "@" + emailDomain;
    }

    private String generateDatePart() {
        String date = new SimpleDateFormat("yyMMdd").format(new Date());
        StringBuilder builder = new StringBuilder();
        for (char c : date.toCharArray()) {
            builder.append(emailMagicYYMMDD.charAt(Character.getNumericValue(c)));
        }
        return builder.toString();
    }

    private String generateRandomPart() {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < emailRandomLength; i++) {
            builder.append(emailRandomCharacters.charAt(random.nextInt(emailRandomCharacters.length())));
        }
        return builder.toString();
    }

}
