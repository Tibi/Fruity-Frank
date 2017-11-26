package tibi.fruity

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas





class FruityFrankGame : ApplicationAdapter() {

    lateinit var spriteBatch: SpriteBatch
    lateinit var pruneAnim: Animation

    // A variable for tracking elapsed time for the animation
    var stateTime: Float = 0f

    override fun create() {
        spriteBatch = SpriteBatch()

        val atlas = TextureAtlas("main.atlas")
        pruneAnim = Animation(0.25F, atlas.findRegions("frank/ball right"))

    }

    override fun render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT) // Clear screen
        stateTime += Gdx.graphics.deltaTime // Accumulate elapsed animation time

        // Get current frame of animation for the current stateTime
        val currentFrame = pruneAnim.getKeyFrame(stateTime, true)
        spriteBatch.begin()
        spriteBatch.draw(currentFrame, 50F, 50F) // Draw current frame at (50, 50)
        spriteBatch.end()
    }

    override fun dispose() {
        spriteBatch.dispose()
    }
}
