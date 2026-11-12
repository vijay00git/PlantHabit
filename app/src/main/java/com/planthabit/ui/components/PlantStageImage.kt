package com.planthabit.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.planthabit.R

/** Plant growth stage (0–4) for a streak: seedling, sprout, bloom, flourishing, golden tree. */
fun plantStageFor(streakDays: Int): Int = when {
    streakDays >= 30 -> 4
    streakDays >= 14 -> 3
    streakDays >= 7 -> 2
    streakDays >= 3 -> 1
    else -> 0
}

private val stageArt = listOf(
    R.drawable.plant_stage_0, R.drawable.plant_stage_1, R.drawable.plant_stage_2,
    R.drawable.plant_stage_3, R.drawable.plant_stage_4,
)

val plantStageNames = listOf("Seedling", "Thriving Sprout", "Blooming Sprout", "Flourishing Plant", "Golden Tree")

/** The illustrated plant for [streakDays]; cross-fades when it reaches a new stage. */
@Composable
fun PlantStageImage(streakDays: Int, modifier: Modifier = Modifier) {
    val stage = plantStageFor(streakDays)
    Crossfade(targetState = stage, animationSpec = tween(700), label = "plantStage") { s ->
        Image(
            painter = painterResource(stageArt[s]),
            contentDescription = "${plantStageNames[s]} plant",
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    }
}
