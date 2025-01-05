package com.mongodb.kitchensink;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import util.OAuthUtil;

import java.security.NoSuchAlgorithmException;

@SpringBootTest
class KitchensinkApplicationTests {

    @Test
    void contextLoads() {
        try {
            OAuthUtil.getAuthorizationCode();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }


    }

}
