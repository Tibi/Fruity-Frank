package tibi.fruity.desktop;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import org.jetbrains.annotations.NotNull;
import tibi.fruity.FruityFrankGame;
import tibi.fruity.MusicPlayer;

import static tibi.fruity.UtilKt.SCREEN_HEIGHT;
import static tibi.fruity.UtilKt.SCREEN_WIDTH;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = (int) SCREEN_WIDTH;
		config.height = (int) SCREEN_HEIGHT;
		new LwjglApplication(new FruityFrankGame(new MusicPlayerDesktop()), config);
	}


	public static class MusicPlayerDesktop implements MusicPlayer {

		private Sequencer sequencer;
		private Sequence sequence;

		public MusicPlayerDesktop() {
			try {
				sequencer = MidiSystem.getSequencer();
			} catch (MidiUnavailableException e) {
				Gdx.app.error("", "Error opening midi device.", e);
			}
		}

		@Override public void play(@NotNull String fileName, float speedFactor) {
			release();
			FileHandle file = Gdx.files.internal(fileName);
			try {
				sequence = MidiSystem.getSequence(file.read());
				sequencer.open();
				sequencer.setSequence(sequence);
				sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
				sequencer.setTempoFactor(speedFactor);
			} catch (Exception e) {
				Gdx.app.error("", "Error opening midi: " + fileName + ".", e);
			}

			sequencer.start();
		}

		@Override public void release() {
			if (sequencer != null && sequencer.isOpen()) {
				sequencer.stop();
				sequencer.close();
			}
		}
	}
}
