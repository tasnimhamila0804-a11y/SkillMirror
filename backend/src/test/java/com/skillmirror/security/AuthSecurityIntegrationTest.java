package com.skillmirror.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillmirror.user.User;
import com.skillmirror.user.UserRepository;
import com.skillmirror.user.UserRole;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void rejectsAccessToPrivateEndpointsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/profiles/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/overview")).andExpect(status().isUnauthorized());
    }

    @Test
    void keepsQuestionnaireDefinitionPublicButResponsesProtected() throws Exception {
        mockMvc.perform(get("/api/questionnaire")).andExpect(status().isOk());
        mockMvc.perform(get("/api/questionnaire/responses")).andExpect(status().isUnauthorized());
    }

    @Test
    void registersLoginAndRejectsDuplicateEmail() throws Exception {
        String email = "security-" + System.nanoTime() + "@test.dev";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Secure User", "email", email, "password", "demo1234"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Other", "email", email, "password", "demo1234"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", email, "password", "wrong-password"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", email, "password", "demo1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(email));
    }

    @Test
    void blocksAdminEndpointsForRegularUsers() throws Exception {
        String email = "role-" + System.nanoTime() + "@test.dev";
        var response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Regular", "email", email, "password", "demo1234"))))
                .andExpect(status().isCreated())
                .andReturn();
        String body = response.getResponse().getContentAsString();
        String token = objectMapper.readTree(body).get("token").asText();

        mockMvc.perform(get("/api/admin/overview").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        // promote the user, then the same call must succeed
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        user.setRole(UserRole.ADMIN);
        userRepository.save(user);

        mockMvc.perform(get("/api/admin/overview").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isNumber());
    }

    @Test
    void rejectsWeakPasswords() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Weak", "email", "weak-" + System.nanoTime() + "@test.dev",
                                        "password", "abcdefgh"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WEAK_PASSWORD"));
    }

    @Test
    void hashesPasswordsWithBcrypt() throws Exception {
        String raw = "plain-password-1";
        assertThatBcryptNotRaw(raw);
    }

    private void assertThatBcryptNotRaw(String raw) {
        String hash = passwordEncoder.encode(raw);
        org.assertj.core.api.Assertions.assertThat(hash).isNotEqualTo(raw).startsWith("$2");
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches(raw, hash)).isTrue();
    }
}
