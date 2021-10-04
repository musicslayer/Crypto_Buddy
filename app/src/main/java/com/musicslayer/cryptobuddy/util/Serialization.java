package com.musicslayer.cryptobuddy.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

// Note Serialization has to be perfect, or we throw errors. There are no "default" values here.

public class Serialization {
    // Keep this short because it will appear on every piece of stored data.
    public final static String SERIALIZATION_VERSION_MARKER = "!V!";

    // Any class implementing this can be serialized and deserialized with JSON.
    public interface SerializableToJSON {
        // Can be different for every individual class that implements this interface.
        String serializationVersion();

        // Classes also need to implement a static method "deserializeFromJSON" + VERSION for each version they support.
        // "serializeToJSON" is always assumed to be the latest version for that class.
        String serializeToJSON() throws org.json.JSONException;
    }

    // Classes to properly handle null String values.
    public static class JSONObjectWithNull {
        JSONObject o;

        public JSONObjectWithNull() {
            o = new JSONObject();
        }

        public JSONObjectWithNull(String s) throws org.json.JSONException {
            o = s == null ? null : new JSONObject(s);
        }

        public JSONObjectWithNull(JSONObject o) {
            this.o = o;
        }

        public String toStringOrNull() {
            return o == null ? null : o.toString();
        }

        public String getString(String key) throws org.json.JSONException {
            return (String)(o.get(key) instanceof String ? o.get(key) : null);
        }

        public JSONObjectWithNull getJSONObject(String key) throws org.json.JSONException {
            return new JSONObjectWithNull((JSONObject)(o.get(key) instanceof JSONObject ? o.get(key) : null));
        }

        public JSONArrayWithNull getJSONArray(String key) throws org.json.JSONException {
            return new JSONArrayWithNull((JSONArray)(o.get(key) instanceof JSONArray ? o.get(key) : null));
        }

        public JSONObjectWithNull put(String key, String s) throws org.json.JSONException {
            o = s == null ? o.put(key, JSONObject.NULL) : o.put(key, s);
            return this;
        }

        public JSONObjectWithNull put(String key, JSONObjectWithNull obj) throws org.json.JSONException {
            o = obj.o == null ? o.put(key, JSONObject.NULL) : o.put(key, obj.o);
            return this;
        }

        public JSONObjectWithNull put(String key, JSONArrayWithNull arr) throws org.json.JSONException {
            o = arr.a == null ? o.put(key, JSONObject.NULL) : o.put(key, arr.a);
            return this;
        }
    }

    public static class JSONArrayWithNull {
        JSONArray a;

        public JSONArrayWithNull() {
            a = new JSONArray();
        }

        public JSONArrayWithNull(String s) throws org.json.JSONException {
            a = s == null ? null : new JSONArray(s);
        }

        public JSONArrayWithNull(JSONArray a) {
            this.a = a;
        }

        public String toStringOrNull() {
            return a == null ? null : a.toString();
        }

        public String getString(int i) throws org.json.JSONException {
            return a.get(i) instanceof String ? (String)a.get(i) : null;
        }

        public int length() {
            return a.length();
        }

        public JSONObjectWithNull getJSONObject(int i) throws org.json.JSONException {
            return new JSONObjectWithNull((JSONObject)(a.get(i) instanceof JSONObject ? a.get(i) : null));
        }

        public JSONArrayWithNull put(String s) throws org.json.JSONException {
            a = s == null ? a.put(JSONObject.NULL) : a.put(s);
            return this;
        }

        public JSONArrayWithNull put(JSONObjectWithNull obj) throws org.json.JSONException {
            a = obj.o == null ? a.put(JSONObject.NULL) : a.put(obj.o);
            return this;
        }

        public JSONArrayWithNull put(JSONArrayWithNull arr) throws org.json.JSONException {
            a = arr.a == null ? a.put(JSONObject.NULL) : a.put(arr.a);
            return this;
        }
    }

    // Methods for objects that implement "SerializableToJSON".
    public static <T extends SerializableToJSON> String serialize(T obj) {
        if(obj == null) { return null; }

        String s;
        try {
            s = obj.serializeToJSON();
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            throw new IllegalStateException();
        }

        // Add the version to every individual object that we serialize, or error if we cannot.
        try {
            JSONObjectWithNull o = new JSONObjectWithNull(s);
            o.put(SERIALIZATION_VERSION_MARKER, obj.serializationVersion());
            s = o.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            throw new IllegalStateException();
        }

        return s;
    }

    public static <T extends SerializableToJSON> T deserialize(String s, Class<T> clazzT) {
        if(s == null) { return null; }

        // First try to get the version number. If we cannot, then error.
        String version;
        try {
            JSONObjectWithNull o = new JSONObjectWithNull(s);
            version = o.getString(SERIALIZATION_VERSION_MARKER);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            throw new IllegalStateException();
        }

        try {
            return Reflect.callStaticMethodOrError(clazzT, "deserializeFromJSON" + version, s);
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            throw new IllegalStateException();
        }
    }

    public static <T extends SerializableToJSON> String serializeArrayList(ArrayList<T> arrayList) {
        if(arrayList == null) { return null; }

        try {
            JSONArrayWithNull a = new JSONArrayWithNull();
            for(T t : arrayList) {
                a.put(new JSONObjectWithNull(Serialization.serialize(t)));
            }

            return a.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            throw new IllegalStateException();
        }
    }

    public static <T extends SerializableToJSON> ArrayList<T> deserializeArrayList(String s, Class<T> clazzT) {
        if(s == null) { return null; }

        try {
            ArrayList<T> arrayList = new ArrayList<>();

            JSONArrayWithNull a = new JSONArrayWithNull(s);
            for(int i = 0; i < a.length(); i++) {
                JSONObjectWithNull o = a.getJSONObject(i);
                arrayList.add(Serialization.deserialize(o.toStringOrNull(), clazzT));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            throw new IllegalStateException();
        }
    }

    public static <T extends SerializableToJSON, U extends SerializableToJSON> String serializeHashMap(HashMap<T, U> hashMap) {
        if(hashMap == null) { return null; }

        // Serialize a hashmap as an array of keys and an array of values, in the same order.
        ArrayList<T> keyArrayList = new ArrayList<>(hashMap.keySet());
        ArrayList<U> valueArrayList = new ArrayList<>();
        for(T key : keyArrayList) {
            valueArrayList.add(hashMap.get(key));
        }

        try {
            // Both arrays are non-null so cast to String to avoid needing a newer Android API.
            return new JSONObjectWithNull()
                .put("keys", new JSONArrayWithNull((String)Serialization.serializeArrayList(keyArrayList)))
                .put("values", new JSONArrayWithNull((String)Serialization.serializeArrayList(valueArrayList)))
                .toStringOrNull();
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            throw new IllegalStateException();
        }
    }

    public static <T extends SerializableToJSON, U extends SerializableToJSON> HashMap<T, U> deserializeHashMap(String s, Class<T> clazzT, Class<U> clazzU) {
        if(s == null) { return null; }

        try {
            JSONObjectWithNull o = new JSONObjectWithNull(s);

            ArrayList<T> arrayListT = Serialization.deserializeArrayList(o.getJSONArray("keys").toStringOrNull(), clazzT);
            ArrayList<U> arrayListU = Serialization.deserializeArrayList(o.getJSONArray("values").toStringOrNull(), clazzU);

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
            ThrowableLogger.processThrowable(e);
            throw new IllegalStateException();
        }
    }

    // Methods for types that do not implement the interface and can be serialized into a single string.
    public static String string_serialize(String obj) {
        return obj; // Same output for null and non-null
    }

    public static String string_deserialize(String s) {
        return s; // Same output for null and non-null
    }

    public static String string_serializeArrayList(ArrayList<String> arrayList) {
        if(arrayList == null) { return null; }

        try{
            JSONArrayWithNull a = new JSONArrayWithNull();
            for(String s : arrayList) {
                a.put(Serialization.string_serialize(s));
            }

            return a.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            throw new IllegalStateException();
        }
    }

    public static ArrayList<String> string_deserializeArrayList(String s) {
        if(s == null) { return null; }

        try {
            ArrayList<String> arrayList = new ArrayList<>();

            JSONArrayWithNull a = new JSONArrayWithNull(s);
            for(int i = 0; i < a.length(); i++) {
                String o = a.getString(i);
                arrayList.add(Serialization.string_deserialize(o));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            throw new IllegalStateException();
        }
    }

    public static String boolean_serialize(boolean b) {
        return string_serialize(Boolean.toString(b));
    }

    public static boolean boolean_deserialize(String s) {
        return Boolean.parseBoolean(string_deserialize(s));
    }

    public static String int_serialize(int i) {
        return string_serialize(Integer.toString(i));
    }

    public static int int_deserialize(String s) {
        return Integer.parseInt(string_deserialize(s));
    }

    public static String int_serializeArrayList(ArrayList<Integer> arrayList) {
        if(arrayList == null) { return null; }

        try {
            JSONArrayWithNull a = new JSONArrayWithNull();
            for(int i : arrayList) {
                a.put(Serialization.int_serialize(i));
            }

            return a.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            throw new IllegalStateException();
        }
    }

    public static ArrayList<Integer> int_deserializeArrayList(String s) {
        if(s == null) { return null; }

        try {
            ArrayList<Integer> arrayList = new ArrayList<>();

            JSONArrayWithNull a = new JSONArrayWithNull(s);
            for(int i = 0; i < a.length(); i++) {
                String o = a.getString(i);
                arrayList.add(Serialization.int_deserialize(o));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            throw new IllegalStateException();
        }
    }

    public static String date_serialize(Date obj) {
        return obj == null ? null : string_serialize(Long.toString(obj.getTime()));
    }

    public static Date date_deserialize(String s) {
        return s == null ? null : new Date(Long.parseLong(string_deserialize(s)));
    }

    public static String bigdecimal_serialize(BigDecimal obj) {
        return obj == null ? null : string_serialize(obj.toString());
    }

    public static BigDecimal bigdecimal_deserialize(String s) {
        return s == null ? null : new BigDecimal(string_deserialize(s));
    }
}
