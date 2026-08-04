package com.plsql2java.web;

import com.plsql2java.model.DependencyGraph;
import com.plsql2java.model.DiscoveryMode;
import com.plsql2java.model.DiscoveryResult;
import com.plsql2java.orchestration.MigrationOrchestratorService;
import com.plsql2java.orchestration.model.AnalysisResult;
import com.plsql2java.orchestration.model.MigrationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebIntegrationTest {

    @Autowired MockMvc mockMvc;
    @MockBean MigrationOrchestratorService orchestrator;

    @Test
    @WithMockUser
    void uploadAndAnalyze_fullFlow_returns202() throws Exception {
        DiscoveryResult discovery = new DiscoveryResult("id", "SCHEMA", DiscoveryMode.FILE);
        DependencyGraph graph = new DependencyGraph("id");
        when(orchestrator.analyze(any())).thenReturn(new AnalysisResult("id", discovery, graph));

        // Upload file
        MockMultipartFile file = new MockMultipartFile(
                "file", "schema.sql", "text/plain",
                "CREATE OR REPLACE PROCEDURE p AS BEGIN NULL; END;".getBytes());

        String uploadResponse = mockMvc.perform(multipart("/api/migrations/upload").file(file).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").exists())
                .andReturn().getResponse().getContentAsString();

        String uploadId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(uploadResponse).get("uploadId").asText();

        // Trigger analyze
        mockMvc.perform(post("/api/migrations/analyze").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"uploadId\":\"" + uploadId + "\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").exists());
    }

    @Test
    void homePageRedirectsToLogin_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    void homePage_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void jdbcConfig_validRequest_returnsConfigId() throws Exception {
        mockMvc.perform(post("/api/migrations/jdbc-config").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jdbcUrl\":\"jdbc:oracle:thin:@host:1521:XE\"," +
                         "\"username\":\"scott\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configId").exists());
    }

    @Test
    @WithMockUser
    void jdbcConfig_invalidPackage_returns400() throws Exception {
        mockMvc.perform(post("/api/migrations/jdbc-config").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"jdbcUrl\":\"jdbc:oracle:thin:@host:1521:XE\"," +
                         "\"username\":\"scott\",\"password\":\"secret\"," +
                         "\"targetPackage\":\"Invalid.Package\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
