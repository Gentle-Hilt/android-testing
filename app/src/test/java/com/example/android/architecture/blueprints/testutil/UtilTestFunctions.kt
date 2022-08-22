package com.example.android.architecture.blueprints.testutil

import androidx.lifecycle.LiveData
import com.example.android.architecture.blueprints.todoapp.util.Event
import org.junit.Assert.assertEquals

fun assertSnackbarMessage(snackbarLiveData: LiveData<Event<Int>>, messageId: Int) {
    val value: Event<Int> = snackbarLiveData.getOrAwaitValue()
    assertEquals(value.getContentIfNotHandled(), messageId)
}

fun assertLiveDataEventTriggered(
    liveData: LiveData<Event<String>>,
    taskId: String
) {
    val value = liveData.getOrAwaitValue()
    assertEquals(value.getContentIfNotHandled(), taskId)
}