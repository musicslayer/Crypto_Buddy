package com.musicslayer.cryptobuddy.transaction;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.util.DateTime;
import com.musicslayer.cryptobuddy.util.Serialization;

import org.json.JSONObject;

import java.util.Date;

public class Timestamp implements Serialization.SerializableToJSON {
    public Date date;

    public Timestamp(Date date) {
        this.date = date;
    }

    @NonNull
    public String toString() {
        if(date == null) {
            return "-";
        }
        else {
            return DateTime.toDateString(date);
        }
    }

    private int compare(Timestamp other) {
        return DateTime.compare(date, other.date);
    }

    public static int compare(Timestamp a, Timestamp b) {
        boolean isValidA = a != null;
        boolean isValidB = b != null;

        // Null is always smaller than a real Timestamp.
        if(isValidA & isValidB) { return a.compare(b); }
        else { return Boolean.compare(isValidA, isValidB); }
    }

    public String serializeToJSON() {
        return "{\"date\":\"" + Long.toString(date.getTime()) + "\"}";
    }

    public static Timestamp deserializeFromJSON(String s) throws org.json.JSONException {
        JSONObject o = new JSONObject(s);
        Date date = new Date(Long.parseLong(o.getString("date")));
        return new Timestamp(date);
    }
}
