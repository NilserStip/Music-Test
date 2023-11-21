package com.nilserllamo.musictest.presentation

import android.annotation.SuppressLint
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.nilserllamo.musictest.R
import com.nilserllamo.musictest.databinding.ActivityMainBinding
import com.nilserllamo.musictest.presentation.model.Song
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), SongAdapter.OnItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private var mediaPlayer: MediaPlayer? = null
    private var adapter = SongAdapter(this)
    private var isPlay = false
    private val songs: ArrayList<Song> = ArrayList()
    private var currentSong: Song? = null
    private var oTime = 0
    private val handle: Handler = Handler(Looper.getMainLooper())
    private val updateSongTime: Runnable = object : Runnable {
        @SuppressLint("DefaultLocale")
        override fun run() {
            val start = mediaPlayer?.currentPosition!!
            binding.textviewTimeStart.text = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(start.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(start.toLong()) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(start.toLong())
                )
            )
            binding.seekBar.progress = start
            handle.postDelayed(this, 100)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT

        loadRecyclerView()
        loadData()
        setupActions()
    }

    private fun loadData() {
        val names = resources.getStringArray(R.array.names_songs)
        val titles = resources.getStringArray(R.array.titles_songs)

        names.forEachIndexed { index, s ->
            songs.add(Song(s, titles[index]))
        }

        adapter.setList(songs)
    }

    private fun setupActions() {
        binding.apply {
            imageviewAction.setOnClickListener {
                isPlay = if (isPlay) {
                    imageviewAction.setImageResource(R.drawable.ic_play)
                    mediaPlayer?.pause()
                    false
                } else {
                    imageviewAction.setImageResource(R.drawable.ic_pause)
                    mediaPlayer?.start()
                    true
                }
            }

            imageviewPrevious.setOnClickListener { previousAndNextSong(false) }
            imageviewNext.setOnClickListener { previousAndNextSong(true) }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun getId(name: String): Int {
        val res: Resources = resources
        return res.getIdentifier(name, "raw", packageName)
    }

    @SuppressLint("DiscouragedApi")
    private fun getDrawable(name: String): Int {
        val res: Resources = resources
        return res.getIdentifier(name, "drawable", packageName)
    }

    private fun loadRecyclerView() {
        binding.recyclerSongs.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerSongs.adapter = adapter
    }

    override fun onItemClick(song: Song) {

        val value = loadMediaPlayer(song)
        updateUI(song, value)
    }

    private fun updateUI(song: Song, value: Boolean) {
        binding.apply {
            Glide.with(baseContext).load(getDrawable(song.name)).centerCrop()
                .placeholder(R.drawable.loading).into(imageviewSong)
            textviewTitleSong.text = song.title

            val start = mediaPlayer?.currentPosition?.toLong()!!
            val end = mediaPlayer?.duration?.toLong()!!

            if (oTime == 0) {
                binding.seekBar.max = end.toInt()
                oTime = 1;
            }

            textviewTimeStart.text =
                String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(start),
                    TimeUnit.MILLISECONDS.toSeconds(start) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(start)
                    )
                )
            textviewTimeEnd.text =
                String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(end),
                    TimeUnit.MILLISECONDS.toSeconds(end) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(end)
                    )
                )
            seekBar.progress = start.toInt()
            handle.postDelayed(updateSongTime, 100);

            constraintTop.visibility = if (value) View.VISIBLE else View.GONE

        }
    }

    private fun loadMediaPlayer(song: Song): Boolean {
        return try {
            if (mediaPlayer != null) {
                mediaPlayer?.stop()
            }
            currentSong = song
            mediaPlayer = MediaPlayer.create(this, getId(song.name))
            mediaPlayer?.setOnCompletionListener { previousAndNextSong(true) }
            mediaPlayer?.seekTo(0)
            mediaPlayer?.start()
            isPlay = true
            true
        } catch (e: NotFoundException) {
            Toast.makeText(this, getString(R.string.file_failed), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            binding.imageviewAction.setImageResource(R.drawable.ic_pause)
            isPlay = false
            false
        }
    }

    private fun previousAndNextSong(isNext: Boolean) {
        var index = songs.indexOf(currentSong) + if (isNext) +1 else -1

        if (index == -1 || index == songs.size + 1)
            index = 0

        val song = songs[index]
        adapter.updateSelected(index)
        val value = loadMediaPlayer(song)
        updateUI(song, value)
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlay = false
    }

}