package app.bodyforger.mobile.workout

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

interface WorkoutHaptics {
    fun setCompleted()
    fun restWarning()
    fun restFinished()
}

class AndroidWorkoutHaptics(private val context: Context) : WorkoutHaptics {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun setCompleted() {
        vibrate(SET_COMPLETED_DURATION_MS, SET_COMPLETED_AMPLITUDE)
    }

    override fun restWarning() {
        vibrate(REST_WARNING_DURATION_MS, REST_WARNING_AMPLITUDE)
    }

    override fun restFinished() {
        vibrate(REST_FINISHED_DURATION_MS, REST_FINISHED_AMPLITUDE)
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(MIN_AMPLITUDE, MAX_AMPLITUDE)))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(durationMs)
        }
    }

    companion object {
        private const val MIN_AMPLITUDE = 1
        private const val MAX_AMPLITUDE = 255

        private const val SET_COMPLETED_DURATION_MS = 35L
        private const val SET_COMPLETED_AMPLITUDE = 120

        private const val REST_WARNING_DURATION_MS = 60L
        private const val REST_WARNING_AMPLITUDE = 180

        private const val REST_FINISHED_DURATION_MS = 500L
        private const val REST_FINISHED_AMPLITUDE = 255
    }
}

object NoOpWorkoutHaptics : WorkoutHaptics {
    override fun setCompleted() {}
    override fun restWarning() {}
    override fun restFinished() {}
}
