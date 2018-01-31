package tibi.fruity

import ktx.app.KtxScreen
import ktx.app.use


class GameOverScreen(private val game: FruityFrankGame) : KtxScreen {

    var timer = 0f

    override fun show() {
        timer = 0f
    }

    override fun render(delta: Float) {
        timer += delta
        if (timer < 3f || timer % 0.5f < 0.3f) {
            game.batch.use {
                game.font.draw(game.batch, textAppear("GAME OVER", 3f, timer),
                        250f, GAME_HEIGHT / 2)
            }
        }
    }

}