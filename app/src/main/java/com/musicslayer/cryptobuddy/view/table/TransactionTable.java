package com.musicslayer.cryptobuddy.view.table;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.persistence.Settings;
import com.musicslayer.cryptobuddy.view.AssetTextView;

public class TransactionTable extends Table {
    public BaseRow getRow(Context context, Transaction transaction) {
        return new TransactionTable.TransactionRow(context, transaction);
    }

    public static boolean shouldAddBackwardsPrice() {
        return "ForwardBackward".equals(Settings.setting_price);
    }

    public TransactionTable(Context context) {
        this(context, null);
    }

    public TransactionTable(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        this.addColumn("action", "Action", "discrete", 1);
        this.addColumn("quantity", "Actioned Asset", "discrete", 1);
        this.addColumn("other_quantity", "Other Asset",  "discrete", 1);
        this.addColumn("price", "Forward Price", "discrete", 1);
        if(shouldAddBackwardsPrice()) { this.addColumn("other_price", "Backward Price", "discrete", 1); }
        this.addColumn("timestamp", "Timestamp", "date", 0);
        this.addColumn("info", "Info", "discrete", 1);
    }

    static class TransactionRow extends BaseRow {
        public TransactionRow(Context context) {
            super(context);
        }

        public TransactionRow(Context context, Transaction transaction) {
            super(context, transaction);
        }

        public void makeRow(Context context, Transaction transaction) {
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