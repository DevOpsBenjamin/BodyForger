package app.bodyforger.mobile.ui.components

import androidx.annotation.StringRes
import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.SessionFailure
import app.bodyforger.mobile.R

/** Instructions come from the shared vocabulary; the interface only renders them. */
@StringRes
internal fun instructionLabel(instruction: AthleteInstruction): Int = when (instruction) {
    AthleteInstruction.TAP_SCALE_TO_WAKE -> R.string.scale_instruction_tap_to_wake
    AthleteInstruction.STAY_OFF_PLATFORM -> R.string.scale_instruction_stay_off
    AthleteInstruction.STEP_ON -> R.string.scale_instruction_step_on
    AthleteInstruction.STEP_ON_BAREFOOT -> R.string.scale_instruction_step_on_barefoot
    AthleteInstruction.GRIP_HANDLE -> R.string.scale_instruction_grip_handle
    AthleteInstruction.STEP_OFF -> R.string.scale_instruction_step_off
}

@StringRes
internal fun failureLabel(failure: SessionFailure): Int = when (failure) {
    SessionFailure.DEVICE_NOT_FOUND -> R.string.scale_failure_not_found
    SessionFailure.CONNECTION_LOST -> R.string.scale_failure_connection_lost
    SessionFailure.REJECTED_BY_DEVICE -> R.string.scale_failure_rejected
    SessionFailure.TIMED_OUT -> R.string.scale_failure_timed_out
    SessionFailure.NOT_ASSOCIATED -> R.string.scale_failure_not_associated
    SessionFailure.DEVICE_ERROR -> R.string.scale_failure_device_error
}
