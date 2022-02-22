package com.musicslayer.cryptobuddy.data.bridge;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;

import com.musicslayer.cryptobuddy.util.ReflectUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class DataBridge {
    public static <T> String serialize(T obj, Class<T> clazzT) {
        if(obj == null) { return null; }

        Writer writer = new Writer();

        try {
            writer.serialize(null, obj, clazzT);
            writer.safeFlushAndClose();
            return writer.stringWriter.toString();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            writer.safeFlushAndClose();
            throw new IllegalStateException(e);
        }
    }

    public static <T> T deserialize(String s, Class<T> clazzT) {
        if(s == null) { return null; }

        Reader reader = new Reader(s);
        try {
            T t = reader.deserialize(null, clazzT);
            reader.safeClose();
            return t;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            reader.safeClose();
            throw new IllegalStateException(e);
        }
    }

    public static <T> String cycleSerialization(String s, Class<T> clazzT) {
        // Do a round trip of deserializing and serializing to make sure the string represents an object of the class.
        T obj = deserialize(s, clazzT);
        return serialize(obj, clazzT);
    }

    public static class Writer {
        public JsonWriter jsonWriter;
        public StringWriter stringWriter;

        public Writer() {
            stringWriter = new StringWriter();
            jsonWriter = new JsonWriter(stringWriter);
        }

        public void safeFlushAndClose() {
            try {
                jsonWriter.flush();
                jsonWriter.close();
            }
            catch(Exception ignored) {
            }
        }

        public Writer beginObject() throws IOException {
            jsonWriter.beginObject();
            return this;
        }

        public Writer endObject() throws IOException {
            jsonWriter.endObject();
            return this;
        }

        public Writer beginArray() throws IOException {
            jsonWriter.beginArray();
            return this;
        }

        public Writer endArray() throws IOException {
            jsonWriter.endArray();
            return this;
        }

        public <T> Writer serialize(String key, T obj, Class<T> clazzT) throws IOException {
            if(key != null) {
                jsonWriter.name(key);
            }

            if(obj == null) {
                jsonWriter.nullValue();
            }
            else {
                Serialization.wrapObj(obj).serializeToJSONX(this);
            }

            return this;
        }

        public <T> Writer serializeArrayList(String key, ArrayList<T> arrayList, Class<T> clazzT) throws IOException {
            if(key != null) {
                jsonWriter.name(key);
            }

            if(arrayList == null) {
                jsonWriter.nullValue();
            }
            else {
                jsonWriter.beginArray();
                for(T t : arrayList) {
                    serialize(null, t, clazzT);
                }
                jsonWriter.endArray();
            }

            return this;
        }

        public <T, U> Writer serializeHashMap(String key, HashMap<T, U> hashMap, Class<T> clazzT, Class<U> clazzU) throws IOException {
            if(key != null) {
                jsonWriter.name(key);
            }

            if(hashMap == null) {
                jsonWriter.nullValue();
            }
            else {
                ArrayList<T> keyArrayList = new ArrayList<>(hashMap.keySet());
                ArrayList<U> valueArrayList = new ArrayList<>();
                for(T keyT : keyArrayList) {
                    valueArrayList.add(hashMap.get(keyT));
                }

                jsonWriter.beginObject();
                serializeArrayList("keys", keyArrayList, clazzT);
                serializeArrayList("values", valueArrayList, clazzU);
                jsonWriter.endObject();
            }

            return this;
        }
    }

    public static class Reader {
        public JsonReader jsonReader;
        public StringReader stringReader;

        public Reader(String s) {
            stringReader = new StringReader(s);
            jsonReader = new JsonReader(stringReader);
        }

        public void safeClose() {
            try {
                jsonReader.close();
            }
            catch(Exception ignored) {
            }
        }

        public String getName() throws IOException {
            return jsonReader.nextName().replace("\"", "\\\"");
        }

        public String getString() throws IOException {
            return jsonReader.nextString().replace("\"", "\\\"");
        }

        public Reader beginObject() throws IOException {
            jsonReader.beginObject();
            return this;
        }

        public Reader endObject() throws IOException {
            jsonReader.endObject();
            return this;
        }

        public Reader beginArray() throws IOException {
            jsonReader.beginArray();
            return this;
        }

        public Reader endArray() throws IOException {
            jsonReader.endArray();
            return this;
        }

        public <T> T deserialize(String key, Class<T> clazzT) throws IOException {
            if(key != null) {
                String nextKey = getName();
                if(!key.equals(nextKey)) {
                    // Expected key was not found.
                    throw new IllegalStateException();
                }
            }

            if(jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }
            else {
                Class<? extends Serialization.SerializableToJSON> wrappedClass = Serialization.wrapClass(clazzT);
                return ReflectUtil.callStaticMethod(wrappedClass, "deserializeFromJSONX", this);
            }
        }

        public <T> ArrayList<T> deserializeArrayList(String key, Class<T> clazzT) throws IOException {
            if(key != null) {
                String nextKey = getName();
                if(!key.equals(nextKey)) {
                    // Expected key was not found.
                    throw new IllegalStateException();
                }
            }

            if(jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }
            else {
                ArrayList<T> arrayList = new ArrayList<>();

                jsonReader.beginArray();
                while(jsonReader.hasNext()) {
                    arrayList.add(deserialize(null, clazzT));
                }
                jsonReader.endArray();

                return arrayList;
            }
        }

        public <T, U> HashMap<T, U> deserializeHashMap(String key, Class<T> clazzT, Class<U> clazzU) throws IOException {
            if(key != null) {
                String nextKey = getName();
                if(!key.equals(nextKey)) {
                    // Expected key was not found.
                    throw new IllegalStateException();
                }
            }

            if(jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }
            else {
                jsonReader.beginObject();
                ArrayList<T> arrayListT = deserializeArrayList("keys", clazzT);
                ArrayList<U> arrayListU = deserializeArrayList("values", clazzU);
                jsonReader.endObject();

                if(arrayListT == null || arrayListU == null || arrayListT.size() != arrayListU.size()) {
                    return null;
                }

                HashMap<T, U> hashMap = new HashMap<>();
                for(int i = 0; i < arrayListT.size(); i++) {
                    hashMap.put(arrayListT.get(i), arrayListU.get(i));
                }

                return hashMap;
            }
        }
    }
}
