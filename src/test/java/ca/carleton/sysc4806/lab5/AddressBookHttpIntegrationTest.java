package ca.carleton.sysc4806.lab5;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/** Real HTTP requests against embedded Tomcat, without a browser or test transaction. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:http-tests;DB_CLOSE_ON_EXIT=FALSE")
class AddressBookHttpIntegrationTest {
    @Autowired TestRestTemplate http;
    @Autowired AddressBookRepository books;
    @Autowired BuddyInfoRepository buddies;

    @AfterEach
    void cleanDatabase() {
        books.deleteAll();
        buddies.deleteAll();
    }

    private long createBook() {
        ResponseEntity<JsonNode> response = http.postForEntity("/api/addressbooks", Map.of(), JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long id = response.getBody().path("id").asLong();
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/addressbooks/" + id);
        return id;
    }

    private ResponseEntity<JsonNode> createBuddy(Map<String, Object> fields) {
        return http.postForEntity("/api/buddies", fields, JsonNode.class);
    }

    private Map<String, Object> buddyFields() {
        return Map.of("firstName", "Daniel", "lastName", "Esenwa", "address", 324, "phone", 234);
    }

    private long createBuddyWithEmail(String email) {
        Map<String, Object> fields = new java.util.HashMap<>(buddyFields());
        fields.put("email", email);
        ResponseEntity<JsonNode> response = createBuddy(fields);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long id = response.getBody().path("id").asLong();
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/buddies/" + id);
        return id;
    }

    @Test
    void postAndGetWorkflowPersistsEmailAndRendersBothBuddies() {
        long book = createBook();
        long buddy = createBuddyWithEmail(" daniel@example.com ");
        ResponseEntity<JsonNode> legacy = createBuddy(buddyFields());
        assertThat(legacy.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(legacy.getBody().path("email").isNull()).isTrue();

        for (long id : new long[]{buddy, legacy.getBody().path("id").asLong()}) {
            assertThat(http.postForEntity("/api/addressbooks/" + book + "/buddies",
                    Map.of("buddyId", id), JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        ResponseEntity<JsonNode> response = http.getForEntity("/api/addressbooks/" + book, JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
        assertThat(response.getBody().path("buddies").size()).isEqualTo(2);
        assertThat(response.getBody().path("buddies").get(0).path("email").asText()).isEqualTo("daniel@example.com");

        ResponseEntity<JsonNode> savedBuddy = http.getForEntity("/api/buddies/" + buddy, JsonNode.class);
        assertThat(savedBuddy.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(savedBuddy.getBody().path("email").asText()).isEqualTo("daniel@example.com");

        ResponseEntity<String> html = http.getForEntity("/addressbooks/" + book, String.class);
        assertThat(html.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(html.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_HTML)).isTrue();
        assertThat(html.getBody()).contains("Daniel", "Esenwa", "daniel@example.com", "Email", "add-buddy");
    }

    @Test
    void invalidEmailIsRejectedWithoutSavingAndBlankEmailIsOptional() {
        for (String email : new String[]{"invalid", "a@@example.com", "a b@example.com", "x".repeat(243) + "@example.com"}) {
            Map<String, Object> fields = new java.util.HashMap<>(buddyFields());
            fields.put("email", email);
            assertThat(createBuddy(fields).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
        assertThat(buddies.count()).isZero();
        long buddy = createBuddyWithEmail("   ");
        assertThat(http.getForObject("/api/buddies/" + buddy, JsonNode.class).path("email").isNull()).isTrue();
    }

    @Test
    void emptyBooksBadRequestsAndMissingRecordsHaveExpectedResponses() {
        long book = createBook();
        assertThat(http.getForObject("/api/addressbooks/" + book, JsonNode.class).path("buddies").isEmpty()).isTrue();
        assertThat(http.getForObject("/addressbooks/" + book, String.class)).contains("has no buddies");
        for (String path : new String[]{"/api/addressbooks/999999", "/api/buddies/999999", "/addressbooks/999999"}) {
            assertThat(http.getForEntity(path, String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
        assertThat(createBuddy(Map.of()).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(http.postForEntity("/api/addressbooks/" + book + "/buddies", Map.of(), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(http.postForEntity("/api/addressbooks/" + book + "/buddies", Map.of("buddyId", 999999), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void duplicateMembershipIsIdempotentAndConflictingBookReturnsConflict() {
        long book = createBook();
        long other = createBook();
        long buddy = createBuddyWithEmail("buddy@example.com");
        for (int attempt = 0; attempt < 2; attempt++) {
            ResponseEntity<JsonNode> response = http.postForEntity("/api/addressbooks/" + book + "/buddies",
                    Map.of("buddyId", buddy), JsonNode.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().path("buddies").size()).isEqualTo(1);
        }
        assertThat(http.postForEntity("/api/addressbooks/" + other + "/buddies", Map.of("buddyId", buddy), String.class)
                .getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void homePageAndBrowserAssetsAreServed() {
        ResponseEntity<String> home = http.getForEntity("/", String.class);
        assertThat(home.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(home.getBody()).contains("Daniel Esenwa", "101199099", "create-book");
        assertThat(http.getForEntity("/addressbook.js", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(http.getForEntity("/style.css", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
