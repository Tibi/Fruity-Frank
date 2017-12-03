package tibi.fruity

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import java.util.*


class FruityFrankGame : ApplicationAdapter() {

    lateinit var batch: SpriteBatch
    lateinit var frank: Perso

    lateinit var atlas: TextureAtlas
    lateinit var level: Level

    val cam = OrthographicCamera()


    override fun create() {
        cam.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT)
        batch = SpriteBatch()

        atlas = TextureAtlas("main.atlas")

        level = Level(this)

        Gdx.input.inputProcessor = FruityInput(this)
    }

    override fun render() {
        processInput()

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.projectionMatrix = cam.combined
        batch.begin()
        batch.disableBlending()

        level.render(batch, Gdx.graphics.deltaTime)

        batch.end()
    }

    private fun processInput() {
        if (isKeyPressed(Keys.X, Keys.D, Keys.RIGHT)) level.movePlayer(Direction.RIGHT)
        if (isKeyPressed(Keys.Z, Keys.A, Keys.LEFT)) level.movePlayer(Direction.LEFT)
        if (isKeyPressed(Keys.SEMICOLON, Keys.W, Keys.UP)) level.movePlayer(Direction.UP)
        if (isKeyPressed(Keys.PERIOD, Keys.S, Keys.DOWN)) level.movePlayer(Direction.DOWN)
    }

    private fun isKeyPressed(vararg keys: Int) = keys.any { Gdx.input.isKeyPressed(it) }

    override fun dispose() {
        batch.dispose()
    }


    val random = Random()

    fun rand(from: Int, to: Int) : Int {
        return random.nextInt(to - from) + from
    }

}

enum class Direction { UP, DOWN, LEFT, RIGHT }

class FruityInput(val game: FruityFrankGame) : InputAdapter() {
    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Keys.RIGHT_BRACKET) game.level.throwBall()
        return true
    }
}
