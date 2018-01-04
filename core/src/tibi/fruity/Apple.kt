package tibi.fruity

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import tibi.fruity.Apple.AppleState.*

const val FALL_HEIGHT_BEFORE_CRASH = 3  // in cells

class Apple(level: Level, pos: IntPoint)
    : Fruit(level, level.appleTex, pos, 0) {

    enum class AppleState { IDLE, PUSHED, FALLING_SLOW, FALLING_FAST, CRASHING }

    var state = IDLE
        set(value) {
            speedFactor = when (value) {
                FALLING_SLOW -> 0.1f
                else -> 1.0f
            }
            anim = if (value == CRASHING) level.appleCrashAnim else null
            field = value
        }

    private var animTime = 0f
    private var anim: Animation<TextureAtlas.AtlasRegion>? = null
    private var fallingFor = 0
    var dead = false

    override fun update(deltaTime: Float) {
        if (state == FALLING_SLOW && pos.y < grid2Pos(gridPos).y - LOW_CELL_CEILING) {
            state = FALLING_FAST
        }
        super.update(deltaTime)
        anim?.let { anima ->
            animTime += deltaTime
            if (animTime > anima.animationDuration) {
                dead = true
            }
        }
    }

    override fun getNewDirection(): Direction {
        if (state == PUSHED) {
            state = IDLE
        }
        val below = gridPos.plus(0, -1)
        val canFall = level.blackBlocks.contains(below) && level.fruitAt(below) == null
        val canFallFast = level.highBlackBlocks.contains(below)
        if (!isFalling()) {
            fallingFor = 0
            if (canFall && state != CRASHING) {
                state = if (canFallFast) FALLING_FAST else FALLING_SLOW
            }
        } else {
            fallingFor++
            state = if (state == FALLING_SLOW || fallingFor < FALL_HEIGHT_BEFORE_CRASH) {
                if (!canFall) IDLE else if (canFallFast) FALLING_FAST else FALLING_SLOW
            } else {  // falling really fast
                if (canFallFast) FALLING_FAST else CRASHING
            }
        }
        return when (state) {
            FALLING_FAST, FALLING_SLOW -> Direction.DOWN
            IDLE, CRASHING -> Direction.NONE
            else -> direction
        }
    }

    override fun dig(pos: IntPoint, direction: Direction) {
        if (state != IDLE) level.dig(pos, direction)
    }

    // TODO put all this in move()?
    fun push(dir: Direction): Boolean {
        if (state != IDLE || dir != Direction.LEFT && dir != Direction.RIGHT) {
            return false
        }
        val newPos = gridPos + dir
        if (level.isOut(newPos) || level.fruitAt(newPos) != null) {
            return false
        }
        state = PUSHED
        move(dir)
        return true
    }

    override fun detectCollision(newPos: Vector2): Vector2 {
        if (isFalling()) {
            level.monsters.removeIf { collides(it) }
            if (collides(level.player)) {
                level.killFrank()
            }
        }
        return newPos
    }

    override fun render(batch: SpriteBatch) {
        renderDigging(batch)
        val frame = anim?.getKeyFrame(animTime) ?: textureRegion
        batch.draw(frame, pos.x, pos.y)
    }

    fun isFalling() = state == FALLING_SLOW || state == FALLING_FAST

}