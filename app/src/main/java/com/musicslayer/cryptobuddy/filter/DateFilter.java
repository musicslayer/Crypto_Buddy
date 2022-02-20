package com.musicslayer.cryptobuddy.filter;

import com.musicslayer.cryptobuddy.data.DataBridge;
import com.musicslayer.cryptobuddy.dialog.DateFilterDialog;
import com.musicslayer.cryptobuddy.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.Date;

public class DateFilter extends Filter {
    public Date user_startDate;
    public Date user_endDate;

    public String serializeToJSON_sub() throws org.json.JSONException {
        return new DataBridge.JSONObjectDataBridge()
            .serialize("filterType", getFilterType(), String.class)
            .serialize("user_startDate", user_startDate, Date.class)
            .serialize("user_endDate", user_endDate, Date.class)
            .toStringOrNull();
    }

    public static DateFilter deserializeFromJSON_sub(String s) throws org.json.JSONException {
        DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
        Date user_startDate = o.deserialize("user_startDate", Date.class);
        Date user_endDate = o.deserialize("user_endDate", Date.class);

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
        String startString = user_startDate == null ? "" : "After: " + DateTimeUtil.toDateString(user_startDate);
        String endString = user_endDate == null ? "" : "Before: " + DateTimeUtil.toDateString(user_endDate);

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

    public String getFilterType() {
        return "!DATE!";
    }
}