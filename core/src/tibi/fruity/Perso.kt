package tibi.fruity

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion

open class GridItem(val level: Level, var gridX: Int, var gridY: Int) {

    var x = 0f
    var y = 0f
    var xSpeed = 0f
    var ySpeed = 0f

    init {
        x = Companion.gridX2x(gridX)
        y = Companion.gridY2y(gridY)
    }

    open fun update(deltaTime: Float) {
        if (xSpeed != 0f) {
            x += xSpeed * deltaTime
            if (xSpeed > 0 && x >= gridX2x(gridX)
                    || xSpeed < 0 && x <= gridX2x(gridX)) {
                x = gridX2x(gridX)
                xSpeed = 0f
            }
        }
        if (ySpeed != 0f) {
            y += ySpeed * deltaTime
            if (ySpeed > 0 && y >= gridY2y(gridY)
                    || ySpeed < 0 && y <= gridY2y(gridY)) {
                ySpeed = 0f
                y = gridY2y(gridY)
            }
        }
    }

    open fun move(dir: Direction) {
        if (xSpeed != 0f || ySpeed != 0f) return
        when (dir) {
            Direction.RIGHT -> if (gridX < GRID_WIDTH - 1) {
                gridX++
                xSpeed = level.speedMult
            }
            Direction.LEFT -> if (gridX > 0) {
                gridX--
                xSpeed = -level.speedMult
            }
            Direction.UP -> if (gridY < GRID_HEIGHT - 1) {
                gridY++
                ySpeed = level.speedMult
            }
            Direction.DOWN -> if (gridY > 0) {
                gridY--
                ySpeed = -level.speedMult
            }
        }
    }

    open fun render(batch: SpriteBatch, deltaTime: Float) {
        update(deltaTime)
    }

    fun collides(other: GridItem) = x in other.x-CELL_WIDTH+1   .. other.x+CELL_WIDTH-1
                                 && y in other.y- CELL_HEIGHT   .. other.y+ CELL_HEIGHT

    companion object {
        fun gridY2y(gridY: Int) = GRID_START_Y + gridY * (CELL_HEIGHT + GRID_MARGIN)
        fun gridX2x(gridX: Int) = GRID_START_X + gridX * CELL_WIDTH
    }
}


open class Perso(level: Level, val anims: AnimationMap, gridX: Int, gridY: Int)
    : GridItem(level, gridX, gridY) {

    enum class State { IDLE, MOVING, FALLING }

    var state = State.IDLE
    var direction = Direction.RIGHT

    init {
        anims.values.forEach { it.playMode = Animation.PlayMode.LOOP }
    }

    var stateTime: Float = 0f

    override fun update(deltaTime: Float) {
        stateTime += deltaTime
        super.update(deltaTime)
        if (xSpeed == 0f && ySpeed == 0f) {
            state = State.IDLE
        }
    }

    override fun render(batch: SpriteBatch, deltaTime: Float) {
        super.render(batch, deltaTime)
        val anim = anims[direction]
//        println("$state -> $anim")
        if (anim != null) {
            batch.draw(anim.getKeyFrame(stateTime), x, y)
        }
    }

    override fun move(dir: Direction) {
        super.move(dir)
        state = State.MOVING
    }
}

class Fruit(level: Level, val textureRegion: TextureRegion, gridX: Int, gridY: Int, val points: Int) : GridItem(level, gridX, gridY) {
    override fun render(batch: SpriteBatch, deltaTime: Float) {
        super.render(batch, deltaTime)
        batch.draw(textureRegion, x, y)
    }
}

class Frank(level: Level, atlas: TextureAtlas) : Perso(level, createAnimations(atlas, "frank/ball "), 0, 0)

class Monster(level: Level, anims: AnimationMap, x: Int, y: Int) : Perso(level, anims, x, y) {
    override fun update(deltaTime: Float) {

        super.update(deltaTime)
    }
}