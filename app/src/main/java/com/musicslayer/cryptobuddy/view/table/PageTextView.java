package com.musicslayer.cryptobuddy.view.table;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

// A workaround to have a classic FloatingActionButton with text inside.
public class PageTextView extends ConstraintLayout {
    FloatingActionButton fab;
    AppCompatTextView T;

    public PageTextView(Context context) {
        this(context, null);
    }

    public PageTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        makeLayout();
    }

    public void makeLayout() {
        Context context = getContext();

        fab = new FloatingActionButton(context);
        fab.setId(ViewCompat.generateViewId());

        T = new AppCompatTextView(context);
        T.setId(ViewCompat.generateViewId());

        addView(fab);
        addView(T);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(T.getId(),ConstraintSet.LEFT,fab.getId(),ConstraintSet.LEFT,0);
        constraintSet.connect(T.getId(),ConstraintSet.RIGHT,fab.getId(),ConstraintSet.RIGHT,0);
        constraintSet.connect(T.getId(),ConstraintSet.TOP,fab.getId(),ConstraintSet.TOP,0);
        constraintSet.connect(T.getId(),ConstraintSet.BOTTOM,fab.getId(),ConstraintSet.BOTTOM,0);
        constraintSet.applyTo(this);
    }

    public void setGravity(int gravity) {
        T.setGravity(gravity);
    }

    public void setText(String s) {
        T.setText(s);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        fab.setOnClickListener(onClickListener);
        T.setOnClickListener(onClickListener);
    }
}
