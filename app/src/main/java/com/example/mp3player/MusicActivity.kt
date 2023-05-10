package com.example.mp3player

import android.content.*
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.mp3player.databinding.ActivityMusicBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class MusicActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMusicBinding
    private var title : String? = null
    private var filePath : String? = null
    private var position : Int = 0
    private lateinit var list : ArrayList<String>
    private var runnable : Runnable? = null
    private var handler : Handler? = null
    private var totalTime : Int = 0
    private lateinit var animation: Animation
    var mediaPlayer: MediaPlayer? = null
    lateinit var musicService: MusicService
    private lateinit var serviceConnection: ServiceConnection
    var isServiceConnected = false
    var isPlaying = false
    private lateinit var intentService: Intent


    private val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val bundle = p1?.extras ?: return
            isPlaying = bundle.getBoolean("status_player")
            val  action = bundle.getInt("action_music")

            handleLayoutMusic(action)
        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)


        animation = AnimationUtils.loadAnimation(this, R.anim.translate_animation)

        binding.textViewFileNameMusic.animation = animation
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter(MusicService.ACTION_SEND_DATA_TO_ACTIVITY))

        title = intent.getStringExtra("title")
        filePath = intent.getStringExtra("filePath")
        position = intent.getIntExtra("position", 0)
        list = intent.getStringArrayListExtra("list")!!

        intentService = Intent(this, MusicService::class.java)
        intentService.putExtra("title", title)
        intentService.putExtra("filePath", filePath)
        intentService.putExtra("position", position)
        intentService.putExtra("list", list)

        CoroutineScope(Dispatchers.Default).launch {

            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                    val musicBinder = p1 as MusicService.MyBinder
                    musicService = musicBinder.service
                    Log.e("Service connection", "from Service Connected")
                    if (musicService.mediaPlayer == null){
                        Toast.makeText(this@MusicActivity, "null mediaplayer", Toast.LENGTH_LONG).show()
                        return
                    } else
                    {
                        mediaPlayer = musicService.mediaPlayer!!

                        handler = Handler()
                        runnable = Runnable {
                            if (mediaPlayer == null){
                                Toast.makeText(this@MusicActivity, "Media Player null", Toast.LENGTH_SHORT).show()
                                return@Runnable
                            } else {
                                totalTime = mediaPlayer?.duration ?: 20
                                binding.musicSeekBar.max = totalTime
                                val currentPosition = mediaPlayer!!.currentPosition
                                binding.musicSeekBar.progress = currentPosition
                                handler!!.postDelayed(runnable!!, 1000)

                                val elapsedTime = createTimeLable(currentPosition)
                                val lastTime = createTimeLable(totalTime)
                                binding.textViewProgess.text = elapsedTime
                                binding.textViewTotalTime.text = lastTime
                                mediaPlayer?.setOnCompletionListener {
                                    nextSong()
                                }
                            }
                        }
                        handler?.post(runnable!!)
                    }

                    isServiceConnected = true


                }

                override fun onServiceDisconnected(p0: ComponentName?) {
//                musicService = null
                    isServiceConnected = false
                }
            }
        }



        //foreground and bound



        binding.textViewFileNameMusic.text = title

        binding.buttonPlayPause.setOnClickListener {
            pauseOrPlaySong()
        }

        binding.buttonPrevious.setOnClickListener {
            previousSong()

        }
        binding.buttonNext.setOnClickListener {
            nextSong()
        }



        binding.volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, fromUser: Boolean) {
                if (fromUser) // meaning user modify seekbar
                {
                    binding.volumeSeekBar.progress = p1
                    val volumeLevel = p1/100f
                    mediaPlayer?.setVolume(volumeLevel, volumeLevel) //value for both left and right
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
        binding.musicSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, fromUser: Boolean) {
                if (fromUser) // meaning user modify seekbar
                {
                    mediaPlayer?.seekTo(p1)
                    binding.musicSeekBar.progress = p1
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
        startService(intentService)
        bindService(intentService, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun pauseOrPlaySong() {
        if (mediaPlayer!!.isPlaying){
            mediaPlayer?.pause()
            binding.buttonPlayPause.setBackgroundResource(R.drawable.baseline_play_arrow_24)
            musicService.sendNotificationMedia(filePath!!, title!!)
        }else{
            mediaPlayer?.start()
            binding.buttonPlayPause.setBackgroundResource(R.drawable.baseline_pause_24)
            musicService.sendNotificationMedia(filePath!!, title!!)
        }
    }

    private fun previousSong(){

        handler!!.removeCallbacks(runnable!!)

        mediaPlayer?.reset() // if not reset app will crash
        position = if (position == 0) list.size - 1 else position - 1

        filePath = list[position]


        stopService(intentService)
        unbindService(serviceConnection)

        intentService = Intent(this, MusicService::class.java)
        title = filePath?.substring(filePath!!.lastIndexOf("/")+ 1)

        intentService.putExtra("title", title)
        intentService.putExtra("filePath", filePath)
        intentService.putExtra("position", position)
        intentService.putExtra("list", list)

        startService(intentService)

        bindService(intentService, serviceConnection, Context.BIND_AUTO_CREATE)

        binding.textViewFileNameMusic.clearAnimation()
        binding.textViewFileNameMusic.startAnimation(animation)
        binding.buttonPlayPause.setBackgroundResource(R.drawable.baseline_pause_24)
        binding.textViewFileNameMusic.text = title

    }
    fun nextSong(){
        handler!!.removeCallbacks(runnable!!)

        position = if (list.size - 1 == position) 0 else position + 1

        filePath = list[position]


        stopService(intentService)
        unbindService(serviceConnection)

        intentService = Intent(this, MusicService::class.java)
        title = filePath?.substring(filePath!!.lastIndexOf("/")+ 1)

        intentService.putExtra("title", title)
        intentService.putExtra("filePath", filePath)
        intentService.putExtra("position", position)
        intentService.putExtra("list", list)

        startService(intentService)

        bindService(intentService, serviceConnection, Context.BIND_AUTO_CREATE)

        binding.textViewFileNameMusic.clearAnimation()
        binding.textViewFileNameMusic.startAnimation(animation)
        binding.buttonPlayPause.setBackgroundResource(R.drawable.baseline_pause_24)
        binding.textViewFileNameMusic.text = title

    }
    fun createTimeLable(currentPosition: Int) : String{

        val minute = currentPosition /1000 /60
        val second = currentPosition /1000 % 60

        return if (second < 10) "$minute:0$second" else "$minute:$second"
    }


    private fun handleLayoutMusic(action: Int) {
        when (action){
            MusicService.ACTION_PAUSE -> {
                pauseOrPlaySong()
                Log.e("handleLayoutMusic", "Action pause")

            }
            MusicService.ACTION_RESUME -> {
                pauseOrPlaySong()
            }
            MusicService.ACTION_PREVIOUS -> {
                previousSong()
            }
            MusicService.ACTION_NEXT -> {
                nextSong()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacks(runnable!!)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        stopService(Intent(this, MusicService::class.java))
        if (isServiceConnected){
            unbindService(serviceConnection)
            isServiceConnected = false
        }
        // remember to unregister the broadcast
    }
}