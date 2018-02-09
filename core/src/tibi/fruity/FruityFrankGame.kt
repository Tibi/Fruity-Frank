package tibi.fruity

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import ktx.app.KtxGame
import kotlin.math.max


class FruityFrankGame(val musicPlayer: MusicPlayer) : KtxGame<Screen>() {

    lateinit var batch: SpriteBatch
    lateinit var atlas: TextureAtlas
    lateinit var font: BitmapFont

    var score = 0
    var highScore = 0

    override fun create() {
        batch = SpriteBatch()
        atlas = TextureAtlas("sprites/main.atlas")
        font = BitmapFont(Gdx.files.internal("font/fruity-font.fnt"))
        val header = Header(atlas, font)
        addScreen(StartScreen(this))
        addScreen(GameScreen(this, header))
        addScreen(GameOverScreen(this, header))
        setScreen<StartScreen>()
    }

    override fun dispose() {
        super.dispose()
        batch.dispose()
        atlas.dispose()
        font.dispose()
    }

    fun gameOver(score: Int) {
        this.score = score
        highScore = max(highScore, getScreen<GameScreen>().score)
        setScreen<GameOverScreen>()
        schedule(5f) {
            setScreen<StartScreen>()
        }
    }
}

interface MusicPlayer {

    fun play(fileNamePrefix: String, speedFactor: Float)
    fun pause(value: Boolean)

    fun release()

    fun findFile(prefix: String): FileHandle {
        return Gdx.files.internal("music").list({ _, name -> name.startsWith(prefix) })[0]
    }
}
