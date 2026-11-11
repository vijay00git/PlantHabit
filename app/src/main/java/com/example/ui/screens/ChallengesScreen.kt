package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ChallengeEntity
import com.example.ui.components.EmptyState
import com.example.ui.components.IconBubble
import com.example.ui.components.PlantCard
import com.example.ui.components.ProgressRing
import com.example.ui.components.SectionHeader
import com.example.ui.components.bounceClick
import com.example.ui.components.staggeredEntrance
import com.example.ui.theme.AccentPair
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import com.example.ui.theme.backgroundGlow
import com.example.ui.theme.categoryColors
import com.example.ui.theme.dreamGradient
import com.example.ui.theme.leafGradient
import com.example.ui.theme.sunriseGradient

private const val ALL_CATEGORIES = "All"

@Composable
fun ChallengesScreen(
    challenges: List<ChallengeEntity>,
    onChallengeClick: (ChallengeEntity) -> Unit,
    onCreateCustomChallenge: () -> Unit,
    onImportJsonClick: () -> Unit,
    onDownloadTemplateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = PlantTheme.colors

    var selectedCategory by rememberSaveable { mutableStateOf(ALL_CATEGORIES) }
    val categories = remember(challenges) {
        listOf(ALL_CATEGORIES) + challenges.map { it.category.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
    }
    // If the selected category disappeared (e.g. challenge removed), fall back to "All".
    val activeCategory = if (categories.any { it.equals(selectedCategory, ignoreCase = true) }) {
        selectedCategory
    } else ALL_CATEGORIES

    fun matches(ch: ChallengeEntity): Boolean =
        activeCategory == ALL_CATEGORIES || ch.category.trim().equals(activeCategory, ignoreCase = true)

    val joinedChallenges = challenges.filter { it.isJoined && matches(it) }
    val availableChallenges = challenges.filter { !it.isJoined && matches(it) }
    val gratitudeChallenge = challenges.find { it.id == "gratitude_month" }
    val runGoal = challenges.find { it.id == "goal_counter_run" }?.takeIf { !it.isJoined && matches(it) }
    val relationshipChallenge = challenges.find { it.id == "relationship_week" }?.takeIf { !it.isJoined && matches(it) }
    val remainingChallenges = availableChallenges.filter {
        it.id != "gratitude_month" && it.id != "goal_counter_run" && it.id != "relationship_week"
    }
    val nothingMatches = joinedChallenges.isEmpty() && runGoal == null &&
        relationshipChallenge == null && remainingChallenges.isEmpty()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(c.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header: title + JSON actions on a soft green glow
        item(key = "header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.backgroundGlow)
                    .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Challenges",
                            style = MaterialTheme.typography.headlineLarge,
                            color = c.textPrimary
                        )
                        Text(
                            text = "Tap any challenge to inspect habits & schedule",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderPill(
                            text = "Template",
                            icon = Icons.Default.Download,
                            background = null,
                            container = c.sunSoft,
                            contentColor = c.sun,
                            onClick = onDownloadTemplateClick,
                            modifier = Modifier.testTag("btn_top_template")
                        )
                        HeaderPill(
                            text = "Import",
                            icon = Icons.Default.FileUpload,
                            background = c.leafGradient,
                            container = c.primary,
                            contentColor = c.onPrimary,
                            onClick = onImportJsonClick,
                            modifier = Modifier.testTag("btn_top_import_json")
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Quick stats
                val joinedCount = challenges.count { it.isJoined }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip(
                        icon = Icons.Default.EmojiEvents,
                        text = "$joinedCount active",
                        accent = AccentPair(c.primary, c.primarySoft)
                    )
                    StatChip(
                        icon = Icons.Default.AutoAwesome,
                        text = "${challenges.size - joinedCount} to discover",
                        accent = AccentPair(c.sunrise, c.sunriseSoft)
                    )
                }
            }
        }

        // Hero monthly banner (Gratitude Month)
        if (gratitudeChallenge != null) {
            item(key = "hero_${gratitudeChallenge.id}") {
                FeaturedHeroCard(
                    challenge = gratitudeChallenge,
                    onClick = { onChallengeClick(gratitudeChallenge) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .staggeredEntrance(0)
                )
            }
        }

        // Category filter chips
        if (categories.size > 2) {
            item(key = "category_chips") {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp)
                        .testTag("challenge_category_chips"),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it.lowercase() }) { category ->
                        val accent = if (category == ALL_CATEGORIES) {
                            AccentPair(c.primary, c.primarySoft)
                        } else c.categoryColors(category)
                        CategoryChip(
                            label = category,
                            accent = accent,
                            selected = category.equals(activeCategory, ignoreCase = true),
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }
        }

        // Section: active challenges
        if (joinedChallenges.isNotEmpty()) {
            item(key = "active_header") {
                SectionHeader(
                    title = "Your active challenges",
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 6.dp)
                        .animateItem(),
                    action = {
                        CountBadge(joinedChallenges.size, AccentPair(c.primary, c.primarySoft))
                    }
                )
            }

            items(joinedChallenges, key = { "joined_${it.id}" }) { challenge ->
                val index = joinedChallenges.indexOf(challenge)
                ActiveChallengeCard(
                    challenge = challenge,
                    onClick = { onChallengeClick(challenge) },
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .staggeredEntrance(index + 1)
                        .testTag("challenge_item_${challenge.id}")
                )
            }
        }

        // Section: community challenges
        item(key = "community_header") {
            SectionHeader(
                title = "Community challenges",
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp)
                    .animateItem()
            )
        }

        if (nothingMatches) {
            item(key = "empty_filter") {
                EmptyState(
                    emoji = "🌱",
                    title = "No $activeCategory challenges yet",
                    message = "Try another category or import your own challenge as JSON.",
                    modifier = Modifier.animateItem()
                )
            }
        }

        // Milestone goal-counter challenge card
        if (runGoal != null) {
            item(key = "goal_${runGoal.id}") {
                PlantCard(
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .staggeredEntrance(1)
                        .fillMaxWidth()
                        .testTag("challenge_goal_counter_card"),
                    onClick = { onChallengeClick(runGoal) },
                    brush = c.dreamGradient,
                    borderColor = Color.Transparent,
                    contentPadding = 18.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(AppShapes.pill)
                                    .background(c.onAccent.copy(alpha = 0.18f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "MILESTONE GOAL COUNTER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = c.onAccent
                                )
                            }
                            Text(
                                text = runGoal.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = c.onAccent
                            )
                            Text(
                                text = "Count progress (+1, +5, +10) and stay on track with weekly pacing",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.onAccent.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        IconBubble(background = c.onAccent.copy(alpha = 0.2f), size = 52.dp) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                tint = c.onAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Relationship Week photo card
        if (relationshipChallenge != null) {
            item(key = "photo_${relationshipChallenge.id}") {
                PhotoChallengeCard(
                    challenge = relationshipChallenge,
                    onClick = { onChallengeClick(relationshipChallenge) },
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .staggeredEntrance(2)
                        .testTag("recommended_challenge_card")
                )
            }
        }

        // Remaining unjoined challenges
        items(remainingChallenges, key = { "available_${it.id}" }) { challenge ->
            val index = remainingChallenges.indexOf(challenge)
            DiscoverChallengeCard(
                challenge = challenge,
                onClick = { onChallengeClick(challenge) },
                modifier = Modifier
                    .animateItem()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .staggeredEntrance(index + 3)
                    .testTag("challenge_card_${challenge.id}")
            )
        }

        // Create custom challenge via JSON
        item(key = "create_json") {
            PlantCard(
                modifier = Modifier
                    .animateItem()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                borderColor = c.primary.copy(alpha = 0.35f),
                contentPadding = 18.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconBubble(background = c.lavenderSoft, size = 38.dp) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = c.lavender,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Create Challenge via JSON",
                        style = MaterialTheme.typography.titleMedium,
                        color = c.textPrimary
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Build custom multi-day challenges with habits, durations, and milestones. Download the template, edit it on your device, and import it right away.",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChallengeOutlinedButton(
                        text = "Template",
                        icon = Icons.Default.Download,
                        accent = c.sun,
                        onClick = onDownloadTemplateClick,
                        modifier = Modifier.weight(1f),
                        height = 46.dp
                    )
                    ChallengeGradientButton(
                        text = "Import JSON",
                        icon = Icons.Default.FileUpload,
                        onClick = onImportJsonClick,
                        modifier = Modifier.weight(1f),
                        height = 46.dp
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------------------------------------ */
/* Pieces                                                                                       */
/* ------------------------------------------------------------------------------------------ */

/** Photo used for a challenge's hero/header, if it has one. */
internal fun challengePhotoRes(challenge: ChallengeEntity): Int? = when (challenge.id) {
    "gratitude_month", "relationship_week" -> R.drawable.challenge_gratitude
    else -> when (challenge.category.trim().lowercase()) {
        "routine", "mindfulness" -> R.drawable.routine_morning
        else -> null
    }
}

/** Icon representing a challenge category. */
internal fun challengeCategoryIcon(category: String): ImageVector = when (category.trim().lowercase()) {
    "mindfulness", "mind", "meditation", "mental" -> Icons.Default.SelfImprovement
    "health", "nutrition", "hydration", "water" -> Icons.Default.Favorite
    "fitness", "exercise", "sport", "movement" -> Icons.Default.FitnessCenter
    "people", "social", "relationships", "love" -> Icons.Default.Groups
    "finance", "money", "savings" -> Icons.Default.Savings
    "routine", "morning" -> Icons.Default.WbSunny
    "productivity", "work", "learning", "study" -> Icons.Default.Bolt
    else -> Icons.Default.Spa
}

@Composable
private fun HeaderPill(
    text: String,
    icon: ImageVector,
    background: Brush?,
    container: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .bounceClick(onClick = onClick)
            .clip(AppShapes.pill)
            .then(if (background != null) Modifier.background(background) else Modifier.background(container))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(15.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = contentColor)
    }
}

@Composable
private fun StatChip(icon: ImageVector, text: String, accent: AccentPair) {
    Row(
        modifier = Modifier
            .clip(AppShapes.pill)
            .background(accent.soft)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = accent.color, modifier = Modifier.size(14.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = accent.color)
    }
}

@Composable
private fun CountBadge(count: Int, accent: AccentPair) {
    Box(
        modifier = Modifier
            .clip(AppShapes.pill)
            .background(accent.soft)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text = "$count", style = MaterialTheme.typography.labelMedium, color = accent.color)
    }
}

@Composable
private fun CategoryChip(
    label: String,
    accent: AccentPair,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val c = PlantTheme.colors
    val container by animateColorAsState(
        targetValue = if (selected) accent.color else accent.soft,
        animationSpec = tween(250),
        label = "chipContainer"
    )
    val content by animateColorAsState(
        targetValue = if (selected) c.onAccent else accent.color,
        animationSpec = tween(250),
        label = "chipContent"
    )
    Row(
        modifier = Modifier
            .bounceClick(pressedScale = 0.92f, onClick = onClick)
            .clip(AppShapes.pill)
            .background(container)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("chip_category_${label.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(200)) + expandHorizontally(tween(250)),
            exit = fadeOut(tween(150)) + shrinkHorizontally(tween(250))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
            }
        }
        if (label != ALL_CATEGORIES) {
            Icon(
                imageVector = challengeCategoryIcon(label),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(5.dp))
        }
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = content)
    }
}

/** Horizontal progress bar that grows smoothly from 0 to its value on first show. */
@Composable
private fun AnimatedProgressBar(
    progress: Float,
    brush: Brush,
    track: Color,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 8.dp,
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        anim.animateTo(progress.coerceIn(0f, 1f), tween(450, delayMillis = 120, easing = FastOutSlowInEasing))
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(AppShapes.pill)
            .background(track)
    ) {
        if (anim.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(anim.value)
                    .fillMaxHeight()
                    .clip(AppShapes.pill)
                    .background(brush)
            )
        }
    }
}

@Composable
private fun FeaturedHeroCard(
    challenge: ChallengeEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PlantTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(196.dp)
            .bounceClick(pressedScale = 0.98f, onClick = onClick)
            .clip(AppShapes.xl)
            .testTag("banner_gratitude_month")
    ) {
        Image(
            painter = painterResource(id = R.drawable.challenge_gratitude),
            contentDescription = "Gratitude Month Banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Photo scrim: keeps white text legible on any image.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.05f),
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Featured badge
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(AppShapes.pill)
                .background(c.sunriseGradient)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            Text(text = "FEATURED CHALLENGE", style = MaterialTheme.typography.labelSmall, color = Color.White)
        }

        if (challenge.isJoined) {
            ProgressRing(
                progress = challenge.currentDay.toFloat() / challenge.durationDays.coerceAtLeast(1),
                size = 46.dp,
                strokeWidth = 5.dp,
                colors = listOf(c.sun, c.sunrise),
                trackColor = Color.White.copy(alpha = 0.25f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
            ) {
                Text(
                    text = "D${challenge.currentDay}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${challenge.durationDays} days · Tap to review habits & reminders",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .bounceClick(onClick = onClick)
                    .clip(AppShapes.pill)
                    .background(c.leafGradient)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("btn_inspect_gratitude"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (challenge.isJoined) "Active ✓" else "Review & Join",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.onPrimary
                )
            }
        }
    }
}

@Composable
private fun ActiveChallengeCard(
    challenge: ChallengeEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PlantTheme.colors
    val accent = c.categoryColors(challenge.category)
    val dayProgress = challenge.currentDay.toFloat() / challenge.durationDays.coerceAtLeast(1)
    val todayProgress = challenge.completedTasksToday.toFloat() / challenge.totalTasks.coerceAtLeast(1)
    val isGoal = challenge.goalTarget > 0f
    val barProgress = if (isGoal) challenge.goalCurrent / challenge.goalTarget else dayProgress

    PlantCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProgressRing(
                progress = todayProgress,
                size = 54.dp,
                strokeWidth = 6.dp,
                colors = listOf(accent.color, c.primary)
            ) {
                Icon(
                    imageVector = challengeCategoryIcon(challenge.category),
                    contentDescription = null,
                    tint = accent.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = challenge.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier
                    .clip(AppShapes.pill)
                    .background(c.primarySoft)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(AppShapes.pill)
                        .background(c.primary)
                )
                Text(text = "Active", style = MaterialTheme.typography.labelSmall, color = c.primary)
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Day ${challenge.currentDay} of ${challenge.durationDays}",
                style = MaterialTheme.typography.labelMedium,
                color = c.textSecondary
            )
            Text(
                text = "${(barProgress.coerceIn(0f, 1f) * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = c.sunrise
            )
        }
        Spacer(Modifier.height(6.dp))
        AnimatedProgressBar(
            progress = barProgress,
            brush = c.sunriseGradient,
            track = c.cardMuted
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isGoal) {
                    "Goal: ${challenge.goalCurrent.toInt()}/${challenge.goalTarget.toInt()} ${challenge.goalUnit}"
                } else {
                    "Today · ${challenge.completedTasksToday}/${challenge.totalTasks} tasks done"
                },
                style = MaterialTheme.typography.bodySmall,
                color = c.textPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Roadmap & tips",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.primary
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Details",
                    tint = c.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun PhotoChallengeCard(
    challenge: ChallengeEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PlantTheme.colors
    val accent = c.categoryColors("relationships")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(184.dp)
            .bounceClick(pressedScale = 0.98f, onClick = onClick)
            .clip(AppShapes.large)
    ) {
        Image(
            painter = painterResource(id = challengePhotoRes(challenge) ?: R.drawable.challenge_gratitude),
            contentDescription = challenge.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.82f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(AppShapes.pill)
                        .background(accent.soft)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = accent.color, modifier = Modifier.size(12.dp))
                    Text(text = "Relationships", style = MaterialTheme.typography.labelSmall, color = accent.color)
                }

                Box(
                    modifier = Modifier
                        .clip(AppShapes.pill)
                        .background(c.leafGradient)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "View Challenge", style = MaterialTheme.typography.labelMedium, color = c.onPrimary)
                }
            }

            Column {
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Text(
                    text = "${challenge.durationDays} days • Daily connection prompts and reminders",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun DiscoverChallengeCard(
    challenge: ChallengeEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PlantTheme.colors
    val accent = c.categoryColors(challenge.category)
    PlantCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IconBubble(background = accent.soft, size = 50.dp) {
                Icon(
                    imageVector = challengeCategoryIcon(challenge.category),
                    contentDescription = null,
                    tint = accent.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textPrimary
                )
                Text(
                    text = challenge.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${challenge.durationDays} days · ${challenge.totalTasks} scheduled habits",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent.color,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(AppShapes.pill)
                    .background(c.primarySoft)
                    .border(1.dp, c.primary.copy(alpha = 0.25f), AppShapes.pill)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(text = "Inspect →", style = MaterialTheme.typography.labelMedium, color = c.primary)
            }
        }
    }
}
