package com.musicslayer.cryptobuddy.view.table;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.settings.setting.PriceDisplaySetting;
import com.musicslayer.cryptobuddy.transaction.Transaction;

import java.util.ArrayList;

public class ExchangeTable extends Table {
    public BaseRow getRow(Transaction transaction) {
        return new ExchangeRow(getContext(), transaction);
    }

    public static boolean shouldAddForwardsPrice() {
        return "Forward".equals(PriceDisplaySetting.value) || "ForwardBackward".equals(PriceDisplaySetting.value);
    }

    public static boolean shouldAddBackwardsPrice() {
        return "ForwardBackward".equals(PriceDisplaySetting.value);
    }

    public ExchangeTable(Context context) {
        this(context, null);
    }

    public ExchangeTable(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

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

    public void addRowsFromExchangeDataArray(ArrayList<ExchangeData> exchangeDataArrayList) {
        if(exchangeDataArrayList == null) { return; }

        boolean isAny = false;
        for(ExchangeData exchangeData : exchangeDataArrayList) {
            if(exchangeData != null && exchangeData.isTransactionsComplete()) {
                isAny = true;
                addRowsImpl(exchangeData.transactionArrayList);
            }
        }

        if(isAny) {
            finishRows();
        }
    }

    static class ExchangeRow extends BaseRow {
        public ExchangeRow(Context context) {
            super(context);
        }

        public ExchangeRow(Context context, Transaction transaction) {
            super(context, transaction);
        }

        public void makeRow(Transaction transaction) {
            Context context = getContext();

            TextView t0 = new TextView(context);
            t0.setText(transaction.action.toString());
            t0.setBackgroundResource(R.drawable.border);

            TextView t1 = new TextView(context);
            t1.setBackgroundResource(R.drawable.border);

            RichStringBuilder s1 = new RichStringBuilder(true);
            s1.appendAssetQuantity(transaction.actionedAssetQuantity);
            t1.setText(Html.fromHtml(s1.toString()));

            TextView t2 = new TextView(context);
            t2.setBackgroundResource(R.drawable.border);

            RichStringBuilder s2 = new RichStringBuilder(true);
            s2.appendAssetQuantity(transaction.otherAssetQuantity);
            t2.setText(Html.fromHtml(s2.toString()));

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
            if(shouldAddForwardsPrice()){this.addView(t3);}
            if(shouldAddBackwardsPrice()){this.addView(t4);}
            this.addView(t5);
            this.addView(t6);
        }
    }
}