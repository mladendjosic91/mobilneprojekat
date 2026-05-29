package com.example.rma_premiere.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.repository.MoviesRepository
import com.example.rma_premiere.data.repository.QuizRepository
import com.example.rma_premiere.domain.model.QuizQuestion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min

const val QUIZ_DURATION_SECONDS = 60
const val TOTAL_QUESTIONS = 10

data class QuizState(
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

data class QuizResult(
    val score: Float,
    val correctAnswers: Int,
    val timeUsedSeconds: Int
)

enum class QuizPhase { IDLE, LOADING, IN_PROGRESS, ANSWER_REVEALED, FINISHED }

sealed class QuizIntent {
    object StartQuiz : QuizIntent()
    data class SelectAnswer(val answer: String) : QuizIntent()
    object NextQuestion : QuizIntent()
    object ShowAbandonDialog : QuizIntent()
    object ConfirmAbandon : QuizIntent()
    object DismissAbandonDialog : QuizIntent()
    object Reset : QuizIntent()
}

class QuizViewModel(
    private val quizRepository: QuizRepository,
    private val moviesRepository: MoviesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun onIntent(intent: QuizIntent) {
        when (intent) {
            is QuizIntent.StartQuiz -> startQuiz()
            is QuizIntent.SelectAnswer -> selectAnswer(intent.answer)
            is QuizIntent.NextQuestion -> nextQuestion()
            is QuizIntent.ShowAbandonDialog -> _state.update { it.copy(showAbandonDialog = true) }
            is QuizIntent.ConfirmAbandon -> abandonQuiz()
            is QuizIntent.DismissAbandonDialog -> _state.update { it.copy(showAbandonDialog = false) }
            is QuizIntent.Reset -> _state.update { QuizState() }
        }
    }

    private fun startQuiz() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, phase = QuizPhase.LOADING) }
            try {
                if (!quizRepository.canStartQuiz()) {
                    _state.update { it.copy(
                        isLoading = false,
                        phase = QuizPhase.IDLE,
                        error = "Browse the catalog first to populate your quiz pool."
                    )}
                    return@launch
                }
                val questions = quizRepository.generateQuiz()
                if (questions.size < TOTAL_QUESTIONS) {
                    _state.update { it.copy(
                        isLoading = false,
                        phase = QuizPhase.IDLE,
                        error = "Not enough movie data for quiz. Please browse the catalog."
                    )}
                    return@launch
                }
                _state.update { it.copy(
                    isLoading = false,
                    phase = QuizPhase.IN_PROGRESS,
                    questions = questions,
                    currentQuestionIndex = 0,
                    answers = List(questions.size) { null },
                    timeRemainingSeconds = QUIZ_DURATION_SECONDS,
                    selectedAnswer = null
                )}
                startTimer()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, phase = QuizPhase.IDLE, error = e.message ?: "Failed to start quiz") }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timeRemainingSeconds > 0 && _state.value.phase != QuizPhase.FINISHED) {
                delay(1000L)
                _state.update { it.copy(timeRemainingSeconds = it.timeRemainingSeconds - 1) }
                if (_state.value.timeRemainingSeconds == 0) {
                    finishQuiz()
                }
            }
        }
    }

    private fun selectAnswer(answer: String) {
        val state = _state.value
        if (state.phase != QuizPhase.IN_PROGRESS) return

        val updatedAnswers = state.answers.toMutableList()
        updatedAnswers[state.currentQuestionIndex] = answer

        _state.update { it.copy(
            selectedAnswer = answer,
            answers = updatedAnswers,
            phase = QuizPhase.ANSWER_REVEALED
        )}

        // Auto-advance after a short delay
        viewModelScope.launch {
            delay(1200L)
            if (_state.value.phase == QuizPhase.ANSWER_REVEALED) {
                nextQuestion()
            }
        }
    }

    private fun nextQuestion() {
        val state = _state.value
        val nextIndex = state.currentQuestionIndex + 1
        if (nextIndex >= TOTAL_QUESTIONS) {
            finishQuiz()
        } else {
            _state.update { it.copy(
                currentQuestionIndex = nextIndex,
                selectedAnswer = null,
                phase = QuizPhase.IN_PROGRESS
            )}
        }
    }

    private fun finishQuiz() {
        timerJob?.cancel()
        val state = _state.value
        val timeUsed = QUIZ_DURATION_SECONDS - state.timeRemainingSeconds
        val correctAnswers = state.questions.zip(state.answers).count { (q, a) -> a == q.correctAnswer }
        val score = calculateScore(correctAnswers, state.timeRemainingSeconds)

        _state.update { it.copy(
            phase = QuizPhase.FINISHED,
            result = QuizResult(score, correctAnswers, timeUsed)
        )}

        viewModelScope.launch {
            quizRepository.saveResult(score, correctAnswers, TOTAL_QUESTIONS, timeUsed)
        }
    }

    private fun abandonQuiz() {
        timerJob?.cancel()
        _state.update { QuizState() }
    }

    private fun calculateScore(correct: Int, timeRemaining: Int): Float {
        if (correct == 0) return 0f
        val score = correct * (9f + timeRemaining.toFloat() / QUIZ_DURATION_SECONDS)
        return min(score, 100f)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
