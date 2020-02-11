package bogomolov.aa.fitrack.view

import bogomolov.aa.fitrack.core.model.Tag

interface TagResultListener {

    fun onTagSelectionResult(tag: Tag?)
}
