package com.musicslayer.cryptobuddy.dialog;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.AddressData;
import com.musicslayer.cryptobuddy.crash.CrashOnClickListener;
import com.musicslayer.cryptobuddy.crash.CrashOnItemSelectedListener;
import com.musicslayer.cryptobuddy.util.ClipboardUtil;
import com.musicslayer.cryptobuddy.util.WindowUtil;
import com.musicslayer.cryptobuddy.view.BorderedSpinnerView;

import net.glxn.qrgen.android.QRCode;

import java.util.ArrayList;

public class AddressQRCodeDialog extends BaseDialog {
    public ArrayList<AddressData> addressDataArrayList;

    public AddressQRCodeDialog(Activity activity, ArrayList<AddressData> addressDataArrayList) {
        super(activity);
        this.addressDataArrayList = addressDataArrayList;
    }

    public int getBaseViewID() {
        return R.id.address_qr_code_dialog;
    }

    public void createLayout() {
        setContentView(R.layout.dialog_address_qr_code);

        ArrayList<String> options = new ArrayList<>();
        for(AddressData addressData : addressDataArrayList) {
            options.add(addressData.cryptoAddress.toString());
        }

        TextView T = findViewById(R.id.address_qr_code_dialog_textView);
        Button B = findViewById(R.id.address_qr_code_dialog_copyButton);
        ImageView I = findViewById(R.id.address_qr_code_dialog_qrCode);

        final int[] dimensions = WindowUtil.getDimensions(AddressQRCodeDialog.this.activity);
        final int s = (int) Math.min(dimensions[0] * 0.9f * 0.9f, dimensions[1] * 0.9f * 0.9f); // First 0.9 for dialog, second 0.9 for QR code.

        BorderedSpinnerView bsv = findViewById(R.id.address_qr_code_dialog_spinner);
        bsv.setOptions(options);
        bsv.setOnItemSelectedListener(new CrashOnItemSelectedListener(this.activity) {
            public void onNothingSelectedImpl(AdapterView<?> parent) {}
            public void onItemSelectedImpl(AdapterView<?> parent, View view, int pos, long id) {
                AddressData addressData = addressDataArrayList.get(pos);

                T.setText(addressData.cryptoAddress.toString());

                B.setOnClickListener(new CrashOnClickListener(AddressQRCodeDialog.this.activity) {
                    @Override
                    public void onClickImpl(View view) {
                        ClipboardUtil.copy(AddressQRCodeDialog.this.activity, "wallet_address", addressData.cryptoAddress.address);
                    }
                });

                Bitmap bitmap = QRCode.from(addressData.cryptoAddress.address).withSize(s, s).bitmap();
                I.setImageBitmap(bitmap);
            }
        });

        if(addressDataArrayList.size() == 1) {
            bsv.setVisibility(View.GONE);
        }

        if(addressDataArrayList.size() == 0) {
            bsv.setVisibility(View.GONE);
            B.setVisibility(View.GONE);
            T.setText("No addresses found.");
        }
    }
}
