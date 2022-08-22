package com.example.android.architecture.blueprints.todoapp.di

import androidx.annotation.VisibleForTesting
import com.example.android.architecture.blueprints.todoapp.data.MainRepository
import com.example.android.architecture.blueprints.todoapp.data.source.FakeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@VisibleForTesting
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [TasksRepositoryModule::class],
)
object TestTasksRepositoryModule {

    // Hilt will inject a FakeRepository instead of DefaultMainRepository.
    @OptIn(ExperimentalCoroutinesApi::class)
    @Singleton
    @Provides
    fun repository() = FakeRepository() as MainRepository
}