package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.crash.CrashAdapterView;
import com.musicslayer.cryptobuddy.crash.CrashView;
import com.musicslayer.cryptobuddy.util.ClipboardUtil;
import com.musicslayer.cryptobuddy.util.WindowUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import net.glxn.qrgen.android.QRCode;

import java.util.ArrayList;

public class AddressQRCodeDialog extends BaseDialog {
    public ArrayList<CryptoAddress> cryptoAddressArrayList;

    public AddressQRCodeDialog(Activity activity, ArrayList<CryptoAddress> cryptoAddressArrayList) {
        super(activity);
        this.cryptoAddressArrayList = cryptoAddressArrayList;
    }

    public int getBaseViewID() {
        return R.id.address_qr_code_dialog;
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_address_qr_code);

        ArrayList<String> options = new ArrayList<>();
        for(CryptoAddress cryptoAddress : cryptoAddressArrayList) {
            options.add(cryptoAddress.toString());
        }

        TextView T = findViewById(R.id.address_qr_code_dialog_textView);
        Button B = findViewById(R.id.address_qr_code_dialog_copyButton);
        ImageView I = findViewById(R.id.address_qr_code_dialog_qrCode);

        final int[] dimensions = WindowUtil.getDimensions(AddressQRCodeDialog.this.activity);
        final int s = (int) Math.min(dimensions[0] * 0.9f * 0.9f, dimensions[1] * 0.9f * 0.9f); // First 0.9 for dialog, second 0.9 for QR code.

        BorderedSpinnerView bsv = findViewById(R.id.address_qr_code_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashAdapterView.CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                CryptoAddress cryptoAddress = cryptoAddressArrayList.get(pos);

                T.setText(cryptoAddress.toString());

                B.setOnClickListener(new CrashView.CrashOnClickListener(AddressQRCodeDialog.this.activity) {
                    @Override
                    public void onClickImpl(View view) {
                        ClipboardUtil.copy("wallet_address", cryptoAddress.address);
                    }
                });

                Bitmap bitmap = QRCode.from(cryptoAddress.address).withSize(s, s).bitmap();
                I.setImageBitmap(bitmap);
            }
        });

        if(cryptoAddressArrayList.size() == 1) {
            bsv.setVisibility(View.GONE);
        }

        if(cryptoAddressArrayList.size() == 0) {
            bsv.setVisibility(View.GONE);
            B.setVisibility(View.GONE);
            T.setText("No addresses found.");
        }
    }
}
