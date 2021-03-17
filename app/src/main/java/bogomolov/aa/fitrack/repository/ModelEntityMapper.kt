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
    val to = Point()
    to.id = from.id
    to.smoothed = from.smoothed
    to.time = from.time
    to.lat = from.lat
    to.lng = from.lng
    return to
}

fun modelToEntity(from: Point): PointEntity {
    val to = PointEntity()
    to.id = from.id
    to.smoothed = from.smoothed
    to.time = from.time
    to.lat = from.lat
    to.lng = from.lng
    return to
}

fun entityToModel(from: TagEntity?): Tag? {
    if (from == null) return null
    val to = Tag()
    to.id = from.id
    to.name = from.name
    return to
}

fun modelToEntity(from: Tag): TagEntity {
    val to = TagEntity()
    to.id = from.id
    to.name = from.name
    return to
}

fun entityToModel(from: TrackEntity?): Track? {
    if (from == null) return null
    val to = Track()
    to.id = from.id
    to.distance = from.distance
    to.endPointId = from.endPointId
    to.endSmoothedPointId = from.endSmoothedPointId
    to.endTime = from.endTime
    to.startPointId = from.startPointId
    to.startSmoothedPointId = from.startSmoothedPointId
    to.startTime = from.startTime
    to.tag = from.tag
    return to
}

fun modelToEntity(from: Track): TrackEntity {
    val to = TrackEntity()
    to.id = from.id
    to.distance = from.distance
    to.endPointId = from.endPointId
    to.endSmoothedPointId = from.endSmoothedPointId
    to.endTime = from.endTime
    to.startPointId = from.startPointId
    to.startSmoothedPointId = from.startSmoothedPointId
    to.startTime = from.startTime
    to.tag = from.tag
    return to
}

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

fun <Q> modelToEntity(fromList: List<*>, toClass: Class<Q>): List<Q> {
    val toList: MutableList<Q> = ArrayList()
    if (toClass.isAssignableFrom(PointEntity::class.java))
        for (fromEntity in fromList) toList.add(modelToEntity(fromEntity as Point) as Q)
    if (toClass.isAssignableFrom(TagEntity::class.java))
        for (fromEntity in fromList) toList.add(modelToEntity(fromEntity as Tag) as Q)
    if (toClass.isAssignableFrom(TrackEntity::class.java))
        for (fromEntity in fromList) toList.add(modelToEntity(fromEntity as Track) as Q)
    return toList
}