package com.plsql2java.web;

import com.plsql2java.web.controller.MigrationController;
import com.plsql2java.web.packaging.ZipPackager;
import com.plsql2java.web.progress.SseEmitterRegistry;
import com.plsql2java.web.service.CredentialStore;
import com.plsql2java.web.service.FileUploadService;
import com.plsql2java.web.service.MigrationJobRegistry;
import com.plsql2java.web.service.MigrationJobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MigrationController.class)
class MigrationControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean FileUploadService uploadService;
    @MockBean MigrationJobService jobService;
    @MockBean MigrationJobRegistry jobRegistry;
    @MockBean SseEmitterRegistry sseRegistry;
    @MockBean ZipPackager zipPackager;
    @MockBean CredentialStore credentialStore;

    @Test
    @WithMockUser
    void upload_validFile_returns200WithUploadId() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "schema.sql", "text/plain", "CREATE TABLE t (id NUMBER);".getBytes());

        com.plsql2java.web.model.UploadedFile uploaded =
                new com.plsql2java.web.model.UploadedFile("uid-1", "schema.sql", "schema.sql",
                        java.nio.file.Path.of("/tmp/schema.sql"), 100L);
        when(uploadService.store(any())).thenReturn(uploaded);

        mockMvc.perform(multipart("/api/migrations/upload").file(file).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").value("uid-1"))
                .andExpect(jsonPath("$.filename").value("schema.sql"));
    }

    @Test
    void upload_unauthenticated_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "schema.sql", "text/plain", "SELECT 1;".getBytes());

        mockMvc.perform(multipart("/api/migrations/upload").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void analyze_withUploadId_returns202() throws Exception {
        when(jobService.submitAnalyze(any(), any())).thenReturn("job-123");

        mockMvc.perform(post("/api/migrations/analyze").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"uploadId\":\"uid-1\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("job-123"));
    }

    @Test
    @WithMockUser
    void analyze_noSource_returns400() throws Exception {
        mockMvc.perform(post("/api/migrations/analyze").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void generate_withUploadId_returns202() throws Exception {
        when(jobService.submitGenerate(any())).thenReturn("job-456");

        mockMvc.perform(post("/api/migrations/generate").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"uploadId\":\"uid-1\",\"targetPackage\":\"com.example\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("job-456"));
    }

    @Test
    @WithMockUser
    void status_unknownJob_returns404() throws Exception {
        when(jobRegistry.getOrThrow("unknown"))
                .thenThrow(new MigrationJobRegistry.JobNotFoundException("Job not found: unknown"));

        mockMvc.perform(get("/api/migrations/unknown/status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser
    void securityHeaders_presentOnAllResponses() throws Exception {
        when(jobService.submitAnalyze(any(), any())).thenReturn("job-789");

        mockMvc.perform(post("/api/migrations/analyze").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"uploadId\":\"uid-1\"}"))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"));
    }
}
