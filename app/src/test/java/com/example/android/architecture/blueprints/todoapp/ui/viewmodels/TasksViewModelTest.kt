package com.example.android.architecture.blueprints.todoapp.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.android.architecture.blueprints.testutil.*
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.FakeRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.entity.Task
import com.example.android.architecture.blueprints.todoapp.tasks.ADD_EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.tasks.DELETE_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.tasks.EDIT_RESULT_OK
import com.example.android.architecture.blueprints.todoapp.tasks.TasksFilterType
import com.example.android.architecture.blueprints.todoapp.util.Event
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class TasksViewModelTest {

    @get:Rule
    var mainDispatchersRule = MainDispatchersRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    private lateinit var viewModel: TasksViewModel

    private lateinit var repository: FakeRepository


    @Before
    fun setup() {
        repository = FakeRepository()

        val task1 = Task("Title1", "description1")
        val task2 = Task("Title2", "description2", true)
        val task3 = Task("Title1", "description1", true)
        repository.addTasks(task1, task2, task3)

        viewModel = TasksViewModel(repository, SavedStateHandle())
    }

    @Test
    fun `loading all tasks into view dataLoading toggle off`() = runTest {
        // given - viewModel with initialised tasks and set filtering
        viewModel.setFiltering(TasksFilterType.ALL_TASKS)

        // Trigger load of tasks
        viewModel.loadTasks(true)



        withContext(UnconfinedTestDispatcher(testScheduler)) {
            // it should theoretically engage our liveData to update itself
            // but something went wrong so i just added refresh() functionality
            viewModel.items.observeForTesting {

                // Force refresh to show the loading indicator
                // but it should bee without it ...
                viewModel.refresh()

                // then - The progress bar is shown
                assertThat(viewModel.dataLoading.getOrAwaitValue()).isTrue()
            }

        }

        // then - The progress bar is hidden
        assertThat(viewModel.dataLoading.getOrAwaitValue()).isFalse()
        // then - amount of tasks should be the amount of items
        assertThat(viewModel.items.getOrAwaitValue()).hasSize(3)

    }

    @Test
    fun `load active tasks into view`() = runTest {
        // given - initialised viewModel and set filter
        viewModel.setFiltering(TasksFilterType.ACTIVE_TASKS)
        // Trigger tasks to load
        viewModel.loadTasks(true)

        withContext(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.items.observeForTesting {

                // artificial force update it should work automatically
                viewModel.refresh()

                // then progress bar is shown
                assertThat(viewModel.dataLoading.getOrAwaitValue()).isTrue()

            }
        }
        // then -  progress bar is hidden with amount of 1
        assertThat(viewModel.dataLoading.getOrAwaitValue()).isFalse()
        assertThat(viewModel.items.getOrAwaitValue()).hasSize(1)
    }

    @Test
    fun `load completed tasks into view`() = runTest {

        viewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)
        viewModel.loadTasks(true)
        viewModel.items.observeForTesting {


            advanceUntilIdle()
            assertThat(viewModel.dataLoading.getOrAwaitValue()).isFalse()
            assertThat(viewModel.items.getOrAwaitValue()).hasSize(2)
        }
    }

    @Test
    fun `load tasks error`() = runTest {
        // make and error
        repository.setReturnError(true)

        // load tasks
        viewModel.loadTasks(true)

        //Observe the tasks to keep liveData emitting
        viewModel.items.observeForTesting {
            // the progress indicator is hidden
            assertThat(viewModel.dataLoading.getOrAwaitValue()).isFalse()
            // And the list of items is empty
            assertThat(viewModel.items.getOrAwaitValue()).isEmpty()

            // And snackbar is updated
            assertSnackbarMessage(viewModel.snackbarText, R.string.loading_tasks_error)
        }
    }

    @Test
    fun `click on fab show task ui`() {
        // when adding a new task
        viewModel.addNewTask()

        // then the event is triggered
        val value = viewModel.newTaskEvent.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled()).isNotNull()
    }

    @Test
    fun `click open task sets event`() {
        // when clicking a task
        val taskId = "42"
        viewModel.openTask(taskId)

        // then the event is Triggered
        assertLiveDataEventTriggered(viewModel.openTaskEvent, taskId)
    }

    @Test
    fun `clear completed tasks, clear tasks`() {
        // When completed tasks are cleared
        viewModel.clearCompletedTasks()

        //Fetch tasks
        viewModel.loadTasks(true)

        //Fetch tasks
        val allTasks = viewModel.items.getOrAwaitValue()
        val completedTask = allTasks.filter { it.isCompleted }

        // Assert there are  no completed tasks left
        assertThat(completedTask).isEmpty()
        // assert active tasks is not clear
        assertThat(allTasks).hasSize(1)

        // assert snackbar is updated
        assertSnackbarMessage(viewModel.snackbarText, R.string.completed_tasks_cleared)

    }

    @Test
    fun `show edit result message, edit ok ,snackbar updated`() {
        // When viewmodel receives result from another destination
        viewModel.showEditResultMessage(EDIT_RESULT_OK)

        // the snackbar is updated
        assertSnackbarMessage(viewModel.snackbarText, R.string.successfully_saved_task_message)
    }

    @Test
    fun `show edit result message, add ok, snackbar updated`() {
        // When viewmodel receives result from another destination
        viewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)

        // the snackbar is updated
        assertSnackbarMessage(viewModel.snackbarText, R.string.successfully_added_task_message)
    }

    @Test
    fun `show edit result message, delete ok, snackbar updated`() {
        // When viewmdedol receives result from another destination
        viewModel.showEditResultMessage(DELETE_RESULT_OK)

        // the snackbar is updated
        assertSnackbarMessage(viewModel.snackbarText, R.string.successfully_deleted_task_message)
    }

    @Test
    fun `complete task, data and snackbar updated`() {
        // with a repository that has active task
        val task = Task("one", "two")
        repository.addTasks(task)

        // Complete task
        viewModel.completeTask(task, true)

        // assert that task is completed
        assertThat(repository.tasksServiceData[task.id]?.isCompleted).isTrue()

        // the snackbar is updated
        assertSnackbarMessage(viewModel.snackbarText, R.string.task_marked_complete)


    }

    @Test
    fun `active task, data and snackbar updated`() {
        // with repository that has completed task
        val task = Task("one", "two", true)

        // active task
        viewModel.completeTask(task, false)

        // assert that task is active
        assertThat(repository.tasksServiceData[task.id]?.isActive).isTrue()
        // the snackbar is updated
        assertSnackbarMessage(viewModel.snackbarText, R.string.task_marked_active)
    }

    @Test
    fun `add task, "Add View" is visible `() {
        // when fliter type is ALL_TASKS
        viewModel.setFiltering(TasksFilterType.ALL_TASKS)

        //Then the "Add task " action is visible
        assertThat(viewModel.tasksAddViewVisible.getOrAwaitValue()).isTrue()
    }


    @Test
    fun `add new task, sets new task event`() {
        //when
        viewModel.addNewTask()


        val value = viewModel.newTaskEvent.getOrAwaitValue()
        //then
        assertThat(value.getContentIfNotHandled()).isNotNull()
    }

    @Test
    fun `set filter type to show all tasks, add task button visible`() {
        //when
        viewModel.setFiltering(TasksFilterType.ALL_TASKS)

        val value = viewModel.tasksAddViewVisible.getOrAwaitValue()

        //then
        assertThat(value).isTrue()

    }

    @Test
    fun `complete tasks, snackbar and data updating`() = runTest {
        val task = Task("Title", "Description")
        repository.addTasks(task)

        // Mark the task as complete task.
        viewModel.completeTask(task, true)

        val completedTask = repository.tasksServiceData[task.id]?.isCompleted
        // Verify the task is completed.
        assertThat(repository.tasksServiceData[task.id]?.isCompleted).isEqualTo(completedTask)

        // Assert that the snackbar has been updated with the correct text.
        val snackbarText: Event<Int> = viewModel.snackbarText.getOrAwaitValue()


        assertThat(snackbarText.getContentIfNotHandled()).isEqualTo(R.string.task_marked_complete)
    }


}












