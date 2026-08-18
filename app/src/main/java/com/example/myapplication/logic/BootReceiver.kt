package com.example.myapplication.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.data.SafetyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val repository = SafetyRepository(context.applicationContext)
            CoroutineScope(Dispatchers.IO).launch {
                val settings = repository.getSettings().first()
                if (settings.shakeDetection) {
                    ShakeTriggerService.start(context)
                }
            }
        }
    }
}
