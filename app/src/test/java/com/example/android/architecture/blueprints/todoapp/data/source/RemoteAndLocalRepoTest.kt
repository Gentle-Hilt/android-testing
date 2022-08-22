package com.example.android.architecture.blueprints.todoapp.data.source

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.testutil.MainDispatchersRule
import com.example.android.architecture.blueprints.todoapp.data.FakeDataSource
import com.example.android.architecture.blueprints.todoapp.data.combined.DefaultTasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.entity.Task
import com.example.android.architecture.blueprints.todoapp.util.Result
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemoteAndLocalRepoTest {

    @get:Rule
    var mainDispatchersRule = MainDispatchersRule()

    private val task1 = Task("Title1", "Description1")
    private val task2 = Task("Title2", "Description2")
    private val task3 = Task("Title3", "Description3")
    private val newTask = Task("Title new", "Description new")
    private val remoteTasks = listOf(task1, task2).sortedBy { it.id }
    private val localTasks = listOf(task3).sortedBy { it.id }
    private val newTasks = listOf(task3).sortedBy { it.id }


    private lateinit var tasksRemoteDataSource: FakeDataSource
    private lateinit var tasksLocalDataSource: FakeDataSource

    // Class under test
    private lateinit var repository: DefaultTasksRepository


    @Before
    fun setUp() {
        // FakeDataSource need to be of type TasksDataSource
        // because we set it in DefaultTasksRepository constructor
        tasksRemoteDataSource = FakeDataSource(remoteTasks.toMutableList())
        tasksLocalDataSource = FakeDataSource(localTasks.toMutableList())


        repository = DefaultTasksRepository(
            tasksRemoteDataSource, tasksLocalDataSource, Dispatchers.Main
        )

    }

    @Test
    fun getTasks_emptyRepoAndUninitialisedCache() = runTest{
        val emptySource = FakeDataSource()

        val tasksRepository = DefaultTasksRepository(
            emptySource, emptySource, Dispatchers.Main
        )

        assertThat(tasksRepository.getTasks() is Result.Success).isEqualTo(true)
    }





    @Test
    fun getTask_repositoryCashesAfterFirstApiCall() = runTest {
        // Trigger the repository to load data, which loads from remote and caches
        val initial = repository.getTasks()

        tasksLocalDataSource.tasks = newTasks.toMutableList()

        val second = repository.getTasks()

        // Initial and second should match because we didn't force a refresh
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getTasks_requestAllTaskFromRemoteDataSource() = runTest{
        // When tasks are requested from the tasks repository
        val tasks = repository.getTasks(true) as Result.Success

        // Then tasks are loaded from the remote data source
        assertThat(tasks.data).isEqualTo(remoteTasks)
    }

    @Test
    fun saveTasks_SaveToLocalAndRemote() = runTest{
        // make sure that our new tasks not in the remote or local data sources
        assertThat(tasksRemoteDataSource.tasks).doesNotContain(newTask)
        assertThat(tasksLocalDataSource.tasks).doesNotContain(newTask)

        // saving our new tasks
        repository.saveTask(newTask)

        // assert that we have it on both of our sources
        assertThat(tasksRemoteDataSource.tasks).contains(newTask)
        assertThat(tasksLocalDataSource.tasks).contains(newTask)

    }


    @Test
    fun getTasks_WithDirtyCache_tasksAreRetrievedFromRemote() = runTest {
        // First call returns from REMOTE
        val tasks = repository.getTasks()

        // Set a different list of tasks in REMOTE
        tasksRemoteDataSource.tasks = newTasks.toMutableList()

        // But if tasks are cached, subsequent calls load from cache
        val cachedTasks = repository.getTasks()
        assertThat(cachedTasks).isEqualTo(tasks)

        // Now force remote loading
        val refreshedTasks = repository.getTasks(true) as Result.Success

        // Tasks must be the recently updated in REMOTE
        assertThat(refreshedTasks.data).isEqualTo(newTasks)
    }

    @Test
    fun getTasks_WithDirtyCache_remoteUnavailableError() = runTest{
        // making our remote unavailable
        tasksRemoteDataSource.tasks = null

        // force remote load
        val refreshedTasks = repository.getTasks(true)

        // assert our error
        assertThat(refreshedTasks).isInstanceOf(Result.Error::class.java)
    }

    @Test
    fun getTasks_WithBothDataSourcesUnavailable_returnsError() = runTest{
        // making both sources unavailable
        tasksRemoteDataSource.tasks = null
        tasksLocalDataSource.tasks = null

        // assert the error is shown
        assertThat(repository.getTasks()).isInstanceOf(Result.Error::class.java)
    }

    @Test
    fun getTasks_RefreshLocalDataSource() = runTest{
        val initialLocal = tasksLocalDataSource.tasks

        // First load will fetch from remote
        val newTask = (repository.getTasks(true) as Result.Success).data

        assertThat(newTask).isEqualTo(remoteTasks)
        assertThat(newTask).isEqualTo(tasksLocalDataSource.tasks)
        assertThat(tasksLocalDataSource.tasks).isEqualTo(initialLocal)
    }

    @Test
    fun completeTask_completesTaskToServiceApiUpdatesCache() = runTest{
        // saving task
        repository.saveTask(newTask)
        // make sure it's active
        assertThat((repository.getTask(newTask.id) as Result.Success).data.isCompleted).isFalse()
        // mark it as complete
        repository.completeTask(newTask.id)
        // assert that it's completed
        assertThat((repository.getTask(newTask.id) as Result.Success).data.isCompleted).isTrue()
    }

    @Test
    fun completeTask_activeTaskToServiceApiUpdatesCache() = runTest{
        // save a task
        repository.saveTask(newTask)
        repository.completeTask(newTask.id)

        // make sure it's completed
        assertThat((repository.getTask(newTask.id) as Result.Success).data.isCompleted).isTrue()

        // mark it as active
        repository.activateTask(newTask.id)

        // assert our task should be active
        val result = repository.getTask(newTask.id) as Result.Success
        assertThat(result.data.isActive).isTrue()

    }

    @Test
    fun getTask_repositoryCachesAfterFirstApiCall() = runTest{
        // remote is task2
        tasksRemoteDataSource.tasks = mutableListOf(task2)
        // task2 saved in local with forceUpdate
        val localImSure = repository.getTask(task2.id, true) as Result.Success
        // assert task2SecondTime saved in local
        assertThat(localImSure.data.id).isEqualTo(task2.id)


        //remote is task1
        tasksRemoteDataSource.tasks = mutableListOf(task1)
        // with force update saving in local
        val shouldBeRemote = repository.getTask(task1.id, true) as Result.Success
        // assert that task1SecondTime saved in local
        assertThat(shouldBeRemote.data.id).isEqualTo(task1.id)

        // idk how this should somehow assert that something is in remote by google XD
        // because without force update all those assertions are failed
    }



    @Test
    fun getTask_forceRefresh() = runTest{
        // we already set it up in before function idk why they bring it here
        tasksRemoteDataSource.tasks = mutableListOf(task1)
        repository.getTask(task1.id)

        // changing remote to not have task1
        tasksRemoteDataSource.tasks = mutableListOf(task2)

        // Force refresh only task2 should stay after cuz only it in remote
        val task1SecondTime = repository.getTask(task1.id, true)
        val task2SecondTime = repository.getTask(task2.id, true)

        // assert that only task2 exist
        assertThat((task1SecondTime as? Result.Success)?.data?.id).isNull()
        assertThat((task2SecondTime as? Result.Success)?.data?.id).isEqualTo(task2.id)
    }


    @Test
    fun clearCompletedTasks() = runTest{
        val completedTasks = task1.copy().apply { isCompleted = true }
        tasksRemoteDataSource.tasks = mutableListOf(completedTasks, task2)
        repository.clearCompletedTasks()

        val tasks = (repository.getTasks(true) as Result.Success).data


        assertThat(tasks).hasSize(1)
        assertThat(tasks).contains(task2)
        assertThat(tasks).doesNotContain(completedTasks)

    }

    @Test
    fun deleteAllTasks() = runTest{
         val initialTasks = (repository.getTasks(true) as Result.Success).data

        // Deleting tasks
        repository.deleteAllTasks()

        // Fetch data
        val afterTasksDeletion = (repository.getTasks() as Result.Success).data

        // assert that tasks are empty
        assertThat(initialTasks).isNotEmpty()
        assertThat(afterTasksDeletion).isEmpty()
    }

    @Test
    fun deletingSingleTask() = runTest{
        // given
        val initialTasks = (repository.getTasks(true) as? Result.Success)?.data
        val deleteOneTask = (repository.getTask(task1.id) as Result.Success).data

        // deleting single task
        repository.deleteTask(deleteOneTask.id)

        // fetch data
        val afterTasksDeletion = (repository.getTasks(true) as Result.Success).data

        // assert deletion
        assertThat(afterTasksDeletion.size).isEqualTo(initialTasks!!.size - 1)
        assertThat(afterTasksDeletion).doesNotContain(task1)
    }



}