package bogomolov.aa.fitrack.features.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.drawerlayout.widget.DrawerLayout

class DrawerMotionLayout : MotionLayout, DrawerLayout.DrawerListener {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        progress = slideOffset
    }

    override fun onDrawerOpened(drawerView: View) {}

    override fun onDrawerClosed(drawerView: View) {}

    override fun onDrawerStateChanged(newState: Int) {}

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as DrawerLayout).addDrawerListener(this)
    }
}
