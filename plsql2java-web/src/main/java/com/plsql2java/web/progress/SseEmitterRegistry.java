package com.plsql2java.web.progress;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseEmitterRegistry {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterRegistry.class);
    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000L;

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    /** Buffered events per job for clients that connect after events were emitted. */
    private final ConcurrentHashMap<String, List<BufferedEvent>> buffers = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public SseEmitterRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Called when a job is created — initialises the event buffer. */
    public void initBuffer(String jobId) {
        buffers.put(jobId, new CopyOnWriteArrayList<>());
    }

    /** Called when the browser connects to the /events endpoint. Replays buffered events. */
    public SseEmitter register(String jobId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitter.onCompletion(() -> emitters.remove(jobId));
        emitter.onTimeout(() -> emitters.remove(jobId));
        emitter.onError(e -> emitters.remove(jobId));
        emitters.put(jobId, emitter);

        List<BufferedEvent> buffered = buffers.get(jobId);
        if (buffered != null && !buffered.isEmpty()) {
            boolean terminal = false;
            for (BufferedEvent ev : buffered) {
                try {
                    emitter.send(SseEmitter.event().name(ev.type()).data(ev.json()));
                    if (ev.type().equals("complete") || ev.type().equals("error")) terminal = true;
                } catch (IOException e) {
                    log.debug("Replay send failed for job {}: {}", jobId, e.getMessage());
                    emitters.remove(jobId);
                    return emitter;
                }
            }
            // If a terminal event was replayed, close the stream so the browser isn't left pending
            if (terminal) {
                emitters.remove(jobId);
                emitter.complete();
            }
        }
        return emitter;
    }

    public void emit(String jobId, String eventType, Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            // Buffer first so late-connecting clients get the full history
            List<BufferedEvent> buf = buffers.get(jobId);
            if (buf != null) buf.add(new BufferedEvent(eventType, json));

            SseEmitter emitter = emitters.get(jobId);
            if (emitter == null) return;
            emitter.send(SseEmitter.event().name(eventType).data(json));
        } catch (IOException | IllegalStateException e) {
            log.debug("SSE send failed for job {}, removing emitter: {}", jobId, e.getMessage());
            emitters.remove(jobId);
        }
    }

    public void complete(String jobId) {
        // Buffer a synthetic complete event so late-connecting clients see it on replay
        List<BufferedEvent> buf = buffers.get(jobId);
        if (buf != null) buf.add(new BufferedEvent("complete", "{\"status\":\"COMPLETE\"}"));

        SseEmitter emitter = emitters.remove(jobId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("complete").data("{\"status\":\"COMPLETE\"}"));
            } catch (IOException ignored) {}
            emitter.complete();
        }
    }

    public void error(String jobId, String message) {
        List<BufferedEvent> buf = buffers.get(jobId);
        if (buf != null) buf.add(new BufferedEvent("error", "{\"error\":\"Migration failed\"}"));

        SseEmitter emitter = emitters.remove(jobId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("error").data("{\"error\":\"Migration failed\"}"));
            } catch (IOException ignored) {}
            emitter.complete();
        }
    }

    private record BufferedEvent(String type, String json) {}
}
