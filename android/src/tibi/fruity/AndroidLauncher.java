package tibi.fruity;

import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Bundle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFileHandle;

public class AndroidLauncher extends AndroidApplication {

	private final MusicPlayer player = new MusicPlayerAndroid();

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;

		initialize(new FruityFrankGame(player), config);
	}

	@Override protected void onPause() {
		super.onPause();
		player.release();
	}


	public class MusicPlayerAndroid implements MusicPlayer {

		private MediaPlayer player;

		@Override public void play(float speedFactor) {
			try {
				release();
				player = new MediaPlayer();
				AndroidFileHandle file = (AndroidFileHandle) Gdx.files.internal("level 1.mid");
				AssetFileDescriptor descriptor = file.getAssetFileDescriptor();
				player.setDataSource(descriptor.getFileDescriptor(),
				                     descriptor.getStartOffset(), descriptor.getLength());
				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				PlaybackParams params = new PlaybackParams();
				params.setSpeed(speedFactor);
				player.setPlaybackParams(params);
				player.setLooping(true);
				player.prepare();
				player.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override public void release() {
			if (player != null) {
				player.release();
			}
		}
	}
}
