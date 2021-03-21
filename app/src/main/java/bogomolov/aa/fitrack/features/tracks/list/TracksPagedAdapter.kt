package bogomolov.aa.fitrack.features.tracks.list

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import bogomolov.aa.fitrack.R
import bogomolov.aa.fitrack.databinding.TrackCardViewBinding
import bogomolov.aa.fitrack.repository.MapSaver
import bogomolov.aa.fitrack.domain.model.Track
import bogomolov.aa.fitrack.repository.getTrackImageFile
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class TracksPagedAdapter(
    private val tracksListFragment: TracksListFragment,
    private val mapSaver: MapSaver
) : PagedListAdapter<Track, TracksPagedAdapter.TrackViewHolder>(DIFF_CALLBACK) {
    val selectedIds: MutableSet<Long> = HashSet()
    private var checkMode = false

    fun disableCheckMode() {
        selectedIds.clear()
        checkMode = false
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val trackCardViewBinding =
            TrackCardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val cv = trackCardViewBinding.trackCardView
        return TrackViewHolder(cv, trackCardViewBinding, this)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)
        val cardView = holder.cardView as MaterialCardView
        if (track != null) cardView.isChecked = selectedIds.contains(track.id)
        holder.bind(track)
    }

    private fun saveMap(track: Track, imageView: ImageView) {
        mapSaver.save(track, imageView, tracksListFragment.lifecycleScope)
    }

    class TrackViewHolder(
        val cardView: CardView,
        private val binding: TrackCardViewBinding,
        private val adapter: TracksPagedAdapter
    ) : RecyclerView.ViewHolder(cardView), View.OnClickListener, View.OnLongClickListener {

        init {
            cardView.setOnClickListener(this)
            cardView.setOnLongClickListener(this)
        }

        @SuppressLint("SetTextI18n")
        fun bind(track: Track?) {
            if (track != null) {
                val bitmap = BitmapFactory.decodeFile(getTrackImageFile(cardView.context, track))
                if (bitmap != null) {
                    binding.trackImage.setImageBitmap(bitmap)
                } else {
                    binding.trackImage.setImageDrawable(null)
                    adapter.saveMap(track, binding.trackImage)
                }
                binding.cardTextName.text = track.name()
                binding.cardTextName.transitionName = "track_name_${track.id}"
                binding.cardTextDistance.text = "${track.distance.toInt()} m"
                binding.cardTextDistance.transitionName = "track_distance_${track.id}"
                binding.cardTextTime.text = track.getTimeString()
                binding.cardTextTime.transitionName = "track_time_${track.id}"
                binding.cardTextAvgSpeed.text = "${String.format("%.1f", track.getSpeed())} km/h"
                binding.cardTextAvgSpeed.transitionName = "track_speed_${track.id}"
                binding.cardTagName.text = track.tag
                binding.cardTagName.transitionName = "track_tag_${track.id}"
                binding.trackImage.transitionName = "track_image_${track.id}"

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
                    findNavController(v).navigate(
                        R.id.trackViewFragment,
                        Bundle().apply { putLong("trackId", track.id) },
                        null,
                        getExtras()
                    )
                }
            }
        }

        private fun getExtras() =
            FragmentNavigator.Extras.Builder()
                .addSharedElement(
                    binding.cardTextDistance,
                    binding.cardTextDistance.transitionName
                )
                .addSharedElement(binding.cardTextTime, binding.cardTextTime.transitionName)
                .addSharedElement(
                    binding.cardTextAvgSpeed,
                    binding.cardTextAvgSpeed.transitionName
                )
                .addSharedElement(binding.cardTextName, binding.cardTextName.transitionName)
                .addSharedElement(binding.cardTagName, binding.cardTagName.transitionName)
                .addSharedElement(binding.trackImage, binding.trackImage.transitionName)
                .build()

        override fun onLongClick(view: View): Boolean {
            adapter.checkMode = true
            adapter.selectedIds.clear()
            onClick(view)
            adapter.notifyDataSetChanged()
            adapter.tracksListFragment.onLongClick()
            return true
        }
    }
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Track>() {
    override fun areItemsTheSame(track1: Track, track2: Track): Boolean =
        track1.id == track2.id

    override fun areContentsTheSame(track1: Track, track2: Track): Boolean =
        track1.id == track2.id
}