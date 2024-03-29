package com.musicslayer.cryptobuddy.filter;

import android.os.Parcel;
import android.os.Parcelable;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;

import java.io.IOException;
import java.util.ArrayList;

abstract public class Filter implements DataBridge.SerializableToJSON, Parcelable {
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
    abstract public void serializeToJSON_sub(DataBridge.Writer o) throws IOException;

    @Override
    public void serializeToJSON(DataBridge.Writer o) throws IOException {
        serializeToJSON_sub(o);
    }

    public static Filter deserializeFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();
        String filterType = o.deserialize("filterType", String.class);
        if("!DISCRETE!".equals(filterType)) {
            return DiscreteFilter.deserializeFromJSON_sub(o);
        }
        else if("!DATE!".equals(filterType)) {
            return DateFilter.deserializeFromJSON_sub(o);
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
