package tibi.fruity

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFileHandle
import java.io.IOException

class AndroidLauncher : AndroidApplication() {

    private val player = MusicPlayerAndroid()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        config.useAccelerometer = false
        config.useCompass = false
        config.useImmersiveMode = true
        initialize(FruityFrankGame(player), config)
    }

    override fun onPause() {
        super.onPause()
        player.pause(true)
    }

    override fun onResume() {
        super.onResume()
        player.pause(false)
    }

    inner class MusicPlayerAndroid : MusicPlayer {

        private var mplayer: MediaPlayer? = null
        var fileNamePrefix: String? = null
        var speedFactor: Float? = null

        override fun play(fileNamePrefix: String, speedFactor: Float) {
            this.fileNamePrefix = fileNamePrefix
            this.speedFactor = speedFactor
            release()
            start()
        }

        private fun start() {
            try {
                if (fileNamePrefix == null) {
                    return
                }
                val player = MediaPlayer()
                val file = findFile(fileNamePrefix!!) as AndroidFileHandle
                val descriptor = file.assetFileDescriptor
                player.setDataSource(descriptor.fileDescriptor,
                        descriptor.startOffset, descriptor.length)
                player.setAudioAttributes(AudioAttributes
                               .Builder()
                               .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                               .build())
                val params = PlaybackParams()
                params.speed = speedFactor!!
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
            else {
                if (mplayer == null) start()
                else mplayer?.start()
            }
        }

        override fun release() {
            mplayer?.release()
            mplayer = null
        }
    }
}
