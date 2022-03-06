package com.musicslayer.cryptobuddy.view.table;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.exchange.ExchangeData;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.transaction.Transaction;

import java.util.ArrayList;

public class ExchangeTable extends Table {
    public BaseRow getRow(Transaction transaction) {
        return new ExchangeTable.ExchangeRow(getContext(), transaction);
    }

    public ExchangeTable(Context context) {
        this(context, null);
    }

    public ExchangeTable(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        this.addColumn("action", "Action","discrete", 1);
        this.addColumn("quantity", "Asset", "discrete", 1);
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

            AppCompatTextView t0 = new AppCompatTextView(context);
            t0.setText(transaction.action.toString());
            t0.setBackgroundResource(R.drawable.border);

            AppCompatTextView t1 = new AppCompatTextView(context);
            t1.setBackgroundResource(R.drawable.border);

            RichStringBuilder s1 = new RichStringBuilder(true);
            s1.appendAssetQuantity(transaction.actionedAssetQuantity);
            t1.setText(Html.fromHtml(s1.toString()));

            AppCompatTextView t2 = new AppCompatTextView(context);
            t2.setText(transaction.timestamp.toString());
            t2.setBackgroundResource(R.drawable.border);

            AppCompatTextView t3 = new AppCompatTextView(context);
            t3.setText(transaction.info);
            t3.setBackgroundResource(R.drawable.border);

            this.addView(t0);
            this.addView(t1);
            this.addView(t2);
            this.addView(t3);
        }
    }
}