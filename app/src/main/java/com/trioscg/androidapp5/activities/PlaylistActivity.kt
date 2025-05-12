package com.trioscg.androidapp5.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.trioscg.androidapp5.R
import com.trioscg.androidapp5.adapters.VideoAdapter
import com.trioscg.androidapp5.databinding.ActivityPlaylistBinding
import com.trioscg.androidapp5.utils.PlaylistManager

class PlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding
    private val playlistName = "playlist"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPlaylist()
        setupPlayAllButton()
    }

    private fun initPlaylist() {
        val playlist = PlaylistManager.getPlaylist(playlistName)

        if (playlist?.videos.isNullOrEmpty()) {
            Toast.makeText(this, "Playlist is empty", Toast.LENGTH_SHORT).show()
            return
        }

        with(binding.playlistRecyclerView) {
            layoutManager = LinearLayoutManager(this@PlaylistActivity)
            adapter = VideoAdapter(playlist!!.videos) { video ->
                launchVideoDetail(video.id, video.snippet.title, video.snippet.description, video.snippet.channelTitle, video.statistics.viewCount)
            }
        }
    }

    private fun setupPlayAllButton() {
        binding.playAllButton.setOnClickListener {
            Intent(this, PlaylistPlayerActivity::class.java).apply {
                putExtra("playlist_name", playlistName)
            }.also {
                startActivity(it)
            }
        }
    }

    private fun launchVideoDetail(videoId: String, title: String, description: String, channel: String, views: String) {
        Intent(this, VideoDetailActivity::class.java).apply {
            putExtra("video_id", videoId)
            putExtra("title", title)
            putExtra("description", description)
            putExtra("channel", channel)
            putExtra("views", views)
        }.also {
            startActivity(it)
        }
    }
}
