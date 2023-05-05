package com.example.mp3player

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mp3player.databinding.CardMusicBinding

class MusicAdapter (var list: ArrayList<String>, var mContext: Context ) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>(){

    inner class MusicViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding : CardMusicBinding
        init {
            binding = CardMusicBinding.bind(itemView)
        }
        fun bindData(filePath : String, position: Int){
            val title = filePath.substring(filePath.lastIndexOf("/")+ 1)
            binding.textViewFileName.text = title
            binding.cardView.setOnClickListener{
                val intent = Intent(mContext, MusicActivity::class.java)
                intent.putExtra("title", title)
                intent.putExtra("filePath", filePath)
                intent.putExtra("position", position)
                intent.putExtra("list", list)

                mContext.startActivity(intent)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_music, parent, false)
        return MusicViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bindData(list[position], position)
    }

}