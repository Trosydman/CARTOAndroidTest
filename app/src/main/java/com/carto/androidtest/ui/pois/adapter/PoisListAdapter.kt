package com.carto.androidtest.ui.pois.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.carto.androidtest.databinding.ItemPoiBinding
import com.carto.androidtest.domain.model.Poi
import com.squareup.picasso.Picasso

class PoisListAdapter(var onPoiClickListener: OnPoiClickListener? = null)
    : ListAdapter<Poi, PoisListAdapter.PoiViewHolder>(PoiComparator) {

    interface OnPoiClickListener {
        fun onItemRootClickListener(poi: Poi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoiViewHolder =
        PoiViewHolder(ItemPoiBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PoiViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it, onPoiClickListener) }
    }

    inner class PoiViewHolder(private val binding: ItemPoiBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(poiItem: Poi, clickListener: OnPoiClickListener?) {
            with(binding) {
                Picasso.get()
                    .load(poiItem.image)
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .error(android.R.drawable.ic_delete)
                    .into(thumbnail)
                directionIcon.setImageResource(poiItem.directionImage)
                title.text = poiItem.title
                description.text = poiItem.description

                root.setOnClickListener {
                    clickListener?.onItemRootClickListener(poiItem)
                }
            }
        }
    }

    object PoiComparator: DiffUtil.ItemCallback<Poi>() {
        override fun areItemsTheSame(oldItem: Poi, newItem: Poi): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Poi, newItem: Poi): Boolean =
            oldItem == newItem
    }
}
