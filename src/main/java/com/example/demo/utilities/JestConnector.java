package com.example.demo.utilities;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



public class JestConnector {


    public JestClient makeJestClient() {

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder("http://localhost:9200").multiThreaded(true).defaultMaxTotalConnectionPerRoute(2).maxTotalConnection(20).build());
        JestClient client = factory.getObject();

        return client;
    }

}
