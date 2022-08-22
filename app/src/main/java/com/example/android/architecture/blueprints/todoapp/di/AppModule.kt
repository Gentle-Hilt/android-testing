package com.example.android.architecture.blueprints.todoapp.di

import android.content.Context
import androidx.room.Room
import com.example.android.architecture.blueprints.todoapp.data.MainRepository
import com.example.android.architecture.blueprints.todoapp.data.DefaultMainRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksDao
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideTasksDb(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, ToDoDatabase::class.java, "Tasks.db")
        .build()

    @Singleton
    @Provides
    fun provideTaskDao(
        database: ToDoDatabase
    ) = database.taskDao()


    @Singleton
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO


}

// in its own module to easily swap in tests
@Module
@InstallIn(SingletonComponent::class)
object TasksRepositoryModule{
    @Singleton
    @Provides
    fun provideTasksRepository(
        dao: TasksDao
    ) = DefaultMainRepository(dao, Dispatchers.IO) as MainRepository
}
