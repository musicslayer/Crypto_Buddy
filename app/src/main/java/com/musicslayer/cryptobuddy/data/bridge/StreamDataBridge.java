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

public class StreamDataBridge {
    public static <T> String serialize(T obj, Class<T> clazzT) {
        try {
            return new JSONStreamDataBridge().serializeString(null, obj, clazzT);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> T deserialize(String s, Class<T> clazzT) {
        try {
            return new JSONStreamDataBridge(s).deserialize(null, clazzT);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> String cycle(String s, Class<T> clazzT) {
        // Do a round trip of deserializing and serializing to make sure the string represents an object of the class.
        T obj = deserialize(s, clazzT);
        return serialize(obj, clazzT);
    }

    public static class JSONStreamDataBridge {
        public JsonWriter jsonWriter;
        public StringWriter stringWriter;

        public JsonReader jsonReader;
        public StringReader stringReader;

        public JSONStreamDataBridge() throws IOException {
            stringWriter = new StringWriter();
            jsonWriter = new JsonWriter(stringWriter);
        }

        public JSONStreamDataBridge(String s) throws IOException {
            stringReader = new StringReader(s);
            jsonReader = new JsonReader(stringReader);
        }

        public JSONStreamDataBridge beginObject() throws IOException {
            if(jsonWriter != null) {
                jsonWriter.beginObject();
            }
            else if(jsonReader != null) {
                jsonReader.beginObject();
            }

            return this;
        }

        public JSONStreamDataBridge endObject() throws IOException {
            if(jsonWriter != null) {
                jsonWriter.endObject();
            }
            else if(jsonReader != null) {
                jsonReader.endObject();
            }

            return this;
        }

        public JSONStreamDataBridge beginArray() throws IOException {
            if(jsonWriter != null) {
                jsonWriter.beginArray();
            }
            else if(jsonReader != null) {
                jsonReader.beginArray();
            }

            return this;
        }

        public JSONStreamDataBridge endArray() throws IOException {
            if(jsonWriter != null) {
                jsonWriter.endArray();
            }
            else if(jsonReader != null) {
                jsonReader.endArray();
            }

            return this;
        }

        public void consumeVersion() throws IOException {
            if(jsonReader.peek() == JsonToken.NAME) {
                String name = jsonReader.nextName();
                if(!"!V!".equals(name)) {
                    // Expected key was not found.
                    throw new IllegalStateException();
                }

                jsonReader.skipValue();
            }
        }

        public void finish() throws IOException {
            consumeVersion();
            jsonReader.endObject();
            jsonReader.close();
        }

        public String toStringOrNull() throws IOException {
            // TODO Can we get NULL?
            jsonWriter.flush();
            jsonWriter.close();
            return stringWriter.toString();
        }

        public <T> String serializeString(String key, T obj, Class<T> clazzT) throws IOException {
            serialize(key, obj, clazzT);
            return toStringOrNull();
        }

        public <T> JSONStreamDataBridge serialize(String key, T obj, Class<T> clazzT) throws IOException {
            //String s = Serialization.serialize(obj, clazzT);
            //if(obj == null) { return null; }
            Serialization.SerializableToJSON wrappedObj = Serialization.wrapObj(obj);
            Class<? extends Serialization.SerializableToJSON> wrappedClass = Serialization.wrapClass(clazzT);

            if(key != null) {
                jsonWriter.name(key);
            }

            try {
                wrappedObj.serializeToJSONX(this);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }

            return this;
        }

        public <T> JSONStreamDataBridge serializeArrayList(String key, ArrayList<T> arrayList, Class<T> clazzT) throws IOException {
            //String s = Serialization.serializeArrayList(obj, clazzT);
            //if(arrayList == null) { return null; }

            if(key != null) {
                jsonWriter.name(key);
            }

            try {
                jsonWriter.beginArray();
                for(T t : arrayList) {
                    serialize(null, t, clazzT);
                    //a.serialize(t, clazzT);
                }
                jsonWriter.endArray();

                //return a.toStringOrNull();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }

            return this;
        }

        public <T, U> JSONStreamDataBridge serializeHashMap(String key, HashMap<T, U> hashMap, Class<T> clazzT, Class<U> clazzU) throws IOException {
            //String s = Serialization.serializeHashMap(hashMap, clazzT, clazzU);
            //if(hashMap == null) { return null; }

            if(key != null) {
                jsonWriter.name(key);
            }

            ArrayList<T> keyArrayList = new ArrayList<>(hashMap.keySet());
            ArrayList<U> valueArrayList = new ArrayList<>();
            for(T keyT : keyArrayList) {
                valueArrayList.add(hashMap.get(keyT));
            }

            try {
                jsonWriter.beginObject();
                serializeArrayList("keys", keyArrayList, clazzT);
                serializeArrayList("values", valueArrayList, clazzU);
                jsonWriter.endObject();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }

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

            //if(s == null) { return null; }

            Class<? extends Serialization.SerializableToJSON> wrappedClass = Serialization.wrapClass(clazzT);

            try {
                return ReflectUtil.callStaticMethod(wrappedClass, "deserializeFromJSONX", this);
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
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

            try {
                ArrayList<T> arrayList = new ArrayList<>();

                jsonReader.beginArray();
                for(int i = 0; jsonReader.hasNext(); i++) {
                    arrayList.add(deserialize(null, clazzT));
                }
                jsonReader.endArray();

                return arrayList;
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
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

            //if(s == null) { return null; }

            try {
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
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }
        }

        public String getName() throws IOException {
            return jsonReader.nextName().replace("\"", "\\\"");
        }

        public String getString() throws IOException {
            return jsonReader.nextString().replace("\"", "\\\"");
        }
    }
}
