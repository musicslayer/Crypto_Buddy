package com.musicslayer.cryptobuddy.transaction;

import androidx.annotation.NonNull;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.Serialization;

import java.util.ArrayList;

public class Action implements Serialization.SerializableToJSON, Serialization.Versionable {
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

    public static String serializationVersion() {
        return "1";
    }

    public static String serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String serializeToJSON() throws org.json.JSONException {
        return new DataBridge.JSONObjectDataBridge()
            .serialize("actionString", actionString, String.class)
            .toStringOrNull();
    }

    public static Action deserializeFromJSON(String s, String version) throws org.json.JSONException {
        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
        String actionString = o.deserialize("actionString", String.class);
        return new Action(actionString);
    }
}
