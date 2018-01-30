package tibi.fruity

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import ktx.app.KtxGame


class FruityFrankGame(val musicPlayer: MusicPlayer) : KtxGame<Screen>() {

    lateinit var batch: SpriteBatch
    lateinit var atlas: TextureAtlas

    override fun create() {
        batch = SpriteBatch()
        atlas = TextureAtlas("main.atlas")
        addScreen(GameScreen(this))
        setScreen<GameScreen>()
    }

    override fun dispose() {
        super.dispose()
        batch.dispose()
        atlas.dispose()
    }

    fun gameOver() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

const val MUSIC_DIR = "music"

interface MusicPlayer {

    fun play(fileNamePrefix: String, speedFactor: Float)
    fun pause(value: Boolean)

    fun release()

    fun findFile(prefix: String): FileHandle {
        return Gdx.files.internal(MUSIC_DIR).list({ _, name -> name.startsWith(prefix) })[0]
    }
}
