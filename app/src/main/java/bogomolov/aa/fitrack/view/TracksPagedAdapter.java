package bogomolov.aa.fitrack.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.HashSet;
import java.util.Set;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.android.MapSaver;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.databinding.TrackCardViewBinding;
import bogomolov.aa.fitrack.view.fragments.TracksListFragment;

public class TracksPagedAdapter extends PagedListAdapter<Track, TracksPagedAdapter.TrackViewHolder> {
    private Set<Long> selectedIds;
    private boolean checkMode;
    private TracksListFragment tracksListFragment;

    public TracksPagedAdapter(TracksListFragment tracksListFragment) {
        super(DIFF_CALLBACK);
        this.tracksListFragment = tracksListFragment;
        selectedIds = new HashSet<>();
    }

    public void disableCheckMode() {
        selectedIds = new HashSet<>();
        checkMode = false;
        notifyDataSetChanged();
    }

    public Set<Long> getSelectedIds() {
        return selectedIds;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TrackCardViewBinding trackCardViewBinding = TrackCardViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        MaterialCardView cv = trackCardViewBinding.trackCardView;
        return new TrackViewHolder(cv, trackCardViewBinding, this);
    }


    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = getItem(position);
        MaterialCardView cardView = (MaterialCardView) holder.cardView;
        if (track != null) {
            boolean selected = selectedIds.contains(track.getId());
            cardView.setChecked(selected);
        }
        holder.bind(track);
    }


    private static DiffUtil.ItemCallback<Track> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Track>() {
                @Override
                public boolean areItemsTheSame(Track track1, Track track2) {
                    return track1.getId() == track2.getId();
                }

                @Override
                public boolean areContentsTheSame(Track track1, Track track2) {
                    return track1.getId() == track2.getId();
                }
            };

    public static class TrackViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private CardView cardView;
        private TracksPagedAdapter adapter;
        private TrackCardViewBinding binding;

        public TrackViewHolder(@NonNull CardView itemView, TrackCardViewBinding binding, TracksPagedAdapter adapter) {
            super(itemView);
            cardView = itemView;
            this.binding = binding;
            this.adapter = adapter;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void bind(Track track) {
            binding.setTrack(track);
            binding.executePendingBindings();
            if (track != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(MapSaver.getTrackImageFile(adapter.tracksListFragment.getContext(), track));
                binding.trackImage.setImageBitmap(bitmap);
            }else{
                binding.trackImage.setImageBitmap(null);
            }
        }


        @Override
        public void onClick(View v) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
            int position = getAdapterPosition();
            Track track = adapter.getItem(position);
            if (track != null) {
                if (adapter.checkMode) {
                    if (adapter.selectedIds.contains(track.getId())) {
                        adapter.selectedIds.remove(track.getId());
                    } else {
                        adapter.selectedIds.add(track.getId());
                    }
                    adapter.notifyItemChanged(position);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putLong("trackId", track.getId());
                    FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                            .addSharedElement(binding.cardTextDistance, binding.cardTextDistance.getTransitionName())
                            .addSharedElement(binding.cardTextTime, binding.cardTextTime.getTransitionName())
                            .addSharedElement(binding.cardTextAvgSpeed, binding.cardTextAvgSpeed.getTransitionName())
                            .addSharedElement(binding.cardTextName, binding.cardTextName.getTransitionName())
                            .addSharedElement(binding.cardTagName, binding.cardTagName.getTransitionName())
                            .addSharedElement(binding.trackImage, binding.trackImage.getTransitionName())
                            .build();
                    Navigation.findNavController(v).navigate(R.id.action_tracksListFragment_to_trackViewFragment, bundle, null, extras);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            adapter.checkMode = true;
            adapter.selectedIds.clear();
            onClick(view);
            adapter.notifyDataSetChanged();
            adapter.tracksListFragment.onLongClick();
            return true;
        }
    }
}