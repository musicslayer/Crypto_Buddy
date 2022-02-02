package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.api.price.CryptoPrice;
import com.musicslayer.cryptobuddy.api.price.PriceData;
import com.musicslayer.cryptobuddy.asset.Asset;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashDialogInterface;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.rich.RichStringBuilder;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.state.StateObj;
import com.musicslayer.cryptobuddy.transaction.AssetAmount;
import com.musicslayer.cryptobuddy.transaction.AssetQuantity;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.HelpUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;
import com.musicslayer.cryptobuddy.view.SelectAndSearchView;

import java.util.ArrayList;
import java.util.HashMap;

public class AddressDiscrepancyDialog extends BaseDialog {
    ArrayList<CryptoAddress> cryptoAddressArrayList;
    HashMap<CryptoAddress, AddressData> addressDataMap;
    int cryptoAddressIdx;

    HashMap<Asset, AssetAmount> deltaMap = new HashMap<>();
    HashMap<Asset, AssetQuantity> priceMap = new HashMap<>();

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

        SelectAndSearchView fssv = findViewById(R.id.address_discrepancy_dialog_fiatSelectAndSearchView);
        fssv.setIncludesFiat(true);
        fssv.setIncludesCoin(false);
        fssv.setIncludesToken(false);
        fssv.setOptionsFiat();

        ProgressDialogFragment progressDialogFragment = ProgressDialogFragment.newInstance(ProgressDialog.class);
        progressDialogFragment.setOnShowListener(new CrashDialogInterface.CrashOnShowListener(this.activity) {
            @Override
            public void onShowImpl(DialogInterface dialog) {
                ProgressDialogFragment.updateProgressTitle("Calculating Total...");

                HashMap<Asset, AssetQuantity> newPriceMap = new HashMap<>();

                ArrayList<Asset> assetKeySet = new ArrayList<>(deltaMap.keySet());

                Fiat priceFiat = (Fiat)fssv.getChosenAsset();
                CryptoPrice cryptoPrice = new CryptoPrice(assetKeySet, priceFiat);

                PriceData priceData = PriceData.getPriceData(cryptoPrice);
                if(priceData.isPriceComplete()) {
                    HashMap<Asset, AssetQuantity> priceHashMap = priceData.priceHashMap;
                    for(Asset asset : priceHashMap.keySet()) {
                        AssetQuantity price = HashMapUtil.getValueFromMap(priceHashMap, asset);
                        if(price != null) {
                            HashMapUtil.putValueInMap(newPriceMap, asset, price);
                        }
                    }
                }

                ProgressDialogFragment.setValue(Serialization.serializeHashMap(newPriceMap));
            }
        });

        progressDialogFragment.setOnDismissListener(new CrashDialogInterface.CrashOnDismissListener(this.activity) {
            @Override
            public void onDismissImpl(DialogInterface dialog) {
                HashMap<Asset, AssetQuantity> newPriceMap = Serialization.deserializeHashMap(ProgressDialogFragment.getValue(), Asset.class, AssetQuantity.class);

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
        // For each entry, display the discrepancy.
        RichStringBuilder s = new RichStringBuilder(true);
        s.appendRich("Address = ").appendRich(addressData.cryptoAddress.toString()).appendRich("\n");

        if(!addressData.hasDiscrepancy()) {
            s.appendRich("\nThis address has no discrepancies.");
        }
        else {
            s.appendRich("\nDiscrepancies:");
            s.append(AssetQuantity.getAssetInfo(deltaMap, priceMap, true));
            if(priceMap != null && !priceMap.isEmpty()) {
                s.appendRich("\n\nData Source = CoinGecko API V3");
            }
        }

        TextView T = findViewById(R.id.address_discrepancy_dialog_assetTextView);
        T.setText(Html.fromHtml(s.toString()));
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
            priceMap = (HashMap<Asset, AssetQuantity>)bundle.getSerializable("priceMap");
            cryptoAddressIdx = bundle.getInt("cryptoAddressIdx");
        }
    }
}