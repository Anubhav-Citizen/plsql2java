package com.plsql2java.web.progress;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterRegistry.class);
    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000L; // 5 minutes

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public SseEmitterRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SseEmitter register(String jobId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitter.onCompletion(() -> emitters.remove(jobId));
        emitter.onTimeout(() -> emitters.remove(jobId));
        emitter.onError(e -> emitters.remove(jobId));
        emitters.put(jobId, emitter);
        return emitter;
    }

    public void emit(String jobId, String eventType, Object data) {
        SseEmitter emitter = emitters.get(jobId);
        if (emitter == null) return;
        try {
            String json = objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event().name(eventType).data(json));
        } catch (IOException e) {
            log.debug("SSE send failed for job {}, removing emitter: {}", jobId, e.getMessage());
            emitters.remove(jobId);
        }
    }

    public void complete(String jobId) {
        SseEmitter emitter = emitters.remove(jobId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("complete").data("{\"status\":\"COMPLETE\"}"));
            } catch (IOException ignored) {}
            emitter.complete();
        }
    }

    public void error(String jobId, String message) {
        SseEmitter emitter = emitters.remove(jobId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("error").data("{\"error\":\"Migration failed\"}"));
            } catch (IOException ignored) {}
            emitter.complete();
        }
    }
}
