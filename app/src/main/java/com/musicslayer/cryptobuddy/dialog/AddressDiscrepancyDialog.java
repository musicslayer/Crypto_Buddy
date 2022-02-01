package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.api.price.BulkPriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.crypto.Crypto;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class AddressDiscrepancyDialog extends BaseDialog {
    ArrayList<CryptoAddress> cryptoAddressArrayList;
    HashMap<CryptoAddress, AddressData> addressDataMap;
    int cryptoAddressIdx;

    HashMap<Asset, AssetAmount> deltaMap = new HashMap<>();
    HashMap<Asset, AssetAmount> priceMap = new HashMap<>();

    public AddressDiscrepancyDialog(Activity activity, ArrayList<CryptoAddress> cryptoAddressArrayList) {
        super(activity);
        this.cryptoAddressArrayList = cryptoAddressArrayList;
        this.addressDataMap = StateObj.addressDataMap;
    }

    public int getBaseViewID() {
        return R.id.address_discrepancy_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_address_discrepancy);

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this.activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Calculating Total...");

                HashMap<Asset, AssetAmount> newPriceMap = new HashMap<>();

                ArrayList<Asset> keySet = new ArrayList<>(deltaMap.keySet());
                Asset.sortAscendingByType(keySet);

                // For now, USD is the only fiat, and it's price is 1 by definition.
                // Take it out of the array and deal with it ourselves.
                ArrayList<Crypto> cryptoKeySet = new ArrayList<>();
                for(Asset asset : keySet) {
                    if(asset instanceof Fiat) {
                        HashMapUtil.putValueInMap(newPriceMap, asset, new AssetAmount("1"));
                    }
                    else if(asset instanceof Crypto) {
                        cryptoKeySet.add((Crypto)asset);
                    }
                }

                BulkPriceData bulkPriceData = BulkPriceData.getBulkPriceData(cryptoKeySet);
                if(bulkPriceData.isPriceComplete()) {
                    HashMap<Crypto, AssetQuantity> priceHashMap = bulkPriceData.priceHashMap;
                    for(Crypto crypto : priceHashMap.keySet()) {
                        AssetQuantity price = HashMapUtil.getValueFromMap(priceHashMap, crypto);
                        if(price != null) {
                            HashMapUtil.putValueInMap(newPriceMap, crypto, price.assetAmount);
                        }
                    }
                }

                ProgressDialogFragment.setValue(Serialization.serializeHashMap(newPriceMap));
            }
        });

        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                HashMap<Asset, AssetAmount> newPriceMap = Serialization.deserializeHashMap(ProgressDialogFragment.getValue(), Asset.class, AssetAmount.class);

                if(newPriceMap.size() != deltaMap.size()) {
                    ToastUtil.showToast(activity,"incomplete_price_data");
                }

                priceMap.clear();
                for(Asset asset : newPriceMap.keySet()) {
                    HashMapUtil.putValueInMap(priceMap, asset, newPriceMap.get(asset));
                }

                CryptoAddress cryptoAddress = cryptoAddressArrayList.get(cryptoAddressIdx);
                AddressData addressData = HashMapUtil.getValueFromMap(addressDataMap, cryptoAddress);

                updateLayout(addressData);
            }
        });
        progressDialogFragment.restoreListeners(this.activity, "progress");

        Button B_PRICES = findViewById(R.id.address_discrepancy_dialog_priceButton);
        B_PRICES.setOnClickListener(new CrashView.CrashOnClickListener(this.activity) {
            public void onClickImpl(View v) {
                if(deltaMap.isEmpty()) {
                    ToastUtil.showToast(activity, "no_discrepancies_found");
                    return;
                }

                progressDialogFragment.show(activity, "progress");
            }
        });

        ArrayList<String> options = new ArrayList<>();
        for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
            options.add(cryptoAddress.toString());
        }

        TextView T = findViewById(R.id.address_discrepancy_dialog_assetTextView);

        ImageButton helpButton = findViewById(R.id.address_discrepancy_dialog_helpButton);
        helpButton.setOnClickListener(new CrashView.CrashOnClickListener(activity) {
            @Override
            public void onClickImpl(View view) {
                HelpUtil.showHelp(activity, R.raw.help_discrepancy);
            }
        });

        BorderedSpinnerView bsv = findViewById(R.id.address_discrepancy_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                cryptoAddressIdx = pos;

                CryptoAddress cryptoAddress = cryptoAddressArrayList.get(cryptoAddressIdx);
                AddressData addressData = HashMapUtil.getValueFromMap(addressDataMap, cryptoAddress);

                deltaMap = addressData.getDiscrepancyMap();

                updateLayout(addressData);
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

    public void updateLayout(AddressData addressData) {
        // For each non-zero entry, display the discrepancy.
        StringBuilder s = new StringBuilder();
        s.append("Address = ").append(addressData.cryptoAddress.toString()).append("\n");

        if(!addressData.hasDiscrepancy()) {
            s.append("\nThis address has no discrepancies.");
        }
        else {
            s.append("\nDiscrepancies:");
            s.append(AssetQuantity.getAssetInfo(deltaMap, priceMap));

            if(priceMap != null && !priceMap.isEmpty()) {
                s.append("\n\nData Source = CoinGecko API V3");
            }
        }

        TextView T = findViewById(R.id.address_discrepancy_dialog_assetTextView);
        T.setText(s.toString());
    }

    @Override
    public Bundle onSaveInstanceStateImpl(Bundle bundle) {
        bundle.putInt("cryptoAddressIdx", cryptoAddressIdx);
        bundle.putSerializable("priceMap", priceMap);
        return bundle;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceStateImpl(Bundle bundle) {
        if(bundle != null) {
            priceMap = (HashMap<Asset, AssetAmount>)bundle.getSerializable("priceMap");
            cryptoAddressIdx = bundle.getInt("cryptoAddressIdx");
        }
    }
}