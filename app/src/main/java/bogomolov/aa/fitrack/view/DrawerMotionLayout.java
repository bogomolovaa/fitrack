package bogomolov.aa.fitrack.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.drawerlayout.widget.DrawerLayout;

public class DrawerMotionLayout extends MotionLayout implements DrawerLayout.DrawerListener{

    public DrawerMotionLayout(Context context) {
        super(context);
    }

    public DrawerMotionLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawerMotionLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
        setProgress(slideOffset);
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((DrawerLayout)getParent()).addDrawerListener(this);
    }
}
