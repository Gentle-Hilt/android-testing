package com.example.android.architecture.blueprints.todoapp.ui.fragments

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.MainRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import com.example.android.architecture.blueprints.todoapp.testutil.launchFragmentInHiltContainer
import com.example.android.architecture.blueprints.todoapp.util.Result
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
@ExperimentalCoroutinesApi
@MediumTest
class AddEditTaskFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()



    @Inject
    lateinit var repository: MainRepository


    @Before
    fun setUp() = runTest {
        hiltRule.inject()

    }


    @Test
    fun emptyTask_isNotSaved() {
        // GIVEN - On the "Add Task" screen.
        val bundle = AddEditTaskFragmentArgs(
            null, getApplicationContext<Context>().getString(R.string.add_task)
        ).toBundle()
        launchFragmentInHiltContainer<AddEditTaskFragment>(bundle)

        // WHEN - Enter invalid title and description combination and click save
        onView(withId(R.id.add_task_title_edit_text)).perform(clearText())
        onView(withId(R.id.add_task_description_edit_text)).perform(clearText())
        onView(withId(R.id.save_task_fab)).perform(click())

        // THEN - Entered Task is still displayed (a correct task would close it).
        onView(withId(R.id.add_task_title_edit_text)).check(matches(isDisplayed()))
    }

    @Test
    fun validTask_isSaved() = runTest {
        // GIVEN - On the "Add Task" screen.
        val navController = TestNavHostController(getApplicationContext())
        val bundle = AddEditTaskFragmentArgs(
            null, getApplicationContext<Context>().getString(R.string.add_task)
        ).toBundle()

        launchFragmentInHiltContainer<AddEditTaskFragment>(
            bundle
        ) {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.add_edit_task_fragment_dest)
            Navigation.setViewNavController(requireView(), navController)
        }

        // WHEN - Valid title and description combination and click save
        onView(withId(R.id.add_task_title_edit_text)).perform(replaceText("title"))
        onView(withId(R.id.add_task_description_edit_text)).perform(replaceText("description"))
        onView(withId(R.id.save_task_fab)).perform(click())

        // THEN - Verify that the repository saved the task
        val tasks = (repository.getTasks(true) as Result.Success).data
        assertThat(tasks.size).isEqualTo(1)
        assertThat(tasks[0].title).isEqualTo("title")
        assertThat(tasks[0].description).isEqualTo("description")


    }

    @Test
    fun validTask_NavigatesBack() {
        val navController = TestNavHostController(getApplicationContext())
        val bundle = AddEditTaskFragmentArgs(
            null, getApplicationContext<Context>().getString(R.string.add_task)
        ).toBundle()

        launchFragmentInHiltContainer<AddEditTaskFragment>(
            bundle
        ) {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.add_edit_task_fragment_dest)
            Navigation.setViewNavController(requireView(), navController)
        }

        // WHEN - Valid title and description combination and click save
        onView(withId(R.id.add_task_title_edit_text)).perform(replaceText("title"))
        onView(withId(R.id.add_task_description_edit_text)).perform(replaceText("description"))
        onView(withId(R.id.save_task_fab)).perform(click())

        // THEN - Verify that we navigated back to the tasks screen.
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.tasks_fragment_dest)

    }


    @Test
    fun validTask_isNotSaved() {
        // GIVEN - On the "Add Task" screen.
        val navController = TestNavHostController(getApplicationContext())
        val bundle = AddEditTaskFragmentArgs(
            null, getApplicationContext<Context>().getString(R.string.add_task)
        ).toBundle()

        launchFragmentInHiltContainer<AddEditTaskFragment>(
            bundle
        ) {
            navController.setGraph(R.navigation.nav_graph)
            navController.setCurrentDestination(R.id.add_edit_task_fragment_dest)
            Navigation.setViewNavController(requireView(), navController)
        }

        // when - Enter invalid title and description combination and click save
        onView(withId(R.id.add_task_title_edit_text)).perform(clearText())
        onView(withId(R.id.add_task_description_edit_text)).perform(clearText())
        onView(withId(R.id.save_task_fab)).perform(click())

        // THEN - Entered Task is still displayed (a correct task would close it).
        onView(withId(R.id.add_task_title_edit_text)).check(matches(isDisplayed()))

    }


}