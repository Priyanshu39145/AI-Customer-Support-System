package com.Spring.AI_Customer_Support_Backend_System.Services;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitingService {
    //A single client allowed to make 10000 requests per minute --
    private static final int REQUEST_PER_MINUTE = 10000;

    //Storage and Manager for Buckets --- (IP-Address -> Bucket)
    private final ProxyManager<String> proxyManager;

    //This method will be called for every incoming request
    //The key here represents whatever we are rate limiting by --- like IP Address or UserId or something else
    //This method gives us the bucket for every request from where we can find out how many requests it is left to give --
    //We allow 10 requests per minute
    public Bucket resolveBucket(String key) {
        log.debug("Resolving bucket for key: {}", key);
        Supplier<BucketConfiguration> configSupplier = this::getConfig;
        //This just asks the Redis for the bucket of the given key
        //If it is there is given --- if it is not there then Bucket4j creates a new Bucket using getConfig and save it in Redis
        return proxyManager.builder()
                .build(key,configSupplier);
    }

    //This will act as a factory that produces BucketConfiguration objects for newly created bucket.
    //This is where we define actual rate limiting rule
    private BucketConfiguration getConfig() {
        //Rules:
        //1. How many tokens can a bucket hold?
        //2. How quickly tokens are refilled?
        log.info("Creating new bucket configuration: {} requests per minute", REQUEST_PER_MINUTE);
        var limit = Bandwidth.builder()
                .capacity(REQUEST_PER_MINUTE)//Here we give the max capacity ---
                .refillIntervally(REQUEST_PER_MINUTE, Duration.ofMinutes(1)) //Within 1 min the token will be fully regenerated
                //Or within 1 min only 10 requests allowed ---
                .build();

        return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }
}
//Done
