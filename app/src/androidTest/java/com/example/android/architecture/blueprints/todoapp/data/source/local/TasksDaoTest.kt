package com.example.android.architecture.blueprints.todoapp.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.android.architecture.blueprints.todoapp.data.source.local.entity.Task
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named


@ExperimentalCoroutinesApi
@SmallTest
@HiltAndroidTest
class TasksDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @Inject
    @Named("test_db")
    lateinit var database: ToDoDatabase

    private lateinit var dao: TasksDao

    @Before
    fun setUp() {
        hiltRule.inject()

        dao = database.taskDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertTaskAndGetById() = runTest {
        //given
        val task = Task("one", "two", true)

        //when
        dao.insertTask(task)
        val get = dao.getTaskById(task.id)

        //then
        assertThat(get as Task).isNotNull()
        assertThat(get.id).isEqualTo(task.id)

        assertThat(get.title).isEqualTo(task.title)
        assertThat(get.description).isEqualTo(task.description)
        assertThat(get.isCompleted).isEqualTo(task.isCompleted)

    }

    @Test
    fun updateTaskAndGetById() = runTest{
        //given
        val task = Task("one", "two", false,)
        dao.insertTask(task)
        //when
        val task2 = Task("new one", "two", true, task.id)
        dao.updateTask(task2)

        //then
        assertThat(task2.id).isEqualTo(task.id)

        assertThat(task2.isCompleted).isTrue()
        assertThat(task2.title).isEqualTo("new one")
        assertThat(task2.description).isEqualTo(task.description)

    }
}