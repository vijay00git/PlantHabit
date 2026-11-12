package com.planthabit.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NoFood
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.PhonelinkOff
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.planthabit.ui.theme.AccentPair
import com.planthabit.ui.theme.AppColors

/**
 * The single icon set used for habits, challenge tasks and challenges.
 * Keys are stored in the database (HabitEntity.iconName, challenge task "iconName",
 * ChallengeEntity.icon), so only ever add keys — never rename existing ones.
 */
enum class AppIcon(val key: String, val vector: ImageVector) {
    SUN("sun", Icons.Default.WbSunny),
    MOON("moon", Icons.Default.NightsStay),
    BED("bed", Icons.Default.Bedtime),
    ALARM("alarm", Icons.Default.Alarm),
    WATER("water", Icons.Default.WaterDrop),
    WALK("walk", Icons.AutoMirrored.Filled.DirectionsWalk),
    RUN("run", Icons.AutoMirrored.Filled.DirectionsRun),
    BIKE("bike", Icons.Default.DirectionsBike),
    SWIM("swim", Icons.Default.Pool),
    HIKE("hike", Icons.Default.Hiking),
    GYM("gym", Icons.Default.FitnessCenter),
    YOGA("yoga", Icons.Default.SelfImprovement),
    MEDITATE("meditate", Icons.Default.Spa),
    MIND("mind", Icons.Default.Psychology),
    BOOK("book", Icons.AutoMirrored.Filled.MenuBook),
    JOURNAL("journal", Icons.Default.EditNote),
    STUDY("study", Icons.Default.School),
    LANGUAGE("language", Icons.Default.Translate),
    GLOBE("globe", Icons.Default.Language),
    CODE("code", Icons.Default.Code),
    FOCUS("focus", Icons.Default.Timer),
    IDEA("idea", Icons.Default.Lightbulb),
    ART("art", Icons.Default.Brush),
    MUSIC("music", Icons.Default.MusicNote),
    FOOD("food", Icons.Default.Restaurant),
    NO_SUGAR("no_sugar", Icons.Default.NoFood),
    COFFEE("coffee", Icons.Default.LocalCafe),
    SAVINGS("savings", Icons.Default.Savings),
    CLEAN("clean", Icons.Default.CleaningServices),
    DECLUTTER("declutter", Icons.Default.Inventory2),
    PHONE_OFF("phone_off", Icons.Default.PhonelinkOff),
    HEART("heart", Icons.Default.Favorite),
    PEOPLE("people", Icons.Default.Groups),
    CALL("call", Icons.Default.Call),
    CHAT("chat", Icons.Default.Forum),
    SMILE("smile", Icons.Default.SentimentSatisfied),
    NATURE("nature", Icons.Default.Park),
    FIRE("fire", Icons.Default.LocalFireDepartment),
    TROPHY("trophy", Icons.Default.EmojiEvents),
    SPARKLE("sparkle", Icons.Default.AutoAwesome);

    companion object {
        private val byKey = entries.associateBy { it.key }

        /** Unknown or blank keys fall back to [SPARKLE]. */
        fun of(key: String?): AppIcon = byKey[key?.trim()?.lowercase()] ?: SPARKLE

        /** Picks an icon from words in a habit title, e.g. "Read 20 pages" → BOOK. */
        fun guessFromTitle(title: String, category: String = ""): AppIcon {
            val t = title.lowercase()
            fun has(vararg words: String) = words.any { t.contains(it) }
            return when {
                has("sugar", "sweet", "candy") -> NO_SUGAR
                has("water", "drink", "hydrat") -> WATER
                has("run", "jog", "5k", "marathon") -> RUN
                has("walk", "steps") -> WALK
                has("bike", "cycl") -> BIKE
                has("swim") -> SWIM
                has("hike") -> HIKE
                has("gym", "workout", "lift", "push-up", "pushup", "squat", "strength") -> GYM
                has("stretch", "yoga", "pilates") -> YOGA
                has("meditat", "breath", "pray") -> MEDITATE
                has("sleep", "bed", "nap") -> BED
                has("wake", "alarm", "snooze") -> ALARM
                has("read", "book", "pages") -> BOOK
                has("journal", "write", "diary", "pages") -> JOURNAL
                has("study", "learn", "course", "class", "exam") -> STUDY
                has("language", "spanish", "french", "german", "japanese", "hindi", "vocab") -> LANGUAGE
                has("code", "program") -> CODE
                has("focus", "deep work", "pomodoro") -> FOCUS
                has("draw", "paint", "sketch", "art") -> ART
                has("music", "guitar", "piano", "sing", "practice") -> MUSIC
                has("cook", "eat", "meal", "fruit", "veg", "salad", "breakfast") -> FOOD
                has("coffee", "tea") -> COFFEE
                has("save", "saving", "money", "budget", "invest", "spend") -> SAVINGS
                has("clean", "tidy", "laundry", "dishes") -> CLEAN
                has("declutter", "organi") -> DECLUTTER
                has("phone", "screen", "social media", "detox", "scroll") -> PHONE_OFF
                has("call", "mom", "dad", "family") -> CALL
                has("friend", "date", "partner", "kids") -> HEART
                has("gratitude", "thank", "smile", "kind") -> SMILE
                has("outside", "nature", "park", "garden", "plant") -> NATURE
                has("morning", "sunrise", "sun") -> SUN
                has("night", "evening", "wind down") -> MOON
                else -> when (category.trim().lowercase()) {
                    "health" -> WATER
                    "fitness" -> RUN
                    "mindfulness" -> MEDITATE
                    "productivity" -> FOCUS
                    "learning" -> STUDY
                    "sleep" -> BED
                    "nutrition" -> FOOD
                    "creativity" -> ART
                    "home" -> CLEAN
                    "self-care", "self care" -> SMILE
                    "finance" -> SAVINGS
                    "people", "social", "relationships" -> PEOPLE
                    else -> SPARKLE
                }
            }
        }
    }
}

/** Accent colours that suit each icon, so lists of tasks look varied and meaningful. */
fun AppColors.iconAccent(icon: AppIcon): AccentPair = when (icon) {
    AppIcon.SUN, AppIcon.ALARM, AppIcon.IDEA, AppIcon.TROPHY, AppIcon.SAVINGS, AppIcon.COFFEE ->
        AccentPair(sun, sunSoft)
    AppIcon.MOON, AppIcon.BED, AppIcon.MEDITATE, AppIcon.MIND, AppIcon.YOGA, AppIcon.MUSIC, AppIcon.ART ->
        AccentPair(lavender, lavenderSoft)
    AppIcon.WATER, AppIcon.SWIM, AppIcon.LANGUAGE, AppIcon.GLOBE, AppIcon.STUDY, AppIcon.CODE, AppIcon.CLEAN,
    AppIcon.DECLUTTER -> AccentPair(sky, skySoft)
    AppIcon.RUN, AppIcon.WALK, AppIcon.BIKE, AppIcon.HIKE, AppIcon.GYM, AppIcon.FIRE, AppIcon.FOCUS ->
        AccentPair(sunrise, sunriseSoft)
    AppIcon.HEART, AppIcon.PEOPLE, AppIcon.CALL, AppIcon.CHAT, AppIcon.NO_SUGAR, AppIcon.SMILE ->
        AccentPair(berry, berrySoft)
    AppIcon.BOOK, AppIcon.JOURNAL, AppIcon.FOOD, AppIcon.PHONE_OFF, AppIcon.NATURE, AppIcon.SPARKLE ->
        AccentPair(primary, primarySoft)
}
