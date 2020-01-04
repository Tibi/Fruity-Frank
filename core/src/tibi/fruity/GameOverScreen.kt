package tibi.fruity

import ktx.app.KtxScreen
import ktx.graphics.use


class GameOverScreen(private val game: FruityFrankGame, val header: Header) : KtxScreen {

    var timer = 0f

    override fun show() {
        timer = 0f
    }

    override fun render(delta: Float) {
        game.batch.use {
            timer += delta
            if (timer < 3f || timer % 0.5f < 0.3f) {
                game.font.draw(game.batch, textAppear("GAME OVER", 3f, timer),
                        250f, GAME_HEIGHT / 2)
            }
            header.render(game.batch, game.score, game.highScore, 0)
        }
    }

}