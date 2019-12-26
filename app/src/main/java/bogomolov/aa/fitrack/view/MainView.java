package bogomolov.aa.fitrack.view;

import android.content.Context;

import java.util.List;

import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.core.model.Track;

public interface MainView {

    void showStartStopButtons(boolean canStart);

    void updateView(Track track, Point point, List<Point> rawTrackPoints, List<Point> smoothedPoints);

    Context getViewContext();
}
