package com.example.android.architecture.blueprints.todoapp.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description


// COROUTINE RULE ON STEROIDS

@ExperimentalCoroutinesApi
class MainDispatchersRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
): TestWatcher(){

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }

}