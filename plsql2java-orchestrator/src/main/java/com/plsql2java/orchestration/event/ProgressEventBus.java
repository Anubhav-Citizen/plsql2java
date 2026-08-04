package com.plsql2java.orchestration.event;

import com.plsql2java.common.ProgressListener;
import com.plsql2java.orchestration.model.MigrationProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ProgressEventBus {

    private static final Logger log = LoggerFactory.getLogger(ProgressEventBus.class);

    private final List<MigrationProgressListener> listeners = new CopyOnWriteArrayList<>();

    public void register(MigrationProgressListener listener) {
        listeners.add(listener);
    }

    public void unregister(MigrationProgressListener listener) {
        listeners.remove(listener);
    }

    public void emit(MigrationProgress event) {
        for (MigrationProgressListener listener : listeners) {
            try {
                listener.onProgress(event);
            } catch (Exception e) {
                log.warn("Progress listener {} threw exception (ignored): {}",
                        listener.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /** Functional interface for migration progress events. */
    @FunctionalInterface
    public interface MigrationProgressListener {
        void onProgress(MigrationProgress progress);
    }
}
