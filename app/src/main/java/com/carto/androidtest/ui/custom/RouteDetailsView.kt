package com.carto.androidtest.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.carto.androidtest.R
import com.carto.androidtest.databinding.ViewRouteDetailsBinding

@SuppressLint("ClickableViewAccessibility")
class RouteDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    interface OnVisibilityChangedListener {
        fun onShow()
        fun onHide()
    }

    interface OnClickListener {
        fun onCloseButtonClicked()
    }

    private val binding = ViewRouteDetailsBinding.inflate(LayoutInflater.from(context), this, true)

    var onVisibilityChangedListener: OnVisibilityChangedListener? = null
    var onClickListener: OnClickListener? = null

    init {
        if (isInEditMode) {
            isStartingFromCurrentLocation(true)
            setAddressTo("Sevilla, Spain")
        } else {
            // This will disable the touch to be propagated to the view below (map)
            binding.root.setOnTouchListener { _, _ ->
                return@setOnTouchListener true
            }

            // This will disable the touch to be propagated to the view below (close button)
            binding.navigatingBanner.setOnTouchListener { _, _ ->
                return@setOnTouchListener true
            }

            binding.closeButton.setOnClickListener {
                onClickListener?.onCloseButtonClicked()
            }
        }

    }

    override fun isShown(): Boolean {
        return isVisible
    }

    fun show() {
        isVisible = true
        this.animation = AnimationUtils.loadAnimation(context, R.anim.slide_from_top)

        onVisibilityChangedListener?.onShow()
    }

    fun hide() {
        this.animation = AnimationUtils.loadAnimation(context, R.anim.slide_to_top)
        isVisible = false

        clearInfo()

        onVisibilityChangedListener?.onHide()
    }

    fun isNavigating(isNavigating: Boolean) {
        binding.navigatingBanner.isVisible = isNavigating
    }

    fun setAddressFrom(address: String) {
        binding.addressFrom.text = address
        isStartingFromCurrentLocation(false)
    }

    fun setAddressTo(address: String) {
        binding.addressTo.text = address
    }

    fun isStartingFromCurrentLocation(isCurrentLocation: Boolean) {
        with(binding) {
            if (isCurrentLocation) {
                startingRouteIcon.setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.ic_location)
                )
                addressFrom.text = context.getString(R.string.your_location)
            } else {
                startingRouteIcon.setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.ic_marker)
                )
            }
        }
    }

    private fun clearInfo() {
        setAddressFrom("")
        setAddressTo("")
    }
}
