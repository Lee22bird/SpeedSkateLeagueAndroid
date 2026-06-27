package com.speedskateleague.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.speedskateleague.android.ui.auth.LoginScreen
import com.speedskateleague.android.ui.nav.SslNavHost
import com.speedskateleague.android.ui.theme.SpeedSkateLeagueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val apiClient = (application as SslApplication).apiClient

        setContent {
            SpeedSkateLeagueTheme {
                var isSignedIn by remember { mutableStateOf(apiClient.isSignedIn()) }

                if (isSignedIn) {
                    SslNavHost(onSignOut = {
                        apiClient.signOut()
                        isSignedIn = false
                    })
                } else {
                    LoginScreen(onLoggedIn = { isSignedIn = true })
                }
            }
        }
    }
}
