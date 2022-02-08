package com.musicslayer.cryptobuddy.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;

import java.util.ArrayList;

public class BorderedSpinnerView extends CrashLinearLayout {
    public Spinner spinner;

    public BorderedSpinnerView(Context context) {
        this(context, null);
    }

    public BorderedSpinnerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.setBackgroundResource(R.drawable.border_tight);

        spinner = new Spinner(context);

        // Pick a reasonable minimum width.
        spinner.setMinimumWidth(400);
        spinner.setDropDownWidth(LayoutParams.MATCH_PARENT);

        this.addView(spinner);
    }

    public void setOptions(String option) {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(), R.layout.wrapped_text_dropdown_item, R.id.wrapped_text_dropdown_item_textView, new String[] {option});
        adapter.setDropDownViewResource(R.layout.wrapped_text_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void setOptions(String[] options) {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(), R.layout.wrapped_text_dropdown_item, R.id.wrapped_text_dropdown_item_textView, options);
        adapter.setDropDownViewResource(R.layout.wrapped_text_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void setOptions(ArrayList<String> options) {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(), R.layout.wrapped_text_dropdown_item, R.id.wrapped_text_dropdown_item_textView, options.toArray(new String[0]));
        adapter.setDropDownViewResource(R.layout.wrapped_text_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener) {
        this.spinner.setOnItemSelectedListener(onItemSelectedListener);

        // If there is at least one option, choose the first one by default.
        if(this.spinner.getAdapter() != null && this.spinner.getAdapter().getCount() > 0) {
            setSelection(0);
        }
    }

    public void setSelection(int selection) {
        if(this.spinner.getOnItemSelectedListener() != null) {
            this.spinner.getOnItemSelectedListener().onItemSelected(this.spinner, this.spinner.getSelectedView(), selection, 0);
        }
        this.spinner.setSelection(selection);
    }

    public void setMargins(int left, int top, int right, int bottom) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)this.getLayoutParams();
        if(params == null) {
            params = new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        params.setMargins(left, top, right, bottom);
        this.setLayoutParams(params);
    }

    // If this class is used programmatically, these functions won't be called.
    // The caller is responsible for restoring the selection itself.
    @Override
    public Parcelable onSaveInstanceStateImpl(Parcelable state) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", state);
        bundle.putInt("selection", this.spinner.getSelectedItemPosition());

        return bundle;
    }

    @Override
    public Parcelable onRestoreInstanceStateImpl(Parcelable state) {
        if(state instanceof Bundle) { // implicit null check
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");

            this.setSelection(bundle.getInt("selection"));
        }
        return state;
    }
}
