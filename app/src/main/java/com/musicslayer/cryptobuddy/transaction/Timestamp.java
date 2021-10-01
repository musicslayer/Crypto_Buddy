package com.musicslayer.cryptobuddy.transaction;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.util.DateTime;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class Timestamp implements Serializable {
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

    public String serialize() {
        return "{\"date\":\"" + Long.toString(date.getTime()) + "\"}";
    }

    public static Timestamp deserialize(String s) {
        try {
            JSONObject o = new JSONObject(s);
            Date date = new Date(Long.parseLong(o.getString("date")));
            return new Timestamp(date);
        }
        catch(Exception e) {
            return null;
        }
    }
}
