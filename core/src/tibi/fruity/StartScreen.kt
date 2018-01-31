package tibi.fruity

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.app.use

class StartScreen(val game: FruityFrankGame) : KtxScreen {

    override fun show() {
        Gdx.input.inputProcessor = object: KtxInputAdapter {
            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
                    game.setScreen<GameScreen>()
                }
                return true
            }
        }
    }

    override fun render(delta: Float) {
        clearScreen(.1f, .3f,.5f)
        game.batch.use {
            game.font.draw(game.batch, "START SCREEN", GAME_WIDTH / 2, GAME_HEIGHT / 2)
        }
    }

}