package tibi.fruity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import tibi.fruity.Direction.*

operator fun Vector2.times(factor: Float) = Vector2(x * factor, y * factor)
operator fun Vector2.plus(other: Vector2) = Vector2(x + other.x, y + other.y)

abstract class GridItem(val level: Level, gridPos: IntPoint, val speedFactor: Float) {

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
            RIGHT, UP -> newGridPos
            LEFT, DOWN, NONE -> gridPos
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
            NONE  -> Vector2()
        }
    }

    open fun dig(oldPos: IntPoint, newPos: IntPoint) { }

    open fun getNewDirection(closestGridPos: IntPoint): Direction {
        if (nextDirection == LEFT  && closestGridPos.x == 0
         || nextDirection == DOWN  && closestGridPos.y == 0
         || nextDirection == RIGHT && closestGridPos.x == GRID_WIDTH - 1
         || nextDirection == UP    && closestGridPos.y == GRID_HEIGHT - 1) {
            return NONE
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
        direction = NONE
        nextDirection = NONE
        speed = Vector2()
    }

    fun putAt(point: IntPoint) {
        pos = grid2Pos(point)
    }
}


abstract class Perso(level: Level, val anims: AnimationMap, pos: IntPoint, speedFactor: Float) : GridItem(level, pos, speedFactor) {

    var lastFrame: AtlasRegion? = anims[Direction.RIGHT]?.getKeyFrame(0f)
    var stateTime: Float = 0f

    override fun update(deltaTime: Float) {
        stateTime += deltaTime
        super.update(deltaTime)
    }

    override fun render(batch: SpriteBatch) {
        // when digging up, draw a black square below perso to clear the small ground piece left
        if (direction == Direction.UP) {
            val yUp = grid2Pos(gridPos).y + CELL_HEIGHT - 5
            if (pos.y > yUp) batch.draw(level.blackTex, pos.x, yUp)
        }
        else if (direction == Direction.DOWN) {
            val yDown = grid2Pos(gridPos).y + 5
            if (pos.y < yDown) batch.draw(level.blackTex, pos.x, yDown)
        }
        val anim = anims[direction]
        if (anim != null) {
            lastFrame = anim.getKeyFrame(stateTime)
        }
        batch.draw(lastFrame, pos.x, pos.y)
    }
}

open class Fruit(level: Level, val textureRegion: TextureRegion, pos: IntPoint, val score: Int) : GridItem(level, pos, 0f) {
    override fun render(batch: SpriteBatch) {
        batch.draw(textureRegion, pos.x, pos.y)
    }
}

class Apple(level: Level, pos: IntPoint) : Fruit(level, level.appleTex, pos, 0) {

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

open class Monster(level: Level, anims: AnimationMap, pos: IntPoint, speedFactor: Float) : Perso(level, anims, pos, speedFactor) {

    override fun getNewDirection(closestGridPos: IntPoint): Direction {
        val onPath = level.getDirectionsOnPath(closestGridPos)
        if (onPath.isEmpty()) return direction
        if (onPath.size == 1) return onPath.first()
        return onPath.filter { it != direction.reverse() }.shuffled()[0]
    }
    override fun detectCollision(newPos: Vector2): Boolean {
        val col = super.detectCollision(newPos)
        if (col) {
            direction = direction.reverse()
        }
        //TODO kill player
        return col
    }

}

class Frank(level: Level, atlas: TextureAtlas)
    : Perso(level, createAnimations(atlas, "frank/ball "), IntPoint(0, 0), 1f) {

    override fun dig(oldPos: IntPoint, newPos: IntPoint) {
        level.dig(direction, oldPos, newPos)
    }

    override fun detectCollision(newPos: Vector2): Boolean {
        val newGridPos = when (direction) {
            RIGHT -> pos2Grid(newPos) + IntPoint(1, 0)
            UP    -> pos2Grid(newPos) + IntPoint(0, 1)
            else  -> pos2Grid(newPos)
        }
        level.fruits.find { it.gridPos == newGridPos }?.let { level.eat(it) }
        if (level.monsters.any { it.collides(this) }) {
            die()
        }
        return false
    }

    fun die() {
        println("DEAD")
    }
}
