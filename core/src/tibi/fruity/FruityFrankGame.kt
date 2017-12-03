package tibi.fruity

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import java.util.*


class FruityFrankGame : Game() {

    lateinit var batch: SpriteBatch

    lateinit var atlas: TextureAtlas



    override fun create() {
        batch = SpriteBatch()
        atlas = TextureAtlas("main.atlas")
        setScreen(Level(this))
    }

    override fun dispose() {
        super.dispose()
        batch.dispose()
    }


    val random = Random()

    fun rand(from: Int, to: Int) : Int {
        return random.nextInt(to - from) + from
    }

}

