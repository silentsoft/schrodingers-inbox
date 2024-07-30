package net.schrodingersinbox.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class EmailGeneratorServiceTest {

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

    @Autowired
    private EmailGeneratorService emailGeneratorService;

    @Test
    public void testGenerate() {
        String email = emailGeneratorService.generate();
        String emailRegex = String.format("%s[%s]{6}[%s]{%d}@%s",
                emailPrefix,
                emailMagicYYMMDD,
                emailRandomCharacters,
                emailRandomLength,
                emailDomain.replace(".", "\\.").replace("-", "\\-"));
        assertTrue(Pattern.matches(emailRegex, email));
    }

}
