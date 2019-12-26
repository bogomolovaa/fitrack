package bogomolov.aa.fitrack.repository;

import java.util.ArrayList;
import java.util.List;

import bogomolov.aa.fitrack.core.model.Point;
import bogomolov.aa.fitrack.core.model.Tag;
import bogomolov.aa.fitrack.core.model.Track;
import bogomolov.aa.fitrack.repository.entities.PointEntity;
import bogomolov.aa.fitrack.repository.entities.TagEntity;
import bogomolov.aa.fitrack.repository.entities.TrackEntity;

public class ModelEntityMapper {

    public static Point entityToModel(PointEntity from) {
        if(from == null) return null;
        Point to = new Point();
        to.setId(from.getId());
        to.setSmoothed(from.getSmoothed());
        to.setTime(from.getTime());
        to.setLat(from.getLat());
        to.setLng(from.getLng());
        return to;
    }

    public static PointEntity modelToEntity(Point from) {
        if(from == null) return null;
        PointEntity to = new PointEntity();
        to.setId(from.getId());
        to.setSmoothed(from.getSmoothed());
        to.setTime(from.getTime());
        to.setLat(from.getLat());
        to.setLng(from.getLng());
        return to;
    }

    public static Tag entityToModel(TagEntity from) {
        if(from == null) return null;
        Tag to = new Tag();
        to.setId(from.getId());
        to.setName(from.getName());
        return to;
    }

    public static TagEntity modelToEntity(Tag from) {
        if(from == null) return null;
        TagEntity to = new TagEntity();
        to.setId(from.getId());
        to.setName(from.getName());
        return to;
    }

    public static Track entityToModel(TrackEntity from) {
        if(from == null) return null;
        Track to = new Track();
        to.setId(from.getId());
        to.setDistance(from.getDistance());
        to.setEndPoint(from.getEndPoint());
        to.setEndSmoothedPoint(from.getEndSmoothedPoint());
        to.setEndTime(from.getEndTime());
        to.setStartPoint(from.getStartPoint());
        to.setStartSmoothedPoint(from.getStartSmoothedPoint());
        to.setStartTime(from.getStartTime());
        to.setTag(from.getTag());
        return to;
    }

    public static TrackEntity modelToEntity(Track from) {
        if(from == null) return null;
        TrackEntity to = new TrackEntity();
        to.setId(from.getId());
        to.setDistance(from.getDistance());
        to.setEndPoint(from.getEndPoint());
        to.setEndSmoothedPoint(from.getEndSmoothedPoint());
        to.setEndTime(from.getEndTime());
        to.setStartPoint(from.getStartPoint());
        to.setStartSmoothedPoint(from.getStartSmoothedPoint());
        to.setStartTime(from.getStartTime());
        to.setTag(from.getTag());
        return to;
    }

    public static <Q> List<Q> entityToModel(List fromList, Class<Q> toClass) {
        List toList = new ArrayList<>();
        if (toClass.isAssignableFrom(Point.class))
            for (Object fromEntity : fromList) toList.add(entityToModel((PointEntity) fromEntity));
        if (toClass.isAssignableFrom(Tag.class))
            for (Object fromEntity : fromList) toList.add(entityToModel((TagEntity) fromEntity));
        if (toClass.isAssignableFrom(Track.class))
            for (Object fromEntity : fromList) toList.add(entityToModel((TrackEntity) fromEntity));
        return toList;
    }

    public static <Q> List<Q> modelToEntity(List fromList, Class<Q> toClass) {
        List toList = new ArrayList<>();
        if (toClass.isAssignableFrom(PointEntity.class))
            for (Object fromEntity : fromList) toList.add(modelToEntity((Point) fromEntity));
        if (toClass.isAssignableFrom(TagEntity.class))
            for (Object fromEntity : fromList) toList.add(modelToEntity((Tag) fromEntity));
        if (toClass.isAssignableFrom(TrackEntity.class))
            for (Object fromEntity : fromList) toList.add(modelToEntity((Track) fromEntity));
        return toList;
    }

}
