package com.musicslayer.cryptobuddy.transaction;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.util.DateTimeUtil;
import com.musicslayer.cryptobuddy.data.Serialization;

import java.util.Date;

public class Timestamp implements Serialization.SerializableToJSON, Serialization.Versionable {
    public Date date;

    public Timestamp() {
        this.date = new Date();
    }

    public Timestamp(Date date) {
        this.date = date;
    }

    @NonNull
    public String toString() {
        if(date == null) {
            return "-";
        }
        else {
            return DateTimeUtil.toDateString(date);
        }
    }

    private int compare(Timestamp other) {
        return DateTimeUtil.compare(date, other.date);
    }

    public static int compare(Timestamp a, Timestamp b) {
        boolean isValidA = a != null;
        boolean isValidB = b != null;

        // Null is always smaller than a real Timestamp.
        if(isValidA & isValidB) { return a.compare(b); }
        else { return Boolean.compare(isValidA, isValidB); }
    }

    public static String serializationVersion() {
        return "1";
    }

    public static String serializationType() {
        return "!OBJECT!";
    }

    @Override
    public String serializeToJSON() throws org.json.JSONException {
        return new JSONWithNull.JSONObjectWithNull()
            .put("date", date, Date.class)
            .toStringOrNull();
    }

    public static Timestamp deserializeFromJSON(String s, String version) throws org.json.JSONException {
        JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
        Date date = o.get("date", Date.class);
        return new Timestamp(date);
    }
}
