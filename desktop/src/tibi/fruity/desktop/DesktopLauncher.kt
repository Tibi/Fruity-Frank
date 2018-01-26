package tibi.fruity.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import tibi.fruity.FruityFrankGame
import tibi.fruity.MusicPlayer
import tibi.fruity.SCREEN_HEIGHT
import tibi.fruity.SCREEN_WIDTH
import javax.sound.midi.MidiSystem
import javax.sound.midi.MidiUnavailableException
import javax.sound.midi.Sequence
import javax.sound.midi.Sequencer

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration()
        config.width = SCREEN_WIDTH.toInt()
        config.height = SCREEN_HEIGHT.toInt()
        LwjglApplication(FruityFrankGame(MusicPlayerDesktop()), config)
    }


    class MusicPlayerDesktop : MusicPlayer {

        private var sequencer: Sequencer? = null
        private var sequence: Sequence? = null

        init {
            try {
                sequencer = MidiSystem.getSequencer()
            } catch (e: MidiUnavailableException) {
                Gdx.app.error("", "Error opening midi device.", e)
            }

        }

        override fun play(fileNamePrefix: String, speedFactor: Float) {
            release()
            val file = findFile(fileNamePrefix)
            try {
                sequence = MidiSystem.getSequence(file.read())
                sequencer!!.open()
                sequencer!!.sequence = sequence
                sequencer!!.loopCount = Sequencer.LOOP_CONTINUOUSLY
                sequencer!!.tempoFactor = speedFactor
            } catch (e: Exception) {
                Gdx.app.error("", "Error opening midi: $fileNamePrefix.", e)
            }

            sequencer!!.start()
        }

        override fun release() {
            if (sequencer != null && sequencer!!.isOpen) {
                sequencer!!.stop()
                sequencer!!.close()
            }
        }
    }
}
