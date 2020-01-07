package bogomolov.aa.fitrack.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.view.fragments.TracksListFragment;

public class TracksRecyclerAdapter extends RecyclerView.Adapter<TracksRecyclerAdapter.ViewHolder> {

    private List<Track> tracks;
    private Set<Long> selectedIds;
    private boolean checkMode;
    private Context context;
    private TracksListFragment tracksListFragment;


    public TracksRecyclerAdapter(TracksListFragment tracksListFragment, Context context) {
        this.context = context;
        this.tracksListFragment = tracksListFragment;
        tracks = new ArrayList<>();
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
        selectedIds = new HashSet<>();
        notifyDataSetChanged();
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MaterialCardView cv = (MaterialCardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.track_card_view, parent, false);
        return new ViewHolder(cv, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Track track = tracks.get(position);
        MaterialCardView cardView = holder.cardView;
        boolean selected = selectedIds.contains(track.getId());
        cardView.setChecked(selected);

        ((TextView) cardView.findViewById(R.id.card_text_name)).setText(track.getName());
        ((TextView) cardView.findViewById(R.id.card_tag_name)).setText(track.getTag() != null ? track.getTag() : context.getResources().getString(R.string.no_tag));
        ((TextView) cardView.findViewById(R.id.card_text_distance)).setText((int) track.getDistance() + " m");
        ((TextView) cardView.findViewById(R.id.card_text_time)).setText(track.getTimeString());
        ((TextView) cardView.findViewById(R.id.card_text_avg_speed)).setText(String.format("%.1f", 3.6 * track.getSpeed()) + " km/h");
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private MaterialCardView cardView;
        private TracksRecyclerAdapter adapter;

        public ViewHolder(@NonNull MaterialCardView itemView, TracksRecyclerAdapter adapter) {
            super(itemView);
            cardView = itemView;
            this.adapter = adapter;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
            int position = getAdapterPosition();
            Track track = adapter.tracks.get(position);
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
                Navigation.findNavController(v).navigate(R.id.action_tracksListFragment_to_trackViewFragment,bundle);
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
