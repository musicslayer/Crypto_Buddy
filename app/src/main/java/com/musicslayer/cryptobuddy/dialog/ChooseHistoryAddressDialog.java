package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.widget.AppCompatButton;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.persistent.user.AddressHistory;
import com.musicslayer.cryptobuddy.data.persistent.user.AddressHistoryObj;
import com.musicslayer.cryptobuddy.data.persistent.user.PersistentUserDataStore;

public class ChooseHistoryAddressDialog extends BaseDialog {
    CryptoAddress currentDeleteHistoryCryptoAddress;

    public CryptoAddress user_CRYPTOADDRESS;

    public ChooseHistoryAddressDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.choose_history_address_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_choose_history_address);

        updateLayout();
    }

    public void updateLayout() {
        BaseDialogFragment confirmDeleteAddressHistoryDialogFragment = BaseDialogFragment.newInstance(ConfirmDeleteAddressHistoryDialog.class);
        confirmDeleteAddressHistoryDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                if(((ConfirmDeleteAddressHistoryDialog)dialog).isComplete) {
                    PersistentUserDataStore.getInstance(AddressHistory.class).removeAddressFromHistory(AddressHistory.getFromCryptoAddress(currentDeleteHistoryCryptoAddress));
                    updateLayout();
                }
            }
        });
        confirmDeleteAddressHistoryDialogFragment.restoreListeners(activity, "delete");

        TableLayout T = findViewById(R.id.choose_history_dialog_tableLayout);
        T.removeAllViews();

        for(AddressHistoryObj addressHistoryObj : AddressHistory.settings_address_history) {
            AppCompatButton B = new AppCompatButton(activity);
            B.setText(addressHistoryObj.toString());
            B.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
                public void onClickImpl(View v) {
                    PersistentUserDataStore.getInstance(AddressHistory.class).addAddressToHistory(addressHistoryObj);
                    user_CRYPTOADDRESS = addressHistoryObj.cryptoAddress;

                    isComplete = true;
                    dismiss();
                }
            });

            AppCompatButton B_DELETE = new AppCompatButton(activity);
            B_DELETE.setText("Delete");
            B_DELETE.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_delete_24, 0, 0, 0);
            B_DELETE.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
                @Override
                public void onClickImpl(View view) {
                    currentDeleteHistoryCryptoAddress = addressHistoryObj.cryptoAddress;
                    confirmDeleteAddressHistoryDialogFragment.show(activity, "delete");
                }
            });

            TableRow.LayoutParams TRP_DELETE = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TableRow TR_DELETE = new TableRow(activity);
            TR_DELETE.addView(B_DELETE, TRP_DELETE);

            TableRow.LayoutParams TRP = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TRP.setMargins(0,0,0,80);
            TableRow TR = new TableRow(activity);
            TR.addView(B, TRP);

            T.addView(TR_DELETE);
            T.addView(TR);
        }
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putParcelable("currentDeleteHistoryCryptoAddress", currentDeleteHistoryCryptoAddress);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            currentDeleteHistoryCryptoAddress = bundle.getParcelable("currentDeleteHistoryCryptoAddress");
        }
    }
}
