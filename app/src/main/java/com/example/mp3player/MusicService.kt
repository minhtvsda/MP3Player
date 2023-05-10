package com.example.mp3player

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.mp3player.MyApplication.Companion.CHANNEL_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MusicService : Service() {
    companion object{
        const val ACTION_PAUSE = 1
        const val ACTION_RESUME = 2
        const val ACTION_PREVIOUS = 3
        const val ACTION_NEXT = 4
        const val ACTION_MUSIC_SERVICE = "action_music_service"
        const val ACTION_SEND_DATA_TO_ACTIVITY = "action send data to activity"
        const val ACTION_MUSIC = "action_music"
    }
    var mediaPlayer: MediaPlayer? = null
    private var filePath =""
    private var title = ""
    private val mBinder = MyBinder()

    inner class MyBinder : Binder(){
        val service: MusicService
        get()  = this@MusicService

    }

    override fun onBind(p0: Intent?): IBinder {
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.e("MusicService", "onRebind")
    }
    override fun onCreate() {
        super.onCreate()
        Log.e("MusicService", "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

            CoroutineScope(Dispatchers.Main).launch {
                // code to run in the Main thread
                println("Hello from thread ${Thread.currentThread().name}")
                val actionMusic = intent?.getIntExtra(ACTION_MUSIC_SERVICE, 0) ?: 0

                if (actionMusic != 0) {
                    handleActionMusic(actionMusic)
                    Log.e("MusicService", "onStartCommand, handle music")

                    return@launch
                }

                filePath = intent?.getStringExtra("filePath") ?: return@launch
                title = intent.getStringExtra("title") ?: return@launch

                mediaPlayer = MediaPlayer()
                mediaPlayer?.setDataSource(filePath)
                mediaPlayer?.prepare()
                mediaPlayer?.start()


                sendNotificationMedia(filePath, title)

                Log.e("MusicService", "onStartCommand")

            }

        return START_REDELIVER_INTENT
    }



    private fun handleActionMusic(action: Int){
        when (action){
            ACTION_PAUSE -> {
                Log.e("MusicService", "action pause handle")
                sendNotificationMedia(filePath, title)
                sendActionToActivity(ACTION_PAUSE)

            }
            ACTION_RESUME -> {

                Log.e("MusicService", "action resume handle")
                sendNotificationMedia(filePath, title) // phai gui lai notification thi no moi update lai view
                sendActionToActivity(ACTION_RESUME)

            }
            ACTION_PREVIOUS -> {
                sendNotificationMedia(filePath, title) // phai gui lai notification thi no moi update lai view
                sendActionToActivity(ACTION_PREVIOUS)
            }
            ACTION_NEXT -> {
                sendNotificationMedia(filePath, title) // phai gui lai notification thi no moi update lai view
                sendActionToActivity(ACTION_NEXT)
            }

        }
    }

    fun sendNotificationMedia(filePath: String, title: String) {

        val mediaSessionCompat = MediaSessionCompat(this, "tag")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_music_note_24)
            .setSubText("Mp3 Player")
            .setContentTitle(title)
            .setContentText(title)
        //add button
            .addAction(R.drawable.baseline_skip_previous_24, "Previous", getPendingIntent(this, ACTION_PREVIOUS))
            .addAction(if (mediaPlayer!!.isPlaying) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24,
        "Pause", getPendingIntent(this,if (mediaPlayer!!.isPlaying) ACTION_PAUSE else ACTION_RESUME))
            .addAction(R.drawable.baseline_skip_next_24, "Next", getPendingIntent(this@MusicService, ACTION_NEXT))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0,1,2)
                .setMediaSession(mediaSessionCompat.sessionToken))


    startForeground(1, notification.build())

    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun getPendingIntent(context: Context, action: Int) : PendingIntent{
        //use broadcast receiver de nhan data tu intent
        val intent = Intent(this, MyReceiver::class.java)
        // chuyen action sang my receiver

        intent.putExtra(ACTION_MUSIC, action)

        return PendingIntent.getBroadcast(context.applicationContext, action, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.e("MusicService", "onDestroy")
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun sendActionToActivity(action: Int){
        val intent = Intent(ACTION_SEND_DATA_TO_ACTIVITY)
        val bundle = Bundle()
        bundle.putString("title", filePath)
        bundle.putBoolean("status_player", mediaPlayer!!.isPlaying)
        bundle.putInt("action_music", action)

        intent.putExtras(bundle)

        //su dung broadcast receiver trong localBroadcastmanager

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

    }
}