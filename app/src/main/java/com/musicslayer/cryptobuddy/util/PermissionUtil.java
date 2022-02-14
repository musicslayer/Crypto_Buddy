package com.musicslayer.cryptobuddy.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class PermissionUtil {
    public static boolean isGooglePlayServicesAvailable(Activity activity) {
        int g = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);
        if (ConnectionResult.SUCCESS == g) {
            return true;
        }

        if(ConnectionResult.SERVICE_MISSING == g) {
            ToastUtil.showToast(activity,"google_play_missing");
        }
        else if(ConnectionResult.SERVICE_UPDATING == g) {
            ToastUtil.showToast(activity,"google_play_updating");
        }
        else if(ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED == g) {
            ToastUtil.showToast(activity,"google_play_needs_update");
        }
        else if(ConnectionResult.SERVICE_DISABLED == g) {
            ToastUtil.showToast(activity,"google_play_disabled");
        }
        else if(ConnectionResult.SERVICE_INVALID == g) {
            ToastUtil.showToast(activity,"google_play_invalid");
        }
        else {
            ToastUtil.showToast(activity,"unknown_google_play_error");
        }

        return false;
    }

    public static boolean requestCameraPermission(Activity activity) {
        // Older versions cannot run the app unless the permission has already been granted, so only check for newer versions.
        if(Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ToastUtil.showToast(activity,"no_camera_permission");
            activity.requestPermissions(new String[] { Manifest.permission.CAMERA }, 0);
            return false;
        }

        return true;
    }

    @SuppressLint("NewApi") // Code analyzer doesn't recognize our conditional statements.
    public static boolean requestExternalReadWritePermission(Activity activity) {
        // Older versions cannot run the app unless the permission has already been granted, so only check for newer versions.
        // For simplicity, just request both permissions at once.
        // API 29 and above does not need and cannot grant the write permission, so only ask for read permission.
        boolean needsRead = Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        boolean needsWrite = Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29 && ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;

        // To avoid confusion, only ask for permissions we don't already have.
        if(needsRead && needsWrite) {
            ToastUtil.showToast(activity,"no_external_read_write_permission");
            activity.requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
            return false;
        }
        else if(needsRead) {
            ToastUtil.showToast(activity,"no_external_read_permission");
            activity.requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 0);
            return false;
        }
        else if(needsWrite) {
            ToastUtil.showToast(activity,"no_external_write_permission");
            activity.requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
            return false;
        }
        else {
            return true;
        }
    }
}
