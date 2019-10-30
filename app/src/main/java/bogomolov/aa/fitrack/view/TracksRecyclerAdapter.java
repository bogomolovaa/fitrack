package bogomolov.aa.fitrack.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.Track;

public class TracksRecyclerAdapter extends RecyclerView.Adapter<TracksRecyclerAdapter.ViewHolder> {

    private List<Track> tracks;
    private Set<Long> selectedIds;
    private boolean checkMode;


    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
        selectedIds = new HashSet<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.track_card_view, parent, false);
        return new ViewHolder(cv, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Track track = tracks.get(position);
        CardView cardView = holder.cardView;
        RadioButton radioButton = cardView.findViewById(R.id.card_checked_button);
        boolean selected = selectedIds.contains(track.getId());
        radioButton.setVisibility(checkMode ? View.VISIBLE : View.GONE);
        radioButton.setChecked(selected);

        ((TextView) cardView.findViewById(R.id.card_text_distance)).setText((int) track.getDistance() + " m");
        ((TextView) cardView.findViewById(R.id.card_text_time)).setText(track.getTimeString());
        ((TextView) cardView.findViewById(R.id.card_text_avg_speed)).setText(String.format("%.1f", 3.6 * track.getSpeed()) + " km/h");
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private CardView cardView;
        private TracksRecyclerAdapter adapter;

        public ViewHolder(@NonNull CardView itemView, TracksRecyclerAdapter adapter) {
            super(itemView);
            cardView = itemView;
            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
            int position = getAdapterPosition();
            Track track = adapter.tracks.get(position);
            if (adapter.selectedIds.contains(track.getId())) {
                adapter.selectedIds.remove(track.getId());
            } else {
                adapter.selectedIds.add(track.getId());
            }
            adapter.notifyItemChanged(position);
        }

        @Override
        public boolean onLongClick(View view) {
            adapter.checkMode = !adapter.checkMode;
            adapter.notifyDataSetChanged();
            return true;
        }
    }
}
