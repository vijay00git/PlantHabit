package com.planthabit

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.planthabit.data.repository.ChallengeJsonManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sprout", appName)
    }

    @Test
    fun testChallengeTemplateParsing() {
        val templateJson = ChallengeJsonManager.getExampleTemplateJson()
        assertNotNull(templateJson)

        val result = ChallengeJsonManager.parseChallengeJson(templateJson)
        assertTrue("Template JSON should parse successfully", result.isSuccess)

        val (challenge, habits) = result.getOrThrow()
        assertEquals("Mindful Productivity Sprint", challenge.title)
        assertEquals(14, challenge.durationDays)
        assertEquals(3, habits.size)
        assertEquals("Morning Meditation & Intention", habits[0].title)
    }

    @Test
    fun testInvalidChallengeJson() {
        val invalidJson = "{ invalid json content }"
        val result = ChallengeJsonManager.parseChallengeJson(invalidJson)
        assertTrue("Malformed JSON should fail gracefully", result.isFailure)
    }

    @Test
    fun testChallengeParsedTasks() {
        val challenge = com.planthabit.data.model.ChallengeEntity(
            id = "test_challenge",
            title = "Sleep Sprint",
            subtitle = "Rest better",
            category = "Health",
            durationDays = 7,
            totalTasks = 2,
            tasksJson = """[{"title":"No screens after 10pm","scheduledTime":"22:00","timeOfDay":"EVENING","reminderEnabled":true,"tip":"Reduces blue light exposure"}]"""
        )
        val tasks = challenge.getParsedTasks()
        assertEquals(1, tasks.size)
        assertEquals("No screens after 10pm", tasks[0].title)
        assertEquals("22:00", tasks[0].scheduledTime)
        assertEquals("EVENING", tasks[0].timeOfDay)
        assertTrue(tasks[0].reminderEnabled)
    }
}
