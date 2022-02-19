package com.musicslayer.cryptobuddy.data;

import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.musicslayer.cryptobuddy.app.App;
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
        String serializeToJSON() throws org.json.JSONException;

        // Classes also need to implement static methods "deserializeFromJSON" and "serializationType".
    }

    // Any class implementing this supports versioning.
    // This is needed when saving data that may be loaded in a different release.
    public interface Versionable {
        // Classes also need to implement a static method "serializationVersion".
    }

    public static <T> String serialize(T obj, Class<T> clazzT) {
        if(obj == null) { return null; }
        SerializableToJSON wrappedObj = wrapObj(obj);
        Class<? extends SerializableToJSON> wrappedClass = wrapClass(clazzT);

        String s;
        try {
            s = wrappedObj.serializeToJSON();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }

        if(Versionable.class.isAssignableFrom(wrappedClass)) {
            // Add the version to every individual object that we serialize, or error if we cannot.
            try {
                String version = Serialization.getCurrentVersion(wrappedClass);
                String type = Serialization.getCurrentType(wrappedClass);

                if("!OBJECT!".equals(type)) {
                    JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
                    o.put(SERIALIZATION_VERSION_MARKER, version, String.class);
                    s = o.toStringOrNull();
                }
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

        String version = getVersion(s);

        try {
            return ReflectUtil.callStaticMethod(wrappedClass, "deserializeFromJSON", s, version);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> String getCurrentVersion(Class<T> clazz) {
        Class<? extends SerializableToJSON> wrappedClass = wrapClass(clazz);
        if(Versionable.class.isAssignableFrom(wrappedClass)) {
            return ReflectUtil.callStaticMethod(wrappedClass, "serializationVersion");
        }
        else {
            // If there is no version, just call it "version zero".
            return "0";
        }
    }

    public static String getVersion(String s) {
        try {
            JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
            return o.get(SERIALIZATION_VERSION_MARKER, String.class);
        }
        catch(Exception ignored) {
            // If there is no version, just call it "version zero".
            return "0";
        }
    }

    public static <T> String getCurrentType(Class<T> clazz) {
        return getTypeForVersion(getCurrentVersion(clazz), clazz);
    }

    public static <T> String getTypeForVersion(String version, Class<T> clazz) {
        Class<? extends SerializableToJSON> wrappedClass = wrapClass(clazz);
        return ReflectUtil.callStaticMethod(wrappedClass, "serializationType", version);
    }

    public static <T> String validate(String s, Class<T> clazzT) {
        // Do a round trip of deserializing and serializing to make sure the string represents an object of the class.
        T dummyObject = deserialize(s, clazzT);
        return serialize(dummyObject, clazzT);
    }

    public static <T> String serializeArray(T[] array, Class<T> clazzT) {
        if(array == null) { return null; }

        try {
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull();
            for(T t : array) {
                a.put(t, clazzT);
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
                array[i] = a.get(i, clazzT);
            }

            return array;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> String serializeArrayList(ArrayList<T> arrayList, Class<T> clazzT) {
        if(arrayList == null) { return null; }

        try {
            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull();
            for(T t : arrayList) {
                a.put(t, clazzT);
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
                arrayList.add(a.get(i, clazzT));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T, U> String serializeHashMap(HashMap<T, U> hashMap, Class<T> clazzT, Class<U> clazzU) {
        if(hashMap == null) { return null; }

        // Serialize a hashmap as an array of keys and an array of values, in the same order.
        ArrayList<T> keyArrayList = new ArrayList<>(hashMap.keySet());
        ArrayList<U> valueArrayList = new ArrayList<>();
        for(T key : keyArrayList) {
            valueArrayList.add(hashMap.get(key));
        }

        try {
            return new JSONWithNull.JSONObjectWithNull()
                .putArrayList("keys", keyArrayList, clazzT)
                .putArrayList("values", valueArrayList, clazzU)
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

            ArrayList<T> arrayListT = o.getArrayList("keys", clazzT);
            ArrayList<U> arrayListU = o.getArrayList("values", clazzU);

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
        else {
            // Anything else is unsupported.
            throw new IllegalStateException();
        }
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends SerializableToJSON> wrapClass(Class<?> clazz) {
        // Converts an arbitrary class into a SerializableToJSON class.
        if(SerializableToJSON.class.isAssignableFrom(clazz)) {
            return (Class<? extends SerializableToJSON>)clazz;
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

        public static String serializationType(String version) {
            return "!STRING!";
        }

        @Override
        public String serializeToJSON() {
            return obj;
        }

        public static String deserializeFromJSON(String s, String version) {
            return s;
        }
    }

    private static class BooleanSerializableToJSON implements SerializableToJSON {
        boolean obj;
        private BooleanSerializableToJSON(boolean obj) {
            this.obj = obj;
        }

        public static String serializationType(String version) {
            return "!STRING!";
        }

        @Override
        public String serializeToJSON() {
            return Boolean.toString(obj);
        }

        public static boolean deserializeFromJSON(String s, String version) {
            return Boolean.parseBoolean(s);
        }
    }

    private static class ByteSerializableToJSON implements SerializableToJSON {
        byte obj;
        private ByteSerializableToJSON(byte obj) {
            this.obj = obj;
        }

        public static String serializationType(String version) {
            return "!STRING!";
        }

        @Override
        public String serializeToJSON() {
            return Byte.toString(obj);
        }

        public static byte deserializeFromJSON(String s, String version) {
            return Byte.parseByte(s);
        }
    }

    private static class IntegerSerializableToJSON implements SerializableToJSON {
        int obj;
        private IntegerSerializableToJSON(int obj) {
            this.obj = obj;
        }

        public static String serializationType(String version) {
            return "!STRING!";
        }

        @Override
        public String serializeToJSON() {
            return Integer.toString(obj);
        }

        public static int deserializeFromJSON(String s, String version) {
            return Integer.parseInt(s);
        }
    }

    private static class LongSerializableToJSON implements SerializableToJSON {
        long obj;
        private LongSerializableToJSON(long obj) {
            this.obj = obj;
        }

        public static String serializationType(String version) {
            return "!STRING!";
        }

        @Override
        public String serializeToJSON() {
            return Long.toString(obj);
        }

        public static long deserializeFromJSON(String s, String version) {
            return Long.parseLong(s);
        }
    }

    private static class DateSerializableToJSON implements SerializableToJSON {
        Date obj;
        private DateSerializableToJSON(Date obj) {
            this.obj = obj;
        }

        public static String serializationType(String version) {
            return "!STRING!";
        }

        @Override
        public String serializeToJSON() {
            return Long.toString(obj.getTime());
        }

        public static Date deserializeFromJSON(String s, String version) {
            return new Date(Long.parseLong(s));
        }
    }

    private static class BigDecimalSerializableToJSON implements SerializableToJSON {
        BigDecimal obj;
        private BigDecimalSerializableToJSON(BigDecimal obj) {
            this.obj = obj;
        }

        public static String serializationType(String version) {
            return "!STRING!";
        }

        @Override
        public String serializeToJSON() {
            return obj.toString();
        }

        public static BigDecimal deserializeFromJSON(String s, String version) {
            return new BigDecimal(s);
        }
    }

    private static class FileSerializableToJSON implements SerializableToJSON {
        File obj;
        private FileSerializableToJSON(File obj) {
            this.obj = obj;
        }

        public static String serializationType(String version) {
            return "!STRING!";
        }

        @Override
        public String serializeToJSON() {
            return obj.getAbsolutePath();
        }

        public static File deserializeFromJSON(String s, String version) {
            return new File(s);
        }
    }

    private static class DocumentFileSerializableToJSON implements SerializableToJSON {
        DocumentFile obj;
        private DocumentFileSerializableToJSON(DocumentFile obj) {
            this.obj = obj;
        }

        public static String serializationType(String version) {
            return "!OBJECT!";
        }

        @Override
        public String serializeToJSON() throws JSONException {
            return new JSONWithNull.JSONObjectWithNull()
                    .put("class", obj.getClass().getSimpleName(), String.class)
                    .put("uri", obj.getUri().toString(), String.class)
                    .toStringOrNull();
        }

        public static DocumentFile deserializeFromJSON(String s, String version) throws JSONException {
            JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
            String clazz = o.get("class", String.class);
            Uri uri = Uri.parse(o.get("uri", String.class));

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
    }
}
