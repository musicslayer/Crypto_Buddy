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
import com.musicslayer.cryptobuddy.asset.crypto.coin.Coin;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteAssetDialog;

public class CoinView extends CrashLinearLayout {
    public Coin coin;
    public boolean canDelete;
    CoinView.OnDeleteListener onDeleteListener;

    public CoinView(Context context) {
        this(context, null);
    }

    public CoinView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.makeLayout();
    }

    public void setOnDeleteListener(CoinView.OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    public void setCoin(Coin coin, boolean canDelete) {
        this.coin = coin;
        this.canDelete = canDelete;

        this.removeAllViews();
        this.makeLayout();
    }

    public void makeLayout() {
        this.setOrientation(VERTICAL);

        Context context = getContext();

        if(coin == null) {
            setVisibility(GONE);
        }
        else {
            setVisibility(VISIBLE);

            BaseDialogFragment confirmDeleteAssetDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteAssetDialog.class, "Coin");
            confirmDeleteAssetDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
                @Override
                public void onDismissImpl(DialogInterface dialog) {
                    if(((ConfirmDeleteAssetDialog)dialog).isComplete) {
                        if(onDeleteListener != null) {
                            onDeleteListener.onDelete(coin);
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

            String text = "Coin Info:\n" +
                "  Name = " + coin.getDisplayName() + "\n" +
                "  Symbol = " + coin.getName() + "\n" +
                "  Decimals = " + coin.getScale();

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
