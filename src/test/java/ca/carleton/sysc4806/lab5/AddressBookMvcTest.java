package ca.carleton.sysc4806.lab5;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:mvc-tests;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@Transactional
class AddressBookMvcTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private long createBook() throws Exception {
        String body = mvc.perform(post("/api/addressbooks").contentType("application/json").content("{}"))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asLong();
    }

    private long createBuddy() throws Exception {
        String body = mvc.perform(post("/api/buddies").contentType("application/json")
                .content("{\"firstName\":\"Tom\",\"lastName\":\"Carleton\",\"address\":613,\"phone\":2342,\"email\":\"tom@example.com\"}"))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("id").asLong();
    }

    @Test
    void jsonWorkflowPopulatesRenderedBook() throws Exception {
        long book = createBook();
        long buddy = createBuddy();
        mvc.perform(post("/api/addressbooks/" + book + "/buddies").contentType("application/json")
                .content("{\"buddyId\":" + buddy + "}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.buddies[0].firstName").value("Tom"));
        mvc.perform(get("/api/addressbooks/" + book)).andExpect(status().isOk())
                .andExpect(jsonPath("$.buddies[0].phone").value(2342))
                .andExpect(jsonPath("$.buddies[0].email").value("tom@example.com"));
        mvc.perform(get("/api/buddies/" + buddy)).andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Carleton"));
        mvc.perform(get("/addressbooks/" + book)).andExpect(status().isOk())
                .andExpect(view().name("addressbook"))
                .andExpect(content().string(containsString("Tom")))
                .andExpect(content().string(containsString("Carleton")))
                .andExpect(content().string(containsString("tom@example.com")));
    }

    @Test
    void emptyBookAndMissingResourcesAreHandled() throws Exception {
        long book = createBook();
        mvc.perform(get("/addressbooks/" + book)).andExpect(status().isOk())
                .andExpect(content().string(containsString("has no buddies")));
        mvc.perform(get("/api/addressbooks/999999")).andExpect(status().isNotFound());
        mvc.perform(get("/addressbooks/999999")).andExpect(status().isNotFound());
        mvc.perform(post("/api/buddies").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/addressbooks/" + book + "/buddies").contentType("application/json")
                .content("{\"buddyId\":999999}")) .andExpect(status().isNotFound());
    }

    @Test
    void duplicateMembershipIsIdempotentAndOtherBookIsRejected() throws Exception {
        long book = createBook();
        long other = createBook();
        long buddy = createBuddy();
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/addressbooks/" + book + "/buddies").contentType("application/json")
                    .content("{\"buddyId\":" + buddy + "}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.buddies.length()").value(1));
        }
        mvc.perform(post("/api/addressbooks/" + other + "/buddies").contentType("application/json")
                .content("{\"buddyId\":" + buddy + "}")) .andExpect(status().isConflict());
    }

    @Test
    void namesAreEscapedInRenderedHtml() throws Exception {
        long book = createBook();
        String response = mvc.perform(post("/api/buddies").contentType("application/json")
                .content(json.writeValueAsString(java.util.Map.of("firstName", "<script>alert(1)</script>",
                        "lastName", "A & B", "address", 10, "phone", 20, "email", "buddy@example.com"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long buddy = json.readTree(response).get("id").asLong();
        mvc.perform(post("/api/addressbooks/" + book + "/buddies").contentType("application/json")
                .content("{\"buddyId\":" + buddy + "}")).andExpect(status().isOk());
        mvc.perform(get("/addressbooks/" + book)).andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")))
                .andExpect(content().string(containsString("A &amp; B")));
    }
}
