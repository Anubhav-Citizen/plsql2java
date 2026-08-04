package com.plsql2java.orchestration;

import com.plsql2java.orchestration.event.ProgressEventBus;
import com.plsql2java.orchestration.model.MigrationProgress;
import com.plsql2java.orchestration.model.PipelineStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ProgressEventBusTest {

    private ProgressEventBus bus;

    @BeforeEach
    void setUp() {
        bus = new ProgressEventBus();
    }

    @Test
    void emit_reachesAllRegisteredListeners() {
        List<MigrationProgress> received = new ArrayList<>();
        bus.register(received::add);
        bus.register(received::add);

        bus.emit(MigrationProgress.stageStart("m1", PipelineStage.DISCOVERY));
        assertThat(received).hasSize(2);
    }

    @Test
    void emit_brokenListenerDoesNotAbortOthers() {
        List<MigrationProgress> received = new ArrayList<>();
        bus.register(p -> { throw new RuntimeException("broken listener"); });
        bus.register(received::add);

        assertThatNoException().isThrownBy(() ->
                bus.emit(MigrationProgress.stageStart("m1", PipelineStage.TRANSLATION)));
        assertThat(received).hasSize(1);
    }

    @Test
    void unregister_removesListener() {
        List<MigrationProgress> received = new ArrayList<>();
        ProgressEventBus.MigrationProgressListener listener = received::add;
        bus.register(listener);
        bus.unregister(listener);

        bus.emit(MigrationProgress.stageStart("m1", PipelineStage.COMPLETE));
        assertThat(received).isEmpty();
    }

    @Test
    void emit_noListeners_doesNotThrow() {
        assertThatNoException().isThrownBy(() ->
                bus.emit(MigrationProgress.stageStart("m1", PipelineStage.DISCOVERY)));
    }
}
