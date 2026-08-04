package com.plsql2java.orchestration;

import com.plsql2java.orchestration.model.MigrationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationResultTest {

    @Test
    void isPartial_trueWhenSkippedObjectsNonEmpty() {
        MigrationResult result = new MigrationResult("m1", null, List.of(), null, null, null, List.of("FAILED_OBJ"));
        assertThat(result.isPartial()).isTrue();
    }

    @Test
    void isPartial_falseWhenSkippedObjectsEmpty() {
        MigrationResult result = new MigrationResult("m1", null, List.of(), null, null, null, List.of());
        assertThat(result.isPartial()).isFalse();
    }

    @Test
    void completedAt_setOnConstruction() {
        MigrationResult result = new MigrationResult("m1", null, List.of(), null, null, null, List.of());
        assertThat(result.getCompletedAt()).isNotNull();
    }
}
