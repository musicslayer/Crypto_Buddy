package com.musicslayer.cryptobuddy.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.musicslayer.cryptobuddy.activity.BaseActivity;
import com.musicslayer.cryptobuddy.util.Reflect;

public class BaseDialogFragment extends DialogFragment implements DialogInterface.OnShowListener {
    public DialogInterface.OnShowListener SL;
    public DialogInterface.OnDismissListener DL;

    public static BaseDialogFragment newInstance(Class<?> clazz, Object... args) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("class", clazz);
        bundle.putSerializable("args", args);

        BaseDialogFragment fragment = new BaseDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onShow(@NonNull DialogInterface dialog) {
        if(SL != null) {
            SL.onShow(this.getDialog());
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        doDismiss(dialog);
    }

    public void doDismiss(@NonNull DialogInterface dialog) {
        if(DL != null) {
            DL.onDismiss(dialog);
        }
    }

    public void setOnShowListener(DialogInterface.OnShowListener sl) {
        SL = sl;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener dl) {
        DL = dl;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Object object = null;

        Bundle bundle = getArguments();
        if(bundle != null) {
            Class<?> clazz = (Class<?>)bundle.getSerializable("class");
            Object[] argArray = (Object[])bundle.getSerializable("args");

            object = Reflect.constructDialogInstance(clazz, getActivity(), argArray);
            ((Dialog)object).setOnShowListener(this);
        }

        return (Dialog)object;
    }

    public void show(Context context, String tag) {
        if(!isAdded()) {
            this.show(getFragmentManager(context), tag);
            getFragmentManager(context).executePendingTransactions();
        }
    }

    public void restoreListeners(Context context, String tag) {
        BaseDialogFragment gdf = (BaseDialogFragment)getFragmentManager(context).findFragmentByTag(tag);
        if (gdf != null) {
            gdf.setOnShowListener(SL);
            gdf.setOnDismissListener(DL);
        }
    }

    public void updateArguments(Class<?> clazz, Object... args) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("class", clazz);
        bundle.putSerializable("args", args);

        this.setArguments(bundle);
    }

    public boolean isShowing(Context context, String tag) {
        BaseDialogFragment gdf = (BaseDialogFragment)getFragmentManager(context).findFragmentByTag(tag);
        return gdf != null;
    }

    public static FragmentManager getFragmentManager(Context context) {
        while (!(context instanceof BaseActivity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        return ((BaseActivity)context).getSupportFragmentManager();
    }
}
