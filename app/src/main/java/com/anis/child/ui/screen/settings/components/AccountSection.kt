package com.anis.child.ui.screen.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anis.child.R
import androidx.compose.ui.res.stringResource
import com.anis.child.ui.theme.LocalAppColors

@Composable
fun AccountSection(
    onGetMeClick: () -> Unit,
    isFetchingChild: Boolean,
    onChangePin: () -> Unit,
    onLogout: () -> Unit,
) {
    val appColors = LocalAppColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = appColors.darkSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SectionHeader(stringResource(R.string.account_security_section))

            ActionButton(
                icon = Icons.Default.Person,
                label = stringResource(R.string.get_child_info),
                isLoading = isFetchingChild,
                loadingLabel = stringResource(R.string.fetching),
                onClick = onGetMeClick
            )

            SettingsNavRow(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.change_pin),
                description = stringResource(R.string.change_pin_desc),
                onClick = onChangePin
            )

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.error500
                )
            ) {
                Text(
                    text = stringResource(R.string.logout),
                    color = appColors.darkTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
