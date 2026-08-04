package com.plsql2java.web;

import com.plsql2java.common.MigrationConfig;
import com.plsql2java.orchestration.model.MigrationJobStatus;
import com.plsql2java.web.model.MigrationJobState;
import com.plsql2java.web.service.MigrationJobRegistry;
import com.plsql2java.web.service.MigrationJobRegistry.JobNotFoundException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MigrationJobRegistryTest {

    private final MigrationJobRegistry registry = new MigrationJobRegistry();

    @Test
    void register_and_get_returnsState() {
        MigrationJobState state = new MigrationJobState("job-1", new MigrationConfig(), Path.of("/tmp"));
        registry.register(state);

        assertThat(registry.get("job-1")).isPresent().contains(state);
    }

    @Test
    void getOrThrow_unknownJob_throwsJobNotFoundException() {
        assertThatThrownBy(() -> registry.getOrThrow("unknown"))
                .isInstanceOf(JobNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void statusUpdate_reflectedInRegistry() {
        MigrationJobState state = new MigrationJobState("job-2", new MigrationConfig(), Path.of("/tmp"));
        registry.register(state);

        state.setStatus(MigrationJobStatus.COMPLETED);

        assertThat(registry.getOrThrow("job-2").getStatus()).isEqualTo(MigrationJobStatus.COMPLETED);
    }
}
