package bogomolov.aa.fitrack.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import bogomolov.aa.fitrack.model.TrackerService;
import bogomolov.aa.fitrack.view.MainView;

public class MainPresenter {
    private MainView mainView;

    public MainPresenter(MainView mainView) {
        this.mainView = mainView;
    }

    public void startService(){
        Context context = (Context)mainView;
        Intent intent =  new Intent(context, TrackerService.class);
        context.startService(intent);
    }
}
