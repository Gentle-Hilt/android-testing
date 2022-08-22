package com.example.android.architecture.blueprints.todoapp.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.architecture.blueprints.testutil.MainDispatchersRule
import com.example.android.architecture.blueprints.testutil.getOrAwaitValue
import com.example.android.architecture.blueprints.todoapp.data.FakeFailingTasksRemoteDataSource
import com.example.android.architecture.blueprints.todoapp.data.FakeRepository
import com.example.android.architecture.blueprints.todoapp.data.combined.DefaultTasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.entity.Task
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class StatisticsViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainDispatchersRule = MainDispatchersRule()

    private lateinit var viewModel: StatisticsViewModel

    private lateinit var repository: FakeRepository

    @Before
    fun setup(){
        repository = FakeRepository()

        viewModel = StatisticsViewModel(repository)

    }

    @Test
    fun `load empty task from repository, empty result`() = runTest{
        // given no tasks

        // assert that result is empty
        assertThat(viewModel.empty.getOrAwaitValue()).isTrue()

    }

    @Test
    fun `load tasks from repository, result`(){
        // given 4 tasks, one active three completed
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2", true)
        val task3 = Task("Title3", "Description3", true)
        val task4 = Task("Title3", "Description4", true)
        repository.addTasks(task1, task2, task3, task4)

        // Assert that result is not empty
        assertThat(viewModel.empty.getOrAwaitValue()).isFalse()
        assertThat(viewModel.activeTasksPercent.getOrAwaitValue()).isEqualTo(25f)
        assertThat(viewModel.completedTasksPercent.getOrAwaitValue()).isEqualTo(75f)


    }

    @Test
    fun `load statistic when tasks are unavailable, call error to display`() {
        // given - failing datasource in repository
        val errorViewModel = StatisticsViewModel(
            DefaultTasksRepository(
                FakeFailingTasksRemoteDataSource,
                FakeFailingTasksRemoteDataSource,
                Dispatchers.Main
            )
        )
        // then - error message is shown
        assertThat(errorViewModel.empty.getOrAwaitValue()).isTrue()
        assertThat(errorViewModel.error.getOrAwaitValue()).isTrue()

    }

    @Test
    fun `load statistics when tasks are unavailable, call error to display`() {
        repository.setReturnError(true)
        viewModel.refresh()

        // Then empty and error are true (which triggers an error message to be shown).
        assertThat(viewModel.empty.getOrAwaitValue()).isEqualTo(true)
        assertThat(viewModel.error.getOrAwaitValue()).isEqualTo(true)


    }

    @Test
    fun `loading task should load`() = runTest {
        withContext(UnconfinedTestDispatcher(testScheduler)){
            // Force refresh to show the loading indicator
            viewModel.refresh()

            // then - The progress bar is shown
            assertThat(viewModel.dataLoading.getOrAwaitValue()).isTrue()

        }
        // then - The progress bar is hidden
        assertThat(viewModel.dataLoading.getOrAwaitValue()).isFalse()
    }

}