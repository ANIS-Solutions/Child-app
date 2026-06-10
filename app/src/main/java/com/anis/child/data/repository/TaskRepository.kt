package com.anis.child.data.repository

import com.anis.child.data.PreferenceManager
import com.anis.child.data.QuestUpdateRequest
import com.anis.child.data.local.TaskDao
import com.anis.child.data.local.TaskEntity
import com.anis.child.network.ApiService
import com.anis.child.network.ApiResult
import com.anis.child.network.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager
) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    suspend fun createTask(task: TaskEntity): Long = taskDao.insert(task)

    suspend fun completeTask(id: Long) {
        val task = taskDao.getTaskById(id) ?: return
        val childId = preferenceManager.childId ?: return

        safeApiCall {
            apiService.updateQuest(
                childId = childId,
                questId = task.remoteId.ifEmpty { id.toString() },
                QuestUpdateRequest(
                    title = task.title,
                    description = task.description,
                    type = "Education",
                    points = task.rewardValue,
                    stats = "SUBMITTED"
                )
            )
        }

        taskDao.markCompleted(id, "submitted")
    }

    suspend fun syncFromApi(): Boolean {
        val childId = preferenceManager.childId ?: return false
        return when (val result = safeApiCall { apiService.getChildQuests(childId) }) {
            is ApiResult.Success -> {
                val quests = result.data.data ?: emptyList()
                for (quest in quests) {
                    val existing = taskDao.getTaskById(quest.id.toLongOrNull() ?: 0L)
                    if (existing == null) {
                        taskDao.insert(TaskEntity(
                            remoteId = quest.id,
                            title = quest.title,
                            description = quest.description,
                            rewardValue = quest.rewardValue,
                            status = when (quest.state) {
                                "COMPLETED", "SUBMITTED" -> quest.state.lowercase()
                                else -> "pending"
                            }
                        ))
                    }
                }
                true
            }
            is ApiResult.Error -> false
        }
    }

    suspend fun seedSampleDataIfEmpty() {
        val taskList = taskDao.getAllTasks().first()

        if (taskList.isEmpty()) {
            val synced = syncFromApi()
            if (synced) return

            createTask(TaskEntity(title = "Clean your room", description = "Tidy up your room and make the bed", rewardValue = 20))
            createTask(TaskEntity(title = "Finish homework", description = "Complete all today's homework assignments", rewardValue = 30))
            createTask(TaskEntity(title = "Read for 30 minutes", description = "Read a book of your choice for 30 minutes", rewardValue = 25))
            createTask(TaskEntity(title = "Help with dishes", description = "Help wash and dry the dishes after dinner", rewardValue = 15))
        }
    }
}
