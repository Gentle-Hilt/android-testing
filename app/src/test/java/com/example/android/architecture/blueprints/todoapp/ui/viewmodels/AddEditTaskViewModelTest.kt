package com.example.android.architecture.blueprints.todoapp.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.architecture.blueprints.testutil.MainDispatchersRule
import com.example.android.architecture.blueprints.testutil.assertSnackbarMessage
import com.example.android.architecture.blueprints.testutil.getOrAwaitValue
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.data.FakeRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.entity.Task
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AddEditTaskViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainDispatchersRule = MainDispatchersRule()


    private lateinit var repository: FakeRepository

    private lateinit var viewModel: AddEditTaskViewModel

    private val task = Task("one", "two")

    @Before
    fun setUp() {
        repository = FakeRepository()


        viewModel = AddEditTaskViewModel(repository)
    }

    @Test
    fun `saved task to repository, shows success message in Ui`(){
        val newTitle = "New Task Title"
        val newDescription = "Some Task Description"

        (viewModel).apply {
            title.value = newTitle
            description.value = newDescription
        }
        viewModel.saveTask()

        val newTask = repository.tasksServiceData.values.first()

        // then - task is saved in repository and the view updated
        assertThat(newTask.title).isEqualTo(newTitle)
        assertThat(newTask.description).isEqualTo(newDescription)

    }


    @Test
    fun `load task, loading`() = runTest{
        // Add task to repository
        repository.addTasks(task)

        // load the task with the viewmodel
        viewModel.start(task.id)

        // the task is loaded
        assertThat(viewModel.title.getOrAwaitValue()).isEqualTo(task.title)
        assertThat(viewModel.description.getOrAwaitValue()).isEqualTo(task.description)
        assertThat(viewModel.dataLoading.getOrAwaitValue()).isFalse()
    }

    @Test
    fun `save task, empty title, error`(){
        val title = ""
        val description = "Some Task Description"
        (viewModel).apply {
            this.title.value = title
            this.description.value = description
        }

        // When saving an incomplete task
        viewModel.saveTask()

        // Then the snackbar shows an error
        assertSnackbarMessage(viewModel.snackbarText, R.string.empty_task_message)
    }
    @Test
    fun `save task, empty description, error `(){
        val title = "Some Task title"
        val description = ""
        (viewModel).apply {
            this.title.value = title
            this.description.value = description
        }

        // When saving an incomplete task
        viewModel.saveTask()

        // Then the snackbar shows an error
        assertSnackbarMessage(viewModel.snackbarText, R.string.empty_task_message)
    }

    @Test
    fun `save task, null title, error `(){
        val title = null
        val description = "Some Task Description"
        (viewModel).apply {
            this.title.value = title
            this.description.value = description
        }

        // When saving an incomplete task
        viewModel.saveTask()

        // Then the snackbar shows an error
        assertSnackbarMessage(viewModel.snackbarText, R.string.empty_task_message)
    }
    @Test
    fun `save task, null description, error`(){
        val title = "Some Task title"
        val description = null
        (viewModel).apply {
            this.title.value = title
            this.description.value = description
        }

        // When saving an incomplete task
        viewModel.saveTask()

        // Then the snackbar shows an error
        assertSnackbarMessage(viewModel.snackbarText, R.string.empty_task_message)
    }

    @Test
    fun `save task, null description null tittle, error`(){
        val title = null
        val description = null
        (viewModel).apply {
            this.title.value = title
            this.description.value = description
        }

        // When saving an incomplete task
        viewModel.saveTask()

        // Then the snackbar shows an error
        assertSnackbarMessage(viewModel.snackbarText, R.string.empty_task_message)
    }

    @Test
    fun `save task, empty title empty description, error `(){
        val title = ""
        val description = ""
        (viewModel).apply {
            this.title.value = title
            this.description.value = description
        }

        // When saving an incomplete task
        viewModel.saveTask()

        // Then the snackbar shows an error
        assertSnackbarMessage(viewModel.snackbarText, R.string.empty_task_message)
    }


}