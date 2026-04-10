package se.edugrade.java25.enterprise.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import se.edugrade.java25.enterprise.gym.dto.BookingRequest;
import se.edugrade.java25.enterprise.gym.dto.BookingResponse;
import se.edugrade.java25.enterprise.gym.dto.GymClassRequest;
import se.edugrade.java25.enterprise.gym.dto.GymClassResponse;
import se.edugrade.java25.enterprise.gym.exception.GymClassNotFoundException;
import se.edugrade.java25.enterprise.gym.security.JwtAuthenticationFilter;
import se.edugrade.java25.enterprise.gym.security.JwtUtil;
import se.edugrade.java25.enterprise.gym.security.SecurityConfig;
import se.edugrade.java25.enterprise.gym.service.BookingService;
import se.edugrade.java25.enterprise.gym.service.GymClassService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({GymClassController.class, BookingController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class GymClassControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private GymClassService gymClassService;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private JwtUtil jwtUtil;

    private final BookingResponse sampleBookingResponse = new BookingResponse(
            1L,
            "Emma Johansson",
            "emma.j@example.com",
            LocalDateTime.now(),
            1L
    );

    private final GymClassResponse sampleGymClassResponse = new GymClassResponse(
            1L,
            "Morning Yoga",
            "Anna Svensson",
            "Calm yoga session to start your day",
            "Monday",
            "08:00",
            60,
            12,
            List.of(sampleBookingResponse)
    );

    @Test
    @DisplayName("GET /classes returns 200 without authentication")
    void getAllClasses_noAuth_returns200() throws Exception {

        when(gymClassService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleGymClassResponse)));

        mockMvc.perform(get("/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Morning Yoga"))
                .andExpect(jsonPath("$.content[0].instructor").value("Anna Svensson"));
    }

    @Test
    @DisplayName("GET /classes/{id} returns 200 without authentication")
    void getGymClassById_noAuth_returns200() throws Exception {
        when(gymClassService.findById(1L)).thenReturn(sampleGymClassResponse);

        mockMvc.perform(get("/classes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Morning Yoga"))
                .andExpect(jsonPath("$.instructor").value("Anna Svensson"))
                .andExpect(jsonPath("$.bookings[0].participantName").value("Emma Johansson"));
    }

    @Test
    @DisplayName("POST /classes with ADMIN returns 201")
    void createGymClass_withAdmin_returns201() throws Exception {

        GymClassRequest request = new GymClassRequest();
        request.setName("Morning Yoga");
        request.setInstructor("Anna Svensson");
        request.setDescription("Calm yoga session to start your day");
        request.setDayOfWeek("Monday");
        request.setStartTime("08:00");
        request.setDurationMinutes(60);
        request.setMaxParticipants(12);

        when(gymClassService.create(any(GymClassRequest.class))).thenReturn(sampleGymClassResponse);

        mockMvc.perform(post("/classes")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Morning Yoga"))
                .andExpect(jsonPath("$.instructor").value("Anna Svensson"));
    }

    @Test
    @DisplayName("POST /classes without auth returns 401")
    void createGymClass_noAuth_returns401() throws Exception {

        GymClassRequest request = new GymClassRequest();
        request.setName("Morning Yoga");
        request.setInstructor("Anna Svensson");
        request.setDescription("Calm yoga session to start your day");
        request.setDayOfWeek("Monday");
        request.setStartTime("08:00");
        request.setDurationMinutes(60);
        request.setMaxParticipants(12);

        mockMvc.perform(post("/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /classes with USER returns 403")
    void createGymClass_withUser_returns403() throws Exception {

        GymClassRequest request = new GymClassRequest();
        request.setName("Morning Yoga");
        request.setInstructor("Anna Svensson");
        request.setDescription("Calm yoga session to start your day");
        request.setDayOfWeek("Monday");
        request.setStartTime("08:00");
        request.setDurationMinutes(60);
        request.setMaxParticipants(12);

        mockMvc.perform(post("/classes")
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /classes/{id}/bookings with USER returns 201")
    void createBooking_withUser_returns201() throws Exception {

        Long classId = 1L;

        BookingRequest request = new BookingRequest();
        request.setParticipantName("Emma Johansson");
        request.setEmail("emma.j@example.com");

        BookingResponse bookingResponse = new BookingResponse(
                1L,
                "Emma Johansson",
                "emma.j@example.com",
                LocalDateTime.now(),
                classId
        );

        when(bookingService.createBooking(anyLong(), any(BookingRequest.class)))
                .thenReturn(bookingResponse);

        mockMvc.perform(post("/classes/{id}/bookings", classId)
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participantName").value("Emma Johansson"))
                .andExpect(jsonPath("$.email").value("emma.j@example.com"))
                .andExpect(jsonPath("$.gymClassId").value(1));
    }

    @Test
    @DisplayName("POST /classes returns 400 (invalid body)")
    void createGymClass_invalidBody_returns400() throws Exception {
        GymClassRequest invalidRequest = new GymClassRequest();
        invalidRequest.setName("");
        invalidRequest.setInstructor("");
        invalidRequest.setDescription("");
        invalidRequest.setDayOfWeek("");
        invalidRequest.setStartTime("");
        invalidRequest.setDurationMinutes(10);
        invalidRequest.setMaxParticipants(0);

        mockMvc.perform(post("/classes")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /classes/{id} returns 404")
    void getGymClassById_notFound_returns404() throws Exception {

        when(gymClassService.findById(999L))
            .thenThrow(new GymClassNotFoundException(999L));

        mockMvc.perform(get("/classes/999"))
                .andExpect(status().isNotFound());
    }
}


