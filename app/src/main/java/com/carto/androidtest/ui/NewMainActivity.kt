package com.carto.androidtest.ui

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.carto.androidtest.BuildConfig
import com.carto.androidtest.databinding.ActivityNewMainBinding
import com.carto.androidtest.utils.GPSStatusLiveData
import com.carto.androidtest.utils.PermissionsManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class NewMainActivity : AppCompatActivity() {

    /**
     * Allows listening to the system onBackPressed() function, so that fragments who are implementing
     * this interface can react on that.
     */
    interface OnBackPressedListener {

        /**
         * Called when the system onBackPressed() function on the activity has been triggered.
         *
         * @return Should be true, if the back action should be also executed on the Activity, to
         * remove the top most fragment from the back stack. If the fragment wants to only close itself,
         * e.g. only if a certain condition is met, then this should be false.
         */
        fun onBackPressed(): Boolean
    }

    private val viewModel: MainViewModel by viewModels()

    private lateinit var binding: ActivityNewMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.gpsStatusLiveData = GPSStatusLiveData(this)

        PermissionsManager
            .init()
            .addPermissions(*PermissionsManager.CARTO_PERMISSIONS)
            .addRequestCode(PermissionsManager.CARTO_PERMISSION_REQUEST_CODE)
            .request(this)

        binding = ActivityNewMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onBackPressed() {
        if (handleBackPress()) {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && permissions.size == grantResults.size) {
            when (requestCode) {
                PermissionsManager.CARTO_PERMISSION_REQUEST_CODE -> {
                    Timber.v("requestCode = CARTO_PERMISSION_REQUEST_CODE")

                    viewModel.onPermissionsResult(
                        arePermissionsGranted(permissions, grantResults.toTypedArray()))
                }
                else -> if (BuildConfig.DEBUG) {
                    throw IllegalStateException(
                        "The following request code was not handled: $requestCode")
                }
            }
        } else {
            if (BuildConfig.DEBUG) {
                throw IllegalStateException(
                    "grantResults is empty. Please check your permissions request.")
            }
        }
    }

    private fun arePermissionsGranted(permissions: Array<out String>, grantResults: Array<Int>)
            : Boolean {
        val grantedPermissionsSum = permissions.zip(grantResults).sumBy { (permission, result) ->
            if (result == PackageManager.PERMISSION_GRANTED) {
                Timber.d("${permission} -> PERMISSION_GRANTED")
                1
            } else {
                Timber.d("${permission} -> PERMISSION_DENIED")
                0
            }
        }

        return grantedPermissionsSum == permissions.size
    }

    private fun handleBackPress(): Boolean {
        val navHostFragment = supportFragmentManager.primaryNavigationFragment ?: return false
        val lastChildFragment = navHostFragment.childFragmentManager.fragments.last()

        return if (lastChildFragment is OnBackPressedListener) {
            (lastChildFragment as OnBackPressedListener).onBackPressed()
        } else {
            true
        }
    }
}
