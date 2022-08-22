package com.example.android.architecture.blueprints.todoapp.ui.viewmodels


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.architecture.blueprints.testutil.MainDispatchersRule
import com.example.android.architecture.blueprints.testutil.assertSnackbarMessage
import com.example.android.architecture.blueprints.testutil.getOrAwaitValue
import com.example.android.architecture.blueprints.testutil.observeForTesting
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.FakeRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.entity.Task
import com.example.android.architecture.blueprints.todoapp.util.Result
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TaskDetailViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainDispatchersRule = MainDispatchersRule()


    private lateinit var viewModel: TaskDetailViewModel

    private lateinit var repository: FakeRepository

    val task = Task("one", "two")

    @Before
    fun setUp() {
        repository = FakeRepository()
        repository.addTasks(task)

        viewModel = TaskDetailViewModel(repository)
    }

    @Test
    fun `get active task from repository, load into view`() {
        viewModel.start(task.id)


        // then - assert that the view was notified
        assertThat(viewModel.task.getOrAwaitValue()?.title).isEqualTo(task.title)
        assertThat(viewModel.task.getOrAwaitValue()?.description).isEqualTo(task.description)

    }

    @Test
    fun `complete task`() {
        // load the viewModel
        viewModel.start(task.id)

        //Start observing to compute transformations
        viewModel.task.getOrAwaitValue()

        // assert that task wasn't completed initially
        assertThat(repository.tasksServiceData[task.id]?.isCompleted).isFalse()

        // When - the viewModel called to complete the task
        viewModel.setCompleted(true)

        // Then - task is completed, snackbar shows the correct message
        assertThat(repository.tasksServiceData[task.id]?.isCompleted).isTrue()
        assertSnackbarMessage(viewModel.snackbarText, R.string.task_marked_complete)

    }

    @Test
    fun `activate task`() {
        task.isCompleted = true

        viewModel.start(task.id)

        // Start observing to compute transformations
        viewModel.task.observeForTesting {
            // the task was completed initially
            assertThat(repository.tasksServiceData[task.id]?.isCompleted).isTrue()

            // calling viewModel to activate task
            viewModel.setCompleted(false)

            runTest {
                // the task is not completed and snackbar shows correct message
                val newTask = (repository.getTask(task.id) as Result.Success).data
                assertThat(newTask.isActive).isTrue()
                assertSnackbarMessage(viewModel.snackbarText, R.string.task_marked_active)
            }
        }
    }

    @Test
    fun `repository error, viewModel behaviour `() {
        // given - repository that returns error
        repository.setReturnError(true)

        // given - an initialised viewmodel with an active task
        viewModel.start(task.id)

        // Get the computed liveData value
        viewModel.task.observeForTesting {
            // the data is not available
            assertThat(viewModel.isDataAvailable.getOrAwaitValue()).isFalse()
        }

    }

    @Test
    fun `update snackbar, null value`() {
        // given - Before setting the text get the current value
        val snackBar = viewModel.snackbarText.value

        // then - the value is null
        assertThat(snackBar).isNull()
    }

    @Test
    fun `click on edit task, sets event`() {
        // when - opening a new task
        viewModel.editTask()

        // then - the event is triggered
        val value = viewModel.editTaskEvent.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()).isNotNull()
    }

    @Test
    fun `delete task`() {
        // given - was a task from the start
        assertThat(repository.tasksServiceData.containsValue(task)).isTrue()
        // load the viewModel
        viewModel.start(task.id)

        // when - deleting task
        viewModel.deleteTask()

        // then - task should be deleted
        assertThat(repository.tasksServiceData.containsValue(task)).isFalse()

    }

    @Test
    fun `load task, loading`() = runTest{
        // given - load task in viewModel
        viewModel.start(task.id)


        withContext(UnconfinedTestDispatcher(testScheduler)){
            //Start observing to compute transformations
            viewModel.task.observeForTesting {
                // Force refresh to show the loading indicator
                viewModel.refresh()

                // then - The progress bar is shown
                assertThat(viewModel.dataLoading.getOrAwaitValue()).isTrue()
            }
        }
        // then - progress bar is hidden
        assertThat(viewModel.dataLoading.getOrAwaitValue()).isFalse()


    }


}