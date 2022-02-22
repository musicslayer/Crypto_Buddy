package com.musicslayer.cryptobuddy.data.bridge;

import com.musicslayer.cryptobuddy.json.JSONWithNull;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class LegacyDataBridge {
    public static class JSONObjectDataBridge {
        JSONWithNull.JSONObjectWithNull jsonObjectWithNull;

        public JSONObjectDataBridge() {
            jsonObjectWithNull = new JSONWithNull.JSONObjectWithNull();
        }

        public JSONObjectDataBridge(String s) throws JSONException {
            jsonObjectWithNull = new JSONWithNull.JSONObjectWithNull(s);
        }

        public String toStringOrNull() {
            return jsonObjectWithNull.toStringOrNull();
        }

        public boolean has(String key) {
            return jsonObjectWithNull.has(key);
        }

        ///////////
        // These are needed for non-standard workflows.
        public String getJSONObjectString(String key) throws JSONException {
            return jsonObjectWithNull.getJSONObjectString(key);
        }

        public String getJSONArrayString(String key) throws JSONException {
            return jsonObjectWithNull.getJSONArrayString(key);
        }

        public JSONObjectDataBridge putJSONObjectString(String key, String s) throws JSONException {
            jsonObjectWithNull = jsonObjectWithNull.putJSONObjectString(key, s);
            return this;
        }
        ///////////

        public <T> JSONObjectDataBridge serialize(String key, T obj, Class<T> clazzT) throws JSONException {
            String type = LegacySerialization.getCurrentType(clazzT);
            String s = LegacySerialization.serialize(obj, clazzT);

            if("!STRING!".equals(type)) {
                jsonObjectWithNull = jsonObjectWithNull.putString(key, s);
            }
            else if("!OBJECT!".equals(type)) {
                jsonObjectWithNull = jsonObjectWithNull.putJSONObjectString(key, s);
            }
            else if("!ARRAY!".equals(type)) {
                jsonObjectWithNull = jsonObjectWithNull.putJSONArrayString(key, s);
            }
            else {
                // Other types are not supported.
                throw new IllegalStateException();
            }

            return this;
        }

        public <T> JSONObjectDataBridge serializeArray(String key, T[] obj, Class<T> clazzT) throws JSONException {
            // Array always uses type !ARRAY!.
            String s = LegacySerialization.serializeArray(obj, clazzT);
            jsonObjectWithNull = jsonObjectWithNull.putJSONArrayString(key, s);
            return this;
        }

        public <T> JSONObjectDataBridge serializeArrayList(String key, ArrayList<T> obj, Class<T> clazzT) throws JSONException {
            // ArrayList always uses type !ARRAY!.
            String s = LegacySerialization.serializeArrayList(obj, clazzT);
            jsonObjectWithNull = jsonObjectWithNull.putJSONArrayString(key, s);
            return this;
        }

        public <T, U> JSONObjectDataBridge serializeHashMap(String key, HashMap<T, U> obj, Class<T> clazzT, Class<U> clazzU) throws JSONException {
            // HashMap always uses type !OBJECT!.
            String s = LegacySerialization.serializeHashMap(obj, clazzT, clazzU);
            jsonObjectWithNull = jsonObjectWithNull.putJSONObjectString(key, s);
            return this;
        }

        public <T> JSONObjectDataBridge reference(String key, T obj, Class<T> clazzT) throws JSONException {
            String s = DataBridge.reference(obj, clazzT);
            jsonObjectWithNull = jsonObjectWithNull.putJSONObjectString(key, s);
            return this;
        }

        public <T> JSONObjectDataBridge referenceArrayList(String key, ArrayList<T> obj, Class<T> clazzT) throws JSONException {
            // ArrayList always uses type !ARRAY!.
            String s = DataBridge.referenceArrayList(obj, clazzT);
            jsonObjectWithNull = jsonObjectWithNull.putJSONArrayString(key, s);
            return this;
        }

        public <T> T deserialize(String key, Class<T> clazzT) throws JSONException {
            String version;
            try {
                version = LegacySerialization.getVersion(jsonObjectWithNull.getJSONObjectString(key));
            }
            catch(Exception ignored) {
                version = "0";
            }

            String type = LegacySerialization.getTypeForVersion(version, clazzT);
            String s;

            if("!STRING!".equals(type)) {
                s = jsonObjectWithNull.getString(key);
            }
            else if("!OBJECT!".equals(type)) {
                s = jsonObjectWithNull.getJSONObjectString(key);
            }
            else if("!ARRAY!".equals(type)) {
                s = jsonObjectWithNull.getJSONArrayString(key);
            }
            else {
                // Other types are not supported.
                throw new IllegalStateException();
            }

            return LegacySerialization.deserialize(s, clazzT);
        }

        public <T> T[] deserializeArray(String key, Class<T> clazzT) throws JSONException {
            // Array always uses type !ARRAY!.
            String s = jsonObjectWithNull.getJSONArrayString(key);
            return LegacySerialization.deserializeArray(s, clazzT);
        }

        public <T> ArrayList<T> deserializeArrayList(String key, Class<T> clazzT) throws JSONException {
            // ArrayList always uses type !ARRAY!.
            String s = jsonObjectWithNull.getJSONArrayString(key);
            return LegacySerialization.deserializeArrayList(s, clazzT);
        }

        public <T, U> HashMap<T, U> deserializeHashMap(String key, Class<T> clazzT, Class<U> clazzU) throws JSONException {
            // HashMap always uses type !OBJECT!.
            String s = jsonObjectWithNull.getJSONObjectString(key);
            return LegacySerialization.deserializeHashMap(s, clazzT, clazzU);
        }

        public <T> T dereference(String key, Class<T> clazzT) throws JSONException {
            String s = jsonObjectWithNull.getJSONObjectString(key);
            return DataBridge.dereference(s, clazzT);
        }

        public <T> ArrayList<T> dereferenceArrayList(String key, Class<T> clazzT) throws JSONException {
            // ArrayList always uses type !ARRAY!.
            String s = jsonObjectWithNull.getJSONArrayString(key);
            return DataBridge.dereferenceArrayList(s, clazzT);
        }
    }

    public static class JSONArrayDataBridge {
        JSONWithNull.JSONArrayWithNull jsonArrayWithNull;

        public JSONArrayDataBridge() {
            this.jsonArrayWithNull = new JSONWithNull.JSONArrayWithNull();
        }

        public JSONArrayDataBridge(String s) throws JSONException {
            this.jsonArrayWithNull = new JSONWithNull.JSONArrayWithNull(s);
        }

        public int length() {
            return jsonArrayWithNull.length();
        }

        public String toStringOrNull() {
            return jsonArrayWithNull.toStringOrNull();
        }

        public <T> JSONArrayDataBridge serialize(T obj, Class<T> clazzT) throws JSONException {
            String type = LegacySerialization.getCurrentType(clazzT);
            String s = LegacySerialization.serialize(obj, clazzT);

            if("!STRING!".equals(type)) {
                jsonArrayWithNull = jsonArrayWithNull.putString(s);
            }
            else if("!OBJECT!".equals(type)) {
                jsonArrayWithNull = jsonArrayWithNull.putJSONObjectString(s);
            }
            else if("!ARRAY!".equals(type)) {
                jsonArrayWithNull = jsonArrayWithNull.putJSONArrayString(s);
            }
            else {
                // Other types are not supported.
                throw new IllegalStateException();
            }

            return this;
        }

        public <T> T deserialize(int i, Class<T> clazzT) throws JSONException {
            String version;
            try {
                version = LegacySerialization.getVersion(jsonArrayWithNull.getJSONObjectString(i));
            }
            catch(Exception ignored) {
                version = "0";
            }

            String type = LegacySerialization.getTypeForVersion(version, clazzT);
            String s;

            if("!STRING!".equals(type)) {
                s = jsonArrayWithNull.getString(i);
            }
            else if("!OBJECT!".equals(type)) {
                s = jsonArrayWithNull.getJSONObjectString(i);
            }
            else if("!ARRAY!".equals(type)) {
                s = jsonArrayWithNull.getJSONArrayString(i);
            }
            else {
                // Other types are not supported.
                throw new IllegalStateException();
            }

            return LegacySerialization.deserialize(s, clazzT);
        }

        public <T> T dereference(int i, Class<T> clazzT) throws JSONException {
            String s = jsonArrayWithNull.getJSONObjectString(i);
            return DataBridge.dereference(s, clazzT);
        }
    }
}
