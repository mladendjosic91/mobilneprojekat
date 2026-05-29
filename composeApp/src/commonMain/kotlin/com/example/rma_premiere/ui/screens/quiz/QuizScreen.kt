package com.example.rma_premiere.ui.screens.quiz

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.rma_premiere.domain.model.QuizQuestion
import com.example.rma_premiere.domain.model.QuizQuestionType
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onNavigateToResult: (Float, Int, Int) -> Unit,
    onBack: () -> Unit,
    viewModel: QuizViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.result) {
        state.result?.let { result ->
            onNavigateToResult(result.score, result.correctAnswers, result.timeUsedSeconds)
        }
    }

    // Abandon dialog
    if (state.showAbandonDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(QuizIntent.DismissAbandonDialog) },
            title = { Text("Abandon quiz?") },
            text = { Text("Your progress will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onIntent(QuizIntent.ConfirmAbandon)
                    onBack()
                }) { Text("Abandon") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(QuizIntent.DismissAbandonDialog) }) { Text("Continue") }
            }
        )
    }

    when (state.phase) {
        QuizPhase.IDLE -> QuizIdleScreen(
            error = state.error,
            onStart = { viewModel.onIntent(QuizIntent.StartQuiz) },
            onBack = onBack
        )
        QuizPhase.LOADING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        QuizPhase.IN_PROGRESS, QuizPhase.ANSWER_REVEALED -> {
            if (state.questions.isNotEmpty()) {
                QuizInProgressScreen(
                    state = state,
                    onAnswer = { viewModel.onIntent(QuizIntent.SelectAnswer(it)) },
                    onAbandon = { viewModel.onIntent(QuizIntent.ShowAbandonDialog) }
                )
            }
        }
        QuizPhase.FINISHED -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun QuizIdleScreen(error: String?, onStart: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Movie Quiz", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("10 questions • 60 seconds", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
        }
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Start Quiz") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

@Composable
private fun QuizInProgressScreen(
    state: QuizState,
    onAnswer: (String) -> Unit,
    onAbandon: () -> Unit
) {
    val question = state.questions[state.currentQuestionIndex]

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Progress + timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${state.currentQuestionIndex + 1}/$TOTAL_QUESTIONS", style = MaterialTheme.typography.labelLarge)
            LinearProgressIndicator(
                progress = { state.timeRemainingSeconds.toFloat() / QUIZ_DURATION_SECONDS },
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                color = if (state.timeRemainingSeconds <= 10) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
            )
            Text("${state.timeRemainingSeconds}s", style = MaterialTheme.typography.labelLarge,
                color = if (state.timeRemainingSeconds <= 10) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface)
        }

        Spacer(Modifier.height(16.dp))

        // Question content (animated slide)
        AnimatedContent(
            targetState = state.currentQuestionIndex,
            transitionSpec = {
                slideInHorizontally(tween(300)) { it } togetherWith slideOutHorizontally(tween(300)) { -it }
            }
        ) { _ ->
            QuestionCard(
                question = question,
                selectedAnswer = state.selectedAnswer,
                isRevealed = state.phase == QuizPhase.ANSWER_REVEALED,
                onAnswer = { if (state.phase == QuizPhase.IN_PROGRESS) onAnswer(it) }
            )
        }
    }
}

@Composable
private fun QuestionCard(
    question: QuizQuestion,
    selectedAnswer: String?,
    isRevealed: Boolean,
    onAnswer: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image
        question.imageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))
            )
        }

        // Question prompt
        val prompt = when (question.type) {
            QuizQuestionType.GUESS_MOVIE -> "Which movie is this?"
            QuizQuestionType.GUESS_YEAR -> "What year was \"${question.movieTitle}\" released?"
            QuizQuestionType.GUESS_ACTOR -> "Who is the lead actor in \"${question.movieTitle}\"?"
        }
        Text(prompt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Answer options
        question.options.forEach { option ->
            val containerColor = when {
                !isRevealed -> MaterialTheme.colorScheme.surfaceVariant
                option == question.correctAnswer -> Color(0xFF4CAF50)
                option == selectedAnswer -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            Button(
                onClick = { onAnswer(option) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = containerColor),
                enabled = !isRevealed
            ) {
                Text(option, color = if (!isRevealed) MaterialTheme.colorScheme.onSurfaceVariant else Color.White)
            }
        }
    }
}
