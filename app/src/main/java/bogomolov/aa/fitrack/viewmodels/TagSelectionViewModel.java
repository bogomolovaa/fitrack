package bogomolov.aa.fitrack.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.repository.Repository;
import bogomolov.aa.fitrack.core.model.Tag;

import static bogomolov.aa.fitrack.core.Rx.*;


public class TagSelectionViewModel extends ViewModel {
    public MutableLiveData<String> newName = new MutableLiveData<>();
    public MutableLiveData<List<Tag>> tagsLiveData = new MutableLiveData<>();
    private Repository repository;
    private List<Tag> tags;

    @Inject
    public TagSelectionViewModel(Repository repository) {
        this.repository = repository;
        worker(()->{
            tags = repository.getTags();
            tagsLiveData.postValue(tags);
        });
    }

    public void onNewTag() {
        worker(()->{
            String tagName = newName.getValue();
            Tag tag = new Tag(tagName);
            repository.addTag(tag);
            tags.add(tag);
            tagsLiveData.postValue(tags);
        });
    }

    public void deleteTag(Tag tag) {
        worker(()->{
            repository.deleteTag(tag);
            tags.remove(tag);
            tagsLiveData.postValue(tags);
        });
    }
}
