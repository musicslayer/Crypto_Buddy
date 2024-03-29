package com.musicslayer.cryptobuddy.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

// Classes to properly handle null String values in JSON workflows.

public class JSONWithNull {
    // Replacement of JSONObject to properly handle null String values.
    public static class JSONObjectWithNull {
        private JSONObject jsonObject;

        public JSONObjectWithNull() {
            jsonObject = new JSONObject();
        }

        public JSONObjectWithNull(String s) throws JSONException {
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

        public String getString(String key) throws JSONException {
            return (String)(jsonObject.get(key) instanceof String ? jsonObject.get(key) : null);
        }

        public String getJSONObjectString(String key) throws JSONException {
            return getJSONObject(key).toStringOrNull();
        }

        public String getJSONArrayString(String key) throws JSONException {
            return getJSONArray(key).toStringOrNull();
        }

        public JSONObjectWithNull putString(String key, String s) throws JSONException {
            jsonObject = s == null ? jsonObject.put(key, JSONObject.NULL) : jsonObject.put(key, s);
            return this;
        }

        public JSONObjectWithNull putJSONObjectString(String key, String s) throws JSONException {
            JSONObjectWithNull obj = new JSONObjectWithNull(s);
            jsonObject = obj.jsonObject == null ? jsonObject.put(key, JSONObject.NULL) : jsonObject.put(key, obj.jsonObject);
            return this;
        }

        public JSONObjectWithNull putJSONArrayString(String key, String s) throws JSONException {
            JSONArrayWithNull arr = new JSONArrayWithNull(s);
            jsonObject = arr.jsonArray == null ? jsonObject.put(key, JSONObject.NULL) : jsonObject.put(key, arr.jsonArray);
            return this;
        }

        public ArrayList<String> keys() {
            ArrayList<String> keys = new ArrayList<>();
            Iterator<String> it = jsonObject.keys();
            while(it.hasNext()) {
                keys.add(it.next());
            }
            return keys;
        }

        private JSONObjectWithNull getJSONObject(String key) throws JSONException {
            return new JSONObjectWithNull((JSONObject)(jsonObject.get(key) instanceof JSONObject ? jsonObject.get(key) : null));
        }

        private JSONArrayWithNull getJSONArray(String key) throws JSONException {
            return new JSONArrayWithNull((JSONArray)(jsonObject.get(key) instanceof JSONArray ? jsonObject.get(key) : null));
        }
    }

    // Replacement of JSONArray to properly handle null String values.
    public static class JSONArrayWithNull {
        private JSONArray jsonArray;

        public JSONArrayWithNull() {
            jsonArray = new JSONArray();
        }

        public JSONArrayWithNull(String s) throws JSONException {
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

        public String getString(int i) throws JSONException {
            return jsonArray.get(i) instanceof String ? (String)jsonArray.get(i) : null;
        }

        public String getJSONObjectString(int i) throws JSONException {
            return getJSONObject(i).toStringOrNull();
        }

        public String getJSONArrayString(int i) throws JSONException {
            return getJSONArray(i).toStringOrNull();
        }

        public JSONArrayWithNull putString(String s) {
            jsonArray = s == null ? jsonArray.put(JSONObject.NULL) : jsonArray.put(s);
            return this;
        }

        public JSONArrayWithNull putJSONObjectString(String s) throws JSONException {
            JSONObjectWithNull obj = new JSONObjectWithNull(s);
            jsonArray = obj.jsonObject == null ? jsonArray.put(JSONObject.NULL) : jsonArray.put(obj.jsonObject);
            return this;
        }

        public JSONArrayWithNull putJSONArrayString(String s) throws JSONException {
            JSONArrayWithNull arr = new JSONArrayWithNull(s);
            jsonArray = arr.jsonArray == null ? jsonArray.put(JSONObject.NULL) : jsonArray.put(arr.jsonArray);
            return this;
        }

        private JSONObjectWithNull getJSONObject(int i) throws JSONException {
            return new JSONObjectWithNull((JSONObject)(jsonArray.get(i) instanceof JSONObject ? jsonArray.get(i) : null));
        }

        private JSONArrayWithNull getJSONArray(int i) throws JSONException {
            return new JSONArrayWithNull((JSONArray)(jsonArray.get(i) instanceof JSONArray ? jsonArray.get(i) : null));
        }
    }
}