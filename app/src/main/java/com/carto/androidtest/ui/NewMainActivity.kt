package com.carto.androidtest.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carto.androidtest.databinding.ActivityNewMainBinding
import dagger.hilt.android.AndroidEntryPoint

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

    private lateinit var binding: ActivityNewMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onBackPressed() {
        if (handleBackPress()) {
            super.onBackPressed()
        }
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
