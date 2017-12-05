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

    open fun update(deltaTime: Float) {
        // Changes direction if requested and if item just passed a grid line
        if (nextDirection != direction) {
            when (direction) {
                Direction.NONE -> {
                    direction = nextDirection
                    setSpeed()
                }
                Direction.LEFT, Direction.RIGHT -> {
                    val gridXAfter = x2gridX(x + xSpeed * deltaTime)
                    if (gridX != gridXAfter) {
                        x = gridX2x(if (xSpeed < 0) gridX else gridXAfter)
                        direction = nextDirection
                        setSpeed()
                    }
                }
                Direction.UP, Direction.DOWN -> {
                    val gridYAfter = y2gridY(y + ySpeed * deltaTime)
                    if (gridY != gridYAfter) {
                        y = gridY2y(if (ySpeed < 0) gridY else gridYAfter)
                        direction = nextDirection
                        setSpeed()
                    }
                }
            }
        }
        // Move according to speed
        x += xSpeed * deltaTime
        y += ySpeed * deltaTime

        // Prevents running off grid
        if (xSpeed > 0) {
            val maxX = gridX2x(GRID_WIDTH - 1)
            if (x > maxX) {
                x = maxX
                hitWall()
            }
        }
        else if (xSpeed < 0 && x < GRID_START_X) {
            x = GRID_START_X
            hitWall()
        }

        if (ySpeed > 0) {
            val maxY = gridY2y(GRID_HEIGHT - 1)
            if (y > maxY) {
                y = maxY
                hitWall()
            }
        }
        else if (ySpeed < 0 && y < GRID_START_Y) {
            y = GRID_START_Y
            hitWall()
        }
    }

    open fun hitWall() {
        xSpeed = 0f
        ySpeed = 0f
        direction = Direction.NONE
    }

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

    fun move(direction: Direction) {
        this.direction = direction
        nextDirection  = direction
        setSpeed()
    }
}


open class Perso(level: Level, val anims: AnimationMap, pos: IntPoint, speedFactor: Float) : GridItem(level, pos, speedFactor) {

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

class Fruit(level: Level, val textureRegion: TextureRegion, pos: IntPoint, val points: Int) : GridItem(level, pos, 0f) {
    override fun render(batch: SpriteBatch) {
        batch.draw(textureRegion, x, y)
    }

    override fun update(deltaTime: Float) {
        // Not moving or animating
    }
}

class Frank(level: Level, atlas: TextureAtlas)
    : Perso(level, createAnimations(atlas, "frank/ball "), IntPoint(0, 0), 1f)


class Monster(level: Level, anims: AnimationMap, pos: IntPoint, speedFactor: Float) : Perso(level, anims, pos, speedFactor) {
    override fun hitWall() {
        move(direction.reverse())
    }
}