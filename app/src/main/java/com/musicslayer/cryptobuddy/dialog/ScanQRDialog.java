package com.musicslayer.cryptobuddy.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.ToastUtil;
import com.musicslayer.cryptobuddy.util.WindowUtil;

public class ScanQRDialog extends BaseDialog {
    public String user_ADDRESS;

    public ScanQRDialog(Activity activity) {
        super(activity);
    }

    public int getBaseViewID() {
        return R.id.scan_qr_dialog;
    }

    public void adjustDialog() {
        // Stretch to 90% width. This is needed to see any dialog at all.
        ViewGroup v = findViewById(getBaseViewID());
        int[] dimensions = WindowUtil.getDimensions(this.activity);
        v.setLayoutParams(new FrameLayout.LayoutParams((int)(dimensions[0] * 0.9), FrameLayout.LayoutParams.WRAP_CONTENT));

        // Do not add Scrollview like superclass does.
    }

    public void createLayout(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_scan_qr);

        final CameraSource[] cameraSource = new CameraSource[1];

        SurfaceView cameraView = findViewById(R.id.scan_qr_dialog_surfaceView);
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @SuppressLint("MissingPermission") // Camera permission checked by caller.
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource[0] = new CameraSource.Builder(ScanQRDialog.this.activity, createBarcodeDetector()).build();
                    cameraSource[0].start(holder);
                }
                catch(Exception e) {
                    ThrowableUtil.processThrowable(e);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                try {
                    cameraSource[0].release();
                }
                catch(Exception e) {
                    ThrowableUtil.processThrowable(e);
                }
            }
        });
    }

    public BarcodeDetector createBarcodeDetector() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(ScanQRDialog.this.activity).setBarcodeFormats(Barcode.QR_CODE).build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                try {
                    final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                    if(barcodes.size() > 1) {
                        ToastUtil.showToast("multiple_qr_codes_read");
                    }
                    else if(barcodes.size() == 1) {
                        user_ADDRESS = barcodes.valueAt(0).displayValue;

                        isComplete = true;
                        dismiss();
                    }
                }
                catch(Exception e) {
                    ThrowableUtil.processThrowable(e);
                }
            }
        });

        return barcodeDetector;
    }
}
