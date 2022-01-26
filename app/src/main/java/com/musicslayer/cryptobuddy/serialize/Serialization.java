package com.musicslayer.cryptobuddy.serialize;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.util.ReflectUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

// Note: Serialization has to be perfect, or we throw errors. There are no "default" or "fallback" values here.

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
        private JSONObject jsonObject;

        public JSONObjectWithNull() {
            jsonObject = new JSONObject();
        }

        public JSONObjectWithNull(String s) throws org.json.JSONException {
            jsonObject = s == null ? null : new JSONObject(s);
        }

        public JSONObjectWithNull(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public String toStringOrNull() {
            return jsonObject == null ? null : jsonObject.toString();
        }

        public String getString(String key) throws org.json.JSONException {
            return (String)(jsonObject.get(key) instanceof String ? jsonObject.get(key) : null);
        }

        public String getJSONObjectString(String key) throws org.json.JSONException {
            return getJSONObject(key).toStringOrNull();
        }

        public String getJSONArrayString(String key) throws org.json.JSONException {
            return getJSONArray(key).toStringOrNull();
        }

        public JSONObjectWithNull put(String key, String s) throws org.json.JSONException {
            jsonObject = s == null ? jsonObject.put(key, JSONObject.NULL) : jsonObject.put(key, s);
            return this;
        }

        public JSONObjectWithNull put(String key, JSONObjectWithNull obj) throws org.json.JSONException {
            jsonObject = obj.jsonObject == null ? jsonObject.put(key, JSONObject.NULL) : jsonObject.put(key, obj.jsonObject);
            return this;
        }

        public JSONObjectWithNull put(String key, JSONArrayWithNull arr) throws org.json.JSONException {
            jsonObject = arr.jsonArray == null ? jsonObject.put(key, JSONObject.NULL) : jsonObject.put(key, arr.jsonArray);
            return this;
        }

        private JSONObjectWithNull getJSONObject(String key) throws org.json.JSONException {
            return new JSONObjectWithNull((JSONObject)(jsonObject.get(key) instanceof JSONObject ? jsonObject.get(key) : null));
        }

        private JSONArrayWithNull getJSONArray(String key) throws org.json.JSONException {
            return new JSONArrayWithNull((JSONArray)(jsonObject.get(key) instanceof JSONArray ? jsonObject.get(key) : null));
        }
    }

    public static class JSONArrayWithNull {
        private JSONArray jsonArray;

        public JSONArrayWithNull() {
            jsonArray = new JSONArray();
        }

        public JSONArrayWithNull(String s) throws org.json.JSONException {
            jsonArray = s == null ? null : new JSONArray(s);
        }

        public JSONArrayWithNull(JSONArray jsonArray) {
            this.jsonArray = jsonArray;
        }

        public int length() {
            return jsonArray.length();
        }

        public String toStringOrNull() {
            return jsonArray == null ? null : jsonArray.toString();
        }

        public String getString(int i) throws org.json.JSONException {
            return jsonArray.get(i) instanceof String ? (String)jsonArray.get(i) : null;
        }

        public String getJSONObjectString(int i) throws org.json.JSONException {
            return getJSONObject(i).toStringOrNull();
        }

        public JSONArrayWithNull put(String s) {
            jsonArray = s == null ? jsonArray.put(JSONObject.NULL) : jsonArray.put(s);
            return this;
        }

        public JSONArrayWithNull put(JSONObjectWithNull obj) {
            jsonArray = obj.jsonObject == null ? jsonArray.put(JSONObject.NULL) : jsonArray.put(obj.jsonObject);
            return this;
        }

        public JSONArrayWithNull put(JSONArrayWithNull arr) {
            jsonArray = arr.jsonArray == null ? jsonArray.put(JSONObject.NULL) : jsonArray.put(arr.jsonArray);
            return this;
        }

        private JSONObjectWithNull getJSONObject(int i) throws org.json.JSONException {
            return new JSONObjectWithNull((JSONObject)(jsonArray.get(i) instanceof JSONObject ? jsonArray.get(i) : null));
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
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }

        // Add the version to every individual object that we serialize, or error if we cannot.
        try {
            JSONObjectWithNull o = new JSONObjectWithNull(s);
            o.put(SERIALIZATION_VERSION_MARKER, obj.serializationVersion());
            s = o.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }

        return s;
    }

    public static <T extends SerializableToJSON> T deserialize(String s, Class<T> clazzT) {
        if(s == null) { return null; }

        // First try to get the version number. If none is present, then the data is invalid.
        String version;
        try {
            JSONObjectWithNull o = new JSONObjectWithNull(s);
            version = o.getString(SERIALIZATION_VERSION_MARKER);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }

        // Call the appropriate deserialization method for the version number.
        try {
            return ReflectUtil.callStaticMethod(clazzT, "deserializeFromJSON" + version, s);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
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
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T extends SerializableToJSON> ArrayList<T> deserializeArrayList(String s, Class<T> clazzT) {
        if(s == null) { return null; }

        try {
            ArrayList<T> arrayList = new ArrayList<>();

            JSONArrayWithNull a = new JSONArrayWithNull(s);
            for(int i = 0; i < a.length(); i++) {
                arrayList.add(Serialization.deserialize(a.getJSONObjectString(i), clazzT));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
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
            return new JSONObjectWithNull()
                .put("keys", new JSONArrayWithNull(Serialization.serializeArrayList(keyArrayList)))
                .put("values", new JSONArrayWithNull(Serialization.serializeArrayList(valueArrayList)))
                .toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
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
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    // Methods for types that do not implement the interface and can be serialized into a single string.
    // These do not have any version information.
    public static String string_serialize(String obj) {
        return obj; // Same output for null and non-null
    }

    public static String string_deserialize(String s) {
        return s; // Same output for null and non-null
    }

    public static String string_serializeArrayList(ArrayList<String> arrayList) {
        if(arrayList == null) { return null; }

        try {
            JSONArrayWithNull a = new JSONArrayWithNull();
            for(String s : arrayList) {
                a.put(Serialization.string_serialize(s));
            }

            return a.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
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
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static String boolean_serialize(boolean b) {
        return string_serialize(Boolean.toString(b));
    }

    public static boolean boolean_deserialize(String s) {
        return Boolean.parseBoolean(string_deserialize(s));
    }

    public static String boolean_serializeArrayList(ArrayList<Boolean> arrayList) {
        if(arrayList == null) { return null; }

        try {
            JSONArrayWithNull a = new JSONArrayWithNull();
            for(boolean b : arrayList) {
                a.put(Serialization.boolean_serialize(b));
            }

            return a.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static ArrayList<Boolean> boolean_deserializeArrayList(String s) {
        if(s == null) { return null; }

        try {
            ArrayList<Boolean> arrayList = new ArrayList<>();

            JSONArrayWithNull a = new JSONArrayWithNull(s);
            for(int i = 0; i < a.length(); i++) {
                String o = a.getString(i);
                arrayList.add(Serialization.boolean_deserialize(o));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static String byte_serialize(byte b) {
        return string_serialize(Byte.toString(b));
    }

    public static byte byte_deserialize(String s) {
        return Byte.parseByte(string_deserialize(s));
    }

    public static String byte_serializeArray(byte[] array) {
        if(array == null) { return null; }

        try {
            JSONArrayWithNull a = new JSONArrayWithNull();
            for(byte b : array) {
                a.put(Serialization.byte_serialize(b));
            }

            return a.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static byte[] byte_deserializeArray(String s) {
        if(s == null) { return null; }

        try {
            JSONArrayWithNull a = new JSONArrayWithNull(s);
            byte[] array = new byte[a.length()];
            for(int i = 0; i < a.length(); i++) {
                String o = a.getString(i);
                array[i] = Serialization.byte_deserialize(o);
            }

            return array;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
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
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
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
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static String long_serialize(long l) {
        return string_serialize(Long.toString(l));
    }

    public static long long_deserialize(String s) {
        return Long.parseLong(string_deserialize(s));
    }

    public static String long_serializeArrayList(ArrayList<Long> arrayList) {
        if(arrayList == null) { return null; }

        try {
            JSONArrayWithNull a = new JSONArrayWithNull();
            for(long l : arrayList) {
                a.put(Serialization.long_serialize(l));
            }

            return a.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static ArrayList<Long> long_deserializeArrayList(String s) {
        if(s == null) { return null; }

        try {
            ArrayList<Long> arrayList = new ArrayList<>();

            JSONArrayWithNull a = new JSONArrayWithNull(s);
            for(int i = 0; i < a.length(); i++) {
                String o = a.getString(i);
                arrayList.add(Serialization.long_deserialize(o));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
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

    // Asset.serializeToJSON only serializes a key used to lookup an Asset later.
    // This method serializes all the information needed to reconstruct a token from scratch.
    public static String token_serialize(Token obj) {
        if(obj == null) { return null; }

        try {
            // Use original properties directly, not the potentially modified ones from getter functions.
            return new Serialization.JSONObjectWithNull()
                .put("key", string_serialize(obj.key))
                .put("name", string_serialize(obj.original_name))
                .put("display_name", string_serialize(obj.original_display_name))
                .put("scale", int_serialize(obj.scale))
                .put("id", string_serialize(obj.id))
                .put("blockchain_id", string_serialize(obj.blockchain_id))
                .put("token_type", string_serialize(obj.token_type))
                .toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static Token token_deserialize(String s) {
        if(s == null) { return null; }

        try {
            Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
            String key = Serialization.string_deserialize(o.getString("key"));
            String name = Serialization.string_deserialize(o.getString("name"));
            String display_name = Serialization.string_deserialize(o.getString("display_name"));
            int scale = Serialization.int_deserialize(o.getString("scale"));
            String id = Serialization.string_deserialize(o.getString("id"));
            String blockchain_id = Serialization.string_deserialize(o.getString("blockchain_id"));
            String token_type = Serialization.string_deserialize(o.getString("token_type"));
            return new Token(key, name, display_name, scale, id, blockchain_id, token_type);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static String token_serializeArrayList(ArrayList<Token> arrayList) {
        if(arrayList == null) { return null; }

        try {
            JSONArrayWithNull a = new JSONArrayWithNull();
            for(Token obj : arrayList) {
                a.put(Serialization.token_serialize(obj));
            }

            return a.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static ArrayList<Token> token_deserializeArrayList(String s) {
        if(s == null) { return null; }

        try {
            ArrayList<Token> arrayList = new ArrayList<>();

            JSONArrayWithNull a = new JSONArrayWithNull(s);
            for(int i = 0; i < a.length(); i++) {
                String o = a.getString(i);
                arrayList.add(Serialization.token_deserialize(o));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }
}
