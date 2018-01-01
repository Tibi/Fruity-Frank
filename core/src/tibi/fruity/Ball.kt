package tibi.fruity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2

/** The ball frank throws. */
class Ball(val level: Level, atlas: TextureAtlas, val pos: Vector2, frankDir: Direction) {

    val speed = ballSpeed(frankDir) * level.speed
    val tex = atlas.findRegion("frank/ball")

    fun update(deltaTime: Float) {
        val newPos = pos + speed * deltaTime
        pos.set(newPos)
    }

    fun render(batch: SpriteBatch) {
        batch.draw(tex, pos.x, pos.y)
    }

    fun ballSpeed(frankDir: Direction) = when (frankDir) {
        Direction.UP -> Vector2(-1f,  1f)
        Direction.DOWN -> Vector2(-1f, -1f)
        Direction.RIGHT -> Vector2(1f, -1f)
        Direction.LEFT -> Vector2(-1f, -1f)
        Direction.NONE -> Vector2(0f, 0f)
    }
}