package com.example.simpledemo

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class ExampleUnitTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    @Test
    fun testApp() {
        composeTestRule.onNodeWithTag("ResultText").assertTextEquals("")
        composeTestRule.onNode(hasText("Click me")).performClick()

        // Robolectric needs help to start the next activity.
        // Let's get the intent for SecondActivity, that MainActivity launched:
        val shadowMainActivity = Shadows.shadowOf(composeTestRule.activity)
        val nextActivityIntent = shadowMainActivity.nextStartedActivity

        // Now let's use the scenario api to launch the SecondActivity, with this intent:
        var nextActivity: SecondActivity? = null
        val nextActivityScenario = ActivityScenario.launch<SecondActivity>(nextActivityIntent)
        nextActivityScenario.onActivity { activity ->
            // Keep a copy of SecondActivity for later
            nextActivity = activity
        }
        composeTestRule.onNodeWithTag("TextInput").performTextInput("test text")
        composeTestRule.onNode(hasText("Close")).performClick()

        // The user exited SecondActivity, and it set a result.
        // Robolectric needs help to pass this result to MainActivity:
        val shadowNextActivity = Shadows.shadowOf(nextActivity)
        shadowMainActivity.receiveResult(nextActivityIntent, shadowNextActivity.resultCode, shadowNextActivity.resultIntent)

        // Now we can continue our assertions in MainActivity
        composeTestRule.onNodeWithTag("ResultText").assertTextEquals("test text")

    }
}