package com.musicslayer.cryptobuddy.filter;

import com.musicslayer.cryptobuddy.dialog.DateFilterDialog;
import com.musicslayer.cryptobuddy.util.DateTime;
import com.musicslayer.cryptobuddy.util.Serialization;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class DateFilter extends Filter {
    public Date user_startDate;
    public Date user_endDate;

    public String serializeToJSON_sub() throws org.json.JSONException {
        return new JSONObject()
            .put("filterType", Serialization.string_serialize("!DATE!"))
            .put("user_startDate", Serialization.date_serialize(user_startDate))
            .put("user_endDate", Serialization.date_serialize(user_endDate))
            .toString();
    }

    public static DateFilter deserializeFromJSON_sub(String s) throws org.json.JSONException {
        JSONObject o = new JSONObject(s);
        Date user_startDate = Serialization.date_deserialize(o.getString("user_startDate"));
        Date user_endDate = Serialization.date_deserialize(o.getString("user_endDate"));

        DateFilter dateFilter = new DateFilter();
        dateFilter.user_startDate = user_startDate;
        dateFilter.user_endDate = user_endDate;
        return dateFilter;
    }

    public void updateFilterData(ArrayList<String> data) {
        // Dates are not updated based on table transactions
    }

    public boolean isIncluded(String data) {
        if(data == null) { return true; }

        long L = Long.parseLong(data);

        long S;
        long E;

        if(user_startDate == null) {
            S = Long.MIN_VALUE;
        }
        else {
            S = user_startDate.getTime();
        }

        if(user_endDate == null) {
            E = Long.MAX_VALUE;
        }
        else {
            E = user_endDate.getTime();
        }


        return (L >= S) && (L <= E);
    }

    public String getIncludedString() {
        String startString = user_startDate == null ? "" : "After: " + DateTime.toDateString(user_startDate);
        String endString = user_endDate == null ? "" : "Before: " + DateTime.toDateString(user_endDate);

        if(user_startDate == null && user_endDate == null) {
            return "Show All";
        }
        else if(user_startDate != null && user_endDate == null) {
            return startString;
        }
        else if(user_startDate == null) {
            return endString;
        }
        else {
            return startString + "\n" + endString;
        }
    }

    public Class<?> getDialogClass() {
        return DateFilterDialog.class;
    }
}