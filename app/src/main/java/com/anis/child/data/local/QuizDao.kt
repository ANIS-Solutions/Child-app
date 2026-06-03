package com.anis.child.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class QuizWithQuestions(
    val quiz: QuizEntity,
    val questions: List<QuizQuestionEntity>
)

@Dao
interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestionEntity>)

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Query("SELECT * FROM quizzes ORDER BY assignedAt DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :id")
    suspend fun getQuizById(id: Long): QuizEntity?

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId ORDER BY id ASC")
    suspend fun getQuestionsForQuiz(quizId: Long): List<QuizQuestionEntity>

    @Query("SELECT * FROM quizzes WHERE isCompleted = 0 ORDER BY assignedAt DESC")
    fun getPendingQuizzes(): Flow<List<QuizEntity>>

    @Query("UPDATE quiz_questions SET selectedIndex = :selectedIndex WHERE id = :questionId")
    suspend fun answerQuestion(questionId: Long, selectedIndex: Int)

    @Query("UPDATE quizzes SET isCompleted = 1, score = :score, answeredCorrectly = :correct, completedAt = :completedAt WHERE id = :quizId")
    suspend fun completeQuiz(quizId: Long, score: Int, correct: Int, completedAt: Long = System.currentTimeMillis())

    @Transaction
    suspend fun insertFullQuiz(quiz: QuizEntity, questions: List<QuizQuestionEntity>): Long {
        val quizId = insertQuiz(quiz)
        val qs = questions.map { it.copy(quizId = quizId) }
        insertQuestions(qs)
        return quizId
    }
}
