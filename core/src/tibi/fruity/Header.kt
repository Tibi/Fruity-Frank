package tibi.fruity

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas

class Header(atlas: TextureAtlas, val font: BitmapFont) {

    val tex = atlas.findRegion("backgrounds/header")
    val frankTex = atlas.findRegion("frank/right", 1)
    val cyan = Color.valueOf("#00F7F7")
    val yellow = Color.valueOf("#F7F763")
    val y = GAME_HEIGHT - HEADER_HEIGHT - 1

    fun render(batch: SpriteBatch, score: Int, highScore: Int, lives: Int) {
        batch.draw(tex, 0f, y)
        val yText = y + 17f
        font.color = cyan
        font.draw(batch, "SCORE", 16f, yText)
        font.draw(batch, "HIGH  SCORE", 208f, yText)
        font.color = yellow
        font.draw(batch, "%05d".format(score), 112f, yText)
        font.draw(batch, "%05d".format(highScore), 384f, yText)

        for (i in 1..lives) {
            batch.draw(frankTex, GAME_WIDTH - 4 - i * 34, y)
        }
    }

}
