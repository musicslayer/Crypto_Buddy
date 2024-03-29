package com.musicslayer.cryptobuddy.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.musicslayer.cryptobuddy.app.App;

public class PermissionUtil {
    public static boolean isGooglePlayServicesAvailable() {
        int g = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(App.applicationContext);
        if (ConnectionResult.SUCCESS == g) {
            return true;
        }

        if(ConnectionResult.SERVICE_MISSING == g) {
            ToastUtil.showToast("google_play_missing");
        }
        else if(ConnectionResult.SERVICE_UPDATING == g) {
            ToastUtil.showToast("google_play_updating");
        }
        else if(ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED == g) {
            ToastUtil.showToast("google_play_needs_update");
        }
        else if(ConnectionResult.SERVICE_DISABLED == g) {
            ToastUtil.showToast("google_play_disabled");
        }
        else if(ConnectionResult.SERVICE_INVALID == g) {
            ToastUtil.showToast("google_play_invalid");
        }
        else {
            ToastUtil.showToast("unknown_google_play_error");
        }

        return false;
    }

    public static boolean requestCameraPermission(Activity activity) {
        // Older versions cannot run the app unless the permission has already been granted, so only check for newer versions.
        if(Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ToastUtil.showToast("no_camera_permission");
            activity.requestPermissions(new String[] { Manifest.permission.CAMERA }, 0);
            return false;
        }

        return true;
    }
}
