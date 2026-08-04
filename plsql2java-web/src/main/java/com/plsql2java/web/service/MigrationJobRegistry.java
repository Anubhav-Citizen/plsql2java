package com.plsql2java.web.service;

import com.plsql2java.web.model.MigrationJobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MigrationJobRegistry {

    private static final Logger log = LoggerFactory.getLogger(MigrationJobRegistry.class);
    private static final long JOB_TTL_HOURS = 1;

    private final ConcurrentHashMap<String, MigrationJobState> jobs = new ConcurrentHashMap<>();

    public void register(MigrationJobState state) {
        jobs.put(state.getJobId(), state);
    }

    public Optional<MigrationJobState> get(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    public MigrationJobState getOrThrow(String jobId) {
        return get(jobId).orElseThrow(() ->
                new JobNotFoundException("Job not found: " + jobId));
    }

    @Scheduled(fixedDelay = 600_000) // every 10 minutes
    public void cleanupExpiredJobs() {
        Instant cutoff = Instant.now().minus(JOB_TTL_HOURS, ChronoUnit.HOURS);
        jobs.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().getStartedAt().isBefore(cutoff);
            if (expired) log.debug("Removing expired job: {}", entry.getKey());
            return expired;
        });
    }

    public static class JobNotFoundException extends RuntimeException {
        public JobNotFoundException(String message) { super(message); }
    }
}
