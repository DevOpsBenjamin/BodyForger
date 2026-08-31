package app.bodyforger.mobile.workout

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RestCountdown(
    val exerciseIndex: Int,
    val setIndex: Int,
    val totalSeconds: Int,
    val secondsRemaining: Int
)

class RestTimerController(
    private val haptics: WorkoutHaptics = NoOpWorkoutHaptics
) {
    private val _countdown = MutableStateFlow<RestCountdown?>(null)
    val countdown: StateFlow<RestCountdown?> = _countdown.asStateFlow()

    private var tickerJob: Job? = null

    fun start(scope: CoroutineScope, exerciseIndex: Int, setIndex: Int, durationSeconds: Int) {
        if (durationSeconds <= 0) return
        tickerJob?.cancel()
        _countdown.value = RestCountdown(
            exerciseIndex = exerciseIndex,
            setIndex = setIndex,
            totalSeconds = durationSeconds,
            secondsRemaining = durationSeconds
        )
        tickerJob = scope.launch {
            while (true) {
                delay(TICK_MS)
                val current = _countdown.value ?: break
                val next = current.secondsRemaining - 1
                if (next in 1..WARNING_THRESHOLD_SECONDS) {
                    haptics.restWarning()
                }
                if (next <= 0) {
                    haptics.restFinished()
                    _countdown.value = null
                    break
                } else {
                    _countdown.value = current.copy(secondsRemaining = next)
                }
            }
        }
    }

    fun addSeconds(deltaSeconds: Int) {
        val current = _countdown.value ?: return
        val newRemaining = current.secondsRemaining + deltaSeconds
        val newTotal = (current.totalSeconds + deltaSeconds).coerceAtLeast(1)
        if (newRemaining <= 0) {
            stop()
        } else {
            _countdown.value = current.copy(secondsRemaining = newRemaining, totalSeconds = newTotal)
        }
    }

    fun stop() {
        tickerJob?.cancel()
        tickerJob = null
        _countdown.value = null
    }

    companion object {
        private const val TICK_MS = 1_000L
        private const val WARNING_THRESHOLD_SECONDS = 3
    }
}
