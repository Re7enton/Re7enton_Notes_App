package com.example.re7entonnotesapp.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*                                      // TopAppBar, Scaffold, MaterialTheme…
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle             // sign-in icon
import androidx.compose.material.icons.filled.Refresh                   // use Refresh for “Sync”
import androidx.compose.material.icons.automirrored.filled.ExitToApp   // non-deprecated ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.re7entonnotesapp.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    accountEmail: String?,
    driveAuthorized: Boolean,
    onSync: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My Notes") },
                actions = {
                    // Sync button
                    IconButton(
                        onClick = onSync,
                        enabled = driveAuthorized        // ← only tappable once authorized
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.sync)
                        )
                    }
                    if (!driveAuthorized) {
                        Text(
                            text = stringResource(R.string.needs_drive_permission),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    // Signed-in state
                    if (accountEmail != null) {
                        Text(
                            text = accountEmail,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = onSignOut) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = stringResource(R.string.sign_out)
                            )
                        }
                    } else {
                        IconButton(onClick = onSignIn) {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = stringResource(R.string.sign_in)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}