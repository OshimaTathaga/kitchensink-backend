package com.mongodb.kitchensink;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import util.OAuthUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("integrationTest")
class KitchensinkApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(OAuthUtil.getAuthorizationToken());
    }

}
