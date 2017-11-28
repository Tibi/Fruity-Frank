package tibi.fruity

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import java.util.*


class FruityFrankGame : ApplicationAdapter() {

    lateinit var batch: SpriteBatch
    lateinit var frank: Perso

    lateinit var bg: TextureRegion
    lateinit var header: TextureRegion
    lateinit var level: Level

    val random = Random()

    // A variable for tracking elapsed time for the animation
    var stateTime: Float = 0f

    override fun create() {
        batch = SpriteBatch()

        val atlas = TextureAtlas("main.atlas")
        frank = Perso.create(atlas, "frank/ball ", 0, 0)
        val guy = Perso.create(atlas, "guy/", 1, 0)
        val prune = Perso.create(atlas, "prune/", 5, 5)
        val cherry = atlas.findRegion("fruits/cherry")

        val fruits = List(10, { it -> Fruit(cherry, rand(0, GRID_WIDTH), rand(0, GRID_HEIGHT)) })
        level = Level(frank, arrayOf(guy, prune), fruits)


        header = atlas.findRegion("backgrounds/header")
        bg = atlas.findRegion("backgrounds/level1")
        bg.texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        level.horizLine(6, atlas.findRegion("guy/right"))
        level.set(6, 6, atlas.findRegion("backgrounds/gate"))
        Gdx.input.inputProcessor = FruityInput(this)
    }

    fun rand(from: Int, to: Int) : Int {
        return random.nextInt(to - from) + from
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()
        batch.disableBlending()
        TiledDrawable(bg).draw(batch, 0F, 0F, SCREEN_WIDTH, SCREEN_HEIGHT - HEADER_HEIGHT - 1)
        batch.draw(header, 0F, SCREEN_HEIGHT - HEADER_HEIGHT-3)
        level.render(batch, Gdx.graphics.deltaTime)
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
    }
}

enum class Direction { UP, DOWN, LEFT, RIGHT }

class FruityInput(val game: FruityFrankGame) : InputAdapter() {
    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            in arrayOf(Input.Keys.X, Input.Keys.RIGHT) -> game.level.movePlayer(Direction.RIGHT)
            in arrayOf(Input.Keys.Z, Input.Keys.LEFT) -> game.level.movePlayer(Direction.LEFT)
            in arrayOf(Input.Keys.SEMICOLON, Input.Keys.UP) -> game.level.movePlayer(Direction.UP)
            in arrayOf(Input.Keys.PERIOD, Input.Keys.DOWN) -> game.level.movePlayer(Direction.DOWN)
            else -> return super.keyDown(keycode)
        }
        return true
    }
}
