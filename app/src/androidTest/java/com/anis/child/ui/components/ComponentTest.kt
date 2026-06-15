package com.anis.child.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyStateView_displaysMessage() {
        composeTestRule.setContent {
            EmptyStateView(
                icon = Icons.Default.Info,
                message = "No items found"
            )
        }

        composeTestRule.onNodeWithText("No items found").assertIsDisplayed()
    }

    @Test
    fun emptyStateView_displaysLongMessage() {
        composeTestRule.setContent {
            EmptyStateView(
                icon = Icons.Default.Info,
                message = "You have no notifications yet"
            )
        }

        composeTestRule.onNodeWithText("You have no notifications yet").assertIsDisplayed()
    }
}
