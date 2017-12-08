package tibi.fruity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.graphics.g2d.TextureRegion
import tibi.fruity.Level.IntPoint

abstract class GridItem(val level: Level, pos: IntPoint, val speedFactor: Float) {

    var x = 0f
    var y = 0f
    var xSpeed = 0f
    var ySpeed = 0f

    var direction = Direction.NONE
    var nextDirection = Direction.NONE

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
        if (passing || direction == Direction.NONE) {
            val newDirection = getNewDirection()
            if (newDirection != direction) {
                if (direction != Direction.NONE) {
                    val oldPos = pos
                    moveToGrid()
                    gridLinePassed(oldPos)
                }
                direction = newDirection
                setSpeed()
            }
        }
        if (direction == Direction.NONE) {
            return
        }
        // Prevents running off grid
        if (aboutToHitWall(deltaTime)) {
            hitWall()
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
        if (xSpeed > 0f && gridX < GRID_WIDTH - 1) x = gridX2x(gridX + 1)
        if (xSpeed < 0f) x = gridX2x(gridX)
        if (ySpeed > 0f && gridY < GRID_HEIGHT - 1) y = gridY2y(gridY + 1)
        if (ySpeed < 0f) y = gridY2y(gridY)
    }

    open fun gridLinePassed(oldPos: IntPoint) { }

    fun getNextGridPos(): IntPoint {
        if (xSpeed > 0f && gridX < GRID_WIDTH - 1) return IntPoint(gridX + 1, gridY)
        if (ySpeed > 0f && gridY < GRID_HEIGHT - 1) return IntPoint(gridX, gridY + 1)
        return IntPoint(gridX, gridY)
    }

    private fun aboutToHitWall(deltaTime: Float): Boolean =
                   xSpeed > 0 && x + xSpeed * deltaTime > gridX2x(GRID_WIDTH - 1)
                || xSpeed < 0 && x + xSpeed * deltaTime < GRID_START_X
                || ySpeed > 0 && y + ySpeed * deltaTime > gridY2y(GRID_HEIGHT - 1)
                || ySpeed < 0 && y + ySpeed * deltaTime < GRID_START_Y

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
            Direction.NONE  -> {}
        }
    }

    abstract fun render(batch: SpriteBatch)

    fun collides(other: GridItem) = x in other.x-CELL_WIDTH+1 .. other.x+CELL_WIDTH-1
                                 && y in other.y-CELL_HEIGHT  .. other.y+CELL_HEIGHT

    companion object {
        fun gridX2x(gridX: Int) = GRID_START_X + gridX * CELL_WIDTH
        fun gridY2y(gridY: Int) = GRID_START_Y + gridY * (CELL_HEIGHT + GRID_MARGIN)
        fun x2gridX(x: Float) = ((x - GRID_START_X) / CELL_WIDTH).toInt()
        fun y2gridY(y: Float) = ((y - GRID_START_Y) / (CELL_HEIGHT + GRID_MARGIN)).toInt()
    }

    fun requestMove(to: Direction) {
        nextDirection = to
    }

    fun move(to: Direction) {
        direction = to
        setSpeed()
    }

    fun stop() {
        val oldPos = pos
        moveToGrid()
        gridLinePassed(oldPos)
        direction = Direction.NONE
        nextDirection = Direction.NONE
        setSpeed()
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