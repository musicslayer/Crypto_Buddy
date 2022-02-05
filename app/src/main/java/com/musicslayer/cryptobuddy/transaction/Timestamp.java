package com.musicslayer.cryptobuddy.transaction;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.util.DateTimeUtil;
import com.musicslayer.cryptobuddy.serialize.Serialization;

import java.util.Date;

public class Timestamp implements Serialization.SerializableToJSON, Serialization.Versionable {
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

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("date", Serialization.date_serialize(date))
            .toStringOrNull();
    }

    public static Timestamp deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        Date date = Serialization.date_deserialize(o.getString("date"));
        return new Timestamp(date);
    }
}
