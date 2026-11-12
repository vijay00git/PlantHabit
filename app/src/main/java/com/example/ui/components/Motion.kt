package com.example.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppShapes
import com.example.ui.theme.PlantTheme
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** True when the user asked for less motion (setting or system "remove animations"). */
val LocalReduceMotion = androidx.compose.runtime.staticCompositionLocalOf { false }

/* ------------------------------------------------------------------------------------------ */
/* Haptics                                                                                      */
/* ------------------------------------------------------------------------------------------ */

/** Small wrapper over View haptics. Get one with [rememberHaptics]. */
class Haptics(private val view: View) {
    /** Light tick: tab switches, chip selection, steppers. */
    fun tick() {
        view.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= 27) HapticFeedbackConstants.KEYBOARD_TAP
            else HapticFeedbackConstants.VIRTUAL_KEY
        )
    }

    /** Satisfying confirm: completing a habit, accepting a challenge. */
    fun success() {
        view.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM
            else HapticFeedbackConstants.LONG_PRESS
        )
    }

    /** Negative / destructive action. */
    fun reject() {
        view.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.REJECT
            else HapticFeedbackConstants.LONG_PRESS
        )
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view) }
}

/* ------------------------------------------------------------------------------------------ */
/* Press feedback                                                                               */
/* ------------------------------------------------------------------------------------------ */

/**
 * Clickable that springs down when pressed and bounces back, with an optional haptic tick.
 * Use instead of `Modifier.clickable` on cards, chips and custom buttons.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun Modifier.bounceClick(
    enabled: Boolean = true,
    pressedScale: Float = 0.95f,
    haptic: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "bounceScale"
    )
    val haptics = rememberHaptics()
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .then(
            if (onLongClick != null) {
                Modifier.combinedClickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    onLongClick = {
                        haptics.success()
                        onLongClick()
                    },
                    onClick = {
                        if (haptic) haptics.tick()
                        onClick()
                    }
                )
            } else {
                Modifier.clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                ) {
                    if (haptic) haptics.tick()
                    onClick()
                }
            }
        )
}

/* ------------------------------------------------------------------------------------------ */
/* Entrance animation                                                                           */
/* ------------------------------------------------------------------------------------------ */

/**
 * Fades and slides content up the first time it appears. Pass the item's position as [index]
 * to stagger a list (each item starts ~45ms after the previous, capped).
 */
fun Modifier.staggeredEntrance(index: Int = 0, offsetY: Dp = 24.dp): Modifier = composed {
    var played by rememberSaveable { mutableStateOf(false) }
    val progress = remember { Animatable(if (played) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!played) {
            delay((index.coerceIn(0, 10) * 45L))
            progress.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
            played = true
        }
    }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val offsetPx = with(density) { offsetY.toPx() }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * offsetPx
    }
}

/* ------------------------------------------------------------------------------------------ */
/* Building blocks                                                                              */
/* ------------------------------------------------------------------------------------------ */

/** Standard PlantHabit card: rounded, themed surface with a hairline border. */
@Composable
fun PlantCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = AppShapes.large,
    containerColor: Color = PlantTheme.colors.card,
    brush: Brush? = null,
    borderColor: Color = PlantTheme.colors.border,
    contentPadding: Dp = 16.dp,
    onLongClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = modifier
        .then(
            if (onClick != null) Modifier.bounceClick(pressedScale = 0.98f, onLongClick = onLongClick, onClick = onClick)
            else Modifier
        )
        .clip(shape)
        .then(if (brush != null) Modifier.background(brush) else Modifier.background(containerColor))
        .border(1.dp, borderColor, shape)
        .padding(contentPadding)
    Column(modifier = base, content = content)
}

/** Round tinted bubble for an icon or emoji. */
@Composable
fun IconBubble(
    background: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(background),
        contentAlignment = Alignment.Center
    ) { content() }
}

/** Section title row with optional trailing action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            color = PlantTheme.colors.textPrimary
        )
        action?.invoke()
    }
}

/** Friendly empty state with a gently floating emoji. */
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val float = androidx.compose.animation.core.rememberInfiniteTransition(label = "emptyFloat")
    val dy by float.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            tween(1600, easing = FastOutSlowInEasing),
            androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "emptyFloatY"
    )
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconBubble(PlantTheme.colors.primarySoft, size = 88.dp, modifier = Modifier.graphicsLayer { translationY = dy }) {
            Text(emoji, style = androidx.compose.material3.MaterialTheme.typography.displaySmall)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            color = PlantTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = PlantTheme.colors.textSecondary,
            textAlign = TextAlign.Center
        )
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            action()
        }
    }
}

/* ------------------------------------------------------------------------------------------ */
/* Animated numbers & progress                                                                  */
/* ------------------------------------------------------------------------------------------ */

/** Integer that rolls up/down when it changes. */
@Composable
fun AnimatedCounter(
    value: Int,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            val up = targetState > initialState
            (slideInVertically { if (up) it else -it } + fadeIn()) togetherWith
                (slideOutVertically { if (up) -it else it } + fadeOut())
        },
        label = "counter",
        modifier = modifier
    ) { v ->
        Text("$prefix$v$suffix", style = style, color = color)
    }
}

/** Circular progress ring with a springy fill and gradient stroke. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 8.dp,
    colors: List<Color> = listOf(PlantTheme.colors.primary, PlantTheme.colors.sky),
    trackColor: Color = PlantTheme.colors.cardMuted,
    content: @Composable () -> Unit = {},
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "ringProgress"
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arcSize = androidx.compose.ui.geometry.Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(
                color = trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize, style = Stroke(stroke)
            )
            if (animated > 0f) {
                rotate(-90f) {
                    drawArc(
                        brush = Brush.sweepGradient(colors + colors.first()),
                        startAngle = 0f, sweepAngle = 360f * animated, useCenter = false,
                        topLeft = Offset(inset, inset), size = arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
            }
        }
        content()
    }
}

/* ------------------------------------------------------------------------------------------ */
/* Celebration                                                                                  */
/* ------------------------------------------------------------------------------------------ */

private data class Particle(
    val angle: Float, val speed: Float, val size: Float, val color: Color, val spin: Float, val isLeaf: Boolean
)

/**
 * Confetti/leaf burst drawn over its bounds. Increment [trigger] to fire it (0 = idle).
 * Put it in a Box on top of the thing being celebrated; it doesn't intercept touches.
 */
@Composable
fun CelebrationBurst(
    trigger: Int,
    modifier: Modifier = Modifier,
    particleCount: Int = 26,
) {
    val c = PlantTheme.colors
    val palette = listOf(c.primary, c.sun, c.sunrise, c.berry, c.sky, c.lavender)
    val progress = remember { Animatable(1f) }
    val particles = remember(trigger) {
        val rnd = Random(trigger * 7919 + 13)
        List(particleCount) {
            Particle(
                angle = rnd.nextFloat() * 360f,
                speed = 0.55f + rnd.nextFloat() * 0.6f,
                size = 4f + rnd.nextFloat() * 6f,
                color = palette[rnd.nextInt(palette.size)],
                spin = rnd.nextFloat() * 720f - 360f,
                isLeaf = rnd.nextBoolean()
            )
        }
    }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(900, easing = LinearEasing))
        }
    }
    if (progress.value < 1f) {
        Canvas(modifier = modifier) {
            val t = progress.value
            val center = Offset(size.width / 2, size.height / 2)
            val reach = minOf(size.width, size.height).coerceAtLeast(120f) * 0.9f
            val eased = 1f - (1f - t) * (1f - t)
            particles.forEach { p ->
                val rad = Math.toRadians(p.angle.toDouble())
                val dist = reach * p.speed * eased
                val gravity = 140f * t * t
                val pos = Offset(
                    center.x + (cos(rad) * dist).toFloat(),
                    center.y + (sin(rad) * dist).toFloat() + gravity
                )
                val alpha = (1f - t).coerceIn(0f, 1f)
                val s = p.size.dp.toPx() / 2
                rotate(p.spin * t, pivot = pos) {
                    if (p.isLeaf) {
                        drawOval(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(pos.x - s, pos.y - s / 2),
                            size = androidx.compose.ui.geometry.Size(s * 2, s)
                        )
                    } else {
                        drawRect(
                            color = p.color.copy(alpha = alpha),
                            topLeft = Offset(pos.x - s / 2, pos.y - s / 2),
                            size = androidx.compose.ui.geometry.Size(s, s)
                        )
                    }
                }
            }
        }
    }
}
