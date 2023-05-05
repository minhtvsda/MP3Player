package com.example.mp3player

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApplication : Application() {
    companion object{
        const val CHANNEL_ID = "channel_service"
    }

    override fun onCreate() {
        super.onCreate()

        createChannelNotification()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun createChannelNotification() = GlobalScope.launch {
        val channel = NotificationChannel(CHANNEL_ID, "Channel", NotificationManager.IMPORTANCE_HIGH)
        channel.setSound(null, null)        //mute sound for api >25
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        Log.e("MusicService", "onCreatechannel")

    }

}