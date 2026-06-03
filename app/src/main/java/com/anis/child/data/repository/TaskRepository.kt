package com.anis.child.data.repository

import com.anis.child.data.local.TaskDao
import com.anis.child.data.local.TaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    suspend fun createTask(task: TaskEntity): Long = taskDao.insert(task)

    suspend fun completeTask(id: Long) {
        taskDao.markCompleted(id, "submitted")
    }

    suspend fun seedSampleDataIfEmpty() {
        val tasks = taskDao.getAllTasks()
        var taskList = emptyList<TaskEntity>()
        tasks.collect { taskList = it; return@collect }

        if (taskList.isEmpty()) {
            createTask(TaskEntity(title = "Clean your room", description = "Tidy up your room and make the bed", rewardValue = 20))
            createTask(TaskEntity(title = "Finish homework", description = "Complete all today's homework assignments", rewardValue = 30))
            createTask(TaskEntity(title = "Read for 30 minutes", description = "Read a book of your choice for 30 minutes", rewardValue = 25))
            createTask(TaskEntity(title = "Help with dishes", description = "Help wash and dry the dishes after dinner", rewardValue = 15))
        }
    }
}
