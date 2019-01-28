package com.example.demo.services ;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;


import io.lettuce.core.KeyValue;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RedisService {

    @Qualifier("redisConnection")
    @Autowired
    StatefulRedisClusterConnection<String, String> redisConnection;

    public void set(String key, Object object) {

        try{

            Gson gson = new Gson();
            redisConnection.sync().set(key,gson.toJson(object));

        } catch (Exception e) {

            System.out.println("Redis set failed for key: " + key + ", exception: ");

        }

    }

    public ArrayList<Pair<Long,Long>> get(String key){

        ArrayList<Pair<Long,Long>> set = new ArrayList<Pair<Long,Long>>();

        try{

            ObjectMapper objectMapper = new ObjectMapper();

            String json = redisConnection.sync().get(key);
            set = objectMapper.readValue(json, new TypeReference<ArrayList<Pair<Long,Long>>>(){} );


        } catch (Exception e) {
            System.out.println("Redis set failed for key: " + key + ", exception: "+ e);

        }

        return set;

    }


    public void hmset(String key, Map<Long, ArrayList<Pair<Long,Long>>> fieldMap, int ttlseconds) {



        try{
             setKeyExpiry("TrendingHotels",ttlseconds);
                    Gson gson = new Gson();
                     redisConnection.sync().hmset(key, fieldMap.entrySet().stream()
                    .filter(e -> Objects.nonNull(e.getValue()))
                    .collect(Collectors.toMap(e -> e.getKey().toString(), e -> gson.toJson(e.getValue()))));


        } catch (Exception e) {


            System.out.println("Redis hmset failed for key {}, exception: {}  " +  e);

        }


    }


    public Map<Long, ArrayList<Pair<Long,Long> > > hgetall(String key) {

        Map<Long, ArrayList<Pair<Long,Long> > > finalResponse = new LinkedHashMap<>();

        Gson gson = new Gson();

        try {

            Map< String , String > response = redisConnection.sync().hgetall(key);

            if (!CollectionUtils.isEmpty(response)) {
                for (Map.Entry<String, String> entry : response.entrySet()) {

                    //     TypeToken<ArrayList<Pair<Long,Long>> > typeToken = new TypeToken<ArrayList<Pair<Long,Long>>>() {};
                    //  TypeReference<ArrayList<Pair<Long,Long>> > typeReference = new TypeReference<ArrayList<Pair<Long,Long>>>() {};

                    finalResponse.put(Long.parseLong( entry.getKey() ) , gson.fromJson(entry.getValue(), new TypeReference<ArrayList<Pair<Long,Long>>>() {}.getType()));

                }
            }


        } catch (Exception e) {
            System.out.println("Redis hmget failed for key: {}, hashFields : {}, exception: {} "+ e);
        }

        return finalResponse;

    }



    public Map<Long, ArrayList<Pair<Long,Long> > > hmget(String key,String[] hashfields) {

        Map<Long, ArrayList<Pair<Long,Long> > > finalResponse = new LinkedHashMap<>();

        try {

            List< KeyValue<String , String> > response = redisConnection.sync().hmget(key,hashfields);

            for(int i=0;i< response.size(); i++){

                if(response.get(i).hasValue()) {

                    Long cityId = Long.parseLong(response.get(i).getKey());

                    ObjectMapper objectMapper = new ObjectMapper();

                    String json = response.get(i).getValue();

                    ArrayList<Pair<Long,Long>> list = objectMapper.readValue(json, new TypeReference<ArrayList<Pair<Long,Long>>>() {
                    });

                    finalResponse.put(cityId, list);

                }

            }


        } catch (Exception e) {
            System.out.println("Redis hmget failed for key: {}, hashFields : {}, exception: {} "+ e);
        }

        return finalResponse;

    }

    private void setKeyExpiry(String key, int ttlSeconds) {
        try {
            if (ttlSeconds > 0) {
                redisConnection.async().expire(key, ttlSeconds);
            }
        } catch (Exception e) {
            System.out.println("Exception in redis expiry command for key {}, exception: {}") ;
        }
    }





}