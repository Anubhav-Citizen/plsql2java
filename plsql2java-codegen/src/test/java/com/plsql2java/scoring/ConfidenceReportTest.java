package com.plsql2java.scoring;

import com.plsql2java.model.OracleObjectType;
import com.plsql2java.scoring.model.ConfidenceReport;
import com.plsql2java.scoring.model.MethodConfidenceScore;
import com.plsql2java.scoring.model.ObjectConfidenceScore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConfidenceReportTest {

    @Test
    void flaggedObjectCount_derivedFromObjectScores() {
        MethodConfidenceScore m1 = new MethodConfidenceScore("OBJ", "method1", 80, false, List.of());
        MethodConfidenceScore m2 = new MethodConfidenceScore("OBJ2", "method2", 50, true, List.of("GOTO not supported"));

        ObjectConfidenceScore o1 = new ObjectConfidenceScore("OBJ", OracleObjectType.PROCEDURE, 80, false, List.of(m1), false);
        ObjectConfidenceScore o2 = new ObjectConfidenceScore("OBJ2", OracleObjectType.FUNCTION, 50, true, List.of(m2), false);

        ConfidenceReport report = new ConfidenceReport("m1", 70, List.of(o1, o2), 65);

        assertThat(report.getFlaggedObjectCount()).isEqualTo(1);
        assertThat(report.getFlaggedMethodCount()).isEqualTo(1);
        assertThat(report.getScoredAt()).isNotNull();
    }

    @Test
    void flaggedCounts_zeroWhenAllAboveThreshold() {
        ObjectConfidenceScore o = new ObjectConfidenceScore("OBJ", OracleObjectType.PACKAGE, 90, false, List.of(), false);
        ConfidenceReport report = new ConfidenceReport("m1", 70, List.of(o), 90);
        assertThat(report.getFlaggedObjectCount()).isZero();
        assertThat(report.getFlaggedMethodCount()).isZero();
    }
}
