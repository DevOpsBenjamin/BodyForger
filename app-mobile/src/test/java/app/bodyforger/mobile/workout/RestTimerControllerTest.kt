package app.bodyforger.mobile.workout

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RestTimerControllerTest {

    private class RecordingHaptics : WorkoutHaptics {
        var completedCount = 0
        var warningCount = 0
        var finishedCount = 0

        override fun setCompleted() { completedCount++ }
        override fun restWarning() { warningCount++ }
        override fun restFinished() { finishedCount++ }
    }

    @Test
    fun `starting rest initializes countdown with duration`() = runTest {
        val haptics = RecordingHaptics()
        val controller = RestTimerController(haptics)

        controller.start(this, exerciseIndex = 0, setIndex = 1, durationSeconds = 60)

        val state = controller.countdown.value
        assertNotNull(state)
        assertEquals(60, state?.secondsRemaining)
        assertEquals(60, state?.totalSeconds)
        assertEquals(0, state?.exerciseIndex)
        assertEquals(1, state?.setIndex)
    }

    @Test
    fun `rest countdown decrements each second`() = runTest {
        val haptics = RecordingHaptics()
        val controller = RestTimerController(haptics)

        controller.start(this, exerciseIndex = 0, setIndex = 1, durationSeconds = 10)
        advanceTimeBy(3_000L)
        runCurrent()

        assertEquals(7, controller.countdown.value?.secondsRemaining)
    }

    @Test
    fun `warning haptics fire on last three seconds and finish haptics on completion`() = runTest {
        val haptics = RecordingHaptics()
        val controller = RestTimerController(haptics)

        controller.start(this, exerciseIndex = 0, setIndex = 1, durationSeconds = 5)

        advanceTimeBy(1_000L)
        runCurrent() // 4s remaining
        assertEquals(0, haptics.warningCount)
        assertEquals(0, haptics.finishedCount)

        advanceTimeBy(1_000L)
        runCurrent() // 3s remaining
        assertEquals(1, haptics.warningCount)

        advanceTimeBy(1_000L)
        runCurrent() // 2s remaining
        assertEquals(2, haptics.warningCount)

        advanceTimeBy(1_000L)
        runCurrent() // 1s remaining
        assertEquals(3, haptics.warningCount)

        advanceTimeBy(1_000L)
        runCurrent() // 0s remaining (finished)
        assertEquals(1, haptics.finishedCount)
        assertNull(controller.countdown.value)
    }

    @Test
    fun `adding seconds extends remaining and total duration`() = runTest {
        val haptics = RecordingHaptics()
        val controller = RestTimerController(haptics)

        controller.start(this, exerciseIndex = 0, setIndex = 1, durationSeconds = 45)
        advanceTimeBy(5_000L)
        runCurrent() // 40s remaining

        controller.addSeconds(30)
        assertEquals(70, controller.countdown.value?.secondsRemaining)
        assertEquals(75, controller.countdown.value?.totalSeconds)

        controller.addSeconds(-15)
        assertEquals(55, controller.countdown.value?.secondsRemaining)
        assertEquals(60, controller.countdown.value?.totalSeconds)
    }

    @Test
    fun `subtracting more than remaining duration stops rest immediately`() = runTest {
        val haptics = RecordingHaptics()
        val controller = RestTimerController(haptics)

        controller.start(this, exerciseIndex = 0, setIndex = 1, durationSeconds = 10)
        controller.addSeconds(-15)

        assertNull(controller.countdown.value)
    }

    @Test
    fun `stopping rest cancels ticker and clears state`() = runTest {
        val haptics = RecordingHaptics()
        val controller = RestTimerController(haptics)

        controller.start(this, exerciseIndex = 0, setIndex = 1, durationSeconds = 60)
        controller.stop()

        assertNull(controller.countdown.value)
    }
}
