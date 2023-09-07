package com.block.chain.utils;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ThreadMapCache {

    private static final ConcurrentHashMap<String, Integer> highestMap = new ConcurrentHashMap<>();

    public static void updateHighest(Map<String , Integer> map){
        System.out.println("修改前Map" + highestMap.toString());
        map.forEach((key,value)->{
            highestMap.put(key,value);
        });
        System.out.println("修改后Map" + highestMap.toString());
    }

    public static Integer getHighestMap(String key){
        return highestMap.get(key);
    }
}
