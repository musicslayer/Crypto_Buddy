package com.musicslayer.cryptobuddy.transaction;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.serialize.Serialization;

import java.util.ArrayList;

public class Action implements Serialization.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(actionString);
    }

    public static final Parcelable.Creator<Action> CREATOR = new Parcelable.Creator<Action>() {
        @Override
        public Action createFromParcel(Parcel in) {
            String actionString = in.readString();
            return new Action(actionString);
        }

        @Override
        public Action[] newArray(int size) {
            return new Action[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    final public static ArrayList<String> actions;
    static {
        actions = new ArrayList<>();
        actions.add("Buy");
        actions.add("Sell");
        actions.add("Receive");
        actions.add("Send");
        actions.add("Fee");
    }

    public String actionString;

    public Action(String actionString) {
        this.actionString = actionString;
    }

    public boolean isLoss() {
        return "Sell".equals(actionString) || "Send".equals(actionString) || "Fee".equals(actionString) ;
    }

    public int numAssets() {
        int numAssets;

        switch(actionString) {
            case "Receive":
            case "Send":
            case "Fee":
                numAssets = 1;
                break;

            case "Buy":
            case "Sell":
                numAssets = 2;
                break;

            default:
                numAssets = -1;
                break;
        }

        return numAssets;
    }

    @NonNull
    public String toString() {
        return actionString;
    }

    private int compare(Action other) {
        int actionA;
        int actionB;

        switch(actionString) {
            case "Buy":
                actionA = 0;
                break;
            case "Sell":
                actionA = 1;
                break;
            case "Receive":
                actionA = 2;
                break;
            case "Send":
                actionA = 3;
                break;
            case "Fee":
                actionA = 4;
                break;
            default:
                actionA = -1;
                break;
        }

        switch(other.actionString) {
            case "Buy":
                actionB = 0;
                break;
            case "Sell":
                actionB = 1;
                break;
            case "Receive":
                actionB = 2;
                break;
            case "Send":
                actionB = 3;
                break;
            case "Fee":
                actionB = 4;
                break;
            default:
                actionB = -1;
                break;
        }

        return Integer.compare(actionA, actionB);
    }

    public static int compare(Action a, Action b) {
        boolean isValidA = a != null;
        boolean isValidB = b != null;

        // Null is always smaller than a real action.
        if(isValidA & isValidB) { return a.compare(b); }
        else { return Boolean.compare(isValidA, isValidB); }
    }

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return new Serialization.JSONObjectWithNull()
            .put("actionString", Serialization.string_serialize(actionString))
            .toStringOrNull();
    }

    public static Action deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String actionString = Serialization.string_deserialize(o.getString("actionString"));
        return new Action(actionString);
    }
}
