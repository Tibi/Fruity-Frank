package tibi.fruity

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import tibi.fruity.Direction.*


const val SCREEN_WIDTH = 649F // was 646 in original game, 3 columns were only 40 px wide
const val SCREEN_HEIGHT = 378F
const val HEADER_HEIGHT = 28F

const val GRID_START_X = 17F
const val GRID_START_Y = 7F

const val CELL_WIDTH = 41F
const val CELL_HEIGHT = 34F
/**  How much smaller low cells are */
const val LOW_CELL_CEILING = 3f

const val GRID_WIDTH = 15
const val GRID_HEIGHT = 10

const val MONSTER_SPAWN_RATE = 2  // in seconds between monster spawn

enum class Direction { NONE, UP, DOWN, LEFT, RIGHT ;
    fun reverse() = when(this) {
        UP    -> DOWN
        DOWN  -> UP
        RIGHT -> LEFT
        LEFT  -> RIGHT
        NONE  -> NONE
    }
}

fun grid2Pos(gridPos: IntPoint) = Vector2(GRID_START_X + gridPos.x * CELL_WIDTH,
                                          GRID_START_Y + gridPos.y * CELL_HEIGHT )
fun pos2Grid(pos: Vector2) = IntPoint(MathUtils.floor((pos.x - GRID_START_X) / CELL_WIDTH),
                                      MathUtils.floor((pos.y - GRID_START_Y) / CELL_HEIGHT))

data class IntPoint(val x: Int, val y: Int) {
    override fun toString() = "$x, $y"
    fun plus(ox: Int, oy: Int) = IntPoint(x + ox, y + oy)
    operator fun plus(direction: Direction) = when (direction) {
        NONE  -> this
        LEFT  -> IntPoint(x - 1, y)
        RIGHT -> IntPoint(x + 1, y)
        UP    -> IntPoint(x, y + 1)
        DOWN  -> IntPoint(x, y - 1)
    }
}


operator fun Vector2.times(factor: Float) = Vector2(x * factor, y * factor)
operator fun Vector2.plus(other: Vector2) = Vector2(x + other.x, y + other.y)
operator fun Vector2.plusAssign(other: Vector2) { add(other) }

fun TextureRegion.size() = Vector2(regionWidth.toFloat(), regionHeight.toFloat())