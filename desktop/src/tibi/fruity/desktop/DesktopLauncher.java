package tibi.fruity.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import tibi.fruity.FruityFrankGame;

import static tibi.fruity.UtilKt.SCREEN_HEIGHT;
import static tibi.fruity.UtilKt.SCREEN_WIDTH;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = (int) SCREEN_WIDTH;
		config.height = (int) SCREEN_HEIGHT;
		new LwjglApplication(new FruityFrankGame(null), config);
	}
}
