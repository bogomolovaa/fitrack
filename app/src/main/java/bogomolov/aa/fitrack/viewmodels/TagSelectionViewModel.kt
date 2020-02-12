package bogomolov.aa.fitrack.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import javax.inject.Inject

import bogomolov.aa.fitrack.repository.Repository
import bogomolov.aa.fitrack.core.model.Tag

import bogomolov.aa.fitrack.android.worker


class TagSelectionViewModel @Inject
constructor(private val repository: Repository) : ViewModel() {
    var newName = MutableLiveData<String>()
    var tagsLiveData = MutableLiveData<List<Tag>>()
    private var tags: MutableList<Tag>? = null

    init {
        worker(viewModelScope) {
            tags = repository.getTags() as MutableList<Tag>?
            tagsLiveData.postValue(tags)
        }
    }

    fun onNewTag() {
        worker(viewModelScope) {
            val tagName = newName.value
            if (tagName != null) {
                val tag = Tag(tagName)
                repository.addTag(tag)
                tags!!.add(tag)
                tagsLiveData.postValue(tags)
            }
        }
    }

    fun deleteTag(tag: Tag) {
        worker(viewModelScope) {
            repository.deleteTag(tag)
            tags!!.remove(tag)
            tagsLiveData.postValue(tags)
        }
    }
}