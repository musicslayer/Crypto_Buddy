package com.musicslayer.cryptobuddy.filter;

import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;

abstract public class Filter {
    abstract public void updateFilterData(Object data);
    abstract public boolean isIncluded(String data);
    abstract public String getIncludedString();
    abstract public Class<?> getDialogClass();

    public static Filter fromType(String type, String dataType) {
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
