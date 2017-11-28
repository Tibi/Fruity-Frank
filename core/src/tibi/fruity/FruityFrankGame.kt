package tibi.fruity

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable


class FruityFrankGame : ApplicationAdapter() {

    lateinit var batch: SpriteBatch
    lateinit var pruneAnim: Animation
    lateinit var bg: TextureRegion
    lateinit var header: TextureRegion
    val level = Level()

    // A variable for tracking elapsed time for the animation
    var stateTime: Float = 0f

    override fun create() {
        batch = SpriteBatch()

        val atlas = TextureAtlas("main.atlas")
        pruneAnim = Animation(0.15F, atlas.findRegions("frank/ball right"))
        header = atlas.findRegion("backgrounds/header")
        bg = atlas.findRegion("backgrounds/level1")
        bg.texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        level.horizLine(6, atlas.findRegion("guy/right"))
        level.set(6, 6, atlas.findRegion("backgrounds/gate"))
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT) // Clear screen
        stateTime += Gdx.graphics.deltaTime // Accumulate elapsed animation time

        // Get current frame of animation for the current stateTime
        val currentFrame = pruneAnim.getKeyFrame(stateTime, true)
        batch.begin()
        TiledDrawable(bg).draw(batch, 0F, 0F, SCREEN_WIDTH, SCREEN_HEIGHT - HEADER_HEIGHT - 1)
        batch.draw(header, 0F, SCREEN_HEIGHT - HEADER_HEIGHT-3)
//        level.draw(spriteBatch, 0F, 0F)
        batch.draw(currentFrame, 50F, 50F) // Draw current frame at (50, 50)
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
    }
}
