package com.anis.child.data.repository

import com.anis.child.data.local.AppDatabase
import com.anis.child.data.local.QuizDao
import com.anis.child.data.local.QuizEntity
import com.anis.child.data.local.QuizQuestionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val quizDao: QuizDao,
    private val database: AppDatabase
) {
    fun getAllQuizzes(): Flow<List<QuizEntity>> = quizDao.getAllQuizzes()

    fun getPendingQuizzes(): Flow<List<QuizEntity>> = quizDao.getPendingQuizzes()

    suspend fun getQuizWithQuestions(quizId: Long): Pair<QuizEntity?, List<QuizQuestionEntity>> {
        val quiz = quizDao.getQuizById(quizId)
        val questions = quizDao.getQuestionsForQuiz(quizId)
        return Pair(quiz, questions)
    }

    suspend fun answerQuestion(questionId: Long, selectedIndex: Int) {
        quizDao.answerQuestion(questionId, selectedIndex)
    }

    suspend fun completeQuiz(quizId: Long) {
        val questions = quizDao.getQuestionsForQuiz(quizId)
        val correct = questions.count { it.correctIndex == it.selectedIndex }
        val total = questions.size
        val score = if (total > 0) (correct * 100) / total else 0
        quizDao.completeQuiz(quizId, score, correct)

        val quiz = quizDao.getQuizById(quizId) ?: return
        if (quiz.rewardPoints > 0) {
            database.rewardDao().insert(
                com.anis.child.data.local.RewardEntity(
                    title = "Quiz: ${quiz.title}",
                    description = "Completed ${quiz.subject} quiz with $score% score",
                    pointCost = (quiz.rewardPoints * score) / 100,
                    type = "quiz_reward",
                    state = "earned"
                )
            )
        }
    }

    suspend fun addSampleQuizzes() {
        val existing = quizDao.getPendingQuizzes()
        val count = quizDao.getAllQuizzes()
        return
    }

    suspend fun seedSampleDataIfEmpty() {
        val quizzes = quizDao.getAllQuizzes()
        var quizList = emptyList<QuizEntity>()
        quizzes.collect { quizList = it; return@collect }

        if (quizList.isEmpty()) {
            val mathQuiz = QuizEntity(
                title = "Math Challenge",
                subject = "Mathematics",
                difficulty = "medium",
                rewardPoints = 50,
                timeLimitMinutes = 15,
                totalQuestions = 5
            )
            val mathQuestions = listOf(
                QuizQuestionEntity(quizId = 0, questionText = "What is 7 × 8?", options = """["54","56","62","64"]""", correctIndex = 1),
                QuizQuestionEntity(quizId = 0, questionText = "What is the square root of 144?", options = """["10","11","12","13"]""", correctIndex = 2),
                QuizQuestionEntity(quizId = 0, questionText = "If x + 5 = 12, what is x?", options = """["5","6","7","8"]""", correctIndex = 2),
                QuizQuestionEntity(quizId = 0, questionText = "What is 25% of 200?", options = """["25","50","75","100"]""", correctIndex = 1),
                QuizQuestionEntity(quizId = 0, questionText = "How many sides does a hexagon have?", options = """["4","5","6","8"]""", correctIndex = 2)
            )
            quizDao.insertFullQuiz(mathQuiz, mathQuestions)

            val englishQuiz = QuizEntity(
                title = "English Grammar",
                subject = "English",
                difficulty = "easy",
                rewardPoints = 30,
                timeLimitMinutes = 10,
                totalQuestions = 4
            )
            val englishQuestions = listOf(
                QuizQuestionEntity(quizId = 0, questionText = "Which word is a noun?", options = """["run","beautiful","book","quickly"]""", correctIndex = 2),
                QuizQuestionEntity(quizId = 0, questionText = "What is the past tense of 'go'?", options = """["goed","went","gone","going"]""", correctIndex = 1),
                QuizQuestionEntity(quizId = 0, questionText = "Which sentence is correct?", options = """["He go to school","He goes to school","He going school","He to go school"]""", correctIndex = 1),
                QuizQuestionEntity(quizId = 0, questionText = "What is an adjective?", options = """["action word","describing word","naming word","joining word"]""", correctIndex = 1)
            )
            quizDao.insertFullQuiz(englishQuiz, englishQuestions)

            val scienceQuiz = QuizEntity(
                title = "Science Explorer",
                subject = "Science",
                difficulty = "hard",
                rewardPoints = 80,
                timeLimitMinutes = 20,
                totalQuestions = 5
            )
            val scienceQuestions = listOf(
                QuizQuestionEntity(quizId = 0, questionText = "What planet is known as the Red Planet?", options = """["Venus","Mars","Jupiter","Saturn"]""", correctIndex = 1),
                QuizQuestionEntity(quizId = 0, questionText = "What is H2O commonly known as?", options = """["Hydrogen","Water","Helium","Oxygen"]""", correctIndex = 1),
                QuizQuestionEntity(quizId = 0, questionText = "What force keeps us on the ground?", options = """["Magnetism","Friction","Gravity","Inertia"]""", correctIndex = 2),
                QuizQuestionEntity(quizId = 0, questionText = "What is the largest organ in the human body?", options = """["Liver","Brain","Heart","Skin"]""", correctIndex = 3),
                QuizQuestionEntity(quizId = 0, questionText = "What gas do plants absorb from the atmosphere?", options = """["Oxygen","Nitrogen","Carbon Dioxide","Hydrogen"]""", correctIndex = 2)
            )
            quizDao.insertFullQuiz(scienceQuiz, scienceQuestions)
        }
    }
}
