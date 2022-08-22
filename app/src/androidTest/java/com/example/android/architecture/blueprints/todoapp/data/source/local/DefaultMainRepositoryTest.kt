package com.example.android.architecture.blueprints.todoapp.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.data.DefaultMainRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.entity.Task
import com.example.android.architecture.blueprints.todoapp.util.Result
import com.example.android.architecture.blueprints.todoapp.util.succeeded
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@MediumTest
@HiltAndroidTest
class DefaultMainRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    @Named("test_db")
    lateinit var dababase: ToDoDatabase

    lateinit var defaultMainRepository: DefaultMainRepository

    @Before
    fun setUp() {
        hiltAndroidRule.inject()


        defaultMainRepository = DefaultMainRepository(
            dababase.taskDao(),
            Dispatchers.Main
        )
    }

    @After
    fun tearDown() {
        dababase.close()
    }

    @Test
    fun savesTaskThenRetrievesTask() = runTest {
        //given
        val task = Task("one", "two", false)
        defaultMainRepository.saveTask(task)

        //when
        val retrieve = defaultMainRepository.getTask(task.id)

        //then
        assertThat(retrieve.succeeded).isEqualTo(true)
        retrieve as Result.Success
        assertThat(retrieve.data.title).isEqualTo("one")
        assertThat(retrieve.data.description).isEqualTo("two")
        assertThat(retrieve.data.isCompleted).isEqualTo(false)
    }

    @Test
    fun completeTaskThenRetrievedTaskIsComplete() = runTest {
        // given
        val task = Task("one", "two" , false)
        defaultMainRepository.saveTask(task)

        //when
        defaultMainRepository.completeTask(task)
        val retrieve = defaultMainRepository.getTask(task.id)

        //then
        assertThat(retrieve.succeeded).isTrue()
        retrieve as Result.Success
        assertThat(retrieve.data.isCompleted).isTrue()
        assertThat(retrieve.data.title).isEqualTo(task.title)
        assertThat(retrieve.data.description).isEqualTo(task.description)
    }
}