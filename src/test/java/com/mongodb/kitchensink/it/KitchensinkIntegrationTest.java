package com.mongodb.kitchensink.it;

import com.mongodb.kitchensink.document.Member;
import com.mongodb.kitchensink.it.helper.Constants;
import com.mongodb.kitchensink.it.helper.OAuthUtil;
import com.mongodb.kitchensink.model.dto.MemberDTO;
import com.mongodb.kitchensink.repository.MemberRepository;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("integrationTest")
@Testcontainers
@Execution(SAME_THREAD)
class KitchensinkIntegrationTest {

    @Container
    private static final GenericContainer<?> mongoDBContainer = new GenericContainer<>("mongo:7.0")
            .withEnv(Map.of(
                    "MONGO_INITDB_DATABASE", "kitchensink",
                    "MONGO_INITDB_ROOT_USERNAME", "mongo",
                    "MONGO_INITDB_ROOT_PASSWORD", "mongo"
            ))
            .withExposedPorts(27017)
            .waitingFor(Wait.forLogMessage("(?i).*Waiting for connections*.*", 1));

    @DynamicPropertySource
    private static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.port", () -> mongoDBContainer.getMappedPort(27017));
    }

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }


    @BeforeEach
    void setup() {
        Member admin = Member
                .builder()
                .email("admin@kitchensink.com")
                .password(passwordEncoder.encode("admin-password"))
                .name("KS Admin")
                .phoneNumber("+91XXXXXXXXXX")
                .roles(List.of("ADMIN"))
                .build();
        Member someUser = Member
                .builder()
                .email("user@kitchensink.com")
                .password(passwordEncoder.encode("user-password"))
                .name("KS Some User")
                .phoneNumber("+91XXXXXXXXXX")
                .roles(List.of("USER"))
                .build();
        memberRepository.saveAll(List.of(admin, someUser));

    }

    @Test
    void shouldGetAllMembersForAdmin() {
        String authorizationHeaderValue = OAuthUtil.getAuthorizationToken("admin@kitchensink.com", "admin-password");
        List<MemberDTO> response = RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .header("Authorization", authorizationHeaderValue)
                .get("/api/members")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        assertThat(response)
                .hasSize(2)
                .extracting(MemberDTO::email)
                .containsExactlyInAnyOrder(
                        "admin@kitchensink.com",
                        "user@kitchensink.com"
                );
    }

    @Test
    void shouldGetAccessDeniedForNonAdminWhenListingMembers() {
        String authorizationHeaderValue = OAuthUtil.getAuthorizationToken("user@kitchensink.com", "user-password");

        RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .header("Authorization", authorizationHeaderValue)
                .get("/api/members")
                .then()
                .statusCode(403);
    }

}
