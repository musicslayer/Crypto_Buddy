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
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashLinearLayout;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteAssetDialog;

public class TokenView extends CrashLinearLayout {
    public Token token;
    public boolean canDelete;
    TokenView.OnDeleteListener onDeleteListener;

    public TokenView(Context context) {
        this(context, null);
    }

    public TokenView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.makeLayout();
    }

    public void setOnDeleteListener(TokenView.OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    public void setToken(Token token, boolean canDelete) {
        this.token = token;
        this.canDelete = canDelete;

        this.removeAllViews();
        this.makeLayout();
    }

    public void makeLayout() {
        this.setOrientation(VERTICAL);

        Context context = getContext();

        if(token == null) {
            setVisibility(GONE);
        }
        else {
            setVisibility(VISIBLE);

            BaseDialogFragment confirmDeleteAssetDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteAssetDialog.class, "Token");
            confirmDeleteAssetDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
                @Override
                public void onDismissImpl(DialogInterface dialog) {
                    if(((ConfirmDeleteAssetDialog)dialog).isComplete) {
                        if(onDeleteListener != null) {
                            onDeleteListener.onDelete(token);
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

            String text = "Token Info:\n" +
                "  ID / Contract Address = " + token.getContractAddress() + "\n" +
                "  Name = " + token.getDisplayName() + "\n" +
                "  Symbol = " + token.getName() + "\n" +
                "  Decimals = " + token.getScale();

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
