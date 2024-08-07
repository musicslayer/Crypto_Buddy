package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.api.price.CryptoPrice;
import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;
import com.musicslayer.cryptobuddy.view.asset.SelectAndSearchView;

import java.util.ArrayList;
import java.util.HashMap;

public class AddressInfoDialog extends BaseDialog {
    ArrayList<CryptoAddress> cryptoAddressArrayList;
    HashMap<CryptoAddress, AddressData> addressDataMap;
    int cryptoAddressIdx;

    public AddressInfoDialog(Activity activity, ArrayList<CryptoAddress> cryptoAddressArrayList) {
        super(activity);
        this.cryptoAddressArrayList = cryptoAddressArrayList;
        this.addressDataMap = StateObj.addressDataMap;
    }

    public int getBaseViewID() {
        return R.id.address_info_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_address_info);

        if(savedInstanceState == null) {
            StateObj.priceData = null;
        }

        SelectAndSearchView fssv = findViewById(R.id.address_info_dialog_fiatSelectAndSearchView);
        fssv.setIncludesFiat(true);
        fssv.setIncludesCoin(false);
        fssv.setIncludesToken(false);
        fssv.setCompleteOptions();
        fssv.chooseFiat("BASE");

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this.activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Retrieving Fiat Values...");

                CryptoAddress cryptoAddress = cryptoAddressArrayList.get(cryptoAddressIdx);
                AddressData addressData = HashMapUtil.getValueFromMap(addressDataMap, cryptoAddress);
                ArrayList<AssetQuantity> deltaArray = addressData.currentBalanceArrayList;

                HashMap<Asset, AssetAmount> deltaMap = new HashMap<>();
                for(AssetQuantity assetQuantity : deltaArray) {
                    HashMapUtil.putValueInMap(deltaMap, assetQuantity.asset, assetQuantity.assetAmount);
                }

                ArrayList<Asset> assetKeySet = new ArrayList<>(deltaMap.keySet());
                Fiat priceFiat = (Fiat)fssv.getChosenAsset();
                CryptoPrice cryptoPrice = new CryptoPrice(assetKeySet, priceFiat);

                PriceData newPriceData = PriceData.getPriceData(cryptoPrice);

                ProgressDialogFragment.setValue(DataBridge.serialize(newPriceData, PriceData.class));
            }
        });

        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                PriceData newPriceData = DataBridge.deserialize(ProgressDialogFragment.getValue(), PriceData.class);

                if(!newPriceData.isPriceFull()) {
                    ToastUtil.showToast("incomplete_price_data");
                }

                StateObj.priceData = newPriceData;

                updateLayout();
            }
        });
        progressDialogFragment.restoreListeners(this.activity, "progress");

        Button B_PRICES = findViewById(R.id.address_info_dialog_priceButton);
        B_PRICES.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                CryptoAddress cryptoAddress = cryptoAddressArrayList.get(cryptoAddressIdx);
                AddressData addressData = HashMapUtil.getValueFromMap(addressDataMap, cryptoAddress);
                ArrayList<AssetQuantity> deltaArray = addressData.currentBalanceArrayList;

                if(deltaArray == null || deltaArray.isEmpty()) {
                    ToastUtil.showToast("no_balances_found");
                    return;
                }
                else if(fssv.getChosenAsset() == null) {
                    ToastUtil.showToast("must_choose_assets");
                    return;
                }

                progressDialogFragment.show(activity, "progress");
            }
        });

        ArrayList<String> options = new ArrayList<>();
        for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
            options.add(cryptoAddress.toString());
        }

        TextView T = findViewById(R.id.address_info_dialog_textView);

        BorderedSpinnerView bsv = findViewById(R.id.address_info_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                cryptoAddressIdx = pos;
                updateLayout();
            }
        });

        if(cryptoAddressArrayList.size() == 1) {
            bsv.setVisibility(View.GONE);
        }

        if(cryptoAddressArrayList.size() == 0) {
            bsv.setVisibility(View.GONE);
            T.setText("No addresses found.");
        }
    }

    public void updateLayout() {
        CryptoAddress cryptoAddress = cryptoAddressArrayList.get(cryptoAddressIdx);
        AddressData addressData = HashMapUtil.getValueFromMap(addressDataMap, cryptoAddress);

        TextView T = findViewById(R.id.address_info_dialog_textView);
        T.setText(Html.fromHtml(addressData.getInfoString(StateObj.priceData, true)));
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putInt("cryptoAddressIdx", cryptoAddressIdx);
        return bundle;
    }

    @Override
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            cryptoAddressIdx = bundle.getInt("cryptoAddressIdx");
        }
    }
}