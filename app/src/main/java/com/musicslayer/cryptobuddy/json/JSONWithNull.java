package com.musicslayer.cryptobuddy.json;

import com.musicslayer.cryptobuddy.data.Serialization;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

// Classes to properly handle null String values in JSON workflows.

public class JSONWithNull {
    // Replacement of JSONObject to properly handle null String values.
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

        public boolean has(String key) {
            return jsonObject.has(key);
        }

        public <T> T get(String key, Class<T> clazzT) throws org.json.JSONException {
            String version;
            try {
                version = Serialization.getVersion(getJSONObjectString(key));
            }
            catch(Exception ignored) {
                version = "0";
            }

            String type = Serialization.getTypeForVersion(version, clazzT);
            String s;

            if("!STRING!".equals(type)) {
                s = getString(key);
            }
            else if("!OBJECT!".equals(type)) {
                s = getJSONObjectString(key);
            }
            else if("!ARRAY!".equals(type)) {
                s = getJSONArrayString(key);
            }
            else {
                // Other types are not supported.
                throw new IllegalStateException();
            }

            return Serialization.deserialize(s, clazzT);
        }

        public <T> T[] getArray(String key, Class<T> clazzT) throws org.json.JSONException {
            // Array always uses type !ARRAY!.
            String s = getJSONArrayString(key);
            return Serialization.deserializeArray(s, clazzT);
        }

        public <T> ArrayList<T> getArrayList(String key, Class<T> clazzT) throws org.json.JSONException {
            // ArrayList always uses type !ARRAY!.
            String s = getJSONArrayString(key);
            return Serialization.deserializeArrayList(s, clazzT);
        }

        public <T, U> HashMap<T, U> getHashMap(String key, Class<T> clazzT, Class<U> clazzU) throws org.json.JSONException {
            // HashMap always uses type !OBJECT!.
            String s = getJSONObjectString(key);
            return Serialization.deserializeHashMap(s, clazzT, clazzU);
        }

        private String getString(String key) throws org.json.JSONException {
            return (String)(jsonObject.get(key) instanceof String ? jsonObject.get(key) : null);
        }

        private String getJSONObjectString(String key) throws org.json.JSONException {
            return getJSONObject(key).toStringOrNull();
        }

        // TODO Make Private
        // Must be public right now so it can be used by FiatManager/CoinManager/TokenManager.
        public String getJSONArrayString(String key) throws org.json.JSONException {
            return getJSONArray(key).toStringOrNull();
        }

        public <T> JSONObjectWithNull put(String key, T obj, Class<T> clazzT) throws org.json.JSONException {
            String type = Serialization.getCurrentType(clazzT);
            String s = Serialization.serialize(obj, clazzT);

            if("!STRING!".equals(type)) {
                return putString(key, s);
            }
            else if("!OBJECT!".equals(type)) {
                return putJSONObjectString(key, s);
            }
            else if("!ARRAY!".equals(type)) {
                return putJSONArrayString(key, s);
            }
            else {
                // Other types are not supported.
                throw new IllegalStateException();
            }
        }

        public <T> JSONObjectWithNull putArray(String key, T[] obj, Class<T> clazzT) throws org.json.JSONException {
            // Array always uses type !ARRAY!.
            String s = Serialization.serializeArray(obj, clazzT);
            return putJSONArrayString(key, s);
        }

        public <T> JSONObjectWithNull putArrayList(String key, ArrayList<T> obj, Class<T> clazzT) throws org.json.JSONException {
            // ArrayList always uses type !ARRAY!.
            String s = Serialization.serializeArrayList(obj, clazzT);
            return putJSONArrayString(key, s);
        }

        public <T, U> JSONObjectWithNull putHashMap(String key, HashMap<T, U> obj, Class<T> clazzT, Class<U> clazzU) throws org.json.JSONException {
            // HashMap always uses type !OBJECT!.
            String s = Serialization.serializeHashMap(obj, clazzT, clazzU);
            return putJSONObjectString(key, s);
        }

        private JSONObjectWithNull putString(String key, String s) throws org.json.JSONException {
            jsonObject = s == null ? jsonObject.put(key, JSONObject.NULL) : jsonObject.put(key, s);
            return this;
        }

        private JSONObjectWithNull putJSONObjectString(String key, String s) throws org.json.JSONException {
            JSONObjectWithNull obj = new JSONObjectWithNull(s);
            jsonObject = obj.jsonObject == null ? jsonObject.put(key, JSONObject.NULL) : jsonObject.put(key, obj.jsonObject);
            return this;
        }

        private JSONObjectWithNull putJSONArrayString(String key, String s) throws org.json.JSONException {
            JSONArrayWithNull arr = new JSONArrayWithNull(s);
            jsonObject = arr.jsonArray == null ? jsonObject.put(key, JSONObject.NULL) : jsonObject.put(key, arr.jsonArray);
            return this;
        }

        // TODO Remove
        // We need this so it can be used by FiatManagerList/CoinManagerList/TokenManagerList.
        public JSONObjectWithNull putJSONArray(String key, JSONArrayWithNull arr) throws org.json.JSONException {
            jsonObject = arr.jsonArray == null ? jsonObject.put(key, JSONObject.NULL) : jsonObject.put(key, arr.jsonArray);
            return this;
        }

        public void remove(String key) {
            jsonObject.remove(key);
        }

        public ArrayList<String> keys() {
            ArrayList<String> keys = new ArrayList<>();
            Iterator<String> it = jsonObject.keys();
            while(it.hasNext()) {
                keys.add(it.next());
            }
            return keys;
        }

        private JSONObjectWithNull getJSONObject(String key) throws org.json.JSONException {
            return new JSONObjectWithNull((JSONObject)(jsonObject.get(key) instanceof JSONObject ? jsonObject.get(key) : null));
        }

        private JSONArrayWithNull getJSONArray(String key) throws org.json.JSONException {
            return new JSONArrayWithNull((JSONArray)(jsonObject.get(key) instanceof JSONArray ? jsonObject.get(key) : null));
        }
    }

    // Replacement of JSONArray to properly handle null String values.
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

        public <T> T get(int i, Class<T> clazzT) throws org.json.JSONException {
            String version;
            try {
                version = Serialization.getVersion(getJSONObjectString(i));
            }
            catch(Exception ignored) {
                version = "0";
            }

            String type = Serialization.getTypeForVersion(version, clazzT);
            String s;

            if("!STRING!".equals(type)) {
                s = getString(i);
            }
            else if("!OBJECT!".equals(type)) {
                s = getJSONObjectString(i);
            }
            else if("!ARRAY!".equals(type)) {
                s = getJSONArrayString(i);
            }
            else {
                // Other types are not supported.
                throw new IllegalStateException();
            }

            return Serialization.deserialize(s, clazzT);
        }

        // TODO Make Private
        // Must be public right now so it can be used by FiatManager/CoinManager/TokenManager.
        public String getString(int i) throws org.json.JSONException {
            return jsonArray.get(i) instanceof String ? (String)jsonArray.get(i) : null;
        }

        private String getJSONObjectString(int i) throws org.json.JSONException {
            return getJSONObject(i).toStringOrNull();
        }

        private String getJSONArrayString(int i) throws org.json.JSONException {
            return getJSONArray(i).toStringOrNull();
        }

        public <T> JSONArrayWithNull put(T obj, Class<T> clazzT) throws org.json.JSONException {
            String type = Serialization.getCurrentType(clazzT);
            String s = Serialization.serialize(obj, clazzT);

            if("!STRING!".equals(type)) {
                return putString(s);
            }
            else if("!OBJECT!".equals(type)) {
                return putJSONObjectString(s);
            }
            else if("!ARRAY!".equals(type)) {
                return putJSONArrayString(s);
            }
            else {
                // Other types are not supported.
                throw new IllegalStateException();
            }
        }

        private JSONArrayWithNull putString(String s) {
            jsonArray = s == null ? jsonArray.put(JSONObject.NULL) : jsonArray.put(s);
            return this;
        }

        private JSONArrayWithNull putJSONObjectString(String s) throws org.json.JSONException {
            JSONObjectWithNull obj = new JSONObjectWithNull(s);
            jsonArray = obj.jsonObject == null ? jsonArray.put(JSONObject.NULL) : jsonArray.put(obj.jsonObject);
            return this;
        }

        private JSONArrayWithNull putJSONArrayString(String s) throws org.json.JSONException {
            JSONArrayWithNull arr = new JSONArrayWithNull(s);
            jsonArray = arr.jsonArray == null ? jsonArray.put(JSONObject.NULL) : jsonArray.put(arr.jsonArray);
            return this;
        }

        private JSONObjectWithNull getJSONObject(int i) throws org.json.JSONException {
            return new JSONObjectWithNull((JSONObject)(jsonArray.get(i) instanceof JSONObject ? jsonArray.get(i) : null));
        }

        private JSONArrayWithNull getJSONArray(int i) throws org.json.JSONException {
            return new JSONArrayWithNull((JSONArray)(jsonArray.get(i) instanceof JSONArray ? jsonArray.get(i) : null));
        }
    }
}