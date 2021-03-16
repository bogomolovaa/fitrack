package bogomolov.aa.fitrack.features.tracks.tags

import android.util.Log
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
    var tagsLiveData = MutableLiveData<List<Tag>>()
    private var tags = ArrayList<Tag>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            tags.addAll(repository.getTags())
            tagsLiveData.postValue(tags)
        }
    }

    fun onNewTag(tagName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i("test", "tagName $tagName")
            if (tagName.isNotEmpty()) {
                val tag = Tag(name = tagName)
                repository.addTag(tag)
                tags.add(tag)
                tagsLiveData.postValue(tags)
            }
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTag(tag)
            tags.remove(tag)
            tagsLiveData.postValue(tags)
        }
    }
}