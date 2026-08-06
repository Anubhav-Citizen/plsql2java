package com.plsql2java.orchestration;

import com.plsql2java.orchestration.model.MigrationProgress;
import com.plsql2java.orchestration.model.PipelineStage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationProgressTest {

    @Test
    void pct_calculatedCorrectly() {
        MigrationProgress p = new MigrationProgress("m1", PipelineStage.TRANSLATION, "OBJ", 50, 100, 50, "msg");
        assertThat(p.getPct()).isEqualTo(50);
    }

    @Test
    void pct_clampedToZero_whenNegative() {
        MigrationProgress p = new MigrationProgress("m1", PipelineStage.DISCOVERY, null, 0, 0, -5, "msg");
        assertThat(p.getPct()).isEqualTo(0);
    }

    @Test
    void pct_cappedAt100() {
        MigrationProgress p = new MigrationProgress("m1", PipelineStage.TRANSLATION, "OBJ", 200, 100, 150, "msg");
        assertThat(p.getPct()).isEqualTo(100);
    }

    @Test
    void stageStart_setsCorrectStage() {
        MigrationProgress p = MigrationProgress.stageStart("m1", PipelineStage.CODE_GENERATION, 65);
        assertThat(p.getStage()).isEqualTo(PipelineStage.CODE_GENERATION);
        assertThat(p.getTimestamp()).isNotNull();
    }
}
