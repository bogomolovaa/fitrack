package bogomolov.aa.fitrack.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.card.MaterialCardView

import java.util.HashSet

import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.core.model.Track
import bogomolov.aa.fitrack.databinding.TrackCardViewBinding
import bogomolov.aa.fitrack.view.fragments.TracksListFragment

import bogomolov.aa.fitrack.android.getTrackImageFile

class TracksPagedAdapter(private val tracksListFragment: TracksListFragment) : PagedListAdapter<Track, TracksPagedAdapter.TrackViewHolder>(DIFF_CALLBACK) {
    val selectedIds: MutableSet<Long> = HashSet()
    private var checkMode = false


    fun disableCheckMode() {
        selectedIds.clear()
        checkMode = false
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val trackCardViewBinding = TrackCardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val cv = trackCardViewBinding.trackCardView
        return TrackViewHolder(cv, trackCardViewBinding, this)
    }


    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)
        val cardView = holder.cardView as MaterialCardView
        if (track != null) {
            val selected = selectedIds.contains(track.id)
            cardView.isChecked = selected
        }
        holder.bind(track)
    }

    class TrackViewHolder(val cardView: CardView, private val binding: TrackCardViewBinding, private val adapter: TracksPagedAdapter) :
            RecyclerView.ViewHolder(cardView), View.OnClickListener, View.OnLongClickListener {

        init {
            cardView.setOnClickListener(this)
            cardView.setOnLongClickListener(this)
        }

        fun bind(track: Track?) {
            binding.track = track
            binding.executePendingBindings()
            if (track != null) {
                val bitmap = BitmapFactory.decodeFile(getTrackImageFile(adapter.tracksListFragment.context!!, track))
                binding.trackImage.setImageBitmap(bitmap)
            } else {
                binding.trackImage.setImageBitmap(null)
            }
        }


        override fun onClick(v: View) {
            if (adapterPosition == RecyclerView.NO_POSITION) return
            val position = adapterPosition
            val track = adapter.getItem(position)
            if (track != null) {
                if (adapter.checkMode) {
                    if (adapter.selectedIds.contains(track.id)) {
                        adapter.selectedIds.remove(track.id)
                    } else {
                        adapter.selectedIds.add(track.id)
                    }
                    adapter.notifyItemChanged(position)
                } else {
                    val bundle = Bundle()
                    bundle.putLong("trackId", track.id)
                    val extras = FragmentNavigator.Extras.Builder()
                            .addSharedElement(binding.cardTextDistance, binding.cardTextDistance.transitionName)
                            .addSharedElement(binding.cardTextTime, binding.cardTextTime.transitionName)
                            .addSharedElement(binding.cardTextAvgSpeed, binding.cardTextAvgSpeed.transitionName)
                            .addSharedElement(binding.cardTextName, binding.cardTextName.transitionName)
                            .addSharedElement(binding.cardTagName, binding.cardTagName.transitionName)
                            .addSharedElement(binding.trackImage, binding.trackImage.transitionName)
                            .build()
                    Navigation.findNavController(v).navigate(R.id.action_tracksListFragment_to_trackViewFragment, bundle, null, extras)
                }
            }
        }

        override fun onLongClick(view: View): Boolean {
            adapter.checkMode = true
            adapter.selectedIds!!.clear()
            onClick(view)
            adapter.notifyDataSetChanged()
            adapter.tracksListFragment.onLongClick()
            return true
        }
    }

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Track>() {
            override fun areItemsTheSame(track1: Track, track2: Track): Boolean =
                track1.id == track2.id

            override fun areContentsTheSame(track1: Track, track2: Track): Boolean =
                track1.id == track2.id

        }
    }
}
