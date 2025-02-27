package com.example.memorease.adapters

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memorease.R
import com.example.memorease.models.Memory
import java.text.SimpleDateFormat
import java.util.*

class MemoryAdapter(
    private val context: Context,
    private val memories: List<Memory>
) : RecyclerView.Adapter<MemoryAdapter.MemoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_memory, parent, false)
        return MemoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val memory = memories[position]
        holder.bind(memory)
    }

    override fun getItemCount(): Int = memories.size

    inner class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textDescription)
        private val uploadedByTextView: TextView = itemView.findViewById(R.id.textUploadedBy)
        private val dateTextView: TextView = itemView.findViewById(R.id.textDate)
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewMemory)
        private val playButton: ImageView = itemView.findViewById(R.id.playButton)

        private var mediaPlayer: MediaPlayer? = null

        fun bind(memory: Memory) {
            descriptionTextView.text = memory.description
            uploadedByTextView.text = "Uploaded by ${memory.uploadedBy}"
            dateTextView.text = memory.timestamp?.toDate()?.let { formatDate(it) } ?: ""

            imageView.visibility = View.GONE
            playButton.visibility = View.GONE

            when (memory.type) {
                "image" -> {
                    imageView.visibility = View.VISIBLE
                    memory.url?.let { url ->
                        Glide.with(context)
                            .load(url)
                            .into(imageView)
                    }
                }
                "voice" -> {
                    playButton.visibility = View.VISIBLE
                    playButton.setOnClickListener {
                        memory.url?.let { url ->
                            playAudio(url)
                        }
                    }
                }
                "text" -> {
                    // Text memories are only displayed in the descriptionTextView
                }
            }
        }

        private fun formatDate(date: Date): String {
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            return sdf.format(date)
        }

        private fun playAudio(audioUrl: String) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(audioUrl))
                prepare()
                start()
                Toast.makeText(context, "Playing audio...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
