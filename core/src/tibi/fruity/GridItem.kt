package tibi.fruity

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import tibi.fruity.Apple.AppleState.*
import tibi.fruity.Direction.*


/** A game object that can move along the grid. */
abstract class GridItem(val level: Level, var gridPos: IntPoint, var speedFactor: Float) {

    var pos = grid2Pos(gridPos)
    var targetGridPos = gridPos
    var speed = Vector2()
    var direction = NONE
        set(value) {
            if (value != NONE) lastDir = value
            field = value
        }
    var lastDir = RIGHT

    open fun update(deltaTime: Float) {
        setSpeed()
        val newPos = pos + speed * deltaTime
        if (reachedTarget(newPos)) {
            gridPos = targetGridPos
            dig(gridPos, direction)
            direction = getNewDirection()
            targetGridPos = gridPos + direction
            pos = grid2Pos(gridPos)
        } else {
            pos = newPos
        }
    }

    private fun reachedTarget(newPos: Vector2): Boolean {
        val targetPos = grid2Pos(targetGridPos)
        return when (direction) {
            NONE  -> true
            RIGHT -> newPos.x >= targetPos.x
            LEFT  -> newPos.x <= targetPos.x
            UP    -> newPos.y >= targetPos.y
            DOWN  -> newPos.y <= targetPos.y
        }
    }

    private fun setSpeed() {
        val newSpeed = level.speed * speedFactor
        speed = when (direction) {
            RIGHT -> Vector2(newSpeed, 0f)
            UP    -> Vector2(0f, newSpeed)
            LEFT  -> Vector2(- newSpeed, 0f)
            DOWN  -> Vector2(0f, - newSpeed)
            NONE  -> Vector2()
        }
    }

    open fun dig(pos: IntPoint, direction: Direction) { }

    abstract fun getNewDirection(): Direction

    /** Returns true when a collision is found at the new position and item can't move to it. */
    open fun detectCollision(newPos: Vector2): Boolean {
        return !level.isPositionFree(newPos, this)
    }

    abstract fun render(batch: SpriteBatch)

    fun collides(other: GridItem) = collides(other.pos)
    fun collides(bottomLeft: Vector2) = pos.x in bottomLeft.x-CELL_WIDTH+1 .. bottomLeft.x+CELL_WIDTH-1
                                     && pos.y in bottomLeft.y-CELL_HEIGHT  .. bottomLeft.y+CELL_HEIGHT

    fun move(to: Direction) {
        direction = to
        targetGridPos = gridPos + direction
    }

    fun putAt(point: IntPoint) {
        gridPos = point
        pos = grid2Pos(point)
        targetGridPos = gridPos
        direction = NONE
    }

    override fun toString() = javaClass.simpleName + " " + gridPos.toString()

    /** when digging up, draw a black square below perso to clear the small ground piece left */
    protected fun renderDigging(batch: SpriteBatch) {
        if (direction == UP) {
            val yUp = grid2Pos(gridPos).y + CELL_HEIGHT - 5
            if (pos.y > yUp) batch.draw(level.blackTex, pos.x, yUp)
        } else if (direction == DOWN) {
            val yDown = grid2Pos(targetGridPos).y + 5
            if (pos.y < yDown) batch.draw(level.blackTex, pos.x, yDown)
        }
    }
}


open class Fruit(level: Level, val textureRegion: TextureRegion, pos: IntPoint, val score: Int)
    : GridItem(level, pos, 0f) {

    override fun getNewDirection() = NONE

    override fun render(batch: SpriteBatch) {
        batch.draw(textureRegion, pos.x, pos.y)
    }
}


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
            //TODO check if it works
            // just an idea so that apples block monsters and balls
            if (state == IDLE) level.blackBlocks.add(gridPos)
            field = value
        }

    private var animTime = 0f
    private var anim: Animation<AtlasRegion>? = null
    private var fallingFor = 0

    override fun update(deltaTime: Float) {
        if (state == FALLING_SLOW && pos.y < grid2Pos(gridPos).y - 3) {
            state = FALLING_FAST
        }
        super.update(deltaTime)
        anim?.let { anima ->
            animTime += deltaTime
            if (animTime > anima.animationDuration) {
                level.deadApples.add(this)
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
            state = if (state == FALLING_SLOW || fallingFor < 3) {
                if (!canFall) IDLE else if (canFallFast) FALLING_FAST else FALLING_SLOW
            } else {  // falling really fast
                if (canFallFast) FALLING_FAST else CRASHING
            }
        }
        return when (state) {
            FALLING_FAST, FALLING_SLOW -> DOWN
            IDLE, CRASHING -> NONE
            else -> direction
        }
    }

    override fun dig(pos: IntPoint, direction: Direction) {
        level.dig(pos, direction)
    }

    // TODO put all this in move()?
    fun push(dir: Direction): Boolean {
        if (state != IDLE || dir != LEFT && dir != RIGHT) {
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

    override fun render(batch: SpriteBatch) {
        renderDigging(batch)
        val frame = anim?.getKeyFrame(animTime) ?: textureRegion
        batch.draw(frame, pos.x, pos.y)
    }

    fun isFalling() = state == FALLING_SLOW || state == FALLING_FAST

}
