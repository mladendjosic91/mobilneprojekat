package com.example.rma_premiere.ui.screens.quiz

import com.example.rma_premiere.domain.model.QuizQuestion

const val QUIZ_DURATION_SECONDS = 60
const val TOTAL_QUESTIONS = 10

enum class QuizPhase { IDLE, LOADING, IN_PROGRESS, ANSWER_REVEALED, FINISHED }

data class QuizResult(
    val score: Float,
    val correctAnswers: Int,
    val timeUsedSeconds: Int
)

interface QuizContract {

    data class UiState(
        val phase: QuizPhase = QuizPhase.IDLE,
        val questions: List<QuizQuestion> = emptyList(),
        val currentQuestionIndex: Int = 0,
        val selectedAnswer: String? = null,
        val answers: List<String?> = emptyList(),
        val timeRemainingSeconds: Int = QUIZ_DURATION_SECONDS,
        val isLoading: Boolean = false,
        val error: String? = null,
        val showAbandonDialog: Boolean = false,
        val result: QuizResult? = null
    )

    sealed class UiEvent {
        data object StartQuiz : UiEvent()
        data class SelectAnswer(val answer: String) : UiEvent()
        data object NextQuestion : UiEvent()
        data object ShowAbandonDialog : UiEvent()
        data object ConfirmAbandon : UiEvent()
        data object DismissAbandonDialog : UiEvent()
        data object Reset : UiEvent()
    }

    sealed class SideEffect
}
