package com.mongodb.kitchensink.it;

import com.mongodb.kitchensink.document.Member;
import com.mongodb.kitchensink.it.helper.Constants;
import com.mongodb.kitchensink.it.helper.OAuthUtil;
import com.mongodb.kitchensink.model.co.MemberCO;
import com.mongodb.kitchensink.model.co.UpdateMemberCO;
import com.mongodb.kitchensink.model.dto.MemberDTO;
import com.mongodb.kitchensink.repository.MemberRepository;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
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

        Member anotherMember = Member
                .builder()
                .email("user3@kitchensink.com")
                .password(passwordEncoder.encode("user3-password"))
                .name("KS Some User 3")
                .phoneNumber("+91XXXXXXXXXX")
                .roles(List.of("USER"))
                .build();

        memberRepository.saveAll(List.of(admin, someUser, anotherMember));

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
                .hasSize(3)
                .extracting(MemberDTO::email)
                .containsExactlyInAnyOrder(
                        "admin@kitchensink.com",
                        "user@kitchensink.com",
                        "user3@kitchensink.com"
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

    @Test
    void shouldReturnMemberByEmailForAdmin() {
        String authorizationHeaderValue = OAuthUtil.getAuthorizationToken("admin@kitchensink.com", "admin-password");
        String expectedEmail = "user@kitchensink.com";
        MemberDTO response = RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .header("Authorization", authorizationHeaderValue)
                .get("/api/members/%s".formatted(expectedEmail))
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        assertEquals(expectedEmail, response.email());
    }

    @Test
    void shouldReturnMemberByEmailForSelf() {
        String authorizationHeaderValue = OAuthUtil.getAuthorizationToken("user@kitchensink.com", "user-password");
        String expectedEmail = "user@kitchensink.com";
        MemberDTO response = RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .header("Authorization", authorizationHeaderValue)
                .get("/api/members/%s".formatted(expectedEmail))
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        assertEquals(expectedEmail, response.email());
    }

    @Test
    void shouldGetAccessDeniedWhenListingByEmailForNonAdminOrOtherMember() {
        Member someMember = Member
                .builder()
                .email("user2@kitchensink.com")
                .password(passwordEncoder.encode("user-password"))
                .name("KS Some User")
                .phoneNumber("+91XXXXXXXXXX")
                .roles(List.of("USER"))
                .build();

        memberRepository.save(someMember);

        String emailOfMemberToBeListed = "user2@kitchensink.com";


        String authorizationHeaderValue = OAuthUtil.getAuthorizationToken("user@kitchensink.com", "user-password");

        RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .header("Authorization", authorizationHeaderValue)
                .get("/api/members/%s".formatted(emailOfMemberToBeListed))
                .then()
                .statusCode(403);
    }

    @Test
    void shouldSuccessfullyCreateMemberByAdminUser() {
        String authorizationHeaderValue = OAuthUtil.getAuthorizationToken("admin@kitchensink.com", "admin-password");

        MemberCO memberToBeSaved = MemberCO.builder()
                .email("user2@kitchensink.com")
                .password(passwordEncoder.encode("user2-password"))
                .name("KS Some User 2")
                .phoneNumber("+91XXXXXXXXXX")
                .roles(List.of("USER"))
                .build();

        MemberDTO savedMemberResponse = RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .header("Authorization", authorizationHeaderValue)
                .contentType(ContentType.JSON)
                .body(memberToBeSaved)
                .post("/api/members")
                .then()
                .statusCode(201)
                .extract()
                .as(new TypeRef<>() {
                });

        assertEquals("user2@kitchensink.com", savedMemberResponse.email());
        assertEquals(4, memberRepository.count());
    }

    @Test
    void shouldGetAccessDeniedForNonAdminWhenCreatingMember() {
        String authorizationHeaderValue = OAuthUtil.getAuthorizationToken("user@kitchensink.com", "user-password");

        MemberCO memberToBeSaved = MemberCO.builder()
                .email("user2@kitchensink.com")
                .password(passwordEncoder.encode("user2-password"))
                .name("KS Some User 2")
                .phoneNumber("+91XXXXXXXXXX")
                .roles(List.of("USER"))
                .build();

        RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .header("Authorization", authorizationHeaderValue)
                .contentType(ContentType.JSON)
                .body(memberToBeSaved)
                .post("/api/members")
                .then()
                .statusCode(403);

        assertEquals(3, memberRepository.count());
    }

    @CsvSource({
            "admin@kitchensink.com, admin-password",
            "user@kitchensink.com, user-password"
    })
    @ParameterizedTest
    void shouldSuccessfullyUpdateMemberByAuthorisedMembers(String authorisedMemberEmail, String authorisedMemberPassword) {
        String authorizationHeaderValue = OAuthUtil.getAuthorizationToken(authorisedMemberEmail, authorisedMemberPassword);
        String expectedUpdatedName = "KS Some Other User Updated";
        String emailOfUpdatedMember = "user@kitchensink.com";

        UpdateMemberCO someOtherUserUpdated = UpdateMemberCO.builder()
                .name(expectedUpdatedName)
                .build();

        MemberDTO savedMemberResponse = RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .header("Authorization", authorizationHeaderValue)
                .contentType(ContentType.JSON)
                .body(someOtherUserUpdated)
                .patch("/api/members/%s".formatted(emailOfUpdatedMember))
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        assertEquals(expectedUpdatedName, savedMemberResponse.name());
        assertEquals(3, memberRepository.count());
    }

    @CsvSource({
            "user@kitchensink.com, user-password",
            "user3@kitchensink.com, user3-password"
    })
    @ParameterizedTest
    void shouldGetAccessDeniedWhenUpdatingMemberByUnAuthorisedMember(String unauthorisedMemberEmail, String unauthorisedMemberPassword) {
        String authorizationHeaderValue = OAuthUtil.getAuthorizationToken(unauthorisedMemberEmail, unauthorisedMemberPassword);
        String expectedUpdatedName = "KS Some Other User 2 Updated";
        String emailOfUpdatedMember = "user2@kitchensink.com";

        Member memberToBeSavedAndUpdated = Member
                .builder()
                .email("user2@kitchensink.com")
                .password(passwordEncoder.encode("user2-password"))
                .name("KS Some User 2")
                .phoneNumber("+91XXXXXXXXXX")
                .roles(List.of("USER"))
                .build();

        memberRepository.save(memberToBeSavedAndUpdated);

        UpdateMemberCO someOtherUserUpdated = UpdateMemberCO.builder()
                .name(expectedUpdatedName)
                .build();

        RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .header("Authorization", authorizationHeaderValue)
                .contentType(ContentType.JSON)
                .body(someOtherUserUpdated)
                .patch("/api/members/%s".formatted(emailOfUpdatedMember))
                .then()
                .statusCode(403);

        assertEquals(4, memberRepository.count());
    }

    @CsvSource({
            "admin@kitchensink.com, admin-password",
            "user@kitchensink.com, user-password"
    })
    @ParameterizedTest
    void shouldSuccessfullyDeleteMemberByAuthorisedMembers(String authorisedMemberEmail, String authorisedMemberPassword) {
        String authorizationHeaderValue = OAuthUtil.getAuthorizationToken(authorisedMemberEmail, authorisedMemberPassword);
        String emailOfDeletedMember = "user@kitchensink.com";


        RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .header("Authorization", authorizationHeaderValue)
                .contentType(ContentType.JSON)
                .delete("/api/members/%s".formatted(emailOfDeletedMember))
                .then()
                .statusCode(204);

        assertEquals(2, memberRepository.count());
    }

    @CsvSource({
            "user@kitchensink.com, user-password",
            "user3@kitchensink.com, user3-password"
    })
    @ParameterizedTest
    void shouldGetAccessDeniedWhenDeletingMemberByUnAuthorisedMember(String unauthorisedMemberEmail, String unauthorisedMemberPassword) {
        String authorizationHeaderValue = OAuthUtil.getAuthorizationToken(unauthorisedMemberEmail, unauthorisedMemberPassword);
        String emailOfDeletedMember = "admin@kitchensink.com";


        RestAssured.given()
                .baseUri(Constants.BASE_URI)
                .header("Authorization", authorizationHeaderValue)
                .contentType(ContentType.JSON)
                .delete("/api/members/%s".formatted(emailOfDeletedMember))
                .then()
                .statusCode(403);

        assertEquals(3, memberRepository.count());
    }
}
