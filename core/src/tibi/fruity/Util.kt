package tibi.fruity

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Timer
import tibi.fruity.Direction.*

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
                                          GRID_START_Y + gridPos.y * CELL_HEIGHT)
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

fun collides(aPos: Vector2, aSize: Vector2, bPos: Vector2, bSize: Vector2) =
        aPos.x in bPos.x - aSize.x + 1 .. bPos.x + bSize.x - 1
     && aPos.y in bPos.y - aSize.y     .. bPos.y + bSize.y

// from KTX.async
inline fun schedule(
        delaySeconds: Float,
        crossinline task: () -> Unit) = Timer.schedule(object : Timer.Task() {
    override fun run() {
        task()
    }
}, delaySeconds)!!

/** Makes a text appear progressively. */
fun textAppear(text: String, duration: Float, currentTime: Float): String {
    if (currentTime > duration) return text
    val pos = text.length / duration * currentTime
    return text.substring(0..pos.toInt())
}
