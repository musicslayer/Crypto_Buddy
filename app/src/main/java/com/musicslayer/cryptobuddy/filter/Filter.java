package com.musicslayer.cryptobuddy.filter;

import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.data.DataBridge;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.data.Serialization;

import java.util.ArrayList;

abstract public class Filter implements Serialization.SerializableToJSON, Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getType());
    }

    public static final Parcelable.Creator<Filter> CREATOR = new Parcelable.Creator<Filter>() {
        @Override
        public Filter createFromParcel(Parcel in) {
            String type = in.readString();
            return Filter.fromType(type);
        }

        @Override
        public Filter[] newArray(int size) {
            return new Filter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    abstract public void updateFilterData(ArrayList<String> data);
    abstract public boolean isIncluded(String data);
    abstract public String getIncludedString();
    abstract public Class<?> getDialogClass();
    abstract public String getFilterType();

    // Each subclass is serialized and deserialized differently.
    abstract public String serializeToJSON_sub() throws org.json.JSONException;

    public static String serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String serializeToJSON() throws org.json.JSONException {
        return serializeToJSON_sub();
    }

    public static Filter deserializeFromJSON(String s, String version) throws org.json.JSONException {
        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
        String filterType = o.deserialize("filterType", String.class);
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

    // Used by "Table.java". Types are different than the ones used to serialize.
    public String getType() {
        if(this instanceof DiscreteFilter) {
            return "discrete";
        }
        else if(this instanceof DateFilter) {
            return "date";
        }
        else {
            return null;
        }
    }

    public BaseDialogFragment getGenericDialogFragment() {
        return BaseDialogFragment.newInstance(getDialogClass(), this);
    }
}
