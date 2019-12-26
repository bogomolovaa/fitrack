package bogomolov.aa.fitrack.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.repository.Repository;
import bogomolov.aa.fitrack.core.model.Tag;

public class TagSelectionViewModel extends ViewModel {
    public MutableLiveData<String> newName = new MutableLiveData<>();
    public MutableLiveData<List<Tag>> tagsLiveData = new MutableLiveData<>();
    private Repository repository;
    private List<Tag> tags;

    @Inject
    public TagSelectionViewModel(Repository repository) {
        this.repository = repository;
        tags = new ArrayList<>(this.repository.getTags());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.close();
    }

    public void onNewTag(){
        String tagName = newName.getValue();
        Tag tag = new Tag(tagName);
        repository.addTag(tag);
        tags.add(tag);
        tagsLiveData.setValue(tags);
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void deleteTag(Tag tag){
        repository.deleteTag(tag);
        tags.remove(tag);
        tagsLiveData.setValue(tags);
    }
}
