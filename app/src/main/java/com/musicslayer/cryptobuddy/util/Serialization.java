package com.musicslayer.cryptobuddy.util;

// TODO Have versions of serialization here?

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Serialization<T> {
    // Any class implementing this can be serialized and deserialized with JSON.
    public interface SerializableToJSON {
        String serializeToJSON();

        // Classes that implement this interface should implement this themselves.
        static <T extends SerializableToJSON> T deserializeFromJSON(String s) throws org.json.JSONException {
            return null;
        }
    }

    public static <T extends SerializableToJSON> String serialize(T obj) {
        return obj.serializeToJSON();
    }

    public static <T extends SerializableToJSON> T deserialize(String s, Class<T> clazz) {
        try {
            return Reflect.callStaticMethod(clazz, "deserializeFromJSON", s);
        }
        catch(Exception e) {
            return null;
        }
    }

    public static <T extends SerializableToJSON> String serializeArrayList(ArrayList<T> arrayList) {
        StringBuilder s = new StringBuilder();
        s.append("[");

        for(int i = 0; i < arrayList.size(); i++) {
            s.append(Serialization.serialize(arrayList.get(i)));

            if(i < arrayList.size() - 1) {
                s.append(",");
            }
        }

        s.append("]");
        return s.toString();
    }

    public static <T extends SerializableToJSON> ArrayList<T> deserializeArrayList(String s, Class<T> clazz) {
        try {
            ArrayList<T> arrayList = new ArrayList<>();

            JSONArray a = new JSONArray(s);
            for(int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                arrayList.add(Serialization.deserialize(o.toString(), clazz));
            }

            return arrayList;
        }
        catch(Exception e) {
            return null;
        }
    }
}
