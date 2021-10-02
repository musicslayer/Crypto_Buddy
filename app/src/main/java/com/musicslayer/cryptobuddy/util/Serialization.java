package com.musicslayer.cryptobuddy.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Serialization {
    // Use an integer string to keep track of different data formats.
    public final static String SERIALIZATION_VERSION_MARKER = "!SERIALIZATION_VERSION!";

    // Any class implementing this can be serialized and deserialized with JSON.
    public interface SerializableToJSON {
        String serializationVersion(); // Can be different for every individual class that implements this interface.
        String serializeToJSON();
    }

    public static <T extends SerializableToJSON> String serialize(T obj) {
        String s = obj.serializeToJSON();

        // Add the version to every individual object that we serialize, or error if we cannot.
        try {
            JSONObject o = new JSONObject(s);
            o.put(SERIALIZATION_VERSION_MARKER, obj.serializationVersion());
            s = o.toString();
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
            throw new IllegalStateException();
        }

        return s;
    }

    public static <T extends SerializableToJSON> T deserialize(String s, Class<T> clazz) {
        // First try to get the version number. If we cannot, then error.
        String version;
        try {
            JSONObject o = new JSONObject(s);
            version = o.getString(SERIALIZATION_VERSION_MARKER);
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
            throw new IllegalStateException();
        }

        try {
            return Reflect.callStaticMethod(clazz, "deserializeFromJSON" + version, s);
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
