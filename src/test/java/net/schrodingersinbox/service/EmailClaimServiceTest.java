package net.schrodingersinbox.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("dev")
public class EmailClaimServiceTest {

    @Autowired
    private EmailClaimService emailClaimService;

    @Test
    public void testGenerateClaimKey() throws Exception {
        String email = "cat000000abcde@schrodingersinbox.net";
        String claimKey = emailClaimService.generateClaimKey(email);
        assertEquals("9N41ECBbtaUryFENxe0SLTMBVyXnoMuuU-V9zFxVi_9Cavk1xZv-_CO2D5zJTZ-l", claimKey);
    }

    @Test
    public void testGetEmail() throws Exception {
        String claimKey = "9N41ECBbtaUryFENxe0SLTMBVyXnoMuuU-V9zFxVi_9Cavk1xZv-_CO2D5zJTZ-l";
        assertEquals("cat000000abcde@schrodingersinbox.net", emailClaimService.getEmail(claimKey));
    }

}
