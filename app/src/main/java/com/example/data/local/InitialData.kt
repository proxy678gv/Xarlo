package com.example.data.local

import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.ScheduleItemEntity

data class Exercise(
    val id: String,
    val name: String,
    val target: String,
    val description: String,
    val defaultRestSeconds: Int = 45
)

data class WorkoutDayPlan(
    val dayNumber: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val exercises: List<Exercise>
)

object InitialData {
    val defaultSchedules = listOf(
        ScheduleItemEntity(
            id = "water530",
            title = "Lukewarm water",
            timeStr = "5:30 AM",
            hour = 5,
            minute = 30,
            note = "1 glass • 250 ml • hydration",
            recurrence = "DAILY",
            category = "WATER",
            sortOrder = 1
        ),
        ScheduleItemEntity(
            id = "med530",
            title = "Medication",
            timeStr = "5:30 AM",
            hour = 5,
            minute = 30,
            note = "Thyroxine entry from supplied plan",
            recurrence = "DAILY",
            category = "MEDICATION",
            sortOrder = 2
        ),
        ScheduleItemEntity(
            id = "breakfast",
            title = "Breakfast",
            timeStr = "6:15 AM",
            hour = 6,
            minute = 15,
            note = "High protein • whole foods",
            recurrence = "DAILY",
            category = "MEAL",
            sortOrder = 3
        ),
        ScheduleItemEntity(
            id = "med1030",
            title = "Medication",
            timeStr = "10:30 AM",
            hour = 10,
            minute = 30,
            note = "Pendits entry from supplied plan",
            recurrence = "DAILY",
            category = "MEDICATION",
            sortOrder = 4
        ),
        ScheduleItemEntity(
            id = "fishoil",
            title = "Fish oil",
            timeStr = "1:00 PM",
            hour = 13,
            minute = 0,
            note = "1000 mg entry from supplied plan",
            recurrence = "DAILY",
            category = "MEDICATION",
            sortOrder = 5
        ),
        ScheduleItemEntity(
            id = "snack",
            title = "Light snack",
            timeStr = "4:30 PM",
            hour = 16,
            minute = 30,
            note = "Banana / nuts / peanut butter (Pre-workout)",
            recurrence = "DAILY",
            category = "MEAL",
            sortOrder = 6
        ),
        ScheduleItemEntity(
            id = "workout",
            title = "Home workout",
            timeStr = "5:30 PM",
            hour = 17,
            minute = 30,
            note = "5:30–6:15 PM • Controlled sets & form",
            recurrence = "DAILY",
            category = "WORKOUT",
            sortOrder = 7
        ),
        ScheduleItemEntity(
            id = "post",
            title = "Post-workout nutrition",
            timeStr = "6:30 PM",
            hour = 18,
            minute = 30,
            note = "Weight gainer / creatine entry from supplied plan",
            recurrence = "DAILY",
            category = "MEAL",
            sortOrder = 8
        ),
        ScheduleItemEntity(
            id = "dinner",
            title = "Dinner",
            timeStr = "8:00 PM",
            hour = 20,
            minute = 0,
            note = "Balanced meal • protein & vegetables",
            recurrence = "DAILY",
            category = "MEAL",
            sortOrder = 9
        ),
        ScheduleItemEntity(
            id = "sleep",
            title = "Sleep",
            timeStr = "10:00 PM",
            hour = 22,
            minute = 0,
            note = "Planned 7–8 hours • wind down",
            recurrence = "DAILY",
            category = "SLEEP",
            sortOrder = 10
        )
    )

    val defaultHabits = listOf(
        HabitEntity(
            id = "habit_sunlight",
            title = "Morning sunlight (5–10 min)",
            category = "ROUTINE",
            iconName = "WbSunny",
            targetDaysPerWeek = 7
        ),
        HabitEntity(
            id = "habit_mobility",
            title = "Daily mobility routine",
            category = "FITNESS",
            iconName = "FitnessCenter",
            targetDaysPerWeek = 7
        ),
        HabitEntity(
            id = "habit_screen_free",
            title = "No screen 30 min before bed",
            category = "SLEEP",
            iconName = "Bedtime",
            targetDaysPerWeek = 7
        ),
        HabitEntity(
            id = "habit_posture",
            title = "Hourly posture & breathing check",
            category = "HEALTH",
            iconName = "SelfImprovement",
            targetDaysPerWeek = 7
        )
    )

    val waterSlots = listOf(
        "5:30 AM",
        "7:30 AM",
        "10:30 AM",
        "1:00 PM",
        "4:00 PM",
        "6:30 PM",
        "8:30 PM"
    )

    val workoutPlans = listOf(
        WorkoutDayPlan(
            dayNumber = 1,
            title = "Day 1",
            subtitle = "Upper Body",
            description = "Upper body strength and core stabilization.",
            exercises = listOf(
                Exercise("d1_1", "Wall Push-up", "3 × 12–15", "Slow eccentric descent, core braced."),
                Exercise("d1_2", "Incline Push-up", "3 × 10–12", "Hands on elevated bench or counter."),
                Exercise("d1_3", "Pike Push-up", "3 × 8–10", "Shoulder vertical press variation."),
                Exercise("d1_4", "Resistance Band Row", "3 × 12–15", "Squeeze scapulae at peak contraction."),
                Exercise("d1_5", "Shoulder Tap", "3 × 20 taps", "High plank, minimize hip rotation."),
                Exercise("d1_6", "Core: Dead Bug", "3 × 12–15", "Opposite arm/leg extension, lower back flush.")
            )
        ),
        WorkoutDayPlan(
            dayNumber = 2,
            title = "Day 2",
            subtitle = "Lower Body",
            description = "Lower body power, knee resilience, and posterior chain activation.",
            exercises = listOf(
                Exercise("d2_1", "Bodyweight Squat", "3 × 15", "Deep squat depth, knees tracking toes."),
                Exercise("d2_2", "Static Lunge", "3 × 12 each leg", "90-degree angles on both knees."),
                Exercise("d2_3", "Glute Bridge", "3 × 15", "Drive through heels, glute lockout at top."),
                Exercise("d2_4", "Calf Raise", "3 × 20", "Full plantar flexion with 1s pause at peak."),
                Exercise("d2_5", "Wall Sit", "3 × 30 sec", "Thighs parallel to floor, spine flat against wall."),
                Exercise("d2_6", "Leg Raises", "3 × 12–15", "Slow controlled lower, lower abs engaged.")
            )
        ),
        WorkoutDayPlan(
            dayNumber = 3,
            title = "Day 3",
            subtitle = "Full Body + Mobility",
            description = "Aerobic conditioning, agility, and dynamic core tension.",
            exercises = listOf(
                Exercise("d3_1", "March in Place", "3 × 2 min", "High knee drive with arm coordination."),
                Exercise("d3_2", "High Knees", "3 × 1 min", "Up-tempo rhythmic cardiac conditioning."),
                Exercise("d3_3", "Butt Kicks", "3 × 1 min", "Hamstring reflex and footwork cadence."),
                Exercise("d3_4", "Mountain Climbers", "3 × 30 sec", "Solid pushup plank, alternating knee strikes."),
                Exercise("d3_5", "Plank", "3 × 30–45 sec", "Hollow body position, glutes and abs locked."),
                Exercise("d3_6", "Side Plank", "3 × 20–30 sec each", "Oblique support and lateral hip stability.")
            )
        ),
        WorkoutDayPlan(
            dayNumber = 4,
            title = "Day 4",
            subtitle = "Yoga + Endurance",
            description = "Active recovery, joint decompression, and yogic respiratory control.",
            exercises = listOf(
                Exercise("d4_1", "Surya Namaskar", "6 rounds, slow pace", "Sun salutation flowing sequence with breath."),
                Exercise("d4_2", "Cat-Cow", "12 rounds", "Segmental spinal flexion and extension."),
                Exercise("d4_3", "Bhujangasana (Cobra)", "30 sec × 2", "Chest lifted, shoulders drawn away from ears."),
                Exercise("d4_4", "Pawanmuktasana", "30 sec × 2", "Wind-relieving spine decompression."),
                Exercise("d4_5", "Vajrasana + Deep Breathing", "5 min", "Diamond pose for digestion and lung expansion."),
                Exercise("d4_6", "Anulom Vilom", "5 min", "Alternate nostril breathing for nervous regulation.")
            )
        )
    )
}
