package bogomolov.aa.fitrack.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
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
    private int selectedPosition;

    public TracksRecyclerAdapter(List<Track> tracks) {
        this.tracks = tracks;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.track_card_view, parent, false);
        return new ViewHolder(cv,this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setBackgroundColor(selectedPosition == position ? Color.GREEN : Color.TRANSPARENT);
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

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private CardView cardView;
        private TracksRecyclerAdapter adapter;

        public ViewHolder(@NonNull CardView itemView, TracksRecyclerAdapter adapter) {
            super(itemView);
            cardView = itemView;
            this.adapter = adapter;
        }
        @Override
        public void onClick(View v) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
            adapter.notifyItemChanged(adapter.selectedPosition);
            adapter.selectedPosition = getAdapterPosition();
            adapter.notifyItemChanged(adapter.selectedPosition);
        }

    }
}
