package com.plsql2java.reporting;

import com.plsql2java.model.OracleObjectType;
import com.plsql2java.reporting.model.TraceabilityEntry;
import com.plsql2java.reporting.model.TraceabilityMatrix;
import com.plsql2java.reporting.model.TraceabilityStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TraceabilityMatrixTest {

    @Test
    void coveragePct_calculatedCorrectly() {
        List<TraceabilityEntry> entries = List.of(
                entry("A", TraceabilityStatus.MIGRATED),
                entry("B", TraceabilityStatus.PARTIAL),
                entry("C", TraceabilityStatus.FLAGGED),
                entry("D", TraceabilityStatus.SKIPPED)
        );
        TraceabilityMatrix matrix = new TraceabilityMatrix(entries);
        assertThat(matrix.getCoveragePct()).isEqualTo(50.0);
    }

    @Test
    void coveragePct_100_whenEmpty() {
        TraceabilityMatrix matrix = new TraceabilityMatrix(List.of());
        assertThat(matrix.getCoveragePct()).isEqualTo(100.0);
    }

    @Test
    void coveragePct_100_whenAllMigrated() {
        List<TraceabilityEntry> entries = List.of(
                entry("A", TraceabilityStatus.MIGRATED),
                entry("B", TraceabilityStatus.MIGRATED)
        );
        assertThat(new TraceabilityMatrix(entries).getCoveragePct()).isEqualTo(100.0);
    }

    private TraceabilityEntry entry(String name, TraceabilityStatus status) {
        return new TraceabilityEntry(name, OracleObjectType.PROCEDURE, null, List.of(), status, 80);
    }
}
