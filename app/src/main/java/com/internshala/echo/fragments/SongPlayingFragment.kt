package com.internshala.echo.fragments


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.internshala.echo.CurrentSongHelper
import com.internshala.echo.R
import com.internshala.echo.Songs
import com.internshala.echo.databases.EchoDatabase
import com.internshala.echo.fragments.SongPlayingFragment.Staticated.onSongComplete
import com.internshala.echo.fragments.SongPlayingFragment.Staticated.playNext
import com.internshala.echo.fragments.SongPlayingFragment.Staticated.processInformation
import com.internshala.echo.fragments.SongPlayingFragment.Staticated.updateTextViews
import com.internshala.echo.fragments.SongPlayingFragment.Statified.MY_PREFS_NAME
import com.internshala.echo.fragments.SongPlayingFragment.Statified.audioVisualization
import com.internshala.echo.fragments.SongPlayingFragment.Statified.currentPosition
import com.internshala.echo.fragments.SongPlayingFragment.Statified.currentSongHelper
import com.internshala.echo.fragments.SongPlayingFragment.Statified.endTimeText
import com.internshala.echo.fragments.SongPlayingFragment.Statified.fab
import com.internshala.echo.fragments.SongPlayingFragment.Statified.favoriteContent
import com.internshala.echo.fragments.SongPlayingFragment.Statified.fetchSongs
import com.internshala.echo.fragments.SongPlayingFragment.Statified.glView
import com.internshala.echo.fragments.SongPlayingFragment.Statified.loopImageButton
import com.internshala.echo.fragments.SongPlayingFragment.Statified.mSensorListener
import com.internshala.echo.fragments.SongPlayingFragment.Statified.mSensorManager
import com.internshala.echo.fragments.SongPlayingFragment.Statified.mediaplayer
import com.internshala.echo.fragments.SongPlayingFragment.Statified.myActivity
import com.internshala.echo.fragments.SongPlayingFragment.Statified.nextImageButton
import com.internshala.echo.fragments.SongPlayingFragment.Statified.playpauseImageButton
import com.internshala.echo.fragments.SongPlayingFragment.Statified.previousImageButton
import com.internshala.echo.fragments.SongPlayingFragment.Statified.seekbar
import com.internshala.echo.fragments.SongPlayingFragment.Statified.shuffleImageButton
import com.internshala.echo.fragments.SongPlayingFragment.Statified.songArtistView
import com.internshala.echo.fragments.SongPlayingFragment.Statified.songTitleView
import com.internshala.echo.fragments.SongPlayingFragment.Statified.startTimeText
import com.internshala.echo.fragments.SongPlayingFragment.Statified.updateSongTime
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class SongPlayingFragment : Fragment() {

    object Statified {
        var myActivity: Activity? = null
        var mediaplayer: MediaPlayer? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playpauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var seekbar: SeekBar? = null
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null
        var shuffleImageButton: ImageButton? = null

        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>? = null
        var currentSongHelper: CurrentSongHelper? = null
        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null

        var fab: ImageButton? = null

        var favoriteContent: EchoDatabase? = null
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null

        var MY_PREFS_NAME = "ShakeFeature"

        var updateSongTime = object : Runnable {
            override fun run() {
                val getcurrent = mediaplayer?.currentPosition
                startTimeText?.setText(
                    String.format(
                        "%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(getcurrent?.toLong() as Long),
                        TimeUnit.MILLISECONDS.toSeconds(getcurrent?.toLong() as Long) -
                                TimeUnit.MILLISECONDS.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                        getcurrent?.toLong() as Long
                                    )
                                )
                    )
                )
                if (getcurrent != null) {
                    seekbar?.setProgress(getcurrent.toInt())
                }
                Handler().postDelayed(this, 1000)

            }
        }


    }

    object Staticated {
        var MY_PREFS_SHUFFLE = "Shuffle Feature"
        var MY_PREFS_LOOP = "Loop Feature"

        fun onSongComplete() {
            if (currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
                currentSongHelper?.isPlaying = true
            } else {
                if (currentSongHelper?.isLoop as Boolean) {
                    currentSongHelper?.isPlaying = true
                    var nextSong = fetchSongs?.get(currentPosition)

                    currentSongHelper?.songTitle = nextSong?.songTitle
                    currentSongHelper?.songPath = nextSong?.songData
                    currentSongHelper?.currentPosition = currentPosition
                    currentSongHelper?.songId = nextSong?.songID as Long
                    updateTextViews(
                        currentSongHelper?.songTitle as String,
                        currentSongHelper?.songArtist as String
                    )
                    mediaplayer?.reset()
                    try {
                        mediaplayer?.setDataSource(
                            myActivity,
                            Uri.parse(currentSongHelper?.songPath)
                        )
                        mediaplayer?.prepare()
                        mediaplayer?.start()
                        processInformation(mediaplayer as MediaPlayer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    playNext("PlayNextNormal")
                    currentSongHelper?.isPlaying = true
                }
            }
            if (favoriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        myActivity!!,
                        R.drawable.favorite_on
                    )
                )
            } else {
                fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        myActivity!!,
                        R.drawable.favorite_off
                    )
                )
            }
        }

        fun updateTextViews(songTitle: String, songArtist: String) {
            var songTitleUpdated = songTitle
            var songArtistUpdated = songArtist
            if (songTitle.equals("<unknown>",true)){
                songTitleUpdated = "Unknown"
            }
            if (songArtist.equals("<unknown>",true)){
                songArtistUpdated = "Unknown"
            }
            songTitleView?.setText(songTitleUpdated)
            songArtistView?.setText(songArtistUpdated)
        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            seekbar?.max = finalTime
            startTimeText?.setText(
                String.format(
                    "%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(startTime?.toLong() as Long) -
                            TimeUnit.MILLISECONDS.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    startTime?.toLong() as Long
                                )
                            )
                )
            )
            endTimeText?.setText(
                String.format(
                    "%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(finalTime?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime?.toLong() as Long) -
                            TimeUnit.MILLISECONDS.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    finalTime?.toLong() as Long
                                )
                            )
                )
            )
            seekbar?.setProgress(startTime)
            Handler().postDelayed(updateSongTime, 1000)
        }


        fun playNext(check: String) {
            if (check.equals("PlayNextNormal", true)) {
                currentPosition = currentPosition + 1
            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(fetchSongs?.size?.plus(1) as Int)
                currentPosition = randomPosition
            }
            if (currentPosition == fetchSongs?.size) {
                currentPosition = 0
            }
            currentSongHelper?.isLoop = false
            var nextSong = fetchSongs?.get(currentPosition)
            currentSongHelper?.songPath = nextSong?.songData
            currentSongHelper?.songTitle = nextSong?.songTitle
            currentSongHelper?.songId = nextSong?.songID as Long
            currentSongHelper?.currentPosition = currentPosition
            updateTextViews(
                currentSongHelper?.songTitle as String,
                currentSongHelper?.songArtist as String
            )
            mediaplayer?.reset()
            try {
                mediaplayer?.setDataSource(myActivity, Uri.parse(currentSongHelper?.songPath))
                mediaplayer?.prepare()
                mediaplayer?.start()
                processInformation(mediaplayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (favoriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        myActivity!!,
                        R.drawable.favorite_on
                    )
                )
            } else {
                fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        myActivity!!,
                        R.drawable.favorite_off
                    )
                )
            }
        }
    }

    var mAccelaration: Float = 0f
    var mAccelarationCurrent: Float = 0f
    var mAccelarationLast: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        setHasOptionsMenu(true)
        activity?.title  = "Now Playin"
        seekbar = view?.findViewById(R.id.seekBar)
        startTimeText = view?.findViewById(R.id.startTime)
        endTimeText = view?.findViewById(R.id.endTime)
        playpauseImageButton = view?.findViewById(R.id.playPauseButton)
        nextImageButton = view?.findViewById(R.id.nextButton)
        previousImageButton = view?.findViewById(R.id.previousButton)
        loopImageButton = view?.findViewById(R.id.loopButton)
        shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        songArtistView = view?.findViewById(R.id.songArtist)
        songTitleView = view?.findViewById(R.id.songTitle)
        glView = view?.findViewById(R.id.visualizer_view)
        fab = view?.findViewById(R.id.favoriteIcon)
        fab?.alpha = 0.8f

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioVisualization = glView as AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onResume() {
        audioVisualization?.onResume()
        super.onResume()
        mSensorManager?.registerListener(
            mSensorListener,
            mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        audioVisualization?.onPause()
        super.onPause()
        mSensorManager?.unregisterListener(mSensorListener)
    }

    override fun onDestroyView() {
        audioVisualization?.release()
        super.onDestroyView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSensorManager = myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelaration = 0.0f
        mAccelarationCurrent = SensorManager.GRAVITY_EARTH
        mAccelarationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_redirect -> {
                myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        favoriteContent = EchoDatabase(myActivity)
        currentSongHelper = CurrentSongHelper()
        currentSongHelper?.isPlaying = true
        currentSongHelper?.isLoop = false
        currentSongHelper?.isShuffle = false

        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long = 0
        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("songId")!!.toLong()
            currentPosition = arguments?.getInt("songPosition")!!
            fetchSongs = arguments?.getParcelableArrayList("songData")

            currentSongHelper?.songPath = path
            currentSongHelper?.songTitle = _songTitle
            currentSongHelper?.songArtist = _songArtist
            currentSongHelper?.songId = songId
            currentSongHelper?.currentPosition = currentPosition

            updateTextViews(
                currentSongHelper?.songTitle as String,
                currentSongHelper?.songArtist as String
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }

        var fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        if (fromFavBottomBar != null) {
            Statified.mediaplayer = FavoriteFragment.Statified.mediaPlayer
        } else {
            mediaplayer = MediaPlayer()
            mediaplayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                mediaplayer?.setDataSource(myActivity as Context, Uri.parse(path))
                mediaplayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mediaplayer?.start()
        }
        processInformation(mediaplayer as MediaPlayer)
        if (currentSongHelper?.isPlaying as Boolean) {
            playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        mediaplayer?.setOnCompletionListener {
            onSongComplete()
        }
        clickHandler()
        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(myActivity as Context, 0)
        audioVisualization?.linkTo(visualizationHandler)

        var prefsForShuffle =
            myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            currentSongHelper?.isShuffle = true
            currentSongHelper?.isLoop = false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            currentSongHelper?.isShuffle = false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop =
            myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            currentSongHelper?.isShuffle = false
            currentSongHelper?.isLoop = true
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        } else {
            currentSongHelper?.isLoop = false
            shuffleImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }

        if (favoriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_on))
        } else {
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_off))
        }
    }

    fun clickHandler() {

        fab?.setOnClickListener {
            if (favoriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        myActivity!!,
                        R.drawable.favorite_off
                    )
                )
                favoriteContent?.deleteFabourite(currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(myActivity, "Removed from Favourites", Toast.LENGTH_SHORT).show()
            } else {
                fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        myActivity!!,
                        R.drawable.favorite_on
                    )
                )
                favoriteContent?.storeAsFavorite(
                    currentSongHelper?.songId?.toInt(),
                    currentSongHelper?.songArtist,
                    currentSongHelper?.songTitle,
                    currentSongHelper?.songPath
                )
                Toast.makeText(myActivity, "Added to Favourites", Toast.LENGTH_SHORT).show()
            }
        }

        shuffleImageButton?.setOnClickListener {
            var editorShuffle =
                myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
                    ?.edit()
            var editorLoop =
                myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
                    ?.edit()

            if (currentSongHelper?.isShuffle as Boolean) {
                currentSongHelper?.isShuffle = false
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                currentSongHelper?.isLoop = false
                currentSongHelper?.isShuffle = true
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }
        }
        nextImageButton?.setOnClickListener {
            currentSongHelper?.isPlaying = true
            playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if (currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
            } else
                playNext("PlayNextNormal")
        }
        previousImageButton?.setOnClickListener {
            currentSongHelper?.isPlaying = true
            if (currentSongHelper?.isLoop as Boolean) {
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        }
        loopImageButton?.setOnClickListener {
            var editorShuffle =
                myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
                    ?.edit()
            var editorLoop =
                myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
                    ?.edit()

            if (currentSongHelper?.isLoop as Boolean) {
                currentSongHelper?.isLoop = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                currentSongHelper?.isLoop = true
                currentSongHelper?.isShuffle = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            }
        }
        playpauseImageButton?.setOnClickListener {
            if (mediaplayer?.isPlaying as Boolean) {
                mediaplayer?.pause()
                currentSongHelper?.isPlaying = false
                playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                mediaplayer?.start()
                currentSongHelper?.isPlaying = true
                playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                mediaplayer?.seekTo(seekbar?.progress as Int)
            }
        }
    }

    fun playPrevious() {
        currentPosition = currentPosition - 1
        if (currentPosition == -1) {
            currentPosition = 0
        }
        if (currentSongHelper?.isPlaying as Boolean) {
            playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        currentSongHelper?.isLoop = false
        var nextSong = fetchSongs?.get(currentPosition)
        currentSongHelper?.songPath = nextSong?.songData
        currentSongHelper?.songTitle = nextSong?.songTitle
        currentSongHelper?.songId = nextSong?.songID as Long
        currentSongHelper?.currentPosition = currentPosition
        updateTextViews(
            currentSongHelper?.songTitle as String,
            currentSongHelper?.songArtist as String
        )
        mediaplayer?.reset()
        try {
            mediaplayer?.setDataSource(myActivity, Uri.parse(currentSongHelper?.songPath))
            mediaplayer?.prepare()
            mediaplayer?.start()
            processInformation(mediaplayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (favoriteContent?.checkifIdExists(currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_on))
        } else {
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity!!, R.drawable.favorite_off))
        }
    }

    fun bindShakeListener() {
        mSensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sp0: Sensor?, p1: Int) {

            }

            override fun onSensorChanged(p0: SensorEvent) {
                val x = p0.values[0]
                val y = p0.values[1]
                val z = p0.values[2]

                mAccelarationLast = mAccelarationCurrent
                mAccelarationCurrent = Math.sqrt(((x * x + y * y + z * z).toDouble())).toFloat()
                val delta = mAccelarationCurrent - mAccelarationLast
                mAccelaration = mAccelaration * 0.9f + delta

                if (mAccelaration > 12) {
                    val prefs =
                        myActivity?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean) {
                        playNext("PlayNextNormal")
                    }
                }
            }

        }
    }
}