package se.edugrade.java25.enterprise.gym.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GymIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Register -> login -> create booking -> verify round-trip")
    void register_login_createBooking_verify() throws Exception {

        // Register (ignore 409 if already exists)
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "user",
                                  "password": "password"
                                }
                                """))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 201 && status != 409) {
                        throw new AssertionError("Expected 201 or 409 but got " + status);
                    }
                });

        // Login
        String userToken = login("user", "password");

        // Create booking
        mockMvc.perform(post("/classes/1/bookings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participantName": "Anya",
                                  "email": "anya@email.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participantName").value("Anya"))
                .andExpect(jsonPath("$.email").value("anya@email.com"))
                .andExpect(jsonPath("$.gymClassId").value(1));

        // Verify booking exists
        mockMvc.perform(get("/classes/1/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].participantName").exists());
    }

    @Test
    @DisplayName("Login as ADMIN -> create class with real JWT -> verify 201")
    void loginAsAdmin_createClass_returnsCreatedClass() throws Exception {

        String adminToken = login("admin", "password");

        String requestBody = """
                {
                  "name": "Integration Yoga",
                  "instructor": "Anna",
                  "description": "Created for my integration test",
                  "dayOfWeek": "Monday",
                  "startTime": "10:00",
                  "durationMinutes": 60,
                  "maxParticipants": 10
                }
                """;

        mockMvc.perform(post("/classes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Integration Yoga"))
                .andExpect(jsonPath("$.instructor").value("Anna"))
                .andExpect(jsonPath("$.dayOfWeek").value("Monday"))
                .andExpect(jsonPath("$.startTime").value("10:00"))
                .andExpect(jsonPath("$.durationMinutes").value(60))
                .andExpect(jsonPath("$.maxParticipants").value(10));
    }

    @Test
    @DisplayName("POST /classes without token returns 401")
    void createClass_withoutToken_returns401() throws Exception {

        String requestBody = """
                {
                  "name": "Unauthorized Class",
                  "instructor": "Anna",
                  "description": "Should fail without token",
                  "dayOfWeek": "Tuesday",
                  "startTime": "12:00",
                  "durationMinutes": 45,
                  "maxParticipants": 8
                }
                """;

        mockMvc.perform(post("/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /classes/{id}/bookings on full class returns 409")
    void createBooking_onFullClass_returns409() throws Exception {

        // Register USER (ignore 409 if already exists)
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "user",
                                  "password": "password"
                                }
                                """))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 201 && status != 409) {
                        throw new AssertionError("Expected 201 or 409 but got " + status);
                    }
                });

        // Login as USER
        String userToken = login("user", "password");

        // Login as ADMIN
        String adminToken = login("admin", "password");

        // Create class with 1 spot
        String requestBody = """
                {
                  "name": "My Full Class Test",
                  "instructor": "Anna",
                  "description": "Integration test",
                  "dayOfWeek": "Friday",
                  "startTime": "15:00",
                  "durationMinutes": 60,
                  "maxParticipants": 1
                }
                """;

        MvcResult requestResult = mockMvc.perform(post("/classes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract class ID from response
        long classId = objectMapper
                .readTree(requestResult.getResponse().getContentAsString())
                .get("id").asLong();

        // First booking (fills class)
        mockMvc.perform(post("/classes/" + classId + "/bookings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participantName": "First",
                                  "email": "first@email.com"
                                }
                                """))
                .andExpect(status().isCreated());

        // Second booking → 409
        mockMvc.perform(post("/classes/" + classId + "/bookings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participantName": "Second",
                                  "email": "second@email.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("DELETE class then GET returns 404")
    void deleteClass_thenGetById_returns404() throws Exception {

        // Login as ADMIN
        String adminToken = login("admin", "password");

        // Create class
        String requestBody = """
                {
                  "name": "Class To Delete",
                  "instructor": "Anna",
                  "description": "Integration test",
                  "dayOfWeek": "Wednesday",
                  "startTime": "14:00",
                  "durationMinutes": 60,
                  "maxParticipants": 10
                }
                """;

        MvcResult requestResult = mockMvc.perform(post("/classes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract class ID from response
        long classId = objectMapper
                .readTree(requestResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Delete class
        mockMvc.perform(delete("/classes/" + classId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify the class was deleted
        mockMvc.perform(get("/classes/" + classId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /classes/{id}/spots-remaining returns correct count")
    void getSpotsRemaining_returnsCorrectValue() throws Exception {

        // Login as ADMIN
        String adminToken = login("admin", "password");

        // Register USER (ignore 409)
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "user",
                                  "password": "password"
                                }
                                """))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 201 && status != 409) {
                        throw new AssertionError("Expected 201 or 409 but got " + status);
                    }
                });

        // Login as USER
        String userToken = login("user", "password");

        // Create class with 2 spots
        String requestBody = """
                {
                  "name": "Spots Remaining Test",
                  "instructor": "Anna",
                  "description": "Integration test",
                  "dayOfWeek": "Thursday",
                  "startTime": "16:00",
                  "durationMinutes": 60,
                  "maxParticipants": 2
                }
                """;

        MvcResult requestResult = mockMvc.perform(post("/classes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract class ID from response
        long classId = objectMapper
                .readTree(requestResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Make 1 booking
        mockMvc.perform(post("/classes/" + classId + "/bookings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participantName": "Test User",
                                  "email": "test_user@email.com"
                                }
                                """))
                .andExpect(status().isCreated());

        // Verify spots remaining: 2 - 1 = 1
        mockMvc.perform(get("/classes/" + classId + "/spots-remaining"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spotsRemaining").value(1));
    }

    // Extra tests
    @Test
    @DisplayName("GET /classes/available returns only classes with spots")
    void getAvailableClasses_returnsOnlyAvailable() throws Exception {

        // Login as ADMIN
        String adminToken = login("admin", "password");

        // Register USER (ignore 409)
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "user",
                                  "password": "password"
                                }
                                """))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 201 && status != 409) {
                        throw new AssertionError("Expected 201 or 409 but got " + status);
                    }
                });

        // Login as USER
        String userToken = login("user", "password");

        // Create FULL class with 1 spot
        String fullClassBody = """
                {
                  "name": "Full Class Test",
                  "instructor": "Anna",
                  "description": "Will become full",
                  "dayOfWeek": "Monday",
                  "startTime": "10:00",
                  "durationMinutes": 60,
                  "maxParticipants": 1
                }
                """;

        MvcResult fullClassResult = mockMvc.perform(post("/classes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fullClassBody))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract class ID from response
        long fullClassId = objectMapper.readTree(fullClassResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Fill the class
        mockMvc.perform(post("/classes/" + fullClassId + "/bookings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participantName": "Anya",
                                  "email": "anya@email.com"
                                }
                                """))
                .andExpect(status().isCreated());

        // Create AVAILABLE class with free spots
        String availableClassBody = """
                {
                  "name": "Available Class Test",
                  "instructor": "Emma",
                  "description": "Still has free spots",
                  "dayOfWeek": "Tuesday",
                  "startTime": "12:00",
                  "durationMinutes": 60,
                  "maxParticipants": 5
                }
                """;

        // Create the class
        mockMvc.perform(post("/classes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(availableClassBody))
                .andExpect(status().isCreated());

        // Verify only the available class is returned
        MvcResult result = mockMvc.perform(get("/classes/available"))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the response content
        String response = result.getResponse().getContentAsString();

        // Assert that the response contains the available class name
        assertThat(response).contains("Available Class Test");
        assertThat(response).doesNotContain("Full Class Test");
    }

    @Test
    @DisplayName("DELETE /bookings/{id} with ADMIN returns 204")
    void deleteBooking_withAdmin_returns204() throws Exception {

        // Login as ADMIN
        String adminToken = login("admin", "password");

        // Register USER (ignore 409)
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "user",
                                  "password": "password"
                                }
                                """))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 201 && status != 409) {
                        throw new AssertionError("Expected 201 or 409 but got " + status);
                    }
                });

        // Login as USER
        String userToken = login("user", "password");

        // Create booking
        MvcResult result = mockMvc.perform(post("/classes/1/bookings")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participantName": "Vladimir",
                                  "email": "vlad@email.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract booking ID from response
        long bookingId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        // Delete booking
        mockMvc.perform(delete("/bookings/" + bookingId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verify: booking is NOT in list anymore
        MvcResult listResult = mockMvc.perform(get("/classes/1/bookings"))
                .andExpect(status().isOk())
                .andReturn();

        String response = listResult.getResponse().getContentAsString();

        assertThat(response).doesNotContain("Vladimir");
    }


    // Helper
    private String login(String username, String password) throws Exception {
        String body = """
                {"username": "%s", "password": "%s"}
                """.formatted(username, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken")
                .asText();
    }
}
