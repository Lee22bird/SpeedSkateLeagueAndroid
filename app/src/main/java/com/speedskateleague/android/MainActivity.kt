package com.speedskateleague.android

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.speedskateleague.android.push.MeetReminderScheduler
import com.speedskateleague.android.push.PushBootstrap
import com.speedskateleague.android.ui.auth.LoginScreen
import com.speedskateleague.android.ui.nav.SslNavHost
import com.speedskateleague.android.ui.theme.SpeedSkateLeagueTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* no-op: push simply won't show a banner if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apiClient = (application as SslApplication).apiClient

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (apiClient.isSignedIn()) {
            lifecycleScope.launch {
                PushBootstrap.registerCurrentToken(applicationContext, apiClient)
            }
        }

        setContent {
            SpeedSkateLeagueTheme {
                var isSignedIn by remember { mutableStateOf(apiClient.isSignedIn()) }

                if (isSignedIn) {
                    SslNavHost(onSignOut = {
                        lifecycleScope.launch {
                            PushBootstrap.unregisterCurrentToken(applicationContext, apiClient)
                            MeetReminderScheduler.cancelAllReminders(applicationContext)
                            apiClient.signOut()
                            isSignedIn = false
                        }
                    })
                } else {
                    LoginScreen(onLoggedIn = { isSignedIn = true })
                }
            }
        }
    }
}
