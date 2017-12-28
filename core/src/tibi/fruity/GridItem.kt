package tibi.fruity

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import tibi.fruity.Apple.AppleState.*
import tibi.fruity.Direction.DOWN
import tibi.fruity.Direction.NONE


/** A game object that can move along the grid. */
abstract class GridItem(val level: Level, gridPos: IntPoint, var speedFactor: Float) {

    var pos: Vector2
    var speed = Vector2()

    var direction = NONE
    var nextDirection = NONE

    init {
        pos = grid2Pos(gridPos)
    }

    val gridPos: IntPoint get() = pos2Grid(pos)

    open fun update(deltaTime: Float) {
        setSpeed()
        var newPos = pos + speed * deltaTime
        val newGridPos = pos2Grid(newPos)
        val closestGridPos = when (direction) {
            Direction.RIGHT, Direction.UP -> newGridPos
            Direction.LEFT, DOWN, NONE -> gridPos
        }
        // Changes direction if requested and if item is about to pass a grid line.
        if (gridPos != newGridPos || direction == NONE) {
            dig(gridPos, newGridPos)
            val newDirection = getNewDirection(closestGridPos)
            if (newDirection != direction) {
                newPos = grid2Pos(closestGridPos) // TODO also move in the new direction
                direction = newDirection
            }
        }
        if (newPos != pos) {
            if (detectCollision(newPos)) {
                direction = direction.reverse()
            } else {
                pos = newPos
            }
        }
    }

    fun setSpeed() {
        val newSpeed = level.speed * speedFactor
        speed = when (direction) {
            Direction.RIGHT -> Vector2(newSpeed, 0f)
            Direction.UP    -> Vector2(0f, newSpeed)
            Direction.LEFT  -> Vector2(- newSpeed, 0f)
            DOWN  -> Vector2(0f, - newSpeed)
            NONE -> Vector2()
        }
    }

    open fun dig(oldPos: IntPoint, newPos: IntPoint) { }

    open fun getNewDirection(closestGridPos: IntPoint): Direction {
        if (nextDirection == Direction.LEFT && closestGridPos.x == 0
         || nextDirection == DOWN && closestGridPos.y == 0
         || nextDirection == Direction.RIGHT && closestGridPos.x == GRID_WIDTH - 1
         || nextDirection == Direction.UP && closestGridPos.y == GRID_HEIGHT - 1) {
            return NONE
        }
        return nextDirection
    }

    /** Returns true when a collision is found at the new position and item can't move to it. */
    open fun detectCollision(newPos: Vector2): Boolean {
        return !level.isPositionFree(newPos, this)
    }

    abstract fun render(batch: SpriteBatch)

    fun collides(other: GridItem) = collides(other.pos)
    fun collides(bottomLeft: Vector2) = pos.x in bottomLeft.x-CELL_WIDTH+1 .. bottomLeft.x+CELL_WIDTH-1
                                     && pos.y in bottomLeft.y-CELL_HEIGHT  .. bottomLeft.y+CELL_HEIGHT

    fun requestMove(to: Direction) {
        nextDirection = to
    }

    fun move(to: Direction) {
        direction = to
    }

    fun stop() {
        pos = grid2Pos(gridPos)
        direction = NONE
        nextDirection = NONE
        speed = Vector2()
    }

    fun putAt(point: IntPoint) {
        pos = grid2Pos(point)
    }

    override fun toString(): String {
        return gridPos.toString()
    }
}


open class Fruit(level: Level, val textureRegion: TextureRegion, pos: IntPoint, val score: Int)
    : GridItem(level, pos, 0f) {

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
            field = value
        }

    var animTime = 0f
    var anim: Animation<AtlasRegion>? = null

    override fun update(deltaTime: Float) {
        if (state == FALLING_SLOW && pos != grid2Pos(gridPos) && pos.y < grid2Pos(gridPos).y + CELL_HEIGHT - 3) {
            state = FALLING_FAST
        }
        super.update(deltaTime)
        if (anim != null) {
            animTime += deltaTime
            if (animTime > 1.2f) {
                level.deadApples.add(this)
            }
        }
    }

    override fun getNewDirection(closestGridPos: IntPoint): Direction {
        if (state == PUSHED && !collides(level.player)) state = IDLE
        val below = closestGridPos.plus(0, -1)
        state = when {
            level.highBlackBlocks.contains(below) -> FALLING_FAST
            level.blackBlocks.contains(below) -> FALLING_SLOW
            else -> if (state == FALLING_FAST || state == FALLING_SLOW) CRASHING else state
        }
        return when (state) {
            FALLING_FAST, FALLING_SLOW -> DOWN
            IDLE, CRASHING -> NONE
            else -> direction
        }
    }

    fun push(direction: Direction) {
        state = PUSHED
        move(direction)
    }

    override fun render(batch: SpriteBatch) {
        val frame = anim?.getKeyFrame(animTime) ?: textureRegion
        batch.draw(frame, pos.x, pos.y)
    }

}
