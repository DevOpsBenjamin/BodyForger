package app.bodyforger.core.model

/**
 * Le profil transmis à la balance pour ses propres calculs de bio-impédance.
 *
 * Distinct du [BiaProfile] qu'utilise notre moteur : c'est la même physiologie, mais envoyée
 * au matériel, avec le dernier poids connu qui aide certaines balances à cadrer leur mesure.
 */
data class ScaleUserProfile(
    val physiology: BiaProfile,
    val lastWeightKg: Double? = null,
    /** Mode invité : mesure ponctuelle sans occuper un emplacement mémoire de la balance. */
    val isGuest: Boolean = false
)
