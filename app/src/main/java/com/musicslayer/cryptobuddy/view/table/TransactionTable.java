package com.musicslayer.cryptobuddy.view.table;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteTransactionDialog;
import com.musicslayer.cryptobuddy.settings.PriceDisplaySetting;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.view.AssetTextView;

import java.lang.ref.WeakReference;

public class TransactionTable extends Table {
    WeakReference<BaseDialogFragment> confirmDeleteTransactionDialogFragment_w;

    public BaseRow getRow(Context context, Transaction transaction) {
        return new TransactionTable.TransactionRow(context, transaction);
    }

    public static boolean shouldAddBackwardsPrice() {
        return "ForwardBackward".equals(PriceDisplaySetting.value);
    }

    public TransactionTable(Context context) {
        this(context, null);
    }

    public TransactionTable(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        this.addColumn(context,"delete", "Delete (Tap Twice)", null, -1);
        this.addColumn(context,"action", "Action", "discrete", 1);
        this.addColumn(context,"quantity", "Actioned Asset", "discrete", 1);
        this.addColumn(context,"other_quantity", "Other Asset",  "discrete", 1);
        this.addColumn(context,"price", "Forward Price", "discrete", 1);
        if(shouldAddBackwardsPrice()) { this.addColumn(context,"other_price", "Backward Price", "discrete", 1); }
        this.addColumn(context,"timestamp", "Timestamp", "date", 0);
        this.addColumn(context,"info", "Info", "discrete", 1);
    }

    class TransactionRow extends BaseRow {
        public TransactionRow(Context context) {
            super(context);
        }

        public TransactionRow(Context context, Transaction transaction) {
            super(context, transaction);
        }

        public void makeRow(Context context, Transaction transaction) {
            final int ii = TransactionTable.this.getChildCount() - numHeaderRows;

            confirmDeleteTransactionDialogFragment_w = new WeakReference<>(BaseDialogFragment.newInstance(ConfirmDeleteTransactionDialog.class));
            confirmDeleteTransactionDialogFragment_w.get().setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
                @Override
                public void onDismissImpl(DialogInterface dialog) {
                    if(((ConfirmDeleteTransactionDialog)dialog).isComplete) {
                        transactionArrayList.remove(transaction);
                        maskedTransactionArrayList.remove(transaction);
                        finishRows(context);

                        // Potentially perform more actions if a listener exists.
                        checkDeletion(transaction);
                    }
                }
            });
            confirmDeleteTransactionDialogFragment_w.get().restoreListeners(context, "delete" + ii);

            AppCompatButton B_DELETE = new AppCompatButton(context);
            final AppCompatButton B_II = B_DELETE;

            B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_12, 0, 0, 0);
            B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
                @Override
                public void onClickImpl(View view) {
                    if(DrawableCompat.getColorFilter(B_II.getBackground()) != null) {
                        // Reset button status, in case user backs out of deletion.
                        B_II.setBackgroundResource(R.drawable.border_round);
                        B_II.getBackground().clearColorFilter();

                        confirmDeleteTransactionDialogFragment_w.get().show(context, "delete" + ii);
                    }
                    else {
                        // Set button status, and reset all other button statuses.
                        for(int i = numHeaderRows; i < TransactionTable.this.getChildCount(); i++) {
                            ViewGroup childRow = (ViewGroup)TransactionTable.this.getChildAt(i);

                            // The delete button is the first child.
                            View child = childRow.getChildAt(0);
                            child.setBackgroundResource(R.drawable.border_round);
                            child.getBackground().clearColorFilter();
                        }

                        B_II.setBackgroundResource(R.drawable.border_round_red);
                        B_II.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(0xFFFF0000, BlendModeCompat.SRC_ATOP));
                    }
                }
            });
            B_DELETE.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

            TextView t0 = new TextView(context);
            t0.setText(transaction.action.toString());
            t0.setBackgroundResource(R.drawable.border);

            AssetTextView t1 = new AssetTextView(context, transaction.isActionedAssetLoss(), transaction.actionedAssetQuantity);
            t1.setBackgroundResource(R.drawable.border);

            AssetTextView t2 = new AssetTextView(context, transaction.isOtherAssetLoss(), transaction.otherAssetQuantity);
            t2.setBackgroundResource(R.drawable.border);

            TextView t3 = new TextView(context);
            t3.setText(transaction.forwardPrice.toString());
            t3.setBackgroundResource(R.drawable.border);

            TextView t4 = new TextView(context);
            t4.setText(transaction.backwardPrice.toString());
            t4.setBackgroundResource(R.drawable.border);

            TextView t5 = new TextView(context);
            t5.setText(transaction.timestamp.toString());
            t5.setBackgroundResource(R.drawable.border);

            TextView t6 = new TextView(context);
            t6.setText(transaction.info);
            t6.setBackgroundResource(R.drawable.border);

            this.addView(B_DELETE);
            this.addView(t0);
            this.addView(t1);
            this.addView(t2);
            this.addView(t3);
            if(shouldAddBackwardsPrice()){this.addView(t4);}
            this.addView(t5);
            this.addView(t6);
        }
    }
}