package com.efisteiner.slidingviews

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.core.content.ContextCompat

fun mediaPermissionGranted(context: Context): Boolean {
    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        (
                ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) == PERMISSION_GRANTED ||
                        return ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO) == PERMISSION_GRANTED
                )
    ) {
        // Full access on Android 13 (API level 33) or higher
    } else if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
        return ContextCompat.checkSelfPermission(context, READ_MEDIA_VISUAL_USER_SELECTED) == PERMISSION_GRANTED
    ) {
        // Partial access on Android 14 (API level 34) or higher
    }  else if (ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
        // Full access up to Android 12 (API level 32)
        return true
    }

    return false
}