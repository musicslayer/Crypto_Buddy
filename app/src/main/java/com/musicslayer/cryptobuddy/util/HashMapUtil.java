package com.musicslayer.cryptobuddy.util;

import java.util.ArrayList;
import java.util.HashMap;

// We need these methods because HashMap isn't using the equals method as we expect.
public class HashMapUtil {
    public static <T, U> void putValueInMap(HashMap<T, U> map, T desiredKey, U desiredValue) {
        // We need this because HashMap isn't using the equals method as we expect.
        removeValueFromMap(map, desiredKey);
        map.put(desiredKey, desiredValue);
    }

    public static <T, U> U getValueFromMap(HashMap<T, U> map, T desiredKey) {
        // We need this because HashMap isn't using the equals method as we expect.
        for(T key : new ArrayList<>(map.keySet())) {
            if(key.equals(desiredKey)) {
                return map.get(key);
            }
        }
        return null;
    }

    public static <T, U> void removeValueFromMap(HashMap<T, U> map, T desiredKey) {
        // We need this because HashMap isn't using the equals method as we expect.
        for(T key : new ArrayList<>(map.keySet())) {
            if(key.equals(desiredKey)) {
                map.remove(key);
            }
        }
    }
}
