package com.musicslayer.cryptobuddy.util;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Serialization {
    public final static String SERIALIZATION_NULL_STRING = "!SERIALIZATION_NULL!";
    public final static String SERIALIZATION_NULL_OBJECT = "{\"" + SERIALIZATION_NULL_STRING + "\":\"" + SERIALIZATION_NULL_STRING + "\"}";
    public final static String SERIALIZATION_NULL_ARRAY = "[\"" + SERIALIZATION_NULL_STRING + "\"]";
    public final static String SERIALIZATION_NULL_MAP = "{\"keys\":" + SERIALIZATION_NULL_ARRAY + ",\"values\":" + SERIALIZATION_NULL_ARRAY + "}";

    public final static String SERIALIZATION_VERSION_MARKER = "!SERIALIZATION_VERSION!";

    // Any class implementing this can be serialized and deserialized with JSON.
    public interface SerializableToJSON {
        // Can be different for every individual class that implements this interface.
        String serializationVersion();

        // Classes also need to implement a static method "deserializeFromJSON" + VERSION for each version they support.
        // "serializeToJSON" is always assumed to be the latest version for that class.
        String serializeToJSON() throws org.json.JSONException;
    }

    // Methods for objects that implement "SerializableToJSON".
    public static <T extends SerializableToJSON> String serialize(T obj) {
        if(obj == null) { return SERIALIZATION_NULL_OBJECT; }

        String s;
        try {
            s = obj.serializeToJSON();
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
            throw new IllegalStateException();
        }

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

    public static <T extends SerializableToJSON> T deserialize(String s, Class<T> clazzT) {
        if(SERIALIZATION_NULL_OBJECT.equals(s)) { return null; }

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
            return Reflect.callStaticMethod(clazzT, "deserializeFromJSON" + version, s);
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
            return null;
        }
    }

    public static <T extends SerializableToJSON> String serializeArrayList(ArrayList<T> arrayList) {
        if(arrayList == null) { return SERIALIZATION_NULL_ARRAY; }

        try {
            JSONArray a = new JSONArray();
            for(T t : arrayList) {
                a.put(new JSONObject(Serialization.serialize(t)));
            }

            return a.toString();
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
            throw new IllegalStateException();
        }
    }

    public static <T extends SerializableToJSON> ArrayList<T> deserializeArrayList(String s, Class<T> clazzT) {
        if(SERIALIZATION_NULL_ARRAY.equals(s)) { return null; }

        try {
            ArrayList<T> arrayList = new ArrayList<>();

            JSONArray a = new JSONArray(s);
            for(int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                arrayList.add(Serialization.deserialize(o.toString(), clazzT));
            }

            return arrayList;
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
            return null;
        }
    }

    public static <T extends SerializableToJSON, U extends SerializableToJSON> String serializeHashMap(HashMap<T, U> hashMap) {
        if(hashMap == null) { return SERIALIZATION_NULL_MAP; }

        // Serialize a hashmap as an array of keys and an array of values, in the same order.
        ArrayList<T> keyArrayList = new ArrayList<>(hashMap.keySet());
        ArrayList<U> valueArrayList = new ArrayList<>();
        for(T key : keyArrayList) {
            valueArrayList.add(hashMap.get(key));
        }

        try {
            return new JSONObject()
                .put("keys", new JSONArray(Serialization.serializeArrayList(keyArrayList)))
                .put("values", new JSONArray(Serialization.serializeArrayList(valueArrayList)))
                .toString();
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
            throw new IllegalStateException();
        }
    }

    public static <T extends SerializableToJSON, U extends SerializableToJSON> HashMap<T, U> deserializeHashMap(String s, Class<T> clazzT, Class<U> clazzU) {
        if(SERIALIZATION_NULL_MAP.equals(s)) { return null; }

        try {
            JSONObject o = new JSONObject(s);

            ArrayList<T> arrayListT = Serialization.deserializeArrayList(o.getJSONArray("keys").toString(), clazzT);
            ArrayList<U> arrayListU = Serialization.deserializeArrayList(o.getJSONArray("values").toString(), clazzU);

            if(arrayListT == null || arrayListU == null || arrayListT.size() != arrayListU.size()) {
                return null;
            }

            HashMap<T, U> hashMap = new HashMap<>();
            for(int i = 0; i < arrayListT.size(); i++) {
                hashMap.put(arrayListT.get(i), arrayListU.get(i));
            }

            return hashMap;
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
            return null;
        }
    }

    // Methods for types that do not implement the interface and can be serialized into a single string.

    // Individual Strings cannot be null, or else there could be a conflict with the null marker String that we use to denote null objects.
    public static String string_serialize(@NonNull String obj) {
        return obj;
    }

    @NonNull
    public static String string_deserialize(String s) {
        return s;
    }

    public static String string_serializeArrayList(ArrayList<String> arrayList) {
        if(arrayList == null) { return SERIALIZATION_NULL_ARRAY; }

        JSONArray a = new JSONArray();
        for(String s : arrayList) {
            a.put(Serialization.string_serialize(s));
        }
        return a.toString();
    }

    public static ArrayList<String> string_deserializeArrayList(String s) {
        if(SERIALIZATION_NULL_ARRAY.equals(s)) { return null; }

        try {
            ArrayList<String> arrayList = new ArrayList<>();

            JSONArray a = new JSONArray(s);
            for(int i = 0; i < a.length(); i++) {
                String o = a.getString(i);
                arrayList.add(Serialization.string_deserialize(o));
            }

            return arrayList;
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
            return null;
        }
    }

    public static String boolean_serialize(boolean b) {
        return string_serialize(Boolean.toString(b));
    }

    public static boolean boolean_deserialize(String s) {
        return Boolean.parseBoolean(string_deserialize(s));
    }

    public static String date_serialize(Date obj) {
        return obj == null ? SERIALIZATION_NULL_STRING : string_serialize(Long.toString(obj.getTime()));
    }

    public static Date date_deserialize(String s) {
        return SERIALIZATION_NULL_STRING.equals(s) ? null : new Date(Long.parseLong(string_deserialize(s)));
    }

    public static String bigdecimal_serialize(BigDecimal obj) {
        return obj == null ? SERIALIZATION_NULL_STRING : string_serialize(obj.toString());
    }

    public static BigDecimal bigdecimal_deserialize(String s) {
        return SERIALIZATION_NULL_STRING.equals(s) ? null : new BigDecimal(string_deserialize(s));
    }
}
