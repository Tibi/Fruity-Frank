package tibi.fruity

import ktx.app.KtxScreen
import ktx.app.use


class GameOverScreen(private val game: FruityFrankGame) : KtxScreen {

    override fun render(delta: Float) {
        game.batch.use {
            game.font.draw(game.batch, "GAME OVER", GAME_WIDTH / 2, GAME_HEIGHT / 2)
        }
    }

}