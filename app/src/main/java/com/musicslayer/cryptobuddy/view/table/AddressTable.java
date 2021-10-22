package com.musicslayer.cryptobuddy.view.table;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.transaction.Transaction;
import com.musicslayer.cryptobuddy.view.AssetTextView;

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

            TextView t0 = new TextView(context);
            t0.setText(transaction.action.toString());
            t0.setBackgroundResource(R.drawable.border);

            AssetTextView t1 = new AssetTextView(context, transaction.isActionedAssetLoss(), transaction.actionedAssetQuantity);
            t1.setBackgroundResource(R.drawable.border);

            TextView t2 = new TextView(context);
            t2.setText(transaction.timestamp.toString());
            t2.setBackgroundResource(R.drawable.border);

            TextView t3 = new TextView(context);
            t3.setText(transaction.info);
            t3.setBackgroundResource(R.drawable.border);

            this.addView(t0);
            this.addView(t1);
            this.addView(t2);
            this.addView(t3);
        }
    }
}