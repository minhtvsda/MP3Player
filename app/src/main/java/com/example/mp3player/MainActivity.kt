package com.example.mp3player

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mp3player.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val MEDIA_PATH =  Environment.getExternalStorageDirectory().path + "/"
    private var songList = ArrayList<String>()
    private lateinit var adapter: MusicAdapter


    //return res of file part in string type.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.recycleView.layoutManager = LinearLayoutManager(this)


        Log.e("Media path", MEDIA_PATH)

        if (ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1 )
        }   else{
            getAllAudioFile()
        }


        setContentView(binding.root)
    }

    private fun getAllAudioFile() {
        if (MEDIA_PATH != null){
            val mainFile = File(MEDIA_PATH)
            val fileList = mainFile.listFiles()
            for (file in fileList){
                Log.e("Media path", file.toString())
                if (file.isDirectory){
                    scanDirectory(file)
                }   else{
                    val path = file.absolutePath
                    if (path.endsWith(".mp3")){
                        songList.add(path)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
        adapter = MusicAdapter(songList, this@MainActivity)
        binding.recycleView.adapter = adapter
    }

    private fun scanDirectory(directory: File?) {
        if (directory != null){
            val fileList = directory.listFiles()
            for (file in fileList!!){
                Log.e("Media path", file.toString())
                if (file.isDirectory){
                    scanDirectory(file)
                }   else{
                    val path = file.absolutePath
                    if (path.endsWith(".mp3")){
                        songList.add(path)
                    }
                }
            }
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if(requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                getAllAudioFile()
            }
    }
}