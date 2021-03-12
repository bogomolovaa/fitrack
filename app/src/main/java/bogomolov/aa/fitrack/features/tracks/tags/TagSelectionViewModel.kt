package bogomolov.aa.fitrack.features.tracks.tags

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bogomolov.aa.fitrack.domain.Repository
import bogomolov.aa.fitrack.domain.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class TagSelectionViewModel @Inject
constructor(private val repository: Repository) : ViewModel() {
    var newName = MutableLiveData<String>()
    var tagsLiveData = MutableLiveData<List<Tag>>()
    private var tags: MutableList<Tag>? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            tags = repository.getTags() as MutableList<Tag>?
            tagsLiveData.postValue(tags)
        }
    }

    fun onNewTag() {
        viewModelScope.launch(Dispatchers.IO) {
            val tagName = newName.value
            if (tagName != null) {
                val tag = Tag(name = tagName)
                repository.addTag(tag)
                tags!!.add(tag)
                tagsLiveData.postValue(tags)
            }
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTag(tag)
            tags!!.remove(tag)
            tagsLiveData.postValue(tags)
        }
    }
}
