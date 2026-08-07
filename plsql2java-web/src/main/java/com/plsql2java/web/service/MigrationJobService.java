package com.plsql2java.web.service;

import com.plsql2java.common.MigrationConfig;
import com.plsql2java.web.model.GenerateRequest;
import com.plsql2java.web.model.MigrationJobState;
import com.plsql2java.web.progress.SseEmitterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class MigrationJobService {

    private final MigrationJobRunner jobRunner;
    private final SseEmitterRegistry sseRegistry;
    private final MigrationJobRegistry jobRegistry;
    private final FileUploadService uploadService;
    private final CredentialStore credentialStore;

    @Value("${plsql2java.output.dir:plsql2java-output}")
    private String outputBaseDir;

    public MigrationJobService(MigrationJobRunner jobRunner,
                                SseEmitterRegistry sseRegistry,
                                MigrationJobRegistry jobRegistry,
                                FileUploadService uploadService,
                                CredentialStore credentialStore) {
        this.jobRunner = jobRunner;
        this.sseRegistry = sseRegistry;
        this.jobRegistry = jobRegistry;
        this.uploadService = uploadService;
        this.credentialStore = credentialStore;
    }

    public String submitAnalyze(String uploadId, String configId) {
        String jobId = UUID.randomUUID().toString();
        MigrationConfig config = buildConfig(uploadId, configId, null, 0.7);
        Path outputDir = Path.of(outputBaseDir, jobId);
        config.setOutputDir(outputDir);

        MigrationJobState state = new MigrationJobState(jobId, config, outputDir);
        jobRegistry.register(state);
        sseRegistry.initBuffer(jobId);
        jobRunner.runAnalyze(jobId, state);  // async via proxy
        return jobId;
    }

    public String submitGenerate(GenerateRequest request) {
        String jobId = UUID.randomUUID().toString();
        MigrationConfig config = buildConfig(request.getUploadId(), request.getConfigId(),
                request.getTargetPackage(), request.getConfidenceThreshold());
        Path outputDir = Path.of(outputBaseDir, jobId);
        config.setOutputDir(outputDir);

        MigrationJobState state = new MigrationJobState(jobId, config, outputDir);
        jobRegistry.register(state);
        sseRegistry.initBuffer(jobId);
        jobRunner.runGenerate(jobId, state);  // async via proxy
        return jobId;
    }

    private MigrationConfig buildConfig(String uploadId, String configId,
                                         String targetPackage, double confidenceThreshold) {
        MigrationConfig config = new MigrationConfig();
        config.setTargetPackage(targetPackage != null ? targetPackage : "com.example.migrated");
        config.setConfidenceThreshold((int) (confidenceThreshold * 100));

        if (uploadId != null) {
            Path uploadDir = uploadService.resolve(uploadId);
            try (var stream = java.nio.file.Files.list(uploadDir)) {
                List<Path> ddlFiles = stream.filter(p -> p.toString().endsWith(".sql")).toList();
                config.setDdlFiles(ddlFiles);
                if (!ddlFiles.isEmpty() && config.getSchemaName() == null) {
                    String fname = ddlFiles.get(0).getFileName().toString();
                    String derived = fname.replaceAll("\\.sql$", "").replaceAll("[^A-Za-z0-9_]", "_").toUpperCase();
                    config.setSchemaName(derived);
                }
            } catch (java.io.IOException e) {
                throw new IllegalStateException("Cannot read upload directory: " + uploadId);
            }
        } else if (configId != null) {
            char[] password = credentialStore.retrieve(configId);
            credentialStore.clear(configId);
        }

        return config;
    }
}
