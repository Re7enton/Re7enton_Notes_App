package com.example.re7entonnotesapp.presentation.ui

import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
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
                title = { Text(text = stringResource(R.string.my_notes)) },
                actions = {
                    // only show Sync on API 34+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        IconButton(
                            onClick = onSync,
                            enabled = driveAuthorized
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
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                    // account / sign-in
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