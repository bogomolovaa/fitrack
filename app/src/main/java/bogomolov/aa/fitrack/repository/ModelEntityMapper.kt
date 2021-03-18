package bogomolov.aa.fitrack.repository

import bogomolov.aa.fitrack.domain.model.Point
import bogomolov.aa.fitrack.domain.model.Tag
import bogomolov.aa.fitrack.domain.model.Track
import bogomolov.aa.fitrack.repository.entities.PointEntity
import bogomolov.aa.fitrack.repository.entities.TagEntity
import bogomolov.aa.fitrack.repository.entities.TrackEntity
import java.util.*

fun entityToModel(from: PointEntity?): Point? {
    if (from == null) return null
    return Point(
        from.id,
        from.time,
        from.lat,
        from.lng,
        from.smoothed
    )
}

fun modelToEntity(from: Point) =
    PointEntity(
        from.id,
        from.time,
        from.lat,
        from.lng,
        from.smoothed
    )

fun entityToModel(from: TagEntity?): Tag? {
    if (from == null) return null
    return Tag(from.id, from.name ?: "")
}

fun modelToEntity(from: Tag) = TagEntity(from.id, from.name)

fun entityToModel(from: TrackEntity?): Track? {
    if (from == null) return null
    return Track(
        from.id,
        from.startPointId,
        from.endPointId,
        from.startSmoothedPointId,
        from.endSmoothedPointId,
        from.startTime,
        from.endTime,
        from.tag,
        from.distance
    )
}

fun modelToEntity(from: Track) =
    TrackEntity(
        from.id,
        from.startPointId,
        from.endPointId,
        from.startSmoothedPointId,
        from.endSmoothedPointId,
        from.startTime,
        from.endTime,
        from.tag,
        from.distance
    )


fun <Q> entityToModel(fromList: List<*>, toClass: Class<Q>): List<Q> {
    val toList: MutableList<Q> = ArrayList()
    if (toClass.isAssignableFrom(Point::class.java))
        for (fromEntity in fromList) toList.add(entityToModel(fromEntity as PointEntity) as Q)
    if (toClass.isAssignableFrom(Tag::class.java))
        for (fromEntity in fromList) toList.add(entityToModel(fromEntity as TagEntity) as Q)
    if (toClass.isAssignableFrom(Track::class.java))
        for (fromEntity in fromList) toList.add(entityToModel(fromEntity as TrackEntity) as Q)
    return toList
}