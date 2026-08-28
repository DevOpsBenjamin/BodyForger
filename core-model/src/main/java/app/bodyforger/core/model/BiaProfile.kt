package app.bodyforger.core.model

enum class BiologicalSex {
    MALE,
    FEMALE
}

data class BiaProfile(
    val sex: BiologicalSex,
    val ageYears: Int,
    val heightCm: Double
)
