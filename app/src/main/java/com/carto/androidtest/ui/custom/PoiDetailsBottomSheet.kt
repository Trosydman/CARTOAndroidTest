package com.carto.androidtest.ui.custom

import android.annotation.SuppressLint
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.carto.androidtest.databinding.BottomSheetPoiDetailsBinding
import com.carto.androidtest.domain.model.Poi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.picasso.Picasso

@SuppressLint("ClickableViewAccessibility")
class PoiDetailsBottomSheet(bottomSheet: ConstraintLayout) {

    interface OnVisibilityChangedListener {
        fun onShow()
        fun onHide()
    }

    interface OnClickListener {
        fun onDirectionsFabClicked()
    }

    private val binding = BottomSheetPoiDetailsBinding.bind(bottomSheet)

    private var bottomSheetBehaviour: BottomSheetBehavior<ConstraintLayout> =
        BottomSheetBehavior.from(bottomSheet)

    var onVisibilityChangedListener: OnVisibilityChangedListener? = null
    var onClickListener: OnClickListener? = null

    var currentPoiId: String? = null

    init {
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehaviour.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Do nothing
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> onVisibilityChangedListener?.onHide()

                    BottomSheetBehavior.STATE_EXPANDED,
                    BottomSheetBehavior.STATE_COLLAPSED,
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> onVisibilityChangedListener?.onShow()

                    else -> {
                        // TODO
                    }
                }
            }
        })

        // This will disable the touch to be propagated to the view below (map)
        binding.root.setOnTouchListener { _, _ ->
            return@setOnTouchListener true
        }

        binding.directionsFAB.setOnClickListener {
            onClickListener?.onDirectionsFabClicked()
        }
    }

    fun isShown() = bottomSheetBehaviour.state != BottomSheetBehavior.STATE_HIDDEN

    fun hide() {
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun showDetails(poi: Poi) {
        if (currentPoiId != poi.id) {
            currentPoiId = poi.id
            fillDetails(poi)
        }

        show()
    }

    private fun show() {
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun fillDetails(poi: Poi) {
        with(binding) {
            Picasso.get()
                .load(poi.image)
                .fit()
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_camera)
                .error(android.R.drawable.ic_delete)
                .into(poiImage)

            poiTitle.text = poi.title
            poiDirectionIcon.setImageDrawable(ContextCompat.getDrawable(root.context,
                poi.directionImage))
//            distanceToPoi.text =
//            timeToPoi.text =
            poiDescription.text = poi.description
        }
    }
}
