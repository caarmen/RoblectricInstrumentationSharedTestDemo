package com.example.simpledemo

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testApp() {
        composeTestRule.onNodeWithTag("ResultText").assertTextEquals("")
        composeTestRule.onNode(hasText("Click me")).performClick()
        composeTestRule.onNodeWithTag("TextInput").performTextInput("test text")
        composeTestRule.onNode(hasText("Close")).performClick()
        composeTestRule.onNodeWithTag("ResultText").assertTextEquals("test text")
    }
}