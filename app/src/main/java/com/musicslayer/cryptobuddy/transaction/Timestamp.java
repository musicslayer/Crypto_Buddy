package com.musicslayer.cryptobuddy.transaction;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.util.DateTimeUtil;

import java.io.IOException;
import java.util.Date;

public class Timestamp implements DataBridge.SerializableToJSON {
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

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("!V!", "2", String.class)
                .serialize("date", date, Date.class)
                .endObject();
    }

    public static Timestamp deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        Timestamp timestamp;

        if("2".equals(version)) {
            Date date = o.deserialize("date", Date.class);
            o.endObject();

            timestamp = new Timestamp(date);
        }
        else {
            throw new IllegalStateException();
        }

        return timestamp;
    }
}
