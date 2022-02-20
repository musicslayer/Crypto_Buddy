package com.musicslayer.cryptobuddy.json;

import org.json.JSONArray;
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

        public String getString(String key) throws org.json.JSONException {
            return (String)(jsonObject.get(key) instanceof String ? jsonObject.get(key) : null);
        }

        public String getJSONObjectString(String key) throws org.json.JSONException {
            return getJSONObject(key).toStringOrNull();
        }

        // TODO Make Private
        // Must be public right now so it can be used by FiatManager/CoinManager/TokenManager.
        public String getJSONArrayString(String key) throws org.json.JSONException {
            return getJSONArray(key).toStringOrNull();
        }

        public JSONObjectWithNull putString(String key, String s) throws org.json.JSONException {
            jsonObject = s == null ? jsonObject.put(key, JSONObject.NULL) : jsonObject.put(key, s);
            return this;
        }

        public JSONObjectWithNull putJSONObjectString(String key, String s) throws org.json.JSONException {
            JSONObjectWithNull obj = new JSONObjectWithNull(s);
            jsonObject = obj.jsonObject == null ? jsonObject.put(key, JSONObject.NULL) : jsonObject.put(key, obj.jsonObject);
            return this;
        }

        public JSONObjectWithNull putJSONArrayString(String key, String s) throws org.json.JSONException {
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

        public JSONObjectWithNull getJSONObject(String key) throws org.json.JSONException {
            return new JSONObjectWithNull((JSONObject)(jsonObject.get(key) instanceof JSONObject ? jsonObject.get(key) : null));
        }

        public JSONArrayWithNull getJSONArray(String key) throws org.json.JSONException {
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

        // TODO Make Private
        // Must be public right now so it can be used by FiatManager/CoinManager/TokenManager.
        public String getString(int i) throws org.json.JSONException {
            return jsonArray.get(i) instanceof String ? (String)jsonArray.get(i) : null;
        }

        public String getJSONObjectString(int i) throws org.json.JSONException {
            return getJSONObject(i).toStringOrNull();
        }

        public String getJSONArrayString(int i) throws org.json.JSONException {
            return getJSONArray(i).toStringOrNull();
        }

        public JSONArrayWithNull putString(String s) {
            jsonArray = s == null ? jsonArray.put(JSONObject.NULL) : jsonArray.put(s);
            return this;
        }

        public JSONArrayWithNull putJSONObjectString(String s) throws org.json.JSONException {
            JSONObjectWithNull obj = new JSONObjectWithNull(s);
            jsonArray = obj.jsonObject == null ? jsonArray.put(JSONObject.NULL) : jsonArray.put(obj.jsonObject);
            return this;
        }

        public JSONArrayWithNull putJSONArrayString(String s) throws org.json.JSONException {
            JSONArrayWithNull arr = new JSONArrayWithNull(s);
            jsonArray = arr.jsonArray == null ? jsonArray.put(JSONObject.NULL) : jsonArray.put(arr.jsonArray);
            return this;
        }

        public JSONObjectWithNull getJSONObject(int i) throws org.json.JSONException {
            return new JSONObjectWithNull((JSONObject)(jsonArray.get(i) instanceof JSONObject ? jsonArray.get(i) : null));
        }

        public JSONArrayWithNull getJSONArray(int i) throws org.json.JSONException {
            return new JSONArrayWithNull((JSONArray)(jsonArray.get(i) instanceof JSONArray ? jsonArray.get(i) : null));
        }
    }
}