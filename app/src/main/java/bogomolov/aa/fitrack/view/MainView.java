package bogomolov.aa.fitrack.view;

import java.util.List;

import bogomolov.aa.fitrack.model.Point;
import bogomolov.aa.fitrack.model.Track;

public interface MainView {

    void showStartStopButtons(boolean started);

    void updateView(Track track, Point point, List<Point> rawTrackPoints, List<Point> smoothedPoints);

}
