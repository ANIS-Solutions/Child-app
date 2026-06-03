package com.anis.child.ui.screen.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.data.local.QuizEntity
import com.anis.child.data.local.QuizQuestionEntity
import com.anis.child.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QuizUiState {
    data object Loading : QuizUiState()
    data class QuizList(val quizzes: kotlin.collections.List<QuizEntity>) : QuizUiState()
    data class Active(
        val quiz: QuizEntity,
        val questions: List<QuizQuestionEntity>,
        val currentQuestionIndex: Int = 0
    ) : QuizUiState()
    data class Completed(val score: Int, val correct: Int, val total: Int, val rewardEarned: Int) : QuizUiState()
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        loadQuizzes()
    }

    private fun loadQuizzes() {
        viewModelScope.launch {
            quizRepository.seedSampleDataIfEmpty()
            quizRepository.getPendingQuizzes().collect { quizzes ->
                _uiState.value = QuizUiState.QuizList(quizzes)
            }
        }
    }

    fun startQuiz(quizId: Long) {
        viewModelScope.launch {
            val (quiz, questions) = quizRepository.getQuizWithQuestions(quizId)
            if (quiz != null && questions.isNotEmpty()) {
                _uiState.value = QuizUiState.Active(
                    quiz = quiz.copy(totalQuestions = questions.size),
                    questions = questions
                )
            }
        }
    }

    fun answerQuestion(questionId: Long, selectedIndex: Int) {
        val state = _uiState.value
        if (state !is QuizUiState.Active) return

        viewModelScope.launch {
            quizRepository.answerQuestion(questionId, selectedIndex)
        }

        val nextIndex = state.currentQuestionIndex + 1
        if (nextIndex < state.questions.size) {
            _uiState.value = state.copy(currentQuestionIndex = nextIndex)
        }
    }

    fun submitQuiz() {
        val state = _uiState.value
        if (state !is QuizUiState.Active) return

        viewModelScope.launch {
            quizRepository.completeQuiz(state.quiz.id)
            val (quiz, _) = quizRepository.getQuizWithQuestions(state.quiz.id)
            if (quiz != null) {
                val rewardEarned = (quiz.rewardPoints * quiz.score) / 100
                _uiState.value = QuizUiState.Completed(
                    score = quiz.score,
                    correct = quiz.answeredCorrectly,
                    total = quiz.totalQuestions,
                    rewardEarned = rewardEarned
                )
            }
        }
    }

    fun backToList() {
        _uiState.value = QuizUiState.Loading
        loadQuizzes()
    }
}
