package com.carto.androidtest.ui.pois

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.carto.androidtest.R
import com.carto.androidtest.databinding.FragmentDialogPoisListBinding
import com.carto.androidtest.domain.model.Poi
import com.carto.androidtest.ui.MainViewModel
import com.carto.androidtest.ui.pois.adapter.PoisListAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PoisListDialogFragment : BottomSheetDialogFragment(), PoisListAdapter.OnPoiClickListener {

    private val viewModel: MainViewModel by viewModels()

    private val binding: FragmentDialogPoisListBinding
        get() = _binding!!
    private var _binding: FragmentDialogPoisListBinding? = null

    private lateinit var poisListAdapter: PoisListAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomSheetDialog.setOnShowListener { dialog ->
            val bottomSheet = (dialog as BottomSheetDialog).findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

            bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // This allows to show the close button ONLY at the half of the sliding
                    binding.closeButton.visibility = if (slideOffset > 0.5) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN-> dismiss()
                        else -> {
                            // Do nothing
                        }
                    }
                }
            })
        }

        return bottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDialogPoisListBinding.inflate(inflater, container, false)

        initViews()

        viewModel.pois.observe(viewLifecycleOwner) {
            binding.loadingProgressBar.isVisible = false
            poisListAdapter.submitList(it.toMutableList())
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onItemRootClickListener(poi: Poi) {
        // TODO
    }

    private fun initViews() {
        poisListAdapter = PoisListAdapter(this)
        binding.poisList.adapter = poisListAdapter
    }
}
