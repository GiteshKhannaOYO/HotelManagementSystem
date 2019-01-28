package com.example.demo.utilities ;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.awt.*;


@Component

@Configuration
public class RedisConnector {

    @Value("${redis.url}")
    private String redisUrl;

    @Bean("redisConnection")
    public StatefulRedisClusterConnection<String, String> createConnection() {


        RedisClusterClient client = RedisClusterClient.create( redisUrl );
        StatefulRedisClusterConnection<String, String> connection = client.connect();
        return connection;

    }


}