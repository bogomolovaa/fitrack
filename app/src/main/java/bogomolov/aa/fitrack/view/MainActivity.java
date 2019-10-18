package bogomolov.aa.fitrack.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import bogomolov.aa.fitrack.R;
import bogomolov.aa.fitrack.presenter.MainPresenter;

public class MainActivity extends AppCompatActivity {

    MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter.startService();
    }
}
