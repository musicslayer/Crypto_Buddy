package com.musicslayer.cryptobuddy.data;

import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.musicslayer.cryptobuddy.app.App;
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.util.ReflectUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.io.File;
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
        // "serializeToJSON" is always assumed to be the latest version for that class.
        String serializeToJSON() throws org.json.JSONException;

        // Classes also need to implement a static method "deserializeFromJSON".
    }

    // Any class implementing this supports versioning.
    // This is needed when saving data that may be loaded in a different release.
    public interface Versionable {
        // Can be different for every individual class that implements this interface.
        String serializationVersion();

        // Classes also need to implement a static method "deserializeFromJSON" + VERSION for each version they support.
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

        if(obj instanceof Versionable) {
            // Add the version to every individual object that we serialize, or error if we cannot.
            try {
                JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
                o.put(SERIALIZATION_VERSION_MARKER, ((Versionable)obj).serializationVersion());
                s = o.toStringOrNull();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }
        }

        return s;
    }

    public static <T extends SerializableToJSON> T deserialize(String s, Class<T> clazzT) {
        if(s == null) { return null; }

        // First try to get the version number. If none is present, then the data was not versioned.
        String version;
        try {
            JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
            version = o.getString(SERIALIZATION_VERSION_MARKER);
        }
        catch(Exception e) {
            version = null;
        }

        // Call the appropriate deserialization method for the version number.
        try {
            if(version == null) {
                return ReflectUtil.callStaticMethod(clazzT, "deserializeFromJSON", s);
            }
            else {
                return ReflectUtil.callStaticMethod(clazzT, "deserializeFromJSON" + version, s);
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T extends SerializableToJSON> String validate(String s, Class<T> clazzT) {
        // Do a round trip of deserializing and serializing to make sure the string represents an object of the class.
        SerializableToJSON dummyObject = Serialization.deserialize(s, clazzT);
        return Serialization.serialize(dummyObject);
    }

    public static <T extends SerializableToJSON> String serializeArrayList(ArrayList<T> arrayList) {
        if(arrayList == null) { return null; }

        try {
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull();
            for(T t : arrayList) {
                a.put(new JSONWithNull.JSONObjectWithNull(Serialization.serialize(t)));
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

            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull(s);
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
            return new JSONWithNull.JSONObjectWithNull()
                .put("keys", new JSONWithNull.JSONArrayWithNull(Serialization.serializeArrayList(keyArrayList)))
                .put("values", new JSONWithNull.JSONArrayWithNull(Serialization.serializeArrayList(valueArrayList)))
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
            JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);

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
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull();
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

            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull(s);
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

    public static String string_serializeHashMap(HashMap<String, String> hashMap) {
        if(hashMap == null) { return null; }

        // Serialize a hashmap as an array of keys and an array of values, in the same order.
        ArrayList<String> keyArrayList = new ArrayList<>(hashMap.keySet());
        ArrayList<String> valueArrayList = new ArrayList<>();
        for(String key : keyArrayList) {
            valueArrayList.add(hashMap.get(key));
        }

        try {
            return new JSONWithNull.JSONObjectWithNull()
                    .put("keys", new JSONWithNull.JSONArrayWithNull(Serialization.string_serializeArrayList(keyArrayList)))
                    .put("values", new JSONWithNull.JSONArrayWithNull(Serialization.string_serializeArrayList(valueArrayList)))
                    .toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static HashMap<String, String> string_deserializeHashMap(String s) {
        if(s == null) { return null; }

        try {
            JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);

            ArrayList<String> arrayListT = Serialization.string_deserializeArrayList(o.getJSONArray("keys").toStringOrNull());
            ArrayList<String> arrayListU = Serialization.string_deserializeArrayList(o.getJSONArray("values").toStringOrNull());

            if(arrayListT == null || arrayListU == null || arrayListT.size() != arrayListU.size()) {
                return null;
            }

            HashMap<String, String> hashMap = new HashMap<>();
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

    public static String boolean_serialize(boolean b) {
        return string_serialize(Boolean.toString(b));
    }

    public static boolean boolean_deserialize(String s) {
        return Boolean.parseBoolean(string_deserialize(s));
    }

    public static String boolean_serializeArrayList(ArrayList<Boolean> arrayList) {
        if(arrayList == null) { return null; }

        try {
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull();
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

            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull(s);
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
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull();
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
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull(s);
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
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull();
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

            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull(s);
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
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull();
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

            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull(s);
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

    public static String file_serialize(File obj) {
        return obj == null ? null : string_serialize(obj.getAbsolutePath());
    }

    public static File file_deserialize(String s) {
        return s == null ? null : new File(string_deserialize(s));
    }

    public static String documentfile_serialize(DocumentFile obj) {
        if(obj == null) { return null; }

        try {
            return new JSONWithNull.JSONObjectWithNull()
                    .put("class", string_serialize(obj.getClass().getSimpleName()))
                    .put("uri", string_serialize(obj.getUri().toString()))
                    .toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static DocumentFile documentfile_deserialize(String s) {
        if(s == null) { return null; }

        try {
            JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
            String clazz = Serialization.string_deserialize(o.getString("class"));
            Uri uri = Uri.parse(Serialization.string_deserialize(o.getString("uri")));

            if("TreeDocumentFile".equals(clazz)) {
                return DocumentFile.fromTreeUri(App.applicationContext, uri);
            }
            else if("SingleDocumentFile".equals(clazz)) {
                return DocumentFile.fromSingleUri(App.applicationContext, uri);
            }
            else {
                return null;
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    // Asset.serializeToJSON only serializes a key used to lookup an Asset later.
    // These method serializes all the information needed to construct an asset from scratch.
    public static String fiat_serialize(Fiat obj) {
        if(obj == null) { return null; }

        try {
            // Use original properties directly, not the potentially modified ones from getter functions.
            return new JSONWithNull.JSONObjectWithNull()
                    .put("key", string_serialize(obj.getOriginalKey()))
                    .put("name", string_serialize(obj.getOriginalName()))
                    .put("display_name", string_serialize(obj.getOriginalDisplayName()))
                    .put("scale", int_serialize(obj.getOriginalScale()))
                    .put("fiat_type", string_serialize(obj.getOriginalAssetType()))
                    .put("additional_info", string_serializeHashMap(obj.getOriginalAdditionalInfo()))
                    .toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static Fiat fiat_deserialize(String s) {
        if(s == null) { return null; }

        try {
            JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
            String key = Serialization.string_deserialize(o.getString("key"));
            String name = Serialization.string_deserialize(o.getString("name"));
            String display_name = Serialization.string_deserialize(o.getString("display_name"));
            int scale = Serialization.int_deserialize(o.getString("scale"));
            String fiat_type = Serialization.string_deserialize(o.getString("fiat_type"));
            HashMap<String, String> additional_info = Serialization.string_deserializeHashMap(o.getString("additional_info"));

            return new Fiat(key, name, display_name, scale, fiat_type, additional_info);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static String fiat_serializeArrayList(ArrayList<Fiat> arrayList) {
        if(arrayList == null) { return null; }

        try {
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull();
            for(Fiat obj : arrayList) {
                a.put(Serialization.fiat_serialize(obj));
            }

            return a.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static ArrayList<Fiat> fiat_deserializeArrayList(String s) {
        if(s == null) { return null; }

        try {
            ArrayList<Fiat> arrayList = new ArrayList<>();

            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull(s);
            for(int i = 0; i < a.length(); i++) {
                String o = a.getString(i);
                arrayList.add(Serialization.fiat_deserialize(o));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static String coin_serialize(Coin obj) {
        if(obj == null) { return null; }

        try {
            // Use original properties directly, not the potentially modified ones from getter functions.
            return new JSONWithNull.JSONObjectWithNull()
                    .put("key", string_serialize(obj.getOriginalKey()))
                    .put("name", string_serialize(obj.getOriginalName()))
                    .put("display_name", string_serialize(obj.getOriginalDisplayName()))
                    .put("scale", int_serialize(obj.getOriginalScale()))
                    .put("coin_type", string_serialize(obj.getOriginalAssetType()))
                    .put("additional_info", string_serializeHashMap(obj.getOriginalAdditionalInfo()))
                    .toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static Coin coin_deserialize(String s) {
        if(s == null) { return null; }

        try {
            JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
            String key = Serialization.string_deserialize(o.getString("key"));
            String name = Serialization.string_deserialize(o.getString("name"));
            String display_name = Serialization.string_deserialize(o.getString("display_name"));
            int scale = Serialization.int_deserialize(o.getString("scale"));
            String coin_type = Serialization.string_deserialize(o.getString("coin_type"));
            HashMap<String, String> additional_info = Serialization.string_deserializeHashMap(o.getString("additional_info"));

            return new Coin(key, name, display_name, scale, coin_type, additional_info);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static String coin_serializeArrayList(ArrayList<Coin> arrayList) {
        if(arrayList == null) { return null; }

        try {
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull();
            for(Coin obj : arrayList) {
                a.put(Serialization.coin_serialize(obj));
            }

            return a.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static ArrayList<Coin> coin_deserializeArrayList(String s) {
        if(s == null) { return null; }

        try {
            ArrayList<Coin> arrayList = new ArrayList<>();

            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull(s);
            for(int i = 0; i < a.length(); i++) {
                String o = a.getString(i);
                arrayList.add(Serialization.coin_deserialize(o));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static String token_serialize(Token obj) {
        if(obj == null) { return null; }

        try {
            // Use original properties directly, not the potentially modified ones from getter functions.
            return new JSONWithNull.JSONObjectWithNull()
                    .put("key", string_serialize(obj.getOriginalKey()))
                    .put("name", string_serialize(obj.getOriginalName()))
                    .put("display_name", string_serialize(obj.getOriginalDisplayName()))
                    .put("scale", int_serialize(obj.getOriginalScale()))
                    .put("token_type", string_serialize(obj.getOriginalAssetType()))
                    .put("additional_info", string_serializeHashMap(obj.getOriginalAdditionalInfo()))
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
            JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
            String key = Serialization.string_deserialize(o.getString("key"));
            String name = Serialization.string_deserialize(o.getString("name"));
            String display_name = Serialization.string_deserialize(o.getString("display_name"));
            int scale = Serialization.int_deserialize(o.getString("scale"));
            String token_type = Serialization.string_deserialize(o.getString("token_type"));
            HashMap<String, String> additional_info = Serialization.string_deserializeHashMap(o.getString("additional_info"));

            return new Token(key, name, display_name, scale, token_type, additional_info);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static String token_serializeArrayList(ArrayList<Token> arrayList) {
        if(arrayList == null) { return null; }

        try {
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull();
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

            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull(s);
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
