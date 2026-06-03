package com.anis.child.ui.screen.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anis.child.data.local.QuizEntity
import com.anis.child.data.local.QuizQuestionEntity
import com.anis.child.ui.theme.AppColors
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quizzes", color = AppColors.darkTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState is QuizUiState.Active || uiState is QuizUiState.Completed) {
                            viewModel.backToList()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AppColors.darkTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.primary01)
            )
        },
        containerColor = AppColors.surface50
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is QuizUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.primary01)
                    }
                }

                is QuizUiState.QuizList -> {
                    QuizList(
                        quizzes = state.quizzes,
                        onQuizClick = { viewModel.startQuiz(it.id) }
                    )
                }

                is QuizUiState.Active -> {
                    QuizPlayer(
                        quiz = state.quiz,
                        questions = state.questions,
                        currentIndex = state.currentQuestionIndex,
                        onAnswer = { questionId, index -> viewModel.answerQuestion(questionId, index) },
                        onSubmit = { viewModel.submitQuiz() }
                    )
                }

                is QuizUiState.Completed -> {
                    QuizResult(
                        score = state.score,
                        correct = state.correct,
                        total = state.total,
                        rewardEarned = state.rewardEarned,
                        onBackToList = { viewModel.backToList() }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizList(
    quizzes: List<QuizEntity>,
    onQuizClick: (QuizEntity) -> Unit
) {
    if (quizzes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.School, null, tint = AppColors.textDisabled, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("No quizzes available", color = AppColors.textSecondary)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quizzes) { quiz ->
                QuizCard(quiz = quiz, onClick = { onQuizClick(quiz) })
            }
        }
    }
}

@Composable
private fun QuizCard(
    quiz: QuizEntity,
    onClick: () -> Unit
) {
    val difficultyColor = when (quiz.difficulty) {
        "easy" -> AppColors.success500
        "hard" -> AppColors.error500
        else -> AppColors.warning500
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.darkSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.School, null, tint = AppColors.primary01, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(quiz.title, style = MaterialTheme.typography.bodyLarge, color = AppColors.textPrimary, fontWeight = FontWeight.Bold)
                Text("${quiz.subject} · ${quiz.totalQuestions} questions", style = MaterialTheme.typography.bodySmall, color = AppColors.textSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = difficultyColor, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${quiz.rewardPoints} pts · ${quiz.difficulty}", style = MaterialTheme.typography.labelSmall, color = difficultyColor)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = AppColors.textSecondary)
        }
    }
}

@Composable
private fun QuizPlayer(
    quiz: QuizEntity,
    questions: List<QuizQuestionEntity>,
    currentIndex: Int,
    onAnswer: (Long, Int) -> Unit,
    onSubmit: () -> Unit
) {
    val question = questions.getOrNull(currentIndex)
    val progress = if (questions.isNotEmpty()) currentIndex.toFloat() / questions.size else 0f
    val isLastQuestion = currentIndex >= questions.size - 1
    val allAnswered = questions.all { it.selectedIndex != null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = AppColors.primary01,
            trackColor = AppColors.textDisabled.copy(alpha = 0.3f)
        )

        Spacer(Modifier.height(8.dp))
        Text("Question ${currentIndex + 1} of ${questions.size}", style = MaterialTheme.typography.bodySmall, color = AppColors.textSecondary)

        if (question != null) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            val options = parseOptions(question.options)
            options.forEachIndexed { index, option ->
                val isSelected = question.selectedIndex == index
                val bgColor = if (isSelected) AppColors.primary01.copy(alpha = 0.15f) else AppColors.darkSurface.copy(alpha = 0.05f)
                val borderColor = if (isSelected) AppColors.primary01 else AppColors.textDisabled.copy(alpha = 0.3f)

                Card(
                    onClick = { onAnswer(question.id, index) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.textPrimary
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            if (isLastQuestion) {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = allAnswered,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.primary01,
                        disabledContainerColor = AppColors.textDisabled.copy(alpha = 0.3f)
                    )
                ) {
                    Text("Submit Quiz", color = AppColors.darkTextPrimary)
                }
            }
        }
    }
}

@Composable
private fun QuizResult(
    score: Int,
    correct: Int,
    total: Int,
    rewardEarned: Int,
    onBackToList: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val passed = score >= 60
        val emoji = if (passed) "🎉" else "💪"

        Text(emoji, fontSize = MaterialTheme.typography.displayLarge.fontSize)
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (passed) "Great Job!" else "Keep Trying!",
            style = MaterialTheme.typography.headlineMedium,
            color = if (passed) AppColors.success500 else AppColors.warning500,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "$correct / $total correct",
            style = MaterialTheme.typography.titleLarge,
            color = AppColors.textPrimary
        )
        Text(
            text = "Score: $score%",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.textSecondary
        )
        Spacer(Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.success500.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, null, tint = AppColors.success500, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Earned $rewardEarned reward points!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.success500,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBackToList,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.primary01)
        ) {
            Text("Back to Quizzes", color = AppColors.darkTextPrimary)
        }
    }
}

private val json = Json { ignoreUnknownKeys = true }

private fun parseOptions(jsonStr: String): List<String> {
    return try {
        val element = json.parseToJsonElement(jsonStr)
        element.jsonArray.map { it.jsonPrimitive.content }
    } catch (_: Exception) {
        listOf("Option A", "Option B", "Option C", "Option D")
    }
}
