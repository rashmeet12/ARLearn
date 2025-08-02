package com.example.arlearner.data

data class Drill(
    val id: Int,
    val name: String,
    val description: String,
    val tips: List<String>,
    val imageRes: Int = android.R.drawable.ic_menu_camera // Default image
)

object DrillRepository {
    val drills = listOf(
        Drill(
            id = 1,
            name = "Basic Push-Up Drill",
            description = "A fundamental upper body strengthening exercise focusing on chest, shoulders, and triceps.",
            tips = listOf(
                "Keep your body in a straight line",
                "Lower your chest to the ground",
                "Push back up to starting position",
                "Maintain controlled movements"
            )
        ),
        Drill(
            id = 2,
            name = "Sprint Training Drill",
            description = "High-intensity running exercise to improve speed and cardiovascular fitness.",
            tips = listOf(
                "Warm up properly before starting",
                "Maintain proper running form",
                "Focus on quick leg turnover",
                "Cool down with light jogging"
            )
        ),
        Drill(
            id = 3,
            name = "Agility Ladder Drill",
            description = "Footwork exercise designed to improve coordination, speed, and agility.",
            tips = listOf(
                "Stay on the balls of your feet",
                "Keep your head up and eyes forward",
                "Maintain quick, light steps",
                "Practice different step patterns"
            )
        )
    )
}