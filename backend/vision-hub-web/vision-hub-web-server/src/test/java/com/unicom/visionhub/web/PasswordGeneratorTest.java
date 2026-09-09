package com.unicom.visionhub.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordGeneratorTest {

    @Test
    void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "ZJ_Unicom";
        String hash = encoder.encode(password);
        System.out.println("密码: " + password);
        System.out.println("BCrypt hash: " + hash);
    }
}
