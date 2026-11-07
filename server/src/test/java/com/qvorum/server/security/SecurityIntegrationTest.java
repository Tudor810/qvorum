package com.qvorum.server.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.qvorum.server.TestcontainersConfig;
import com.qvorum.server.model.RequestEventType;
import com.qvorum.server.model.RequestLog;
import com.qvorum.server.monitoring.ClientRegistry;
import com.qvorum.server.repository.RequestLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Sends real HTTP requests through the whole server (filters, security, controllers, database)
 * with MockMvc, which skips only the network.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class SecurityIntegrationTest {

    private static final String PASSWORD = "Test1234!";

    @Autowired
    MockMvc mvc;

    @Autowired
    ClientRegistry clientRegistry;

    @Autowired
    RequestLogRepository requestLogRepository;

    @Test
    void loginReturnsTokenThatIdentifiesTheUser() throws Exception {
        String token = login("admin@qvorum.test");

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@qvorum.test"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@qvorum.test\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void malformedJsonGets400() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{bad json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid data."));
    }

    @Test
    void requestWithoutTokenGets401WithJsonMessage() throws Exception {
        mvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Please log in to continue."));
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        String token = login("admin@qvorum.test");
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenStopsWorkingAfterLogout() throws Exception {
        String token = login("resident@qvorum.test");

        mvc.perform(post("/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenStopsWorkingAfterForcedDisconnect() throws Exception {
        String token = login("resident@qvorum.test");
        String clientId = clientRegistry.list().stream()
                .filter(c -> c.email().equals("resident@qvorum.test"))
                .reduce((first, second) -> second) // the most recent login
                .orElseThrow()
                .id();

        clientRegistry.disconnect(clientId); // what the console's "disconnect" command does

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roleRestrictedEndpointChecksTheRole() throws Exception {
        mvc.perform(get("/test/admin-only").header("Authorization", "Bearer " + login("resident@qvorum.test")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not allowed to do this."));
        mvc.perform(get("/test/admin-only").header("Authorization", "Bearer " + login("admin@qvorum.test")))
                .andExpect(status().isOk());
    }

    @Test
    void requestsAndConnectionsAreRecorded() throws Exception {
        String token = login("president@qvorum.test");
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk());

        var logs = requestLogRepository.findAll();
        assertThat(logs).anySatisfy(entry -> {
            assertThat(entry.getEventType()).isEqualTo(RequestEventType.CONNECT);
        });
        RequestLog meRequest = logs.stream()
                .filter(e -> "/auth/me".equals(e.getPath()) && e.getStatus() == 200)
                .reduce((first, second) -> second)
                .orElseThrow();
        assertThat(meRequest.getEventType()).isEqualTo(RequestEventType.REQUEST);
        assertThat(meRequest.getClientId()).isNotNull();
        assertThat(meRequest.getUserId()).isNotNull();
    }

    private String login(String email) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }
}
