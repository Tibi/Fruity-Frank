package tibi.fruity

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFileHandle
import java.io.IOException

class AndroidLauncher : AndroidApplication() {

    private val player = MusicPlayerAndroid()

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        config.useAccelerometer = false
        config.useCompass = false

        initialize(FruityFrankGame(player), config)
    }

    override fun onPause() {
        super.onPause()
        player.release()
    }

    inner class MusicPlayerAndroid : MusicPlayer {

        private var mplayer: MediaPlayer? = null

        override fun play(fileNamePrefix: String, speedFactor: Float) {
            try {
                release()
                val player = MediaPlayer()
                val file = findFile(fileNamePrefix) as AndroidFileHandle
                val descriptor = file.assetFileDescriptor
                player.setDataSource(descriptor.fileDescriptor,
                        descriptor.startOffset, descriptor.length)
                player.setAudioStreamType(AudioManager.STREAM_MUSIC)
                val params = PlaybackParams()
                params.speed = speedFactor
                player.playbackParams = params
                player.isLooping = true
                player.prepare()
                player.start()
                mplayer = player
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        override fun pause(value: Boolean) {
            if (value) mplayer?.pause()
            else mplayer?.start()
        }

        override fun release() {
            if (mplayer != null) {
                mplayer!!.release()
            }
        }
    }
}
