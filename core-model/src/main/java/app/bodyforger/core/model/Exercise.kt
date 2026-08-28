package app.bodyforger.core.model

data class Exercise(
    val id: String,
    val name: String,
    val bodyPart: String,
    val equipment: String,
    val target: String,
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String> = emptyList()
)
