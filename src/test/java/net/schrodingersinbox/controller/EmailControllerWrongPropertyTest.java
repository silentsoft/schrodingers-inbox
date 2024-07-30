package net.schrodingersinbox.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "email.magic.yymmdd="
})
public class EmailControllerWrongPropertyTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void testGenerate() throws Exception {
        mvc.perform(post("/generate"))
                .andExpect(status().isInternalServerError())
                .andReturn();
    }

}
