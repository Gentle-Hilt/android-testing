package com.example.android.architecture.blueprints.todoapp.ui.fragments

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.MainRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.entity.Task
import com.example.android.architecture.blueprints.todoapp.testutil.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@ExperimentalCoroutinesApi
@MediumTest
@HiltAndroidTest
class StatisticsFragmentTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: MainRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }


    @Test
    fun tasksShowsNonEmptyMessage() = runTest {
        repository.saveTask(Task("one", "two", false))
        repository.saveTask(Task("one2", "two2", true))

        launchFragmentInHiltContainer<StatisticsFragment>()

        val expectedActiveTask =
            getApplicationContext<Context>().getString(R.string.statistics_active_tasks, 50.0f)

        val expectedCompleteTask =
            getApplicationContext<Context>().getString(R.string.statistics_completed_tasks, 50.0f)


        onView(withId(R.id.stats_active_text)).check(matches(isDisplayed()))
        onView(withId(R.id.stats_active_text)).check(matches(withText(expectedActiveTask)))

        onView(withId(R.id.stats_completed_text)).check(matches(isDisplayed()))
        onView(withId(R.id.stats_completed_text)).check(matches(withText(expectedCompleteTask)))


    }

}