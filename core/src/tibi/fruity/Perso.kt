package tibi.fruity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.graphics.g2d.TextureRegion
import tibi.fruity.Direction.*
import tibi.fruity.Level.IntPoint

abstract class GridItem(val level: Level, pos: IntPoint, val speedFactor: Float) {

    var x = 0f
    var y = 0f
    var xSpeed = 0f
    var ySpeed = 0f

    var direction = NONE
    var nextDirection = NONE

    val gridX get() = x2gridX(x)
    val gridY get() = y2gridY(y)

    init {
        x = gridX2x(pos.x)
        y = gridY2y(pos.y)
    }

    val pos: IntPoint get() = IntPoint(gridX, gridY)

    open fun update(deltaTime: Float) {
        val passing = isPassingGridLine(deltaTime)
        // Changes direction if requested and if item is about to pass a grid line.
        if (passing || direction == NONE) {
            val newDirection = getNewDirection()
            if (newDirection != direction) {
                if (direction != NONE) {
                    moveToGrid()
                }
                direction = newDirection
                setSpeed()
            }
        }
        // Prevents running off grid
        if (aboutToHitWall(deltaTime)) {
            hitWall()
        }
        if (direction == NONE) {
            return
        }
        val oldPos = pos
        // Move according to speed
        x += xSpeed * deltaTime
        y += ySpeed * deltaTime
        if (oldPos != pos) {
            gridLinePassed(oldPos)
        }
    }

    /** True when a corner is about to cross a grid line. */
    private fun isPassingGridLine(deltaTime: Float) =
            gridX != x2gridX(x + xSpeed * deltaTime)
         || gridY != y2gridY(y + ySpeed * deltaTime)


    /** Moves x or y to the grid line it is about to pass. */
    open fun moveToGrid() {
        val oldPos = pos
        if (xSpeed > 0f && gridX < GRID_WIDTH - 1) x = gridX2x(gridX + 1)
        if (xSpeed < 0f) x = gridX2x(gridX)
        if (ySpeed > 0f && gridY < GRID_HEIGHT - 1) y = gridY2y(gridY + 1)
        if (ySpeed < 0f) y = gridY2y(gridY)
        gridLinePassed(oldPos)
    }

    open fun gridLinePassed(oldPos: IntPoint) { }

    fun getNextGridPos(): IntPoint {
        if (xSpeed > 0f && gridX < GRID_WIDTH - 1) return IntPoint(gridX + 1, gridY)
        if (ySpeed > 0f && gridY < GRID_HEIGHT - 1) return IntPoint(gridX, gridY + 1)
        return IntPoint(gridX, gridY)
    }

    private fun aboutToHitWall(deltaTime: Float): Boolean = direction != NONE && (
                   xSpeed > 0 && x + xSpeed * deltaTime > gridX2x(GRID_WIDTH - 1)
                || xSpeed < 0 && x + xSpeed * deltaTime < GRID_START_X
                || ySpeed > 0 && y + ySpeed * deltaTime > gridY2y(GRID_HEIGHT - 1)
                || ySpeed < 0 && y + ySpeed * deltaTime < GRID_START_Y)

    open fun getNewDirection() = nextDirection

    abstract fun hitWall()

    fun setSpeed() {
        xSpeed = 0f
        ySpeed = 0f
        when (direction) {
            Direction.RIGHT -> xSpeed =  level.speed * speedFactor
            Direction.UP    -> ySpeed =  level.speed * speedFactor
            Direction.LEFT  -> xSpeed = -level.speed * speedFactor
            Direction.DOWN  -> ySpeed = -level.speed * speedFactor
            NONE  -> {}
        }
    }

    abstract fun render(batch: SpriteBatch)

    fun collides(other: GridItem) = collides(other.x, other.y)
    fun collides(ox: Float, oy: Float) = x in ox-CELL_WIDTH+1 .. ox+CELL_WIDTH-1
                                      && y in oy-CELL_HEIGHT  .. oy+CELL_HEIGHT

    fun avoid(other: GridItem) {
        when (direction) {
            RIGHT -> if (x < other.x) { x = other.x - GRID_WIDTH; move(LEFT) }
            LEFT  -> if (other.x < x) { x = other.x + GRID_WIDTH; move(RIGHT) }
            UP    -> if (y < other.y) { y = other.y - GRID_HEIGHT; move(DOWN) }
            DOWN  -> if (other.y < y) { y = other.y + GRID_HEIGHT; move(UP) }
            NONE  -> { }
        }
    }


    fun requestMove(to: Direction) {
        nextDirection = to
    }

    fun move(to: Direction) {
        direction = to
        setSpeed()
    }

    fun stop() {
        moveToGrid()
        direction = NONE
        nextDirection = NONE
        setSpeed()
    }

    fun putAt(point: IntPoint) {
        x = gridX2x(point.x)
        y = gridY2y(point.y)
    }
}


abstract class Perso(level: Level, val anims: AnimationMap, pos: IntPoint, speedFactor: Float) : GridItem(level, pos, speedFactor) {

    enum class State { IDLE, MOVING, FALLING, STOPPING }

    var state = State.IDLE
    var lastFrame: AtlasRegion? = anims[Direction.RIGHT]?.getKeyFrame(0f)
    var stateTime: Float = 0f

    override fun update(deltaTime: Float) {
        stateTime += deltaTime
        super.update(deltaTime)
    }

    override fun render(batch: SpriteBatch) {
        // when digging up, draw a black square below perso to clear the small ground piece left
        if (direction == Direction.UP) {
            val yUp = gridY2y(gridY) + CELL_HEIGHT - 5
            if (y > yUp) batch.draw(level.blackTex, x, yUp)
        }
        else if (direction == Direction.DOWN) {
            val yDown = gridY2y(gridY) + 5
            if (y < yDown) batch.draw(level.blackTex, x, yDown)
        }
        val anim = anims[direction]
        if (anim != null) {
            lastFrame = anim.getKeyFrame(stateTime)
        }
        batch.draw(lastFrame, x, y)
    }
}

open class Fruit(level: Level, val textureRegion: TextureRegion, pos: IntPoint, val score: Int) : GridItem(level, pos, 0f) {
    override fun render(batch: SpriteBatch) {
        batch.draw(textureRegion, x, y)
    }

    override fun update(deltaTime: Float) {
        // Not moving or animating
    }

    override fun hitWall() {

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

class Frank(level: Level, atlas: TextureAtlas)
    : Perso(level, createAnimations(atlas, "frank/ball "), IntPoint(0, 0), 1f) {

    override fun hitWall() {
        stop()
    }

    override fun gridLinePassed(oldPos: IntPoint) {
        level.dig(direction, oldPos, pos)
    }

    fun die() {
        println("DEAD")
    }
}


open class Monster(level: Level, anims: AnimationMap, pos: IntPoint, speedFactor: Float) : Perso(level, anims, pos, speedFactor) {
    override fun hitWall() {
        move(direction.reverse())
    }

    override fun getNewDirection(): Direction {
        val onPath = level.getDirectionsOnPath(getNextGridPos())
        if (onPath.isEmpty()) return direction
        val noReverse = onPath.filter { it != direction.reverse() }
        return if (noReverse.isEmpty()) onPath.first() else noReverse.shuffled()[0]
    }
}