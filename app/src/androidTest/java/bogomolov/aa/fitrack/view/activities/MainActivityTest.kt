package bogomolov.aa.fitrack.view.activities


import android.view.View
import android.widget.TextView

import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import bogomolov.aa.fitrack.R

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.core.AllOf.allOf

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    var mGrantPermissionRule = GrantPermissionRule.grant(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION")

    @Test
    fun mainActivityTest() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText("Tracks list")).perform(click())
        onView(allOf<View>(isAssignableFrom(TextView::class.java), withParent(isAssignableFrom(Toolbar::class.java))))
                .check(matches(withText("Tracks")))
    }
}
