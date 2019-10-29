package bogomolov.aa.fitrack.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.model.Track;

public class TracksRecyclerAdapter extends RecyclerView.Adapter<TracksRecyclerAdapter.ViewHolder> {

    private List<Track> tracks;

    public TracksRecyclerAdapter(List<Track> tracks) {
        this.tracks = tracks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.track_card_view, parent, false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Track track = tracks.get(position);
        CardView cardView = holder.cardView;
        ((TextView) cardView.findViewById(R.id.card_text_distance)).setText((int) track.getDistance() + " m");
        ((TextView) cardView.findViewById(R.id.card_text_time)).setText(track.getTimeString());
        ((TextView) cardView.findViewById(R.id.card_text_avg_speed)).setText(String.format("%.1f", 3.6 * track.getSpeed()) + " km/h");
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;

        public ViewHolder(@NonNull CardView itemView) {
            super(itemView);
            cardView = itemView;
        }
    }
}
