package com.trioscg.androidapp5.activities

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.trioscg.androidapp5.R
import com.trioscg.androidapp5.adapters.VideoAdapter
import com.trioscg.androidapp5.api.RetrofitClient
import com.trioscg.androidapp5.api.YouTubeApiService
import com.trioscg.androidapp5.databinding.ActivityMainBinding
import com.trioscg.androidapp5.models.VideoItem
import com.trioscg.androidapp5.models.VideoResponse
import com.trioscg.androidapp5.utils.PlaylistManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val apiKey = "AIzaSyD01YGdmpOADhEGsXYZbH_07g15tqXxDuY"
    private val playlistName = "playlist"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PlaylistManager.init(applicationContext)
        setupUI()
        fetchTrendingVideos()
    }

    private fun setupUI() = with(binding) {
        videoRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                startSearch()
                true
            } else false
        }

        searchButton.setOnClickListener {
            startSearch()
        }

        viewPlaylistButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, PlaylistActivity::class.java))
        }
    }

    private fun startSearch() {
        val query = binding.searchInput.text.toString().trim()
        if (query.isNotEmpty()) {
            Intent(this, SearchResultsActivity::class.java).also {
                it.putExtra("query", query)
                startActivity(it)
            }
        } else {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchTrendingVideos() {
        val service = RetrofitClient.instance.create(YouTubeApiService::class.java)
        service.getTrendingMusicVideos(apiKey).enqueue(object : Callback<VideoResponse> {
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                if (response.isSuccessful) {
                    val videoList = response.body()?.items ?: emptyList()
                    binding.videoRecyclerView.adapter = VideoAdapter(videoList) { video ->
                        showVideoOptionsDialog(video)
                    }
                } else {
                    showError("Failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                showError("Error: ${t.message}")
            }
        })
    }

    private fun showVideoOptionsDialog(video: VideoItem) {
        val options = arrayOf("Play Video", "Add to Playlist")
        AlertDialog.Builder(this)
            .setTitle(video.snippet.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchVideoDetail(video)
                    1 -> {
                        PlaylistManager.addVideoToPlaylist(video, playlistName)
                        Toast.makeText(this, "Added to playlist", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun launchVideoDetail(video: VideoItem) {
        Intent(this, VideoDetailActivity::class.java).apply {
            putExtra("video_id", video.id)
            putExtra("title", video.snippet.title)
            putExtra("description", video.snippet.description)
            putExtra("channel", video.snippet.channelTitle)
            putExtra("views", video.statistics.viewCount)
        }.also {
            startActivity(it)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
