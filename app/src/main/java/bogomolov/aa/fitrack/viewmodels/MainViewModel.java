package bogomolov.aa.fitrack.viewmodels;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import bogomolov.aa.fitrack.model.DbProvider;
import bogomolov.aa.fitrack.model.Point;
import bogomolov.aa.fitrack.view.MainView;

public class MainViewModel extends AndroidViewModel {
    MutableLiveData<String> distance;
    MutableLiveData<String> time;
    MutableLiveData<String> avgSpeed;
    MutableLiveData<String> speed;


    public MainViewModel(@NonNull Application application) {
        super(application);
    }
}
