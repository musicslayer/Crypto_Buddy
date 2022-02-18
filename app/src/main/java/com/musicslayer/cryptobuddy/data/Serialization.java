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

import org.json.JSONException;

import java.io.File;
import java.lang.reflect.Array;
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
    public static <T> String serialize(T obj) {
        if(obj == null) { return null; }
        SerializableToJSON wrappedObj = wrapObj(obj);

        String s;
        try {
            s = wrappedObj.serializeToJSON();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }

        if(wrappedObj instanceof Versionable) {
            // Add the version to every individual object that we serialize, or error if we cannot.
            try {
                JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
                o.put(SERIALIZATION_VERSION_MARKER, ((Versionable)wrappedObj).serializationVersion());
                s = o.toStringOrNull();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }
        }

        return s;
    }

    public static <T> T deserialize(String s, Class<T> clazzT) {
        if(s == null) { return null; }
        Class<? extends SerializableToJSON> wrappedClass = wrapClass(clazzT);

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
                return ReflectUtil.callStaticMethod(wrappedClass, "deserializeFromJSON", s);
            }
            else {
                return ReflectUtil.callStaticMethod(wrappedClass, "deserializeFromJSON" + version, s);
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> String validate(String s, Class<T> clazzT) {
        // Do a round trip of deserializing and serializing to make sure the string represents an object of the class.
        T dummyObject = deserialize(s, clazzT);
        return serialize(dummyObject);
    }

    public static <T> String serializeArray(T[] array) {
        if(array == null) { return null; }

        try {
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull();
            for(T t : array) {
                a.put(serialize(t));
            }

            return a.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> T[] deserializeArray(String s, Class<T> clazzT) {
        if(s == null) { return null; }

        try {
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull(s);

            @SuppressWarnings("unchecked")
            T[] array = (T[])Array.newInstance(clazzT, a.length());

            for(int i = 0; i < a.length(); i++) {
                String o = a.getString(i);
                array[i] = deserialize(o, clazzT);
            }

            return array;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> String serializeArrayList(ArrayList<T> arrayList) {
        if(arrayList == null) { return null; }

        try {
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull();
            for(T t : arrayList) {
                a.put(new JSONWithNull.JSONObjectWithNull(serialize(t)));
            }

            return a.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> ArrayList<T> deserializeArrayList(String s, Class<T> clazzT) {
        if(s == null) { return null; }

        try {
            ArrayList<T> arrayList = new ArrayList<>();

            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull(s);
            for(int i = 0; i < a.length(); i++) {
                arrayList.add(deserialize(a.getJSONObjectString(i), clazzT));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T, U> String serializeHashMap(HashMap<T, U> hashMap) {
        if(hashMap == null) { return null; }

        // Serialize a hashmap as an array of keys and an array of values, in the same order.
        ArrayList<T> keyArrayList = new ArrayList<>(hashMap.keySet());
        ArrayList<U> valueArrayList = new ArrayList<>();
        for(T key : keyArrayList) {
            valueArrayList.add(hashMap.get(key));
        }

        try {
            return new JSONWithNull.JSONObjectWithNull()
                .put("keys", new JSONWithNull.JSONArrayWithNull(serializeArrayList(keyArrayList)))
                .put("values", new JSONWithNull.JSONArrayWithNull(serializeArrayList(valueArrayList)))
                .toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T, U> HashMap<T, U> deserializeHashMap(String s, Class<T> clazzT, Class<U> clazzU) {
        if(s == null) { return null; }

        try {
            JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);

            ArrayList<T> arrayListT = deserializeArrayList(o.getJSONArray("keys").toStringOrNull(), clazzT);
            ArrayList<U> arrayListU = deserializeArrayList(o.getJSONArray("values").toStringOrNull(), clazzU);

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

    // For classes that do not implement SerializableToJSON, we wrap them in classes that define the required methods.
    public static SerializableToJSON wrapObj(Object obj) {
        // Converts an arbitrary object into a SerializableToJSON subclass.
        // Note that obj will always be non-null.
        if(obj instanceof SerializableToJSON) {
            return (SerializableToJSON)obj;
        }
        else if(obj instanceof String) {
            return new StringSerializableToJSON((String)obj);
        }
        else if(obj instanceof Boolean) {
            return new BooleanSerializableToJSON((Boolean)obj);
        }
        else if(obj instanceof Byte) {
            return new ByteSerializableToJSON((Byte)obj);
        }
        else if(obj instanceof Integer) {
            return new IntegerSerializableToJSON((Integer)obj);
        }
        else if(obj instanceof Long) {
            return new LongSerializableToJSON((Long)obj);
        }
        else if(obj instanceof Date) {
            return new DateSerializableToJSON((Date)obj);
        }
        else if(obj instanceof BigDecimal) {
            return new BigDecimalSerializableToJSON((BigDecimal)obj);
        }
        else if(obj instanceof File) {
            return new FileSerializableToJSON((File)obj);
        }
        else if(obj instanceof DocumentFile) {
            return new DocumentFileSerializableToJSON((DocumentFile)obj);
        }
        else if(obj instanceof Fiat) {
            return new FiatSerializableToJSON((Fiat)obj);
        }
        else if(obj instanceof Coin) {
            return new CoinSerializableToJSON((Coin)obj);
        }
        else if(obj instanceof Token) {
            return new TokenSerializableToJSON((Token)obj);
        }
        else {
            // Anything else is unsupported.
            throw new IllegalStateException();
        }
    }

    public static Class<? extends SerializableToJSON> wrapClass(Class<?> clazz) {
        // Converts an arbitrary class into a SerializableToJSON class.
        if(SerializableToJSON.class.isAssignableFrom(clazz)) {
            return SerializableToJSON.class;
        }
        else if(String.class.isAssignableFrom(clazz)) {
            return StringSerializableToJSON.class;
        }
        else if(Boolean.class.isAssignableFrom(clazz)) {
            return BooleanSerializableToJSON.class;
        }
        else if(Byte.class.isAssignableFrom(clazz)) {
            return ByteSerializableToJSON.class;
        }
        else if(Integer.class.isAssignableFrom(clazz)) {
            return IntegerSerializableToJSON.class;
        }
        else if(Long.class.isAssignableFrom(clazz)) {
            return LongSerializableToJSON.class;
        }
        else if(Date.class.isAssignableFrom(clazz)) {
            return DateSerializableToJSON.class;
        }
        else if(BigDecimal.class.isAssignableFrom(clazz)) {
            return BigDecimalSerializableToJSON.class;
        }
        else if(File.class.isAssignableFrom(clazz)) {
            return FileSerializableToJSON.class;
        }
        else if(DocumentFile.class.isAssignableFrom(clazz)) {
            return DocumentFileSerializableToJSON.class;
        }
        else if(Fiat.class.isAssignableFrom(clazz)) {
            return FiatSerializableToJSON.class;
        }
        else if(Coin.class.isAssignableFrom(clazz)) {
            return CoinSerializableToJSON.class;
        }
        else if(Token.class.isAssignableFrom(clazz)) {
            return TokenSerializableToJSON.class;
        }
        else {
            // Anything else is unsupported.
            throw new IllegalStateException();
        }
    }

    private static class StringSerializableToJSON implements SerializableToJSON {
        String obj;
        private StringSerializableToJSON(String obj) {
            this.obj = obj;
        }

        @Override
        public String serializeToJSON() throws JSONException {
            return obj;
        }

        public String deserializeFromJSON(String s) throws JSONException {
            return s;
        }
    }

    private static class BooleanSerializableToJSON implements SerializableToJSON {
        boolean obj;
        private BooleanSerializableToJSON(boolean obj) {
            this.obj = obj;
        }

        @Override
        public String serializeToJSON() throws JSONException {
            return Boolean.toString(obj);
        }

        public boolean deserializeFromJSON(String s) throws JSONException {
            return Boolean.parseBoolean(s);
        }
    }

    private static class ByteSerializableToJSON implements SerializableToJSON {
        byte obj;
        private ByteSerializableToJSON(byte obj) {
            this.obj = obj;
        }

        @Override
        public String serializeToJSON() throws JSONException {
            return Byte.toString(obj);
        }

        public byte deserializeFromJSON(String s) throws JSONException {
            return Byte.parseByte(s);
        }
    }

    private static class IntegerSerializableToJSON implements SerializableToJSON {
        int obj;
        private IntegerSerializableToJSON(int obj) {
            this.obj = obj;
        }

        @Override
        public String serializeToJSON() throws JSONException {
            return Integer.toString(obj);
        }

        public int deserializeFromJSON(String s) throws JSONException {
            return Integer.parseInt(s);
        }
    }

    private static class LongSerializableToJSON implements SerializableToJSON {
        long obj;
        private LongSerializableToJSON(long obj) {
            this.obj = obj;
        }

        @Override
        public String serializeToJSON() throws JSONException {
            return Long.toString(obj);
        }

        public long deserializeFromJSON(String s) throws JSONException {
            return Long.parseLong(s);
        }
    }

    private static class DateSerializableToJSON implements SerializableToJSON {
        Date obj;
        private DateSerializableToJSON(Date obj) {
            this.obj = obj;
        }

        @Override
        public String serializeToJSON() throws JSONException {
            return Long.toString(obj.getTime());
        }

        public Date deserializeFromJSON(String s) throws JSONException {
            return new Date(Long.parseLong(s));
        }
    }

    private static class BigDecimalSerializableToJSON implements SerializableToJSON {
        BigDecimal obj;
        private BigDecimalSerializableToJSON(BigDecimal obj) {
            this.obj = obj;
        }

        @Override
        public String serializeToJSON() throws JSONException {
            return obj.toString();
        }

        public BigDecimal deserializeFromJSON(String s) throws JSONException {
            return new BigDecimal(s);
        }
    }

    private static class FileSerializableToJSON implements SerializableToJSON {
        File obj;
        private FileSerializableToJSON(File obj) {
            this.obj = obj;
        }

        @Override
        public String serializeToJSON() throws JSONException {
            return obj.getAbsolutePath();
        }

        public File deserializeFromJSON(String s) throws JSONException {
            return new File(s);
        }
    }

    private static class DocumentFileSerializableToJSON implements SerializableToJSON {
        DocumentFile obj;
        private DocumentFileSerializableToJSON(DocumentFile obj) {
            this.obj = obj;
        }

        @Override
        public String serializeToJSON() throws JSONException {
            try {
                return new JSONWithNull.JSONObjectWithNull()
                        .put("class", serialize(obj.getClass().getSimpleName()))
                        .put("uri", serialize(obj.getUri().toString()))
                        .toStringOrNull();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }
        }

        public DocumentFile deserializeFromJSON(String s) throws JSONException {
            try {
                JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
                String clazz = o.getString("class");
                Uri uri = Uri.parse(o.getString("uri"));

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
    }

    // TODO Will these asset methods work?
    // Asset.serializeToJSON only serializes a key used to lookup an Asset later.
    // These method serializes all the information needed to construct an asset from scratch.
    private static class FiatSerializableToJSON implements SerializableToJSON {
        Fiat obj;
        private FiatSerializableToJSON(Fiat obj) {
            this.obj = obj;
        }

        @Override
        public String serializeToJSON() throws JSONException {
            try {
                // Use original properties directly, not the potentially modified ones from getter functions.
                return new JSONWithNull.JSONObjectWithNull()
                        .put("key", serialize(obj.getOriginalKey()))
                        .put("name", serialize(obj.getOriginalName()))
                        .put("display_name", serialize(obj.getOriginalDisplayName()))
                        .put("scale", serialize(obj.getOriginalScale()))
                        .put("fiat_type", serialize(obj.getOriginalAssetType()))
                        .put("additional_info", serializeHashMap(obj.getOriginalAdditionalInfo()))
                        .toStringOrNull();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }
        }

        public Fiat deserializeFromJSON(String s) throws JSONException {
            try {
                JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
                String key = deserialize(o.getString("key"), String.class);
                String name = deserialize(o.getString("name"), String.class);
                String display_name = deserialize(o.getString("display_name"), String.class);
                int scale = deserialize(o.getString("scale"), Integer.class);
                String fiat_type = deserialize(o.getString("fiat_type"), String.class);
                HashMap<String, String> additional_info = deserializeHashMap(o.getString("additional_info"), String.class, String.class);

                return new Fiat(key, name, display_name, scale, fiat_type, additional_info);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }
        }
    }

    private static class CoinSerializableToJSON implements SerializableToJSON {
        Coin obj;
        private CoinSerializableToJSON(Coin obj) {
            this.obj = obj;
        }

        @Override
        public String serializeToJSON() throws JSONException {
            try {
                // Use original properties directly, not the potentially modified ones from getter functions.
                return new JSONWithNull.JSONObjectWithNull()
                        .put("key", serialize(obj.getOriginalKey()))
                        .put("name", serialize(obj.getOriginalName()))
                        .put("display_name", serialize(obj.getOriginalDisplayName()))
                        .put("scale", serialize(obj.getOriginalScale()))
                        .put("fiat_type", serialize(obj.getOriginalAssetType()))
                        .put("additional_info", serializeHashMap(obj.getOriginalAdditionalInfo()))
                        .toStringOrNull();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }
        }

        public Coin deserializeFromJSON(String s) throws JSONException {
            try {
                JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
                String key = deserialize(o.getString("key"), String.class);
                String name = deserialize(o.getString("name"), String.class);
                String display_name = deserialize(o.getString("display_name"), String.class);
                int scale = deserialize(o.getString("scale"), Integer.class);
                String fiat_type = deserialize(o.getString("fiat_type"), String.class);
                HashMap<String, String> additional_info = deserializeHashMap(o.getString("additional_info"), String.class, String.class);

                return new Coin(key, name, display_name, scale, fiat_type, additional_info);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }
        }
    }

    private static class TokenSerializableToJSON implements SerializableToJSON {
        Token obj;
        private TokenSerializableToJSON(Token obj) {
            this.obj = obj;
        }

        @Override
        public String serializeToJSON() throws JSONException {
            try {
                // Use original properties directly, not the potentially modified ones from getter functions.
                return new JSONWithNull.JSONObjectWithNull()
                        .put("key", serialize(obj.getOriginalKey()))
                        .put("name", serialize(obj.getOriginalName()))
                        .put("display_name", serialize(obj.getOriginalDisplayName()))
                        .put("scale", serialize(obj.getOriginalScale()))
                        .put("fiat_type", serialize(obj.getOriginalAssetType()))
                        .put("additional_info", serializeHashMap(obj.getOriginalAdditionalInfo()))
                        .toStringOrNull();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }
        }

        public Token deserializeFromJSON(String s) throws JSONException {
            try {
                JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
                String key = deserialize(o.getString("key"), String.class);
                String name = deserialize(o.getString("name"), String.class);
                String display_name = deserialize(o.getString("display_name"), String.class);
                int scale = deserialize(o.getString("scale"), Integer.class);
                String fiat_type = deserialize(o.getString("fiat_type"), String.class);
                HashMap<String, String> additional_info = deserializeHashMap(o.getString("additional_info"), String.class, String.class);

                return new Token(key, name, display_name, scale, fiat_type, additional_info);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }
        }
    }
}
