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
        AlarmScheduler.createStatusNotificationChannel(this)

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

    override fun onResume() {
        super.onResume()
        if (AlarmSoundService.isRinging) {
            val intent = android.content.Intent(this, AlarmActivity::class.java).apply {
                putExtra("ALARM_ID", AlarmSoundService.currentAlarmId)
                putExtra("ALARM_LABEL", AlarmSoundService.currentAlarmLabel)
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }
        AlarmScheduler.updateActiveAlarmIndicator(this)
    }
}
