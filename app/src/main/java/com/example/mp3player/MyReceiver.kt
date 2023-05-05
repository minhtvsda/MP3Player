package com.example.mp3player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mp3player.MusicService.Companion.ACTION_MUSIC

class MyReceiver : BroadcastReceiver() {


    override fun onReceive(p0: Context?, p1: Intent?) {
        //  nhan du lieu tu intent
        val actionMusic = p1!!.getIntExtra(ACTION_MUSIC, 0)
        //truyen action nguoc lai service de xu ly
        val intentService = Intent(p0!!, MusicService::class.java)
        intentService.putExtra(MusicService.ACTION_MUSIC_SERVICE, actionMusic)

        p0.startService(intentService)

    }
}