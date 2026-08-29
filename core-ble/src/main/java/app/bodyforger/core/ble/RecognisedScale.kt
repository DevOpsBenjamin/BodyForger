package app.bodyforger.core.ble

import app.bodyforger.core.model.ScaleCapability

/**
 * Une balance reconnue par un pilote au vu de son nom annoncé, avant toute connexion.
 *
 * [capability] vaut `null` quand le pilote reconnaît la famille mais pas le modèle exact :
 * le matériel est pilotable, son plafond est simplement inconnu et se révélera à la première
 * pesée. Un plafond inventé vaudrait moins que pas de plafond.
 */
data class RecognisedScale(
    val displayName: String,
    val capability: ScaleCapability?
)
