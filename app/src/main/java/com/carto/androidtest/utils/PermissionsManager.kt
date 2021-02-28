package com.carto.androidtest.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.carto.androidtest.BuildConfig
import com.carto.androidtest.R
import com.carto.androidtest.utils.extensions.toIndentedString
import timber.log.Timber

data class Permission(val name: String)

/**
 * Class which manages the request for several permissions.
 */
class PermissionsManager(private val onPermissionsListener: OnPermissionsListener? = null) {

    private var permissions = emptyList<Permission>()
    private var requestCode: Int? = null

    interface OnPermissionsListener {
        fun onPermissionsNotGranted(permissionsLeft: String)
        fun onPermissionsGranted(permissionsGranted: String)
    }

    companion object {

        const val CARTO_PERMISSION_REQUEST_CODE = 42

        val CARTO_PERMISSIONS = arrayOf(
            Permission(Manifest.permission.ACCESS_FINE_LOCATION)
        )

        fun init(onPermissionsListener: OnPermissionsListener? = null): PermissionsManager {
            return PermissionsManager(onPermissionsListener)
        }

        fun hasPermission(permission: Permission, context: Context): Boolean {
            return (ContextCompat.checkSelfPermission(context, permission.name) ==
                    PackageManager.PERMISSION_GRANTED)
        }

        fun areAllPermissionsGranted(context: Context): Boolean {
            return CARTO_PERMISSIONS.all {
                hasPermission(it, context)
            }.also {
                Timber.d("arePermissionsGranted() = $it")
            }
        }

        /**
         * Checks if the user checked the "Don't ask again" of all given permission.
         */
        fun dontAskAgainEnabled(activity: Activity, vararg permissions: Permission): Boolean =
            permissions.filter { permission ->
                val showRequestPermissionRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.name)

                (!showRequestPermissionRationale && !hasPermission(permission, activity)).also {
                    Timber.d("dontAskAgainEnabled(${permission.name}) = $it")
                }
            }.size == permissions.size

        /**
         * Opens an explanatory dialog to explain the user how to activate the permissions manually.
         */
        fun openExplanatoryDialog(activity: Activity, title: String, message: String,
                                  onSettingsOpen: () -> Unit, onCancelled: () -> Unit) {
            AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.action_settings) { _, _ ->
                    openAppSettings(activity)
                    onSettingsOpen()
                }
                .setNegativeButton(R.string.action_cancel) { _, _ ->
                    onCancelled()
                }
                .show()
        }

        /**
         * Opens the app settings to let the user activate the permissions manually.
         */
        private fun openAppSettings(activity: Activity) {
            with(Intent()) {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package",
                    activity.packageName, null)
                data = uri

                activity.startActivity(this)
            }
        }
    }

    fun addPermissions(vararg permissions: Permission): PermissionsManager {
        for (permission in permissions) {
            this.permissions += (permission)
        }
        return this
    }

    fun addRequestCode(requestCode: Int): PermissionsManager {
        this.requestCode = requestCode
        return this
    }

    fun request(activity: Activity) {
        val permissionsLeft = permissions
            .filter {
                !hasPermission(it, activity)
            }

        if (permissionsLeft.isNotEmpty()) {
            Timber.v("Permissions not granted yet: ${permissionsLeft.toIndentedString()}")

            onPermissionsListener?.onPermissionsNotGranted(permissionsLeft.toIndentedString())

            requestCode?.let {
                activity.requestPermissions(permissionsLeft, it)
            } ?: run {
                if (BuildConfig.DEBUG) {
                    throw IllegalStateException(
                        "No request code was set. Please set one for the requested permissions: " +
                                permissionsLeft.toIndentedString()
                    )
                }
            }
        } else {
            Timber.v("All permissions granted! : ${permissions.toIndentedString()}")

            onPermissionsListener?.onPermissionsGranted(permissionsLeft.toIndentedString())
        }

    }

    private fun Activity.requestPermissions(permissions: List<Permission>, requestCode: Int) {
        ActivityCompat.requestPermissions(this, permissions.map { it.name }.toTypedArray(),
            requestCode)
    }
}
