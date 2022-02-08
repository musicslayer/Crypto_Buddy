package com.musicslayer.cryptobuddy.view.asset;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteAssetDialog;

public class FiatView extends CrashLinearLayout {
    public Fiat fiat;
    boolean canDelete;
    OnDeleteListener onDeleteListener;

    public FiatView(Context context) {
        this(context, null);
    }

    public FiatView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.makeLayout();
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    public void setFiat(Fiat fiat, boolean canDelete) {
        this.fiat = fiat;
        this.canDelete = canDelete;

        this.removeAllViews();
        this.makeLayout();
    }

    public void makeLayout() {
        this.setOrientation(VERTICAL);

        Context context = getContext();

        if(fiat == null) {
            setVisibility(GONE);
        }
        else {
            setVisibility(VISIBLE);

            BaseDialogFragment confirmDeleteAssetDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteAssetDialog.class, "Fiat");
            confirmDeleteAssetDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
                @Override
                public void onDismissImpl(DialogInterface dialog) {
                    if(((ConfirmDeleteAssetDialog)dialog).isComplete) {
                        if(onDeleteListener != null) {
                            onDeleteListener.onDelete(fiat);
                        }
                    }
                }
            });
            confirmDeleteAssetDialogFragment.restoreListeners(context, "confirm_delete_asset");

            AppCompatButton B_DELETE = new AppCompatButton(context);
            B_DELETE.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_24, 0, 0, 0);
            B_DELETE.setText("Delete");
            B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
                public void onClickImpl(View v) {
                    confirmDeleteAssetDialogFragment.show(context, "confirm_delete_asset");
                }
            });

            String text = "Fiat Info:\n" +
                "  Name = " + fiat.getDisplayName() + "\n" +
                "  Symbol = " + fiat.getName() + "\n" +
                "  Decimals = " + fiat.getScale();

            AppCompatTextView T = new AppCompatTextView(context);
            T.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            T.setText(text);

            if(canDelete) {
                this.addView(B_DELETE);
            }
            this.addView(T);
        }
    }

    abstract public static class OnDeleteListener {
        abstract public void onDelete(Asset asset);
    }
}
