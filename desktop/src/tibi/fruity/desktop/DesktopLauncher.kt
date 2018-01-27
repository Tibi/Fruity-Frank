package tibi.fruity.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import tibi.fruity.FruityFrankGame
import tibi.fruity.GAME_HEIGHT
import tibi.fruity.GAME_WIDTH
import tibi.fruity.MusicPlayer
import javax.sound.midi.MidiSystem
import javax.sound.midi.Sequencer

object DesktopLauncher {
    
    @JvmStatic fun main(arg: Array<String>) {
        val config = LwjglApplicationConfiguration()
        config.width = GAME_WIDTH.toInt()
        config.height = GAME_HEIGHT.toInt()
        LwjglApplication(FruityFrankGame(MusicPlayerDesktop()), config)
    }


    class MusicPlayerDesktop : MusicPlayer {

        private val sequencer: Sequencer = MidiSystem.getSequencer()

        override fun play(fileNamePrefix: String, speedFactor: Float) {
            release()
            val file = findFile(fileNamePrefix)
            try {
                val sequence = MidiSystem.getSequence(file.read())
                sequencer.open()
                sequencer.sequence = sequence
                sequencer.loopCount = Sequencer.LOOP_CONTINUOUSLY
                sequencer.tempoFactor = speedFactor
            } catch (e: Exception) {
                Gdx.app.error("", "Error opening midi: $fileNamePrefix.", e)
            }
            sequencer.start()
        }

        override fun pause(value: Boolean) {
            if (value) sequencer.stop()
            else sequencer.start()
        }

        override fun release() {
            if (sequencer.isOpen) {
                sequencer.stop()
                sequencer.close()
            }
        }
    }
}
