package app.bodyforger.core.model

/**

 * officiellement reconnus par l'API Android Health Connect (androidx.health.connect.client.records.ExerciseSegmentType).
 *
 * Ce catalogue sert de socle canonique pour garantir une synchronisation 100% sans perte
 * avec Google Health Connect, Google Fit et Samsung Health.
 */
enum class HealthConnectExerciseType(
    val segmentTypeId: Int,
    val canonicalNameEn: String,
    val canonicalNameFr: String,
    val primaryMuscleGroup: MuscleGroup,
    val defaultEquipment: EquipmentType
) {
    // --- PECTORAUX / CHEST ---
    BENCH_PRESS(29, "Bench Press", "Développé Couché", MuscleGroup.CHEST, EquipmentType.BARBELL),
    INCLINE_BENCH_PRESS(30, "Incline Bench Press", "Développé Incliné", MuscleGroup.CHEST, EquipmentType.BARBELL),
    DUMBBELL_BENCH_PRESS(31, "Dumbbell Bench Press", "Couché Haltères", MuscleGroup.CHEST, EquipmentType.DUMBBELL),
    DUMBBELL_INCLINE_BENCH_PRESS(32, "Incline Dumbbell Bench Press", "Incliné Haltères", MuscleGroup.CHEST, EquipmentType.DUMBBELL),
    CHEST_PRESS(33, "Chest Press Machine", "Presse Pectoraux", MuscleGroup.CHEST, EquipmentType.MACHINE),
    CHEST_FLY(34, "Chest Fly", "Pec Deck / Écarté", MuscleGroup.CHEST, EquipmentType.CABLE),
    DIP(35, "Chest Dip", "Dips", MuscleGroup.CHEST, EquipmentType.BODYWEIGHT),
    PUSH_UP(36, "Push Up", "Pompes", MuscleGroup.CHEST, EquipmentType.BODYWEIGHT),

    // --- DOS / BACK ---
    DEADLIFT(37, "Deadlift", "Soulevé de Terre", MuscleGroup.BACK, EquipmentType.BARBELL),
    ROMANIAN_DEADLIFT(38, "Romanian Deadlift", "Soulevé Roumain", MuscleGroup.HAMSTRINGS, EquipmentType.BARBELL),
    LAT_PULLDOWN(39, "Lat Pulldown", "Tirage Vertical", MuscleGroup.BACK, EquipmentType.CABLE),
    SEATED_CABLE_ROW(40, "Seated Cable Row", "Tirage Horizontal", MuscleGroup.BACK, EquipmentType.CABLE),
    BENT_OVER_ROW(41, "Bent Over Row", "Rowing Barre", MuscleGroup.BACK, EquipmentType.BARBELL),
    PULL_UP(42, "Pull Up", "Tractions", MuscleGroup.BACK, EquipmentType.BODYWEIGHT),
    CHIN_UP(43, "Chin Up", "Tractions Supi", MuscleGroup.BACK, EquipmentType.BODYWEIGHT),
    BACK_EXTENSION(44, "Back Extension", "Banc Lombaires", MuscleGroup.BACK, EquipmentType.BODYWEIGHT),

    // --- ÉPAULES / SHOULDERS ---
    SHOULDER_PRESS(45, "Shoulder Press", "Développé Épaules", MuscleGroup.SHOULDERS, EquipmentType.DUMBBELL),
    OVERHEAD_PRESS(46, "Overhead Press (OHP)", "Développé Militaire", MuscleGroup.SHOULDERS, EquipmentType.BARBELL),
    LATERAL_RAISE(47, "Lateral Raise", "Élévations Latérales", MuscleGroup.SHOULDERS, EquipmentType.DUMBBELL),
    FRONT_RAISE(48, "Front Raise", "Élévations Frontales", MuscleGroup.SHOULDERS, EquipmentType.DUMBBELL),
    REVERSE_FLY(49, "Reverse Fly / Face Pull", "Oiseau / Face Pull", MuscleGroup.SHOULDERS, EquipmentType.CABLE),

    // --- BRAS : BICEPS & TRICEPS ---
    BARBELL_CURL(50, "Barbell Curl", "Curl Barre", MuscleGroup.BICEPS, EquipmentType.BARBELL),
    DUMBBELL_CURL(51, "Dumbbell Curl", "Curl Haltères", MuscleGroup.BICEPS, EquipmentType.DUMBBELL),
    HAMMER_CURL(52, "Hammer Curl", "Curl Marteau", MuscleGroup.BICEPS, EquipmentType.DUMBBELL),
    TRICEPS_EXTENSION(53, "Triceps Pushdown", "Poulie Triceps", MuscleGroup.TRICEPS, EquipmentType.CABLE),
    TRICEPS_DIP(54, "Triceps Bench Dip", "Dips Banc", MuscleGroup.TRICEPS, EquipmentType.BODYWEIGHT),
    SKULL_CRUSHER(55, "Skull Crusher", "Barre au Front", MuscleGroup.TRICEPS, EquipmentType.BARBELL),

    // --- JAMBES & FESSIERS / LEGS ---
    SQUAT(56, "Back Squat", "Squat", MuscleGroup.QUADRICEPS, EquipmentType.BARBELL),
    FRONT_SQUAT(57, "Front Squat", "Front Squat", MuscleGroup.QUADRICEPS, EquipmentType.BARBELL),
    LEG_PRESS(58, "Leg Press", "Presse Cuisses", MuscleGroup.QUADRICEPS, EquipmentType.MACHINE),
    LEG_EXTENSION(59, "Leg Extension", "Leg Extension", MuscleGroup.QUADRICEPS, EquipmentType.MACHINE),
    LEG_CURL(60, "Leg Curl", "Leg Curl", MuscleGroup.HAMSTRINGS, EquipmentType.MACHINE),
    LUNGE(61, "Lunge", "Fentes", MuscleGroup.QUADRICEPS, EquipmentType.DUMBBELL),
    HIP_THRUST(62, "Hip Thrust", "Hip Thrust", MuscleGroup.GLUTES, EquipmentType.BARBELL),
    CALF_RAISE(63, "Calf Raise", "Mollets", MuscleGroup.CALVES, EquipmentType.MACHINE),

    // --- ABDOMINAUX & CORE ---
    PLANK(64, "Plank", "Gainage", MuscleGroup.ABS, EquipmentType.BODYWEIGHT),
    CRUNCH(65, "Crunch", "Crunch", MuscleGroup.ABS, EquipmentType.BODYWEIGHT),
    LEG_RAISE(66, "Leg Raise", "Relevés de Jambes", MuscleGroup.ABS, EquipmentType.BODYWEIGHT),
    RUSSIAN_TWIST(67, "Russian Twist", "Russian Twist", MuscleGroup.ABS, EquipmentType.BODYWEIGHT),
    AB_WHEEL_ROLLOUT(68, "Ab Wheel Rollout", "Roue Abdo", MuscleGroup.ABS, EquipmentType.OTHER),

    // --- CONDITIONNEMENT & OLYMPIQUE ---
    KETTLEBELL_SWING(69, "Kettlebell Swing", "Swing", MuscleGroup.GLUTES, EquipmentType.KETTLEBELL),
    BURPEE(70, "Burpee", "Burpees", MuscleGroup.FULL_BODY, EquipmentType.BODYWEIGHT),
    CLEAN_AND_JERK(71, "Clean and Jerk", "Épaulé-Jeté", MuscleGroup.FULL_BODY, EquipmentType.BARBELL),
    SNATCH(72, "Snatch", "Arraché", MuscleGroup.FULL_BODY, EquipmentType.BARBELL),

    // --- SEGMENT SPÉCIAL & PERSONNALISÉ ---
    REST(0, "Rest Period", "Temps de Repos", MuscleGroup.FULL_BODY, EquipmentType.OTHER),
    OTHER_WORKOUT(999, "Custom / Other", "Personnalisé / Autre", MuscleGroup.FULL_BODY, EquipmentType.OTHER);
}

enum class MuscleGroup(val displayName: String) {
    CHEST("Pectoraux"),
    BACK("Dos"),
    SHOULDERS("Épaules"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    QUADRICEPS("Quadriceps"),
    HAMSTRINGS("Ischios"),
    GLUTES("Fessiers"),
    CALVES("Mollets"),
    ABS("Abdos"),
    FULL_BODY("Corps Complet")
}

enum class EquipmentType(val displayName: String) {
    BARBELL("Barre"),
    DUMBBELL("Haltères"),
    CABLE("Poulie"),
    MACHINE("Machine"),
    MACHINE_CONVERGENT("Convergente"),
    BODYWEIGHT("Poids du corps"),
    KETTLEBELL("Kettlebell"),
    OTHER("Autre")
}
