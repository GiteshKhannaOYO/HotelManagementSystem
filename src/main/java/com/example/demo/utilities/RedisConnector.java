package com.example.demo.utilities ;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.awt.*;

@Configuration
public class RedisConnector {
//
//    @Value("${redis.url}")
//    private String redisUrl;

    @Bean("redisConnection")
    public StatefulRedisClusterConnection<String, String> createConnection() {

        System.out.println("redis");

        RedisClusterClient client = RedisClusterClient.create("redis://localhost:30001");
        StatefulRedisClusterConnection<String, String> connection = client.connect();
        //     System.out.println("redis connection created");

        System.out.println(connection);

        return connection;

    }








//    @Override
//    public void destroy() throws Exception {
//
//        client.shutdown();
//    }




}