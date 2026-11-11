package com.example.data.local

import com.example.data.model.ChallengeEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * The challenges that ship with PlantHabit.
 *
 * [sync] runs every time the database opens: new built-ins are added and existing ones get
 * their latest content (text, tasks, tips, icon, bundled banner) while everything the user did
 * — joined state, start date, goal progress, their own banner photo — is preserved.
 * Imported challenges (ids not in this list) are never touched.
 */
object BuiltInChallenges {

    private fun task(
        title: String,
        description: String,
        timeOfDay: String,
        time: String,
        icon: String,
        tip: String,
        target: Int = 1,
        unit: String = "tasks",
        calendar: Boolean = false,
    ) = JSONObject().apply {
        put("title", title)
        put("description", description)
        put("timeOfDay", timeOfDay)
        put("scheduledTime", time)
        put("targetValue", target)
        put("unit", unit)
        put("iconName", icon)
        put("reminderEnabled", true)
        put("reminderTime", time)
        put("syncWithGoogleCalendar", calendar)
        put("tip", tip)
    }

    private fun challenge(
        id: String,
        title: String,
        subtitle: String,
        description: String,
        days: Int,
        category: String,
        icon: String,
        banner: String,
        tip: String,
        tasks: List<JSONObject>,
        skips: Int = 2,
        goalTarget: Float = 0f,
        goalUnit: String = "",
    ) = ChallengeEntity(
        id = id,
        title = title,
        subtitle = subtitle,
        description = description,
        durationDays = days,
        currentDay = 1,
        skipsLeft = skips,
        isJoined = false,
        category = category,
        bannerResName = banner,
        totalTasks = tasks.size,
        completedTasksToday = 0,
        dailyTip = tip,
        tasksJson = JSONArray(tasks).toString(),
        goalTarget = goalTarget,
        goalCurrent = 0f,
        goalUnit = goalUnit,
        icon = icon,
    )

    val all: List<ChallengeEntity> = listOf(
        challenge(
            id = "gratitude_month",
            title = "Gratitude Month",
            subtitle = "Notice the good, every single day",
            description = "Cultivate daily thankfulness and a positive outlook with short morning and evening reflections and one act of appreciation each day.",
            days = 30, category = "Mindfulness", icon = "smile", banner = "challenge_gratitude", skips = 3,
            tip = "Writing down specific things you're grateful for — not just \"family\" but why — is what makes the habit stick.",
            tasks = listOf(
                task("Morning gratitude list", "Write 3 specific things you appreciate today", "MORNING", "07:30", "journal",
                    "Be specific: the reason matters more than the thing.", calendar = true),
                task("Send an appreciation note", "Thank someone for something they did", "AFTERNOON", "14:00", "chat",
                    "Small, unexpected thanks can brighten someone's whole day."),
                task("Evening win journal", "Celebrate one personal win before sleeping", "EVENING", "21:45", "moon",
                    "Focus on your effort, not just outcomes."),
            )
        ),
        challenge(
            id = "better_sleep",
            title = "Better Sleep Reset",
            subtitle = "Wind down, sleep deeper, wake rested",
            description = "Reset your body clock with a calm, repeatable evening routine: screens off, a cool-down window, paper reading and a consistent bedtime.",
            days = 14, category = "Health", icon = "bed", banner = "banner_better_sleep",
            tip = "A consistent bedtime — even on weekends — is the single biggest lever for better sleep.",
            tasks = listOf(
                task("No caffeine after 2 pm", "Switch to water or herbal tea", "AFTERNOON", "14:00", "coffee",
                    "Caffeine can stay in your system for 6+ hours."),
                task("Screens off", "Phone on the charger outside the bedroom", "EVENING", "21:30", "phone_off",
                    "Blue light and notifications both keep your brain alert."),
                task("Start winding down", "Thirty calm minutes before lights out", "EVENING", "22:00", "moon",
                    "Dim the lights and switch to warm lamps."),
                task("Read paper, not a screen", "A book or magazine — ten pages is plenty", "EVENING", "22:10", "book",
                    "Reading on paper helps your mind settle.", target = 10, unit = "pages"),
                task("In bed at your set time", "Same time every night", "EVENING", "22:30", "bed",
                    "Stay within 15 minutes of your target bedtime.", calendar = true),
            )
        ),
        challenge(
            id = "happy_mornings",
            title = "Happy Mornings",
            subtitle = "A 7-day morning routine that sticks",
            description = "Wake up energised with a simple, consistent morning: no snooze, water first, a few minutes of daylight and a made bed.",
            days = 7, category = "Routine", icon = "sun", banner = "routine_morning", skips = 1,
            tip = "Morning daylight helps set your body clock for the whole day.",
            tasks = listOf(
                task("Up at 7:00, no snooze", "Jump out of bed when the alarm rings", "MORNING", "07:00", "alarm",
                    "Put your alarm across the room so you have to stand up.", calendar = true),
                task("Glass of water when you wake", "Hydrate before coffee", "MORNING", "07:05", "water",
                    "You wake up mildly dehydrated — water first."),
                task("5 minutes of daylight", "Step outside or open a window", "MORNING", "07:15", "sun",
                    "Natural light is far brighter than indoor light, even on cloudy days.", target = 5, unit = "mins"),
                task("Make your bed", "A small first win for the day", "MORNING", "07:20", "clean",
                    "Starting with a finished task builds momentum."),
            )
        ),
        challenge(
            id = "goal_counter_run",
            title = "Run 100 km",
            subtitle = "Log every run and walk toward 100 km",
            description = "A distance goal you chip away at. Log each run or walk and watch the counter climb toward 100 km over ten weeks.",
            days = 70, category = "Fitness", icon = "run", banner = "banner_goal_counter_run", skips = 5,
            goalTarget = 100f, goalUnit = "km",
            tip = "About 10 km a week gets you there on time — three easy runs is plenty.",
            tasks = listOf(
                task("Run or walk", "Log the distance toward your 100 km", "MORNING", "07:30", "run",
                    "Easy, conversational pace builds endurance without burning out.", target = 5, unit = "km", calendar = true),
                task("Post-run stretch", "Calves, hamstrings and hips", "MORNING", "08:15", "yoga",
                    "Five minutes now saves sore legs tomorrow.", target = 5, unit = "mins"),
            )
        ),
        challenge(
            id = "relationship_week",
            title = "Relationship Week",
            subtitle = "Seven days of real connection",
            description = "Strengthen bonds with the people who matter through daily conversation, appreciation and reaching out.",
            days = 7, category = "People", icon = "heart", banner = "banner_relationship_week",
            tip = "Undivided attention is the most valuable thing you can give someone.",
            tasks = listOf(
                task("Express unprompted thanks", "Send a heartfelt message to someone", "AFTERNOON", "13:00", "heart",
                    "Mention something specific they did that helped you."),
                task("Call a friend or family member", "A real call, not just a text", "EVENING", "18:30", "call",
                    "Hearing a voice feels more connecting than any message."),
                task("15-minute focused conversation", "Phones away, full attention", "EVENING", "19:30", "chat",
                    "Put phones in another room.", target = 15, unit = "mins", calendar = true),
            )
        ),
        challenge(
            id = "savings_jar",
            title = "Savings Jar",
            subtitle = "Grow an emergency fund, one deposit at a time",
            description = "Build a savings habit with small daily deposits toward a target, plus a weekly look at where your money goes.",
            days = 60, category = "Finance", icon = "savings", banner = "banner_savings_jar", skips = 5,
            goalTarget = 1850f, goalUnit = "$",
            tip = "Automating even a small daily amount adds up faster than you expect.",
            tasks = listOf(
                task("Daily deposit", "Move a small amount into savings", "EVENING", "20:00", "savings",
                    "Skipping one impulse buy can fund a week of deposits."),
                task("Skip one impulse buy", "Pause 24 hours before non-essential purchases", "AFTERNOON", "12:30", "idea",
                    "Most impulse wants fade within a day."),
            )
        ),
        challenge(
            id = "hydration_hero",
            title = "Hydration Hero",
            subtitle = "21 days of drinking enough water",
            description = "Make hydration automatic: water on waking, a daily target and a calm evening tea instead of late caffeine.",
            days = 21, category = "Health", icon = "water", banner = "banner_hydration_hero",
            tip = "Keep a filled bottle in sight — seeing it is the best reminder.",
            tasks = listOf(
                task("Water on waking", "One full glass before anything else", "MORNING", "07:00", "water",
                    "Keep a glass by your bed.", target = 250, unit = "ml"),
                task("Hit your daily water goal", "Sip steadily through the day", "ANYTIME", "12:00", "water",
                    "Pale yellow is the colour to aim for.", target = 2500, unit = "ml"),
                task("Herbal tea in the evening", "Hydrating and caffeine-free", "EVENING", "20:30", "coffee",
                    "A warm drink also helps you wind down."),
            )
        ),
        challenge(
            id = "digital_detox",
            title = "Digital Detox",
            subtitle = "Take back your attention in 7 days",
            description = "Loosen your phone's grip with phone-free mornings, time outside without screens and an evening cut-off.",
            days = 7, category = "Mindfulness", icon = "phone_off", banner = "banner_digital_detox",
            tip = "Turn off all non-essential notifications — most apps don't need to interrupt you.",
            tasks = listOf(
                task("Phone-free first hour", "No scrolling until you've started your day", "MORNING", "07:00", "phone_off",
                    "Use a real alarm clock so the phone can sleep elsewhere."),
                task("30 minutes outside, no phone", "A walk, a park, the balcony", "AFTERNOON", "17:30", "nature",
                    "Boredom is where good ideas show up.", target = 30, unit = "mins"),
                task("Screens off by 9:30 pm", "Read, talk or stretch instead", "EVENING", "21:30", "moon",
                    "Charge your phone outside the bedroom."),
            )
        ),
        challenge(
            id = "read_daily",
            title = "Read Every Day",
            subtitle = "20 pages a day, one book at a time",
            description = "Rebuild a reading habit with a small daily page goal and a note of one idea worth keeping.",
            days = 30, category = "Learning", icon = "book", banner = "banner_read_daily",
            tip = "Twenty pages a day is roughly a book every two weeks.",
            tasks = listOf(
                task("Read 20 pages", "Any book you enjoy", "EVENING", "21:30", "book",
                    "Keep the book where you usually reach for your phone.", target = 20, unit = "pages"),
                task("Note one idea", "Write down a line or idea worth keeping", "EVENING", "21:55", "journal",
                    "Writing it down is how you remember it."),
            )
        ),
        challenge(
            id = "meditation_21",
            title = "21-Day Meditation",
            subtitle = "Ten calm minutes a day",
            description = "Build a meditation practice gently: a short daily sit and mindful pauses woven into your day.",
            days = 21, category = "Mindfulness", icon = "meditate", banner = "banner_meditation_21",
            tip = "Missing a day doesn't break the habit — skipping two starts a new one. Come back tomorrow.",
            tasks = listOf(
                task("10-minute meditation", "Sit, breathe, notice", "MORNING", "07:30", "meditate",
                    "When your mind wanders, gently return to the breath — that IS the practice.", target = 10, unit = "mins"),
                task("Three mindful breaths", "Before lunch, pause and breathe", "AFTERNOON", "12:30", "mind",
                    "Slow exhales calm the nervous system."),
            )
        ),
        challenge(
            id = "steps_10k",
            title = "10K Steps a Day",
            subtitle = "Move more, every day for a month",
            description = "Hit 10,000 steps daily with walking breaks, taking the stairs and an evening stroll.",
            days = 30, category = "Fitness", icon = "walk", banner = "banner_steps_10k",
            tip = "A 10-minute walk after meals adds about 1,000 steps and helps digestion.",
            tasks = listOf(
                task("Walk 10,000 steps", "Spread through the day", "ANYTIME", "08:00", "walk",
                    "Short walking calls or meetings add up fast.", target = 10000, unit = "steps"),
                task("Take the stairs", "Skip the lift at least once", "AFTERNOON", "13:00", "hike",
                    "Stairs are free strength training."),
            )
        ),
        challenge(
            id = "no_sugar",
            title = "Sugar-Free 14",
            subtitle = "Two weeks without added sugar",
            description = "Reset your taste buds: no added sugar, fruit for dessert and water when cravings hit.",
            days = 14, category = "Nutrition", icon = "no_sugar", banner = "banner_no_sugar",
            tip = "Cravings usually peak in the first 3–5 days, then fade.",
            tasks = listOf(
                task("No added sugar today", "Check labels — sugar hides in sauces and drinks", "ANYTIME", "09:00", "no_sugar",
                    "Plan snacks ahead so you aren't caught hungry."),
                task("Fruit for dessert", "Berries, an apple or citrus", "EVENING", "19:30", "food",
                    "Fruit's fibre makes it far more filling than sweets."),
                task("Water when cravings hit", "Drink a glass and wait 10 minutes", "AFTERNOON", "15:30", "water",
                    "Thirst is often mistaken for a craving."),
            )
        ),
        challenge(
            id = "morning_pages",
            title = "Morning Pages",
            subtitle = "Clear your head on paper each morning",
            description = "Write three pages longhand every morning — whatever comes to mind — then set one intention for the day.",
            days = 21, category = "Mindfulness", icon = "journal", banner = "banner_morning_pages",
            tip = "There's no wrong way to do it. Just keep the pen moving.",
            tasks = listOf(
                task("Write 3 pages", "Stream of consciousness, by hand", "MORNING", "07:15", "journal",
                    "Don't reread or edit — it's for clearing, not crafting.", target = 3, unit = "pages"),
                task("Set one intention", "What would make today good?", "MORNING", "07:40", "idea",
                    "Keep it small and specific."),
            )
        ),
        challenge(
            id = "declutter_week",
            title = "Declutter Week",
            subtitle = "One small zone at a time",
            description = "Lighten your space in a week: one 15-minute zone a day, one bag out the door and a quick evening reset.",
            days = 7, category = "Home", icon = "declutter", banner = "banner_declutter_week",
            tip = "If you haven't used it in a year and wouldn't buy it again, let it go.",
            tasks = listOf(
                task("Declutter one zone", "A drawer, a shelf, a corner", "EVENING", "18:30", "declutter",
                    "Set a timer — stopping at 15 minutes keeps it easy.", target = 15, unit = "mins"),
                task("One bag out", "Donate, recycle or bin", "EVENING", "18:50", "clean",
                    "Put it by the door straight away so it actually leaves."),
                task("10-minute evening reset", "Tidy surfaces before bed", "EVENING", "21:00", "clean",
                    "Waking up to a clear space feels great.", target = 10, unit = "mins"),
            )
        ),
        challenge(
            id = "language_30",
            title = "Learn a Language",
            subtitle = "15 minutes a day for 30 days",
            description = "Make steady progress in a new language with daily practice, ten new words and some listening.",
            days = 30, category = "Learning", icon = "language", banner = "banner_language_30",
            tip = "Daily short sessions beat occasional long ones for memory.",
            tasks = listOf(
                task("15 minutes of practice", "App, textbook or tutor", "MORNING", "08:00", "language",
                    "Say things out loud — speaking builds recall.", target = 15, unit = "mins"),
                task("Learn 10 new words", "Make flashcards from real life", "AFTERNOON", "13:30", "study",
                    "Words you'll actually use stick best.", target = 10, unit = "words"),
                task("Listen for 10 minutes", "A podcast, song or show", "EVENING", "19:00", "music",
                    "Listening trains your ear even when you don't understand it all.", target = 10, unit = "mins"),
            )
        ),
        challenge(
            id = "stretch_daily",
            title = "Daily Stretch",
            subtitle = "Loosen up in 14 days",
            description = "Ease stiffness with a short morning stretch and a mobility break during the day.",
            days = 14, category = "Fitness", icon = "yoga", banner = "banner_stretch_daily",
            tip = "Stretch gently — mild tension, never pain.",
            tasks = listOf(
                task("10-minute morning stretch", "Neck, shoulders, hips, hamstrings", "MORNING", "07:10", "yoga",
                    "Breathe out as you ease into each stretch.", target = 10, unit = "mins"),
                task("Mobility break", "Stand, roll your shoulders, walk a minute", "AFTERNOON", "15:00", "walk",
                    "Every hour of sitting deserves a short break."),
            )
        ),
        challenge(
            id = "deep_work",
            title = "Deep Work",
            subtitle = "One distraction-free block a day",
            description = "Do your most important work first with a daily 90-minute focus block, notifications off and a plan for tomorrow.",
            days = 21, category = "Productivity", icon = "focus", banner = "banner_deep_work",
            tip = "Decide the night before what your focus block is for.",
            tasks = listOf(
                task("Notifications off", "Do Not Disturb before you start", "MORNING", "09:25", "phone_off",
                    "Close email and chat tabs too."),
                task("90-minute focus block", "One important task, no switching", "MORNING", "09:30", "focus",
                    "Keep a notepad for stray thoughts instead of acting on them.", target = 90, unit = "mins", calendar = true),
                task("Plan tomorrow's top 3", "Write tomorrow's three priorities", "EVENING", "18:00", "journal",
                    "A plan made the night before removes morning friction."),
            )
        ),
    )

    private val ids = all.map { it.id }.toSet()

    fun isBuiltIn(id: String) = id in ids

    /** Adds new built-ins and refreshes existing ones' content without touching user progress. */
    suspend fun sync(dao: HabitDao) {
        val existing = dao.getAllChallengesList().associateBy { it.id }
        val toWrite = all.map { def ->
            val current = existing[def.id] ?: return@map def
            def.copy(
                isJoined = current.isJoined,
                currentDay = current.currentDay,
                skipsLeft = current.skipsLeft,
                completedTasksToday = current.completedTasksToday,
                goalCurrent = current.goalCurrent,
                startedAt = current.startedAt,
                // Keep a photo the user attached; otherwise use the bundled banner.
                bannerResName = if (BannerStorage.isCustom(current.bannerResName)) current.bannerResName else def.bannerResName,
            )
        }.filter { it != existing[it.id] }
        if (toWrite.isNotEmpty()) dao.insertChallenges(toWrite)
    }
}
