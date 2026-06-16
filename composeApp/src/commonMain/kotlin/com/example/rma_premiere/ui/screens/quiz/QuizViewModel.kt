package com.example.rma_premiere.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rma_premiere.data.remote.isNetworkError
import com.example.rma_premiere.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlin.math.min

class QuizViewModel(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QuizContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: QuizContract.UiState.() -> QuizContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<QuizContract.UiEvent>()
    fun setEvent(event: QuizContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private var timerJob: Job? = null

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    QuizContract.UiEvent.StartQuiz -> startQuiz()
                    is QuizContract.UiEvent.SelectAnswer -> selectAnswer(event.answer)
                    QuizContract.UiEvent.NextQuestion -> nextQuestion()
                    QuizContract.UiEvent.ShowAbandonDialog -> setState { copy(showAbandonDialog = true) }
                    QuizContract.UiEvent.ConfirmAbandon -> abandonQuiz()
                    QuizContract.UiEvent.DismissAbandonDialog -> setState { copy(showAbandonDialog = false) }
                    QuizContract.UiEvent.Reset -> setState { QuizContract.UiState() }
                }
            }
        }
    }

    private fun startQuiz() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null, phase = QuizPhase.LOADING) }
            try {
                if (!quizRepository.canStartQuiz()) {
                    setState {
                        copy(
                            isLoading = false,
                            phase = QuizPhase.IDLE,
                            error = "Browse the catalog first to populate your quiz pool."
                        )
                    }
                    return@launch
                }
                val questions = quizRepository.generateQuiz()
                if (questions.size < TOTAL_QUESTIONS) {
                    setState {
                        copy(
                            isLoading = false,
                            phase = QuizPhase.IDLE,
                            error = "Not enough movie data for quiz. Please browse the catalog."
                        )
                    }
                    return@launch
                }
                setState {
                    copy(
                        isLoading = false,
                        phase = QuizPhase.IN_PROGRESS,
                        questions = questions,
                        currentQuestionIndex = 0,
                        answers = List(questions.size) { null },
                        timeRemainingSeconds = QUIZ_DURATION_SECONDS,
                        selectedAnswer = null,
                        result = null
                    )
                }
                startTimer()
            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        phase = QuizPhase.IDLE,
                        error = if (e.isNetworkError) "You're offline. Some question types need a connection."
                                else e.message ?: "Failed to start quiz"
                    )
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timeRemainingSeconds > 0 && _state.value.phase != QuizPhase.FINISHED) {
                delay(1000L)
                setState { copy(timeRemainingSeconds = timeRemainingSeconds - 1) }
                if (_state.value.timeRemainingSeconds == 0) {
                    // Istek vremena: automatski vodi na rezultat, neodgovorena pitanja vrede 0
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

        setState {
            copy(
                selectedAnswer = answer,
                answers = updatedAnswers,
                phase = QuizPhase.ANSWER_REVEALED
            )
        }

        // Kratko prikazi tacan/pogresan odgovor pa predji na sledece pitanje
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
            setState {
                copy(
                    currentQuestionIndex = nextIndex,
                    selectedAnswer = null,
                    phase = QuizPhase.IN_PROGRESS
                )
            }
        }
    }

    private fun finishQuiz() {
        timerJob?.cancel()
        val state = _state.value
        val timeUsed = QUIZ_DURATION_SECONDS - state.timeRemainingSeconds
        val correctAnswers = state.questions.zip(state.answers).count { (q, a) -> a == q.correctAnswer }
        val score = calculateScore(correctAnswers, state.timeRemainingSeconds)

        setState {
            copy(
                phase = QuizPhase.FINISHED,
                result = QuizResult(score, correctAnswers, timeUsed)
            )
        }

        viewModelScope.launch {
            quizRepository.saveResult(score, correctAnswers, TOTAL_QUESTIONS, timeUsed)
        }
    }

    private fun abandonQuiz() {
        timerJob?.cancel()
        // Potvrdjen izlazak ne boduje sesiju
        setState { QuizContract.UiState() }
    }

    // UBP = BTO * (9 + PVT / MVT), ograniceno na 100
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
