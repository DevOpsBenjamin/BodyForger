package app.bodyforger.mobile.ui.components

import app.bodyforger.core.ble.AthleteInstruction
import app.bodyforger.core.ble.SessionFailure

/** Instructions come from the shared vocabulary; the interface only renders them. */
internal fun instructionLabel(instruction: AthleteInstruction): String = when (instruction) {
    AthleteInstruction.TAP_SCALE_TO_WAKE -> "Tapotez la balance du pied"
    AthleteInstruction.STAY_OFF_PLATFORM -> "Restez hors du plateau"
    AthleteInstruction.STEP_ON -> "Montez sur la balance"
    AthleteInstruction.STEP_ON_BAREFOOT -> "Montez pieds nus sur la balance"
    AthleteInstruction.GRIP_HANDLE -> "Saisissez la poignée des deux mains"
    AthleteInstruction.STEP_OFF -> "Descendez du plateau"
}

internal fun failureLabel(failure: SessionFailure): String = when (failure) {
    SessionFailure.DEVICE_NOT_FOUND -> "Balance introuvable. Tapotez-la pour la réveiller."
    SessionFailure.CONNECTION_LOST -> "Liaison perdue en cours de séquence."
    SessionFailure.REJECTED_BY_DEVICE -> "La balance a refusé la connexion."
    SessionFailure.TIMED_OUT -> "Aucune mesure : la pesée n'a pas abouti à temps."
    SessionFailure.NOT_ASSOCIATED -> "Aucune balance associée."
    SessionFailure.DEVICE_ERROR -> "La balance a signalé une erreur."
}
