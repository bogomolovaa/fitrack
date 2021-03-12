package bogomolov.aa.fitrack.features.tracks.tags

import bogomolov.aa.fitrack.domain.model.Tag

interface TagResultListener {

    fun onTagSelectionResult(tag: Tag?)
}
