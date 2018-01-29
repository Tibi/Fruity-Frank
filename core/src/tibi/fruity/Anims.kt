package tibi.fruity

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array

class ExplodeAnim(private val source: GridItem, private val tex: TextureRegion, private val color: Color, private val isExplosion: Boolean = true) {

    private var dist = if (isExplosion) 0f else GAME_HEIGHT

    var whenFinished: () -> Unit = {}
    var finished = false

    fun update(deltaTime: Float) {
        if (finished) return
        val speed = if (isExplosion) 300 else - 200
        dist += speed * deltaTime
        if (if (isExplosion) dist > GAME_WIDTH else dist < 0) {
            finished = true
            whenFinished()
        }
    }

    fun render(batch: SpriteBatch) {
        val oldColor = batch.color
        batch.color = color
        val targetX = source.pos.x + CELL_WIDTH / 2
        val targetY = source.pos.y + CELL_HEIGHT / 2
        batch.draw(tex, targetX + dist, targetY + dist)
        batch.draw(tex, targetX + dist, targetY - dist)
        batch.draw(tex, targetX - dist, targetY + dist)
        batch.draw(tex, targetX - dist, targetY - dist)
        if (isExplosion) {
            batch.draw(tex, targetX, targetY + dist)
            batch.draw(tex, targetX, targetY - dist)
            batch.draw(tex, targetX - dist, targetY)
            batch.draw(tex, targetX - dist, targetY)
        }
        batch.color = oldColor
    }
}

typealias AnimationMap = Map<Direction, Animation<out TextureAtlas.AtlasRegion>>

fun createAnimations(atlas: TextureAtlas, name: String): AnimationMap {
    val leftRegions = atlas.findRegions(name + "right")
    val rightRegions = Array(leftRegions.map {
        TextureAtlas.AtlasRegion(it).apply { it.flip(true, false) }
    }.toTypedArray())
    val downRegions = atlas.findRegions(name + "down")
    return mapOf(
        Direction.RIGHT to Animation(
            0.15F,
            rightRegions,
            Animation.PlayMode.LOOP
        ),
        Direction.LEFT to Animation(
            0.15F,
            leftRegions,
            Animation.PlayMode.LOOP
        ),
        Direction.UP to Animation(
            0.15F,
            downRegions,
            Animation.PlayMode.LOOP
        ),
        Direction.DOWN to Animation(
            0.15F,
            downRegions,
            Animation.PlayMode.LOOP
        )
    )
}