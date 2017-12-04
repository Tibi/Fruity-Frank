package tibi.fruity

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion

abstract class GridItem(val level: Level, gridX: Int, gridY: Int) {

    var x = 0f
    var y = 0f
    var xSpeed = 0f
    var ySpeed = 0f

    var direction = Direction.NONE
    var nextDirection = Direction.NONE

    val gridX get() = x2gridX(x)
    val gridY get() = y2gridY(y)

    init {
        x = gridX2x(gridX)
        y = gridY2y(gridY)
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
                stop()
            }
        }
        else if (xSpeed < 0 && x < GRID_START_X) {
            x = GRID_START_X
            stop()
        }

        if (ySpeed > 0) {
            val maxY = gridY2y(GRID_HEIGHT - 1)
            if (y > maxY) {
                y = maxY
                stop()
            }
        }
        else if (ySpeed < 0 && y < GRID_START_Y) {
            y = GRID_START_Y
            stop()
        }
    }

    fun stop() {
        xSpeed = 0f
        ySpeed = 0f
        direction = Direction.NONE
    }

    open fun setSpeed() {
        xSpeed = 0f
        ySpeed = 0f
        when (direction) {
            Direction.RIGHT -> xSpeed =  level.speedMult
            Direction.UP    -> ySpeed =  level.speedMult
            Direction.LEFT  -> xSpeed = -level.speedMult
            Direction.DOWN  -> ySpeed = -level.speedMult
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
}


open class Perso(level: Level, val anims: AnimationMap, gridX: Int, gridY: Int)
    : GridItem(level, gridX, gridY) {

    enum class State { IDLE, MOVING, FALLING, STOPPING }

    var state = State.IDLE

    init {
        anims.values.forEach { it.playMode = Animation.PlayMode.LOOP }
    }

    var stateTime: Float = 0f

    override fun update(deltaTime: Float) {
        stateTime += deltaTime
        super.update(deltaTime)
    }

    override fun render(batch: SpriteBatch) {
        val anim = anims[direction]
//        println("$state -> $anim")
        if (anim != null) {
            batch.draw(anim.getKeyFrame(stateTime), x, y)
        }
    }
}

class Fruit(level: Level, val textureRegion: TextureRegion, gridX: Int, gridY: Int, val points: Int) : GridItem(level, gridX, gridY) {
    override fun render(batch: SpriteBatch) {
        batch.draw(textureRegion, x, y)
    }

    override fun update(deltaTime: Float) {
        // Not moving or animating
    }
}

class Frank(level: Level, atlas: TextureAtlas) : Perso(level, createAnimations(atlas, "frank/ball "), 0, 0) {
    override fun update(deltaTime: Float) {
//        if (state == State.STOPPING && onGrid()) {
//            xSpeed = 0f
//            ySpeed = 0f
//        }
        super.update(deltaTime)
    }
}

class Monster(level: Level, anims: AnimationMap, x: Int, y: Int) : Perso(level, anims, x, y) {
    override fun update(deltaTime: Float) {

        super.update(deltaTime)
    }
}