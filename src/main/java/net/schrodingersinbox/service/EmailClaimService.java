package net.schrodingersinbox.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

@Service
public class EmailClaimService {

    @Value("${crypto.key}")
    private String cryptoKey;

    @Value("${crypto.iv}")
    private String cryptoIv;

    private SecretKeySpec secretKeySpec;
    private IvParameterSpec ivParameterSpec;

    @PostConstruct
    public void postConstruct() {
        byte[] keyBytes = cryptoKey.getBytes(StandardCharsets.UTF_8);
        keyBytes = Arrays.copyOf(keyBytes, 32);
        secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        byte[] ivBytes = cryptoIv.getBytes(StandardCharsets.UTF_8);
        ivBytes = Arrays.copyOf(ivBytes, 16);
        ivParameterSpec = new IvParameterSpec(ivBytes);
    }

    public String generateClaimKey(String email) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] claimKey = cipher.doFinal(email.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().encodeToString(claimKey);
    }

    public String getEmail(String claimKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decodedClaimKey = Base64.getUrlDecoder().decode(claimKey);
        byte[] decryptedEmail = cipher.doFinal(decodedClaimKey);
        return new String(decryptedEmail, StandardCharsets.UTF_8);
    }

}
