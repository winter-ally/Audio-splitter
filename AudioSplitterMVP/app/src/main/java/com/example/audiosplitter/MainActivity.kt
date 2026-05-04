package com.example.audiosplitter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var player1: ExoPlayer
    private lateinit var player2: ExoPlayer
    private lateinit var audioManager: AudioManager
    private var availableDevices: List<AudioDeviceInfo> = emptyList()
    
    private var selectedDevice1: AudioDeviceInfo? = null
    private var selectedDevice2: AudioDeviceInfo? = null

    private var eq1: Equalizer? = null
    private var eq2: Equalizer? = null

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            updateAvailableDevices()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            updateAvailableDevices()
        }
    }

    private val pickAudio1 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            player1.setMediaItem(MediaItem.fromUri(it))
            player1.prepare()
            Toast.makeText(this, "Audio loaded in Player 1", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickAudio2 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            player2.setMediaItem(MediaItem.fromUri(it))
            player2.prepare()
            Toast.makeText(this, "Audio loaded in Player 2", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
        }

        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 100)
        } else {
            setupPlayers()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            setupPlayers()
        }
    }

    private fun setupPlayers() {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        val audioAttributes1 = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val audioAttributes2 = AudioAttributes.Builder()
            .setUsage(C.USAGE_VOICE_COMMUNICATION)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
            .build()

        player1 = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes1, false)
            .setHandleAudioBecomingNoisy(false)
            .build()
            
        player2 = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes2, false)
            .setHandleAudioBecomingNoisy(false)
            .build()

        player1.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                println("AudioSplitter DEBUG: onAudioSessionIdChanged 1 = $audioSessionId")
                setupEqualizer(audioSessionId, 1)
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    println("AudioSplitter DEBUG: onPlaybackStateChanged 1 = ${player1.audioSessionId}")
                    setupEqualizer(player1.audioSessionId, 1)
                }
            }
        })

        player2.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                println("AudioSplitter DEBUG: onAudioSessionIdChanged 2 = $audioSessionId")
                setupEqualizer(audioSessionId, 2)
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    println("AudioSplitter DEBUG: onPlaybackStateChanged 2 = ${player2.audioSessionId}")
                    setupEqualizer(player2.audioSessionId, 2)
                }
            }
        })

        findViewById<PlayerView>(R.id.playerView1).player = player1
        findViewById<PlayerView>(R.id.playerView2).player = player2

        findViewById<Button>(R.id.btnPickAudio1).setOnClickListener {
            pickAudio1.launch("audio/*")
        }

        findViewById<Button>(R.id.btnPickAudio2).setOnClickListener {
            pickAudio2.launch("audio/*")
        }
        
        val layoutEq1 = findViewById<LinearLayout>(R.id.layoutEq1)
        findViewById<Button>(R.id.btnToggleEq1).setOnClickListener {
            layoutEq1.visibility = if (layoutEq1.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        val layoutEq2 = findViewById<LinearLayout>(R.id.layoutEq2)
        findViewById<Button>(R.id.btnToggleEq2).setOnClickListener {
            layoutEq2.visibility = if (layoutEq2.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        setupSpinners()
    }

    private fun setupEqualizer(audioSessionId: Int, playerIndex: Int) {
        runOnUiThread {
            try {
                val equalizer = Equalizer(0, audioSessionId)
                equalizer.enabled = true
                
                if (playerIndex == 1) eq1 = equalizer else eq2 = equalizer

                val bandsContainer = if (playerIndex == 1) findViewById<LinearLayout>(R.id.eqBands1) else findViewById<LinearLayout>(R.id.eqBands2)
                bandsContainer.removeAllViews()

                if (equalizer.numberOfBands.toInt() == 0) {
                    Toast.makeText(this@MainActivity, "Moto Audio blocked the Equalizer (0 supported bands).", Toast.LENGTH_LONG).show()
                    return@runOnUiThread
                }

                val minEQLevel = equalizer.bandLevelRange[0]
                val maxEQLevel = equalizer.bandLevelRange[1]

                for (i in 0 until equalizer.numberOfBands) {
                    val bandId = i.toShort()
                    val bandLayout = LinearLayout(this@MainActivity)
                    bandLayout.orientation = LinearLayout.HORIZONTAL
                    bandLayout.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    bandLayout.setPadding(0, 24, 0, 24)

                    val freqTextView = TextView(this@MainActivity)
                    val freqHz = equalizer.getCenterFreq(bandId) / 1000
                    freqTextView.text = if (freqHz >= 1000) "${freqHz / 1000}kHz" else "${freqHz}Hz"
                    
                    val tvParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, 
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    freqTextView.layoutParams = tvParams
                    freqTextView.minEms = 4

                    val seekBar = SeekBar(this@MainActivity, null, android.R.attr.seekBarStyle)
                    val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    seekBar.layoutParams = params
                    seekBar.max = maxEQLevel - minEQLevel
                    seekBar.progress = equalizer.getBandLevel(bandId) - minEQLevel

                    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            try {
                                if (fromUser) equalizer.setBandLevel(bandId, (progress + minEQLevel).toShort())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })

                    bandLayout.addView(freqTextView)
                    bandLayout.addView(seekBar)
                    bandsContainer.addView(bandLayout)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Device blocked Equalizer: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupSpinners() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            updateAvailableDevices()

            val spinner1 = findViewById<Spinner>(R.id.spinnerDevice1)
            val spinner2 = findViewById<Spinner>(R.id.spinnerDevice2)

            spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (availableDevices.isEmpty()) return
                    val device = availableDevices[position]
                    selectedDevice1 = device
                    player1.setPreferredAudioDevice(device)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (availableDevices.isEmpty()) return
                    val device = availableDevices[position]
                    selectedDevice2 = device
                    player2.setPreferredAudioDevice(device)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun updateAvailableDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val allDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()
            
            val allowedTypes = listOf(
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                AudioDeviceInfo.TYPE_USB_HEADSET
            )
            
            availableDevices = allDevices.filter { it.type in allowedTypes }
                .distinctBy { it.productName.toString() }
            
            val deviceNames = availableDevices.map { 
                "${it.productName} (${getDeviceTypeName(it.type)})" 
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, deviceNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            val spinner1 = findViewById<Spinner>(R.id.spinnerDevice1)
            val spinner2 = findViewById<Spinner>(R.id.spinnerDevice2)
            
            spinner1.adapter = adapter
            spinner2.adapter = adapter
            
            selectedDevice1?.let { prevDev ->
                val index = availableDevices.indexOfFirst { it.id == prevDev.id }
                if (index != -1) spinner1.setSelection(index)
            }
            
            selectedDevice2?.let { prevDev ->
                val index = availableDevices.indexOfFirst { it.id == prevDev.id }
                if (index != -1) spinner2.setSelection(index)
            }
        }
    }

    private fun getDeviceTypeName(type: Int): String {
        return when (type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Speaker"
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Headphones"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth"
            AudioDeviceInfo.TYPE_USB_HEADSET -> "USB"
            else -> "Other"
        }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        }
        if (::player1.isInitialized) player1.release()
        if (::player2.isInitialized) player2.release()
        eq1?.release()
        eq2?.release()
        super.onDestroy()
    }
}
