package tibi.fruity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2


operator fun Vector2.times(factor: Float) = Vector2(x * factor, y * factor)
operator fun Vector2.plus(other: Vector2) = Vector2(x + other.x, y + other.y)

/** A game object that can move along the grid. */
abstract class GridItem(val level: Level, gridPos: IntPoint, val speedFactor: Float) {

    var pos: Vector2
    var speed = Vector2()

    var direction = Direction.NONE
    var nextDirection = Direction.NONE

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
            Direction.LEFT, Direction.DOWN, Direction.NONE -> gridPos
        }
        // Changes direction if requested and if item is about to pass a grid line.
        if (gridPos != newGridPos || direction == Direction.NONE) {
            dig(gridPos, newGridPos)
            val newDirection = getNewDirection(closestGridPos)
            if (newDirection != direction) {
                newPos = grid2Pos(closestGridPos) // TODO also move in the new direction
                direction = newDirection
            }
        }
        if (!detectCollision(newPos)) {
            pos = newPos
        }
    }

    fun setSpeed() {
        val newSpeed = level.speed * speedFactor
        speed = when (direction) {
            Direction.RIGHT -> Vector2(newSpeed, 0f)
            Direction.UP    -> Vector2(0f, newSpeed)
            Direction.LEFT  -> Vector2(- newSpeed, 0f)
            Direction.DOWN  -> Vector2(0f, - newSpeed)
            Direction.NONE -> Vector2()
        }
    }

    open fun dig(oldPos: IntPoint, newPos: IntPoint) { }

    open fun getNewDirection(closestGridPos: IntPoint): Direction {
        if (nextDirection == Direction.LEFT && closestGridPos.x == 0
         || nextDirection == Direction.DOWN && closestGridPos.y == 0
         || nextDirection == Direction.RIGHT && closestGridPos.x == GRID_WIDTH - 1
         || nextDirection == Direction.UP && closestGridPos.y == GRID_HEIGHT - 1) {
            return Direction.NONE
        }
        return nextDirection
    }

    /** Returns true when a collision is found at the new position. */
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
        setSpeed()
    }

    fun stop() {
        pos = grid2Pos(gridPos)
        direction = Direction.NONE
        nextDirection = Direction.NONE
        speed = Vector2()
    }

    fun putAt(point: IntPoint) {
        pos = grid2Pos(point)
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

    enum class AppleState { IDLE, PUSHED, FALLING, CRASHING }
    var state = AppleState.IDLE

    fun isFalling() = state == AppleState.FALLING

    override fun update(deltaTime: Float) {
        // IDLE
        // above black => fall slowly
        // above high black => fall

        // FALLING
        //
        super.update(deltaTime)
    }

}
