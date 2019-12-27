package bogomolov.aa.fitrack.repository.entities;

import androidx.room.Relation;

public class TrackAndPoints {
    @Relation(
            parentColumn = "userId",
            entityColumn = "userOwnerId"
    )

}
