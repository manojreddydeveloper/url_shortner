package com.example.urlshortener.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.urlshortener.health.DatabaseReadiness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/*
 * Author: Manoj reddy <amireddymanojreddy@gmail.com>
 * Since: 2026-09-03
 */
class HealthControllerTest {
    private DatabaseReadiness databaseReadiness;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        databaseReadiness = mock(DatabaseReadiness.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new HealthController(databaseReadiness)).build();
    }

    @Test
    void livenessDoesNotConsultTheDatabase() throws Exception {
        mockMvc.perform(get("/health/live"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value("UP"));

        verify(databaseReadiness, never()).isReady();
    }

    @Test
    void readinessRejectsTrafficWhilePostgresIsUnavailable() throws Exception {
        when(databaseReadiness.isReady()).thenReturn(false);

        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.database").doesNotExist());
    }

    @Test
    void readinessAcceptsTrafficWhenPostgresIsHealthy() throws Exception {
        when(databaseReadiness.isReady()).thenReturn(true);

        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
