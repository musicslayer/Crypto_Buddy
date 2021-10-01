package com.musicslayer.cryptobuddy.transaction;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.ArrayList;

// TODO Actions for delegate, redelegate, undelegate? Burn?

public class Action {
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

    public String serialize() {
        return "{\"actionString\":\"" + actionString + "\"}";
    }

    public static Action deserialize(String s) {
        try {
            JSONObject o = new JSONObject(s);
            String actionString = o.getString("actionString");
            return new Action(actionString);
        }
        catch(Exception e) {
            return null;
        }
    }
}
