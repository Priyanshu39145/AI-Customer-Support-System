package com.Spring.AI_Customer_Support_Backend_System.Configuration;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class RateLimitConfig {
    //We take value from application.properties --- with default value localhost
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    //We build a RedisClient bean with the redisHost and redisPort ---
    @Bean
    public RedisClient redisClient()    {
        log.info("Initializing RedisClient with host: {} and port: {}", redisHost, redisPort);
        return RedisClient.create(
                RedisURI.builder()
                        .withHost(redisHost)
                        .withPort(redisPort)
                        .build()
        );
    }

    @Bean
    public ProxyManager<String> proxyManager(RedisClient redisClient)  {
        //Bucket4j stores bucket state in redis ----
        //We here define how values will be encoded before storing
        //Here we are encoding the keys as String --- and we are encoding the values as ByteArray ---
        //Bucket key will be readable like IP Address and Bucket Data is stored as Binary
        //So this is the redisConnection that Bucket4j can use ---
        //Bucket4j will store the IP Address of the requester and the values in binary ---
        log.info("Initializing Bucket4j ProxyManager with Redis backend");
        var redisConnection = redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        );

        //Define TTL or Expiration ---
        //Without expiration, Redis will slowly fill up with bucket keys
        //If we rate limit by IP --- every new IP will create a new entry in Redis ---
        // if those IPs never come back we dont want to leave the keys inside Redis forever ---
        //So we want to expire the keys after some time ---
        //After 20 minutes keys will expire
        var expirationStrategy = ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(20));
        //we need to apply this strategy into Bucket4j client configuration --
        log.info("Rate limiting bucket TTL set to 20 minutes");
        var clientConfig = ClientSideConfig.getDefault()
                .withExpirationAfterWriteStrategy(expirationStrategy);

        //Build the ProxyManager ---
        //this Proxy Manager will use redisConnection for storage backend --- and with the clientsideConfig containing the bucket4j strategy we will return it
        return LettuceBasedProxyManager.builderFor(redisConnection).withClientSideConfig(clientConfig).build();

    }
}
