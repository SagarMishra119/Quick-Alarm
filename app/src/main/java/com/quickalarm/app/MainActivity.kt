package com.quickalarm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.quickalarm.app.ui.screens.MainScreen
import com.quickalarm.app.ui.theme.AppTheme
import com.quickalarm.app.ui.theme.QuickAlarmTheme
import com.quickalarm.app.util.AlarmScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channel on app start
        AlarmScheduler.createNotificationChannel(this)

        setContent {
            QuickAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppTheme.colors.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}
