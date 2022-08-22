package com.example.android.architecture.blueprints.todoapp.util


import com.example.android.architecture.blueprints.todoapp.data.source.local.entity.Task
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StatisticsUtilsTest {



    @Test
    fun `returns tasks,percentage of completed 0%, of active 100%`() {
        //given
        val tasks = listOf(Task("title", "description", isCompleted = false))
        //when
        val result = getActiveAndCompletedStats(tasks)
        //then
        assertThat(result.completedTasksPercent).isEqualTo(0f)
        assertThat(result.activeTasksPercent).isEqualTo(100f)
    }

    @Test
    fun `returns tasks, percentage of completed 100%, of active 0%`() {
        //given
        val tasks = listOf(Task("title", "description", isCompleted = true))
        //when
        val result = getActiveAndCompletedStats(tasks)
        //then
        assertThat(result.completedTasksPercent).isEqualTo(100f)
        assertThat(result.activeTasksPercent).isEqualTo(0f)
    }

    @Test
    fun `returns tasks, percentage of completed 40%, of active 60%`() {
        //given
        val tasks =
            listOf(
                Task("title", "description", isCompleted = true),
                Task("title", "description", isCompleted = true),

                Task("title", "description", isCompleted = false),
                Task("title", "description", isCompleted = false),
                Task("title", "description", isCompleted = false),


                )
        //when
        val result = getActiveAndCompletedStats(tasks)
        //then
        assertThat(result.completedTasksPercent).isEqualTo(40f)
        assertThat(result.activeTasksPercent).isEqualTo(60f)
    }

    @Test
    fun `returns empty tasks, percentage of completed 0%, of active 0%`(){
        //given
        val tasks = emptyList<Task>()
        //when
        val result = getActiveAndCompletedStats(tasks)
        //then
        assertThat(result.activeTasksPercent).isEqualTo(0f)
        assertThat(result.completedTasksPercent).isEqualTo(0f)
    }

    @Test
    fun `returns error, percentage of completed 0%, of active 0%`(){
        // when
        val result = getActiveAndCompletedStats(null)
        //then
        assertThat(result.completedTasksPercent).isEqualTo(0f)
        assertThat(result.activeTasksPercent).isEqualTo(0f)
    }
}