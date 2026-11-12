package com.example.ui.screens

import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Star
import com.example.data.model.isFinished
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import com.example.ui.theme.heroGradient
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.School
import com.example.ui.components.AppIcon
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
import com.example.ui.components.BannerSource
import com.example.ui.components.ChallengeBannerImage
import com.example.ui.components.rememberBannerSource
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
    modifier: Modifier = Modifier,
    onToggleFavorite: (ChallengeEntity) -> Unit = {},
    onMoveToGroup: (ChallengeEntity, String) -> Unit = { _, _ -> },
    onShare: (ChallengeEntity) -> Unit = {},
    onDelete: (ChallengeEntity) -> Unit = {},
    onUnhide: (ChallengeEntity) -> Unit = {},
) {
    val c = PlantTheme.colors

    var selectedCategory by rememberSaveable { mutableStateOf(ALL_CATEGORIES) }
    var query by rememberSaveable { mutableStateOf("") }
    var groupingName by rememberSaveable { mutableStateOf(ChallengeGrouping.NONE.name) }
    var sortName by rememberSaveable { mutableStateOf(ChallengeSort.A_Z.name) }
    val grouping = ChallengeGrouping.valueOf(groupingName)
    val sort = ChallengeSort.valueOf(sortName)
    var collapsed by rememberSaveable { mutableStateOf(setOf<String>()) }

    // Dialog / sheet targets
    var actionTarget by remember { mutableStateOf<ChallengeEntity?>(null) }
    var groupTarget by remember { mutableStateOf<ChallengeEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<ChallengeEntity?>(null) }
    var showHidden by remember { mutableStateOf(false) }

    val visible = challenges.filter { !it.isHidden }
    val hidden = challenges.filter { it.isHidden }
    val myGroups = remember(visible) { visible.map { it.groupName }.filter { it.isNotBlank() }.distinct().sorted() }
    val favoriteCount = visible.count { it.isFavorite }

    val categories = remember(visible) {
        listOf(ALL_CATEGORIES, FAVORITES) + visible.map { it.category.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
    }
    // If the selected category disappeared (e.g. challenge removed), fall back to "All".
    val activeCategory = if (categories.any { it.equals(selectedCategory, ignoreCase = true) }) {
        selectedCategory
    } else ALL_CATEGORIES

    fun matches(ch: ChallengeEntity): Boolean {
        val categoryOk = when (activeCategory) {
            ALL_CATEGORIES -> true
            FAVORITES -> ch.isFavorite
            else -> ch.category.trim().equals(activeCategory, ignoreCase = true)
        }
        val q = query.trim()
        val queryOk = q.isEmpty() || listOf(ch.title, ch.subtitle, ch.category, ch.groupName, ch.description)
            .any { it.contains(q, ignoreCase = true) }
        return categoryOk && queryOk
    }

    // Favourites first, then the chosen sort
    val ordering = compareByDescending<ChallengeEntity> { it.isFavorite }.then(
        when (sort) {
            ChallengeSort.A_Z -> compareBy { it.title.lowercase() }
            ChallengeSort.SHORTEST -> compareBy<ChallengeEntity> { it.durationDays }.thenBy { it.title.lowercase() }
            ChallengeSort.LONGEST -> compareByDescending<ChallengeEntity> { it.durationDays }.thenBy { it.title.lowercase() }
        }
    )
    val joinedChallenges = visible.filter { it.isJoined && matches(it) }.sortedWith(ordering)
    val discoverChallenges = visible.filter { !it.isJoined && matches(it) }.sortedWith(ordering)

    // Discover sections for the chosen grouping: (key, emoji, title, items)
    val sections: List<Triple<String, String, List<ChallengeEntity>>> = when (grouping) {
        ChallengeGrouping.NONE -> listOf(Triple("Discover challenges", "🌱", discoverChallenges))
        ChallengeGrouping.CATEGORY -> discoverChallenges.groupBy { it.category.ifBlank { "Other" } }
            .toSortedMap(String.CASE_INSENSITIVE_ORDER)
            .map { (cat, list) -> Triple(cat, categoryEmoji(cat), list) }
        ChallengeGrouping.STATUS -> listOf(
            Triple("Completed before", "🏆", discoverChallenges.filter { it.timesCompleted > 0 }),
            Triple("Not started yet", "🌱", discoverChallenges.filter { it.timesCompleted == 0 }),
        ).filter { it.third.isNotEmpty() }
        ChallengeGrouping.MY_GROUPS -> (myGroups.map { g -> Triple(g, "📁", discoverChallenges.filter { it.groupName == g }) } +
            listOf(Triple("Ungrouped", "🗂️", discoverChallenges.filter { it.groupName.isBlank() })))
            .filter { it.third.isNotEmpty() }
    }

    // Carousel only on the plain, unfiltered view
    val showFeatured = query.isBlank() && activeCategory == ALL_CATEGORIES
    val featured = remember(visible) {
        val picks = listOf("gratitude_month", "meditation_21", "digital_detox", "read_daily", "hydration_hero")
        (picks.mapNotNull { id -> visible.find { it.id == id } } + visible.filter { !it.isJoined })
            .distinctBy { it.id }
            .take(5)
    }
    val nothingMatches = joinedChallenges.isEmpty() && discoverChallenges.isEmpty()
    val columns = if (LocalConfiguration.current.screenWidthDp >= 600) 2 else 1

    // ---- sheets & dialogs ----
    actionTarget?.let { ch ->
        ChallengeActionsSheet(
            challenge = ch,
            onToggleFavorite = { onToggleFavorite(ch) },
            onMoveToGroup = { actionTarget = null; groupTarget = ch },
            onShare = { onShare(ch) },
            onDelete = {
                actionTarget = null
                // Built-ins are only hidden (undoable); imported ones are deleted for good, so confirm
                if (com.example.data.local.BuiltInChallenges.isBuiltIn(ch.id)) onDelete(ch) else deleteTarget = ch
            },
            onDismiss = { actionTarget = null }
        )
    }
    groupTarget?.let { ch ->
        MoveToGroupDialog(
            challenge = ch,
            existingGroups = myGroups,
            onSave = { name -> onMoveToGroup(ch, name); groupTarget = null },
            onDismiss = { groupTarget = null }
        )
    }
    deleteTarget?.let { ch ->
        DeleteChallengeDialog(
            challenge = ch,
            onConfirm = { onDelete(ch); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
    if (showHidden) {
        HiddenChallengesDialog(
            hidden = hidden,
            onRestore = { onUnhide(it) },
            onDismiss = { showHidden = false }
        )
    }

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
                val joinedCount = visible.count { it.isJoined }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip(
                        icon = Icons.Default.EmojiEvents,
                        text = "$joinedCount active",
                        accent = AccentPair(c.primary, c.primarySoft)
                    )
                    StatChip(
                        icon = Icons.Default.AutoAwesome,
                        text = "${visible.size - joinedCount} to discover",
                        accent = AccentPair(c.sunrise, c.sunriseSoft)
                    )
                    if (favoriteCount > 0) {
                        StatChip(
                            icon = Icons.Default.Star,
                            text = "$favoriteCount favourite" + if (favoriteCount == 1) "" else "s",
                            accent = AccentPair(c.sun, c.sunSoft)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                ChallengeToolbar(
                    query = query,
                    onQueryChange = { query = it },
                    grouping = grouping,
                    onGroupingChange = { groupingName = it.name },
                    sort = sort,
                    onSortChange = { sortName = it.name }
                )
            }
        }

        // Featured carousel (auto-advances, swipeable)
        if (showFeatured && featured.isNotEmpty()) {
            item(key = "featured_carousel") {
                FeaturedCarousel(
                    challenges = featured,
                    onClick = onChallengeClick,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
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
                        val accent = when (category) {
                            ALL_CATEGORIES -> AccentPair(c.primary, c.primarySoft)
                            FAVORITES -> AccentPair(c.sun, c.sunSoft)
                            else -> c.categoryColors(category)
                        }
                        CategoryChip(
                            label = if (category == FAVORITES) "★ Favourites ($favoriteCount)" else category,
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
                    onLongClick = { actionTarget = challenge },
                    onToggleFavorite = { onToggleFavorite(challenge) },
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .staggeredEntrance(index + 1)
                        .testTag("challenge_item_${challenge.id}")
                )
            }
        }

        if (nothingMatches) {
            item(key = "empty_filter") {
                EmptyState(
                    emoji = if (query.isNotBlank()) "🔍" else if (activeCategory == FAVORITES) "⭐" else "🌱",
                    title = when {
                        query.isNotBlank() -> "No challenges match \"$query\""
                        activeCategory == FAVORITES -> "No favourites yet"
                        else -> "No $activeCategory challenges yet"
                    },
                    message = if (activeCategory == FAVORITES) "Tap the ☆ on any challenge to keep it here."
                        else "Try another search or category, or import your own challenge as JSON.",
                    modifier = Modifier.animateItem()
                )
            }
        }

        // Discover sections (grid of banner tiles), grouped as chosen
        sections.forEach { (title, emoji, list) ->
            val sectionKey = "${grouping.name}:$title"
            val isCollapsed = sectionKey in collapsed
            item(key = "section_$sectionKey") {
                CollapsibleSectionHeader(
                    title = title,
                    emoji = emoji,
                    count = list.size,
                    collapsed = isCollapsed,
                    onToggle = { collapsed = if (isCollapsed) collapsed - sectionKey else collapsed + sectionKey },
                    modifier = Modifier
                        .animateItem()
                        .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 2.dp)
                )
            }
            if (!isCollapsed) {
                val rows = list.chunked(columns)
                itemsIndexed(rows, key = { _, row -> "row_${sectionKey}_" + row.joinToString("_") { it.id } }) { rowIndex, row ->
                    Row(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEachIndexed { i, challenge ->
                            ChallengeTile(
                                challenge = challenge,
                                onClick = { onChallengeClick(challenge) },
                                onLongClick = { actionTarget = challenge },
                                onToggleFavorite = { onToggleFavorite(challenge) },
                                modifier = Modifier
                                    .weight(1f)
                                    .staggeredEntrance(rowIndex * columns + i + 2)
                                    .testTag("challenge_card_${challenge.id}")
                            )
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        // Hidden built-in challenges
        if (hidden.isNotEmpty()) {
            item(key = "hidden_footer") {
                Row(
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(AppShapes.pill)
                        .bounceClick { showHidden = true }
                        .background(c.cardMuted)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("btn_hidden_challenges"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = c.textSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Hidden challenges (${hidden.size})", style = MaterialTheme.typography.labelLarge, color = c.textSecondary)
                }
            }
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

private const val FAVORITES = "__favorites__"

private fun categoryEmoji(category: String): String = when (category.trim().lowercase()) {
    "health", "hydration" -> "💧"
    "fitness" -> "🏃"
    "mindfulness" -> "🧘"
    "sleep" -> "🌙"
    "nutrition" -> "🥗"
    "productivity" -> "⚡"
    "learning" -> "📚"
    "finance" -> "💰"
    "people", "social" -> "💞"
    "home" -> "🏡"
    "routine" -> "☀️"
    "creativity" -> "🎨"
    else -> "🌱"
}

/** Icon representing a challenge category. */
internal fun challengeCategoryIcon(category: String): ImageVector = when (category.trim().lowercase()) {
    "mindfulness", "mind", "meditation", "mental" -> Icons.Default.SelfImprovement
    "health", "nutrition", "hydration", "water" -> Icons.Default.Favorite
    "fitness", "exercise", "sport", "movement" -> Icons.Default.FitnessCenter
    "people", "social", "relationships", "love" -> Icons.Default.Groups
    "finance", "money", "savings" -> Icons.Default.Savings
    "routine", "morning" -> Icons.Default.WbSunny
    "productivity", "work" -> Icons.Default.Bolt
    "learning", "study", "education" -> Icons.Default.School
    "home", "declutter" -> Icons.Default.Inventory2
    "sleep" -> AppIcon.BED.vector
    "creativity", "art" -> AppIcon.ART.vector
    "self-care", "self care" -> AppIcon.SMILE.vector
    "nutrition", "food", "diet" -> Icons.Default.Restaurant
    else -> Icons.Default.Spa
}

/** A challenge's own icon (ChallengeEntity.icon) when set, otherwise its category icon. */
internal fun challengeIcon(challenge: ChallengeEntity): ImageVector =
    if (challenge.icon.isNotBlank()) AppIcon.of(challenge.icon).vector else challengeCategoryIcon(challenge.category)

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
private fun FeaturedCarousel(
    challenges: List<ChallengeEntity>,
    onClick: (ChallengeEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PlantTheme.colors
    val pagerState = rememberPagerState { challenges.size }
    val dragged by pagerState.interactionSource.collectIsDraggedAsState()
    // Auto-advance every 5 s; pauses while the user is swiping.
    LaunchedEffect(dragged, challenges.size) {
        if (dragged || challenges.size < 2) return@LaunchedEffect
        while (true) {
            delay(5000)
            pagerState.animateScrollToPage(
                (pagerState.currentPage + 1) % challenges.size,
                animationSpec = tween(700, easing = FastOutSlowInEasing)
            )
        }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            key = { challenges[it].id }
        ) { page ->
            val challenge = challenges[page]
            val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
            FeaturedHeroCard(
                challenge = challenge,
                onClick = { onClick(challenge) },
                modifier = Modifier.graphicsLayer {
                    val scale = 1f - 0.07f * offset
                    scaleX = scale
                    scaleY = scale
                    alpha = 1f - 0.35f * offset
                }
            )
        }
        if (challenges.size > 1) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(challenges.size) { i ->
                    val selected = pagerState.currentPage == i
                    val width by animateDpAsState(if (selected) 22.dp else 8.dp, label = "dotWidth")
                    val color by animateColorAsState(if (selected) c.primary else c.border, label = "dotColor")
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(AppShapes.pill)
                            .background(color)
                    )
                }
            }
        }
    }
}

/** Category gradient used when a challenge has no banner image. */
@Composable
private fun categoryBrush(category: String): Brush {
    val c = PlantTheme.colors
    return when (category.trim().lowercase()) {
        "fitness", "exercise", "sport", "movement", "routine" -> c.sunriseGradient
        "mindfulness", "mind", "meditation", "mental" -> c.dreamGradient
        else -> c.heroGradient
    }
}

@Composable
private fun FeaturedHeroCard(
    challenge: ChallengeEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = PlantTheme.colors
    val banner = rememberBannerSource(challenge)
    // Slow "Ken Burns" drift so the artwork feels alive
    val drift = rememberInfiniteTransition(label = "kenBurns")
    val zoom by drift.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "kenBurnsZoom"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .bounceClick(pressedScale = 0.98f, onClick = onClick)
            .clip(AppShapes.xl)
            .testTag("featured_${challenge.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { scaleX = zoom; scaleY = zoom }
        ) {
            if (banner != null) {
                ChallengeBannerImage(source = banner, contentDescription = "${challenge.title} banner", modifier = Modifier.fillMaxSize())
            } else {
                Box(Modifier.fillMaxSize().background(categoryBrush(challenge.category)))
            }
        }
        // Photo scrim: keeps white text legible on any image.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.05f), Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.8f))
                    )
                )
        )

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
            Text(text = "FEATURED", style = MaterialTheme.typography.labelSmall, color = Color.White)
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
                Text(text = "D${challenge.currentDay}", style = MaterialTheme.typography.labelSmall, color = Color.White)
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(challengeIcon(challenge), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${challenge.durationDays} days · ${challenge.subtitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .clip(AppShapes.pill)
                    .background(c.leafGradient)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
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

/** Banner tile used in the Discover grid. */
@Composable
private fun ChallengeTile(
    challenge: ChallengeEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onToggleFavorite: () -> Unit = {},
) {
    val c = PlantTheme.colors
    val accent = c.categoryColors(challenge.category)
    val banner = rememberBannerSource(challenge)
    // Each tile's icon floats on its own rhythm
    val float = rememberInfiniteTransition(label = "tileFloat")
    val dy by float.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(
            tween(1700 + (challenge.id.hashCode().absoluteValue % 700), easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "tileFloatY"
    )
    Column(
        modifier = modifier
            .bounceClick(pressedScale = 0.97f, onLongClick = onLongClick, onClick = onClick)
            .clip(AppShapes.large)
            .background(c.card)
            .border(1.dp, c.border, AppShapes.large)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            if (banner != null) {
                ChallengeBannerImage(source = banner, contentDescription = null, modifier = Modifier.fillMaxSize())
            } else {
                Box(Modifier.fillMaxSize().background(categoryBrush(challenge.category)))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))))
            )
            FavoriteStar(
                favorite = challenge.isFavorite,
                onToggle = onToggleFavorite,
                modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
            )
            // Group, duration + goal chips
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (challenge.groupName.isNotBlank()) TileChip("📁 ${challenge.groupName}")
                if (challenge.timesCompleted > 0) TileChip("🏆 ×${challenge.timesCompleted}")
                if (challenge.goalTarget > 0f) TileChip("🎯 Goal")
                TileChip("${challenge.durationDays} days")
            }
            // Floating icon
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .graphicsLayer { translationY = dy.dp.toPx() }
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(c.surface)
                    .border(2.dp, accent.color.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(challengeIcon(challenge), contentDescription = null, tint = accent.color, modifier = Modifier.size(24.dp))
            }
        }
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
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
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(AppShapes.pill)
                        .background(accent.soft)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(text = challenge.category, style = MaterialTheme.typography.labelSmall, color = accent.color)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${challenge.totalTasks} daily tasks",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textMuted,
                    modifier = Modifier.weight(1f)
                )
                Text(text = "Inspect →", style = MaterialTheme.typography.labelMedium, color = c.primary)
            }
        }
    }
}

@Composable
private fun TileChip(text: String) {
    Box(
        modifier = Modifier
            .clip(AppShapes.pill)
            .background(Color.Black.copy(alpha = 0.42f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

@Composable
private fun ActiveChallengeCard(
    challenge: ChallengeEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onToggleFavorite: () -> Unit = {},
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
        onLongClick = onLongClick,
        contentPadding = 16.dp
    ) {
        rememberBannerSource(challenge)?.let { banner ->
            Box {
                ChallengeBannerImage(
                    source = banner,
                    contentDescription = "${challenge.title} banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(AppShapes.medium)
                )
                FavoriteStar(
                    favorite = challenge.isFavorite,
                    onToggle = onToggleFavorite,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
        }
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
                    imageVector = challengeIcon(challenge),
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

            val finished = challenge.isFinished()
            Row(
                modifier = Modifier
                    .clip(AppShapes.pill)
                    .then(if (finished) Modifier.background(c.sunriseGradient) else Modifier.background(c.primarySoft))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!finished) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(AppShapes.pill)
                            .background(c.primary)
                    )
                }
                Text(
                    text = if (finished) "🎉 Completed" else "Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (finished) Color.White else c.primary
                )
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
                    text = if (challenge.isFinished()) "Tap to celebrate" else "Roadmap & tips",
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


