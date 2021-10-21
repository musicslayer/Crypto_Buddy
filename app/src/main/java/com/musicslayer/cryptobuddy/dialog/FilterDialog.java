package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;

import com.musicslayer.cryptobuddy.filter.Filter;

abstract public class FilterDialog extends BaseDialog {
    public Filter filter;

    public FilterDialog(Activity activity, Filter filter) {
        super(activity);
        this.filter = filter;
    }
}
