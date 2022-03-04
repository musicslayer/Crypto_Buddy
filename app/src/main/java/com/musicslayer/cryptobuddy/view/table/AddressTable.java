package com.musicslayer.cryptobuddy.view.table;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.transaction.Transaction;

import java.util.ArrayList;

public class AddressTable extends Table {
    public BaseRow getRow(Transaction transaction) {
        return new AddressTable.AddressRow(getContext(), transaction);
    }

    public AddressTable(Context context) {
        this(context, null);
    }

    public AddressTable(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        this.addColumn("action", "Action","discrete", 1);
        this.addColumn("quantity", "Asset", "discrete", 1);
        this.addColumn("timestamp", "Timestamp", "date", 0);
        this.addColumn("info", "Info", "discrete", 1);

        doSortImpl();
        redrawHeaderRows();
    }

    public void addRowsFromAddressDataArray(ArrayList<AddressData> addressDataArrayList) {
        if(addressDataArrayList == null) { return; }

        boolean isAny = false;
        for(AddressData addressData : addressDataArrayList) {
            if(addressData != null && addressData.isTransactionsComplete()) {
                isAny = true;
                addRowsImpl(addressData.transactionArrayList);
            }
        }

        if(isAny) {
            finishRows();
        }
    }

    static class AddressRow extends BaseRow {
        public AddressRow(Context context) {
            super(context);
        }

        public AddressRow(Context context, Transaction transaction) {
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