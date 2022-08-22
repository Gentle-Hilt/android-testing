package com.example.android.architecture.blueprints.todoapp.tasks


import android.app.Activity
import androidx.appcompat.widget.Toolbar
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.MainRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import com.example.android.architecture.blueprints.todoapp.data.source.local.entity.Task
import com.example.android.architecture.blueprints.todoapp.testutil.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.testutil.monitorActivity
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@LargeTest
@HiltAndroidTest
@ExperimentalCoroutinesApi
class TasksActivityTest {

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @Inject
    lateinit var database: ToDoDatabase

    @Inject
    lateinit var repository: MainRepository


    @Before
    fun setUp() {
        hiltRule.inject()

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun tearDown() = runTest{
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)

        repository.deleteAllTasks()

        database.clearAllTables()
        database.close()

    }

    @Test
    fun editTask() = runTest {
        //given
        val task = Task("one", "two")
        repository.saveTask(task)

        // E2E starts activity and behaving as user does
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario) // LOOK HERE
        // Click on the task on the list and verify that all the data is correct.
        onView(withText("one")).perform(click())
        onView(withId(R.id.task_detail_title_text)).check(matches(withText("one")))
        onView(withId(R.id.task_detail_description_text)).check(matches(withText("two")))
        onView(withId(R.id.task_detail_complete_checkbox)).check(matches((isNotChecked())))
        // Click on the edit button, edit, and save.
        onView(withId(R.id.edit_task_fab)).perform(click())
        onView(withId(R.id.add_task_title_edit_text)).perform(replaceText("NEW TITLE"))
        onView(withId(R.id.add_task_description_edit_text)).perform(replaceText("NEW DESCRIPTION"))
        onView(withId(R.id.save_task_fab)).perform(click())
        // Verify task is displayed on screen in the task list.
        onView(withText("NEW TITLE")).check(matches(isDisplayed()))
        // Verify previous task is not displayed.
        onView(withText("one")).check(doesNotExist())

        activityScenario.close()

    }

    @Test
    fun createOneTask_deleteTask() {
        // 1. Start TasksActivity.
        val scenario = launchActivity<TasksActivity>()
        dataBindingIdlingResource.monitorActivity(scenario)
        // 2. Add an active task by clicking on the FAB and saving a new task.
        onView(withId(R.id.add_task_fab)).perform(click())
        onView(withId(R.id.add_task_title_edit_text)).perform(replaceText("one"))
        onView(withId(R.id.add_task_description_edit_text)).perform(replaceText("two"))
        onView(withId(R.id.save_task_fab)).perform(click())
        // 3. Open the new task in a details view.
        onView(withText("one")).perform(click())
        onView(withId(R.id.task_detail_title_text)).check(matches(withText("one")))
        onView(withId(R.id.task_detail_description_text)).check(matches(withText("two")))
        // 4. Click delete task in menu.
        onView(withId(R.id.menu_delete)).perform(click())
        // 5. Verify it was deleted.
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        onView(withText("one")).check(doesNotExist())
        // 6. Make sure the activity is closed.
        scenario.close()

    }

    @Test
    fun createTwoTask_deleteOne() = runTest{
        repository.saveTask(Task("one","one"))
        repository.saveTask(Task("two","two"))

        val scenario = launchActivity<TasksActivity>()
        dataBindingIdlingResource.monitorActivity(scenario)

        // go to the second task detail screen
        onView(withText("two")).perform(click())

        // pressing delete task at the top right corner
        onView(withId(R.id.menu_delete)).perform(click())

        //Show all tasks
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())

        // assert that we deleted only one task
        onView(withText("one")).check(matches(isDisplayed()))
        onView(withText("two")).check(doesNotExist())

        scenario.close()

    }


    @Test
    fun markTaskAsCompleteInDetailsScreen_() = runTest{
        val task = Task("one", "two")
        repository.saveTask(task)

        val scenario = launchActivity<TasksActivity>()
        dataBindingIdlingResource.monitorActivity(scenario)

        // go to the detail screen
        onView(withText("one")).perform(click())

        // pressing checkbox and returning back
        onView(withId(R.id.task_detail_complete_checkbox)).perform(click())
        pressBack()

        // assert that we checked out box and it shows
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText("one"))))
            .check(matches(isChecked()))

        scenario.close()

    }

    @Test
    fun markTaskAsCompleteInTasksScreen() = runTest{
        repository.saveTask(Task("one", "two"))
        repository.saveTask(Task("two", "two"))
        repository.saveTask(Task("three", "two"))
        repository.saveTask(Task("four", "two"))


        val scenario = launchActivity<TasksActivity>()
        dataBindingIdlingResource.monitorActivity(scenario)

        // the name of a function says it all
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText("one"))))
            .perform(click())
            .check(matches(isChecked()))


        scenario.close()
    }

    @Test
    fun taskIsActive_setShowAllActiveInFilter() = runTest{
        repository.saveTask(Task("one", "two"))
        repository.saveTask(Task("two", "two", true))

        val scenario = launchActivity<TasksActivity>()
        dataBindingIdlingResource.monitorActivity(scenario)

        //click on filter and set only Active task to be shown
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())

        // assert that we see only active task
        onView(withText("one")).check(matches(isDisplayed()))
        onView(withText("two")).check(doesNotExist())
    }

    @Test
    fun taskDetailScreen_CheckBoxCheckAndUncheckedCorrectly() = runTest{
        repository.saveTask(Task("one", "two"))


        val scenario = launchActivity<TasksActivity>()
        dataBindingIdlingResource.monitorActivity(scenario)


        // go to the detail screen and check our checkbox first time
        onView(withText("one")).perform(click())
        onView(withId(R.id.task_detail_complete_checkbox)).perform(click())
        onView(withId(R.id.task_detail_complete_checkbox)).check(matches(isChecked()))

        // Uncheck it and assert that
        onView(withId(R.id.task_detail_complete_checkbox)).perform(click())
        onView(withId(R.id.task_detail_complete_checkbox)).check(matches(isNotChecked()))

        onView(withContentDescription(scenario.getToolbarNavigationContentDescription())).perform(click())

        // assert that our checkbox is unchecked on tasks screen
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText("one"))))
            .check(matches(isNotChecked()))

        scenario.close()

    }

    @Test
    fun tasksMainScreenLifeCycle_CompleteCheckFilter_UncheckFilter() = runTest{
        repository.saveTask(Task("one", "two"))

        val scenario = launchActivity<TasksActivity>()
        dataBindingIdlingResource.monitorActivity(scenario)


        // assert that our task is not checked
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText("one"))))
            .check(matches(isNotChecked()))
        // set filter see only Active
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        // assert that we see our task
        onView(withText("one")).check(matches(isDisplayed()))
        // set filter see All
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        // fill our checkbox and assert it
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText("one"))))
            .perform(click())
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText("one"))))
            .check(matches(isChecked()))
        // set filter see ony Completed
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        // assert that we see our checked task
        onView(withText("one")).check(matches(isDisplayed()))

        scenario.close()


    }
    @Test
    fun createTask() = runTest{
        val scenario = launchActivity<TasksActivity>()
        dataBindingIdlingResource.monitorActivity(scenario)

        // go to add task screen
        onView(withId(R.id.add_task_fab)).perform(click())
        // add text and description
        onView(withId(R.id.add_task_title_edit_text)).perform(typeText("one"))
        onView(withId(R.id.add_task_description_edit_text)).perform(typeText("two"))
        // save task
        onView(withId(R.id.save_task_fab)).perform(click())
        //assert that task has been created
        onView(withText("one")).check(matches(isDisplayed()))


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


