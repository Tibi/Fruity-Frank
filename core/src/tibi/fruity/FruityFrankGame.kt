package tibi.fruity

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import java.util.*


class FruityFrankGame : ApplicationAdapter() {

    lateinit var batch: SpriteBatch
    lateinit var frank: Perso

    lateinit var atlas: TextureAtlas
    lateinit var bg: TextureRegion
    lateinit var header: TextureRegion
    lateinit var level: Level

    val cam = OrthographicCamera()


    override fun create() {
        cam.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT)
        batch = SpriteBatch()

        atlas = TextureAtlas("main.atlas")

        level = Level(this)

        header = atlas.findRegion("backgrounds/header")
        bg = atlas.findRegion("backgrounds/level1")
        bg.texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)

        Gdx.input.inputProcessor = FruityInput(this)
    }

    override fun render() {
        processInput()

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.projectionMatrix = cam.combined
        batch.begin()
        batch.disableBlending()

        TiledDrawable(bg).draw(batch, 0F, 0F, SCREEN_WIDTH, SCREEN_HEIGHT - HEADER_HEIGHT - 1)
        batch.draw(header, 0F, SCREEN_HEIGHT - HEADER_HEIGHT-3)
        level.render(batch, Gdx.graphics.deltaTime)

        batch.end()
    }

    private fun processInput() {
        if (isKeyPressed(Keys.X, Keys.RIGHT)) level.movePlayer(Direction.RIGHT)
        if (isKeyPressed(Keys.Z, Keys.LEFT)) level.movePlayer(Direction.LEFT)
        if (isKeyPressed(Keys.SEMICOLON, Keys.UP)) level.movePlayer(Direction.UP)
        if (isKeyPressed(Keys.PERIOD, Keys.DOWN)) level.movePlayer(Direction.DOWN)
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
