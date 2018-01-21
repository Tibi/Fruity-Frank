package tibi.fruity

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas


class FruityFrankGame(val musicPlayer: MusicPlayer?) : Game() {

    lateinit var batch: SpriteBatch
    lateinit var atlas: TextureAtlas

    override fun create() {
        batch = SpriteBatch()
        atlas = TextureAtlas("main.atlas")
        screen = Level(1, this)
    }

    override fun dispose() {
        super.dispose()
        batch.dispose()
        atlas.dispose()
    }

}

interface MusicPlayer {
    fun play(speedFactor: Float)
    fun release()
}
