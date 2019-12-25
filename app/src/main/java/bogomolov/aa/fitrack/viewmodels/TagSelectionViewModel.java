package bogomolov.aa.fitrack.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Tag;

public class TagSelectionViewModel extends ViewModel {
    public MutableLiveData<String> newName = new MutableLiveData<>();
    public MutableLiveData<List<Tag>> tagsLiveData = new MutableLiveData<>();
    private DbProvider dbProvider;
    private List<Tag> tags;

    @Inject
    public TagSelectionViewModel(DbProvider dbProvider) {
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
        tag = dbProvider.addTag(tag);
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
