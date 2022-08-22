package com.example.android.architecture.blueprints.todoapp.ui.fragments


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.android.architecture.blueprints.todoapp.data.MainRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.entity.Task
import com.example.android.architecture.blueprints.todoapp.tasks.TasksActivity
import com.example.android.architecture.blueprints.todoapp.testutil.launchFragmentInHiltContainer
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltAndroidTest
@MediumTest
class TasksFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @Inject
    lateinit var repository: MainRepository

    lateinit var navController: NavController


    @Before
    fun setUp() = runTest {
        hiltRule.inject()

        navController = mockk(relaxed = true)


    }


    @Test
    fun displayTask_whenRepositoryHasData() = runTest {
        //given
        val task = Task("one", "two")
        repository.saveTask(task)

        //when - true fragment test
        launchFragmentInHiltContainer<TasksFragment>()

        //then - assert that our data is displayed
        onView(withText("one")).check(matches(isDisplayed()))

    }

    @Test
    fun displayActive_Task() = runTest {
        //given
        val task = Task("one", "two", false)
        repository.saveTask(task)

        // we need to launch activity to operate with menu_filter
        launchActivity()

        //then - Open menu filter and set it as Active
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).check(matches(isDisplayed()))
        onView(withText(R.string.nav_active)).perform(click())
        // assert that we see our task
        onView(withText("one")).check(matches(isDisplayed()))
    }

    @Test
    fun displayCompletedTask() = runTest {
        val task = Task("one", "two", true)
        repository.saveTask(task)

        // launch activity to operate with menu_filter
        launchActivity()

        // assert that our saved tasks was added
        onView(withText("one")).check(matches(isDisplayed()))

        // assert that we don't see complete tasks in completed
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        onView(withText("one")).check(matches(not(isDisplayed())))

        // assert that complete task seen in completed
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        onView(withText("one")).check(matches(isDisplayed()))
    }


    @Test
    fun deleteOneTask() = runTest {
        //given
        val task = Task("one", "two")
        repository.saveTask(task)
        // we need to launch activity to operate with menu_filter
        launchActivity()
        // go to the detail screen and press delete menu
        onView(withText("one")).perform(click())
        onView(withId(R.id.menu_delete)).perform(click())
        // assert that in our tasks screen task has been deleted
        onView(withText("one")).check(doesNotExist())

    }

    @Test
    fun deleteOneTaskOfTwo() = runTest {
        //given
        repository.saveTask(Task("one", "two"))
        repository.saveTask(Task("two", "two"))
        //when
        launchActivity()
        //then
        onView(withText("one")).check(matches(isDisplayed()))
        onView(withText("two")).check(matches(isDisplayed()))

        // deleting one of two tasks
        onView(withText("one")).perform(click())
        onView(withId(R.id.menu_delete)).perform(click())
        // assert deletion
        onView(withText("one")).check(doesNotExist())
        onView(withText("two")).check(matches(isDisplayed()))

    }

    @Test
    fun markTaskAsComplete() = runTest {
        //given
        val task = Task("one", "two")
        repository.saveTask(task)
        //when
        launchActivity()
        //then asserting that our task was not checked at the beginning
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        onView(withText("one")).check(matches(not(isDisplayed())))
        // go back to all tasks
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        onView(withText("one")).check(matches(isDisplayed()))
        // marking our task as complete
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText("one")))).perform(click())
        // assert that
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText("one")))).check(
            matches(
                isChecked()
            )
        )

    }

    @Test
    fun markTaskAsActive() = runTest {
        //given
        val task = Task("one", "two", true)
        repository.saveTask(task)
        //when
        launchActivity()
        //then asserting that we don't see task as Active at the beginning
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        onView(withText("one")).check(matches(not(isDisplayed())))
        //go to all tasks screen
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        // making our task active again
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText("one")))).perform(click())
        // assert that we only see task as active
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        // for some reason two above onView is stopping test but everything is correct
        onView(withText("one")).check(matches(isDisplayed()))

    }

    @Test
    fun clearCompletedTasks() = runTest {
        withContext(UnconfinedTestDispatcher(testScheduler)) {
            repository.saveTask(Task("one", "two"))
            repository.saveTask(Task("one2", "two2", true))
        }


        launchActivity()

        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText(R.string.menu_clear)).perform(click())

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())

        onView(withText("one2")).check(doesNotExist())
        onView(withText("one")).check(matches(isDisplayed()))


    }

    @Test
    fun noTasksMessage() {

        launchActivity()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())

        // Verify the "You have no tasks!" text is shown
        onView(withText("You have no tasks!")).check(matches(isDisplayed()))
    }

    @Test
    fun noCompletedTasksMessage() {

        launchActivity()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())

        // Verify the "You have no completed tasks!" text is shown
        onView(withText("You have no completed tasks!")).check(matches((isDisplayed())))
    }

    @Test
    fun noActiveTasksMessage() {

        launchActivity()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())

        // Verify the "You have no active tasks!" text is shown
        onView(withText("You have no active tasks!")).check(matches((isDisplayed())))
    }


    @Test
    fun clickAddTaskButton_NavigateToAddEditFragment() = runTest {
        // given our navController and Task
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val task = Task("one", "two")
        repository.saveTask(task)

        //when we're launching our fragment with navController
        launchFragmentInHiltContainer<TasksFragment>() {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.tasks_fragment_dest)
            Navigation.setViewNavController(requireView(), navController)

        }
        // performing click to go to AddEditTaskFragment
        onView(withId(R.id.add_task_fab)).perform(click())

        // assert that we on the right fragment
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.add_edit_task_fragment_dest)


    }


}


// might be it doesn't matter fragment or activity because it's UI tests
private fun launchActivity(): ActivityScenario<TasksActivity>? {
    val activityScenario = launch(TasksActivity::class.java)
    activityScenario.onActivity { activity ->
        // Disable animations in RecyclerView
        (activity.findViewById(R.id.tasks_list) as RecyclerView).itemAnimator = null
    }
    return activityScenario
}

