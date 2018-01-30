package tibi.fruity

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
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
        game.batch.use {
            game.font.draw(game.batch, "start screen", GAME_WIDTH / 2, GAME_HEIGHT / 2)
        }
    }

}