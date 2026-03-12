package pl.srozga.gluqalc_api.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues();

        RedisCacheConfiguration localSearchConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues();
        RedisCacheConfiguration searchConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(3))
                .disableCachingNullValues();
        RedisCacheConfiguration barcodeConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(3))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("local_search_cache", localSearchConfig)
                .withCacheConfiguration("off_search_cache", searchConfig)
                .withCacheConfiguration("off_barcode_cache", barcodeConfig)
                .build();
    }
}
