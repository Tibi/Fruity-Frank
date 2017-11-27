package tibi.fruity

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion



class FruityFrankGame : ApplicationAdapter() {

    lateinit var spriteBatch: SpriteBatch
    lateinit var pruneAnim: Animation
    lateinit var bg: TextureRegion
    val level = Level()

    // A variable for tracking elapsed time for the animation
    var stateTime: Float = 0f

    override fun create() {
        spriteBatch = SpriteBatch()

        val atlas = TextureAtlas("main.atlas")
        pruneAnim = Animation(0.25F, atlas.findRegions("frank/ball right"))
        bg = atlas.findRegion("backgrounds/level1")
        level.fill(bg)
        level.horizLine(6, atlas.findRegion("guy/right"))
        level.set(6, 6, atlas.findRegion("backgrounds/gate"))
    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT) // Clear screen
        stateTime += Gdx.graphics.deltaTime // Accumulate elapsed animation time

        // Get current frame of animation for the current stateTime
        val currentFrame = pruneAnim.getKeyFrame(stateTime, true)
        spriteBatch.begin()
        level.draw(spriteBatch, 0F, 0F)
        spriteBatch.draw(currentFrame, 50F, 50F) // Draw current frame at (50, 50)
        spriteBatch.end()
    }

    override fun dispose() {
        spriteBatch.dispose()
    }
}
