package com.example.android.architecture.blueprints.todoapp.navigation

import android.app.Activity
import android.view.Gravity
import androidx.appcompat.widget.Toolbar

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.contrib.NavigationViewActions.navigateTo
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest

import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.MainRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import com.example.android.architecture.blueprints.todoapp.data.source.local.entity.Task

import com.example.android.architecture.blueprints.todoapp.tasks.TasksActivity
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import androidx.test.espresso.contrib.DrawerActions.open
import com.example.android.architecture.blueprints.todoapp.testutil.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.testutil.monitorActivity
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltAndroidTest
@LargeTest
class AppNavigationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var repository: MainRepository

    @Inject
    lateinit var database: ToDoDatabase

    private val dataBindingIdlingResource = DataBindingIdlingResource()


    @Before
    fun setup() {
        hiltRule.inject()

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)

    }

    @After
    fun tearDown() = runTest {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)

        repository.deleteAllTasks()

        database.clearAllTables()
        database.close()
    }

    @Test
    fun assert_drawerLayoutNotOpenFromTheStart() {
        // Start the Tasks screen.
        val scenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        // 1. Check that left drawer is closed at startup.
        onView(withId(R.id.drawer_layout)).check(matches(isClosed()))
        // 2. Open drawer by clicking drawer icon.
        onView(withContentDescription(scenario.getToolbarNavigationContentDescription())).perform(
            click()
        )
        // 3. Check if drawer is open.
        onView(withId(R.id.drawer_layout)).check(matches(isOpen()))
        // When using ActivityScenario.launch(), always call close()
        scenario.close()
    }

    @Test
    fun detailScreen_pressingEditThenBackButtonTwiceAllDataStayTheSame() = runTest {
        val task = Task("Up button", "Description")
        repository.saveTask(task)

        // Start the Tasks screen.
        val scenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        // 1. Click on the task on the list.
        onView(withText("Up button")).perform(click())

        // 2. Click on the edit task button.
        onView(withId(R.id.edit_task_fab)).perform(click())
        // 3. Confirm that if we click Up button once, we end up back at the task details page.
        pressBack()

        onView(withId(R.id.task_detail_description_text)).check(matches(isDisplayed()))
        onView(withId(R.id.task_detail_title_text)).check(matches(isDisplayed()))
        // 4. Confirm that if we click Up button a second time, we end up back at the home screen.
        pressBack()
        onView(withText("Up button")).check(matches(isDisplayed()))
        onView(withId(R.id.tasks_container_layout)).check(matches(isDisplayed()))
        // When using ActivityScenario.launch(), always call close().
        scenario.close()
    }


    @Test
    fun detailScreen_pressingEditThenBackToAssertThatButtonChanged() = runTest {
        val task = Task("Back button", "Description")
        repository.saveTask(task)

        // Start Tasks screen.
        val scenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        // 1. Click on the task on the list.
        onView(withText("Back button")).perform(click())

        // 2. Click on the Edit task button.
        onView(withId(R.id.edit_task_fab)).perform(click())

        onView(withId(R.id.save_task_fab)).check(matches(isDisplayed()))
        // 3. Confirm that if we click Back once, we end up back at the task details page.
        onView(withContentDescription(scenario.getToolbarNavigationContentDescription())).perform(
            click()
        )
        onView(withId(R.id.edit_task_fab)).check(matches(isDisplayed()))
        // 4. Confirm that if we click Back a second time, we end up back at the home screen.
        onView(withContentDescription(scenario.getToolbarNavigationContentDescription())).perform(
            click()
        )

        onView(withId(R.id.tasks_container_layout)).check(matches(isDisplayed()))

        // When using ActivityScenario.launch(), always call close()
        scenario.close()
    }

    @Test
    fun tasksScreen_PressMenuIconNavigateToStatisticScreenThenBack() = runTest {
        val task = Task("one", "two")
        repository.saveTask(task)

        val scenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        // click to open menu
        onView(withContentDescription(scenario.getToolbarNavigationContentDescription())).perform(click())

        // click to navigate to statistic screen
        /*onView(withText("Statistics")).perform(click())*/
        onView(withId(R.id.nav_view)).perform(navigateTo(R.id.statistics_fragment_dest))

        // assert that we on statistic screen
        onView(withId(R.id.statistics_layout)).check(matches(isDisplayed()))

        // assert that our statistic screen has the task that we added
        onView(withId(R.id.stats_active_text)).check(matches(isDisplayed()))

        // again opening drawer
        onView(withId(R.id.drawer_layout)).perform(open())

        // assert that we went back
        onView(withId(R.id.nav_view)).perform(navigateTo(R.id.tasks_fragment_dest))
        onView(withId(R.id.tasks_container_layout)).check(matches(isDisplayed()))


        scenario.close()



    }


}


fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
        : String {
    var description = ""
    onActivity {
        description =
            it.findViewById<Toolbar>(R.id.toolbar).navigationContentDescription as String
    }
    return description
}