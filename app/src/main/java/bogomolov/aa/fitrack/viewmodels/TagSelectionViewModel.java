package bogomolov.aa.fitrack.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.repository.RepositoryImpl;
import bogomolov.aa.fitrack.core.model.Tag;

public class TagSelectionViewModel extends ViewModel {
    public MutableLiveData<String> newName = new MutableLiveData<>();
    public MutableLiveData<List<Tag>> tagsLiveData = new MutableLiveData<>();
    private RepositoryImpl dbProvider;
    private List<Tag> tags;

    @Inject
    public TagSelectionViewModel(RepositoryImpl dbProvider) {
        this.dbProvider = dbProvider;
        tags = new ArrayList<>(dbProvider.getTags());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        dbProvider.close();
    }

    public void onNewTag(){
        String tagName = newName.getValue();
        Tag tag = new Tag(tagName);
        dbProvider.addTag(tag);
        tags.add(tag);
        tagsLiveData.setValue(tags);
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void deleteTag(Tag tag){
        dbProvider.deleteTag(tag);
        tags.remove(tag);
        tagsLiveData.setValue(tags);
    }
}
