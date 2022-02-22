package com.musicslayer.cryptobuddy.filter;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.LegacyDataBridge;
import com.musicslayer.cryptobuddy.dialog.DiscreteFilterDialog;

import java.io.IOException;
import java.util.ArrayList;

public class DiscreteFilter extends Filter {
    public ArrayList<String> choices = new ArrayList<>();
    public ArrayList<String> user_choices = new ArrayList<>();
    public ArrayList<String> user_not_choices = new ArrayList<>();

    @Override
    public String legacy_serializeToJSON_sub() throws org.json.JSONException {
        return new LegacyDataBridge.JSONObjectDataBridge()
            .serialize("filterType", getFilterType(), String.class)
            .serializeArrayList("choices", choices, String.class)
            .serializeArrayList("user_choices", user_choices, String.class)
            .serializeArrayList("user_not_choices", user_not_choices, String.class)
            .toStringOrNull();
    }

    public static DiscreteFilter deserializeFromJSON_sub(String s) throws org.json.JSONException {
        LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge(s);
        ArrayList<String> choices = o.deserializeArrayList("choices", String.class);
        ArrayList<String> user_choices = o.deserializeArrayList("user_choices", String.class);
        ArrayList<String> user_not_choices = o.deserializeArrayList("user_not_choices", String.class);

        DiscreteFilter discreteFilter = new DiscreteFilter();
        discreteFilter.choices = choices;
        discreteFilter.user_choices = user_choices;
        discreteFilter.user_not_choices = user_not_choices;
        return discreteFilter;
    }

    @Override
    public void serializeToJSON_sub(DataBridge.Writer o) throws IOException {
        o.beginObject()
                .serialize("filterType", getFilterType(), String.class)
                .serializeArrayList("choices", choices, String.class)
                .serializeArrayList("user_choices", user_choices, String.class)
                .serializeArrayList("user_not_choices", user_not_choices, String.class)
                .endObject();
    }

    public static DiscreteFilter deserializeFromJSON_sub(DataBridge.Reader o) throws IOException {
        ArrayList<String> choices = o.deserializeArrayList("choices", String.class);
        ArrayList<String> user_choices = o.deserializeArrayList("user_choices", String.class);
        ArrayList<String> user_not_choices = o.deserializeArrayList("user_not_choices", String.class);
        o.endObject();

        DiscreteFilter discreteFilter = new DiscreteFilter();
        discreteFilter.choices = choices;
        discreteFilter.user_choices = user_choices;
        discreteFilter.user_not_choices = user_not_choices;
        return discreteFilter;
    }

    public void updateFilterData(ArrayList<String> data) {
        ArrayList<String> choicesToRemove = new ArrayList<>();

        for(String c : choices) {
            if(!data.contains(c)) {
                choicesToRemove.add(c);
                this.user_choices.remove(c);
            }
        }

        for(String r : choicesToRemove) {
            this.choices.remove(r);
        }

        for(String c : data) {
            if(!choices.contains(c)) {
                this.choices.add(c);
                this.user_choices.add(c);
            }
        }
    }

    public boolean isIncluded(String data) {
        // Exact match required.
        for(String s : user_not_choices) {
            if(data.equals(s)) {
                return false;
            }
        }
        return true;
    }

    public String getIncludedString() {
        if(user_choices.size() == choices.size()) {
            return "Show All";
        }
        else if (user_choices.size() == 0) {
            return "Show None";
        }
        else {
            // Only show up to 3 items to save space.
            if(user_choices.size() <= 3) {
                StringBuilder s = new StringBuilder(user_choices.get(0));
                for(int i = 1; i < user_choices.size(); i++) {
                    s.append(", ").append(user_choices.get(i));
                }
                return s.toString();
            }
            else {
                return user_choices.get(0) + ", " + user_choices.get(1) + ", " + user_choices.get(2) + ", ...";
            }
        }
    }

    public Class<?> getDialogClass() {
        return DiscreteFilterDialog.class;
    }

    public String getFilterType() {
        return "!DISCRETE!";
    }
}