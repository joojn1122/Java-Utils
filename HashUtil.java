package com.joojn.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class HashUtil {

    public static <T> int lengthWithoutNulls(T[] arr){
        int count = 0;
        for(T el : arr)
            if (el != null)
                ++count;
        return count;
    }

    public static <T> T getValueByKey(HashMap<T, ?> hashmap, Object value){
        for(T set : hashmap.keySet()){
            Object v = hashmap.get(set);
            if(v == value) return set;
        }
        return null;
    }

    public static <T> ArrayList<T> getValuesByKey(HashMap<T, ?> hashmap, Object value){
        ArrayList<T> values = new ArrayList<>();
        for(T set : hashmap.keySet()){
            Object v = hashmap.get(set);
            if(v == value) values.add(set);
        }
        return values;
    }

}
