package com.musicslayer.cryptobuddy.filter;

import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.serialize.Serialization;

import java.util.ArrayList;

abstract public class Filter implements Serialization.SerializableToJSON {
    abstract public void updateFilterData(ArrayList<String> data);
    abstract public boolean isIncluded(String data);
    abstract public String getIncludedString();
    abstract public Class<?> getDialogClass();

    // Each subclass is serialized and deserialized differently.
    abstract public String serializeToJSON_sub() throws org.json.JSONException;

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        return serializeToJSON_sub();
    }

    public static Filter deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String filterType = Serialization.string_deserialize(o.getString("filterType"));
        if("!DISCRETE!".equals(filterType)) {
            return DiscreteFilter.deserializeFromJSON_sub(s);
        }
        else if("!DATE!".equals(filterType)) {
            return DateFilter.deserializeFromJSON_sub(s);
        }
        else {
            return null;
        }
    }

    // Used by "Table.java". Types are different than the ones used to serialize.
    public static Filter fromType(String type) {
        if("discrete".equals(type)) {
            return new DiscreteFilter();
        }
        else if("date".equals(type)) {
            return new DateFilter();
        }
        else {
            return null;
        }
    }

    public BaseDialogFragment getGenericDialogFragment() {
        return BaseDialogFragment.newInstance(getDialogClass(), this);
    }
}
