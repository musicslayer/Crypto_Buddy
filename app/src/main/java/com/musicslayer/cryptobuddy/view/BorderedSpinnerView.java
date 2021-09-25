package com.musicslayer.cryptobuddy.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.musicslayer.cryptobuddy.R;

import java.util.ArrayList;

public class BorderedSpinnerView extends LinearLayout {
    public Context context;
    public Spinner spinner;

    public BorderedSpinnerView(Context context) {
        this(context, null);
    }

    public BorderedSpinnerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        this.setBackgroundResource(R.drawable.border_tight);

        spinner = new Spinner(context);

        // Pick a reasonable minimum width.
        spinner.setMinimumWidth(400);
        spinner.setDropDownWidth(LayoutParams.MATCH_PARENT);

        this.addView(spinner);
    }

    public void setOptions(String option) {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, new String[] {option});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void setOptions(String[] options) {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void setOptions(ArrayList<String> options) {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, options.toArray(new String[0]));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener) {
        this.spinner.setOnItemSelectedListener(onItemSelectedListener);

        // If there is at least one option, choose the first one by default.
        if(this.spinner.getAdapter().getCount() > 0) {
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
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)this.getLayoutParams();
        if(params == null) {
            params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        params.setMargins(left, top, right, bottom);
        this.setLayoutParams(params);
    }

    @Override
    public Parcelable onSaveInstanceState()
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putInt("selection", this.spinner.getSelectedItemPosition());

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state)
    {
        if (state instanceof Bundle) // implicit null check
        {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");

            this.setSelection(bundle.getInt("selection"));
        }
        super.onRestoreInstanceState(state);
    }
}
