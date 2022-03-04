package com.musicslayer.cryptobuddy.view.table;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;

import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.dialog.BaseDialogFragment;
import com.musicslayer.cryptobuddy.dialog.ConfirmDeleteTransactionDialog;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.settings.setting.PriceDisplaySetting;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.transaction.Transaction;

public class TransactionTable extends Table {
    public BaseRow getRow(Transaction transaction) {
        return new TransactionTable.TransactionRow(getContext(), transaction);
    }

    public static boolean shouldAddForwardsPrice() {
        return "Forward".equals(PriceDisplaySetting.value) || "ForwardBackward".equals(PriceDisplaySetting.value);
    }

    public static boolean shouldAddBackwardsPrice() {
        return "ForwardBackward".equals(PriceDisplaySetting.value);
    }

    public TransactionTable(Context context) {
        this(context, null);
    }

    public TransactionTable(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        this.addColumn("delete", "Delete (Tap Twice)", null, -1);
        this.addColumn("action", "Action", "discrete", 1);
        this.addColumn("quantity", "Actioned Asset", "discrete", 1);
        this.addColumn("other_quantity", "Other Asset",  "discrete", 1);
        if(shouldAddForwardsPrice()) { this.addColumn("price", "Forward Price", "discrete", 1); }
        if(shouldAddBackwardsPrice()) { this.addColumn("other_price", "Backward Price", "discrete", 1); }
        this.addColumn("timestamp", "Timestamp", "date", 0);
        this.addColumn("info", "Info", "discrete", 1);

        doSortImpl();
        redrawHeaderRows();
    }

    class TransactionRow extends BaseRow {
        public TransactionRow(Context context) {
            super(context);
        }

        public TransactionRow(Context context, Transaction transaction) {
            super(context, transaction);
        }

        public void makeRow(Transaction transaction) {
            Context context = getContext();

            final int ii = TransactionTable.this.getChildCount() - numHeaderRows;

            BaseDialogFragment confirmDeleteTransactionDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteTransactionDialog.class);
            confirmDeleteTransactionDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(context) {
                @Override
                public void onDismissImpl(DialogInterface dialog) {
                    if(((ConfirmDeleteTransactionDialog)dialog).isComplete) {
                        StateObj.transactionArrayList.remove(transaction);
                        finishRows();

                        // Potentially perform more actions if a listener exists.
                        checkDeletion(transaction);
                    }
                }
            });
            confirmDeleteTransactionDialogFragment.restoreListeners(context, "delete" + ii);

            // Use a TextView that acts like a Button so heights are consistent.
            AppCompatTextView B_DELETE = new AppCompatTextView(context);
            final AppCompatTextView B_DELETE_F = B_DELETE;

            B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_12, 0, 0, 0);
            B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(context) {
                boolean state = false;

                @Override
                public void onClickImpl(View view) {
                    if(state) {
                        // Second click -> Confirm deletion. Also reset button status in case user backs out of deletion.
                        B_DELETE_F.setBackgroundResource(R.drawable.border);

                        confirmDeleteTransactionDialogFragment.show(context, "delete" + ii);
                    }
                    else {
                        // First click -> Set button status, and reset all other button statuses.
                        for(int i = numHeaderRows; i < TransactionTable.this.getChildCount(); i++) {
                            ViewGroup childRow = (ViewGroup)TransactionTable.this.getChildAt(i);

                            // The delete button is the first child.
                            View child = childRow.getChildAt(0);
                            child.setBackgroundResource(R.drawable.border_round);
                            child.getBackground().clearColorFilter();
                        }

                        B_DELETE_F.setBackgroundResource(R.drawable.border_red);
                    }

                    state = !state;
                }
            });
            B_DELETE.setBackgroundResource(R.drawable.border);

            TableRow.LayoutParams TL = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);

            AppCompatTextView t0 = new AppCompatTextView(context);
            t0.setText(transaction.action.toString());
            t0.setBackgroundResource(R.drawable.border);

            AppCompatTextView t1 = new AppCompatTextView(context);
            t1.setBackgroundResource(R.drawable.border);

            RichStringBuilder s1 = new RichStringBuilder(true);
            s1.appendAssetQuantity(transaction.actionedAssetQuantity);
            t1.setText(Html.fromHtml(s1.toString()));

            AppCompatTextView t2 = new AppCompatTextView(context);
            t2.setBackgroundResource(R.drawable.border);

            RichStringBuilder s2 = new RichStringBuilder(true);
            s2.appendAssetQuantity(transaction.otherAssetQuantity);
            t2.setText(Html.fromHtml(s2.toString()));

            AppCompatTextView t3 = new AppCompatTextView(context);
            t3.setText(transaction.forwardPrice.toString());
            t3.setBackgroundResource(R.drawable.border);

            AppCompatTextView t4 = new AppCompatTextView(context);
            t4.setText(transaction.backwardPrice.toString());
            t4.setBackgroundResource(R.drawable.border);

            AppCompatTextView t5 = new AppCompatTextView(context);
            t5.setText(transaction.timestamp.toString());
            t5.setBackgroundResource(R.drawable.border);

            AppCompatTextView t6 = new AppCompatTextView(context);
            t6.setText(transaction.info);
            t6.setBackgroundResource(R.drawable.border);

            this.addView(B_DELETE, TL);
            this.addView(t0, TL);
            this.addView(t1, TL);
            this.addView(t2, TL);
            if(shouldAddForwardsPrice()){this.addView(t3, TL);}
            if(shouldAddBackwardsPrice()){this.addView(t4, TL);}
            this.addView(t5, TL);
            this.addView(t6, TL);
        }
    }
}