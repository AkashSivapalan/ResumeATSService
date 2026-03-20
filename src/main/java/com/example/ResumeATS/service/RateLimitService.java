package com.example.ResumeATS.service;

import com.example.ResumeATS.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    @Value("${rate-limit.analyze.requests-per-day:10}")
    private int requestsPerDay;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public void checkAnalyzeLimit(String userId) {
        Bucket bucket = buckets.computeIfAbsent(userId, this::newBucket);
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException(
                "Daily analysis limit of " + requestsPerDay + " requests exceeded. Try again tomorrow."
            );
        }
    }

    private Bucket newBucket(String userId) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(requestsPerDay)
                .refillIntervally(requestsPerDay, Duration.ofDays(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}