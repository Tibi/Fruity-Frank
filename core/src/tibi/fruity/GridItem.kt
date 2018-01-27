package tibi.fruity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import tibi.fruity.Direction.*

val gridItemSize = Vector2(CELL_WIDTH, CELL_HEIGHT - LOW_CELL_CEILING)

/** A game object that can move along the grid. */
abstract class GridItem(val gameScreen: GameScreen, var gridPos: IntPoint, var speedFactor: Float) {

    var pos = grid2Pos(gridPos)
    var targetGridPos = gridPos
    var speed = Vector2()
    var direction = NONE
        set(value) {
            if (value != NONE) lastDir = value
            field = value
        }
    var lastDir = RIGHT

    open fun update(deltaTime: Float) {
        setSpeed()
        val newPos = pos + speed * deltaTime
        if (reachedTarget(newPos)) {
            gridPos = targetGridPos
            dig(gridPos, direction)
            direction = getNewDirection()
            targetGridPos = gridPos + direction
            newPos.set(grid2Pos(gridPos))
        }
        if (newPos != pos) {
            pos = detectCollision(newPos)
        }
    }

    private fun reachedTarget(newPos: Vector2): Boolean {
        val targetPos = grid2Pos(targetGridPos)
        return when (direction) {
            NONE  -> true
            RIGHT -> newPos.x >= targetPos.x
            LEFT  -> newPos.x <= targetPos.x
            UP    -> newPos.y >= targetPos.y
            DOWN  -> newPos.y <= targetPos.y
        }
    }

    private fun setSpeed() {
        val newSpeed = gameScreen.speed * speedFactor
        speed = when (direction) {
            RIGHT -> Vector2(newSpeed, 0f)
            UP    -> Vector2(0f, newSpeed)
            LEFT  -> Vector2(- newSpeed, 0f)
            DOWN  -> Vector2(0f, - newSpeed)
            NONE  -> Vector2()
        }
    }

    open fun dig(pos: IntPoint, direction: Direction) { }

    abstract fun getNewDirection(): Direction

    /** Override and return the new position to use after collision. */
    open fun detectCollision(newPos: Vector2) = newPos

    abstract fun render(batch: SpriteBatch)

    fun collides(other: GridItem) = collides(other.pos)
    fun collides(otherPos: Vector2, otherSize: Vector2 = gridItemSize) = collides(pos, gridItemSize, otherPos, otherSize)

    fun move(to: Direction) {
        direction = to
        targetGridPos = gridPos + direction
    }

    fun putAt(point: IntPoint) {
        gridPos = point
        pos = grid2Pos(point)
        targetGridPos = gridPos
        direction = NONE
    }

    override fun toString() = javaClass.simpleName + " " + gridPos.toString()

    /** when digging up, draw a black square below perso to clear the small ground piece left */
    protected fun renderDigging(batch: SpriteBatch) {
        if (direction == UP) {
            val yUp = grid2Pos(gridPos).y + CELL_HEIGHT - 5
            if (pos.y > yUp) batch.draw(gameScreen.blackTex, pos.x, yUp)
        } else if (direction == DOWN) {
            val yDown = grid2Pos(targetGridPos).y + 5
            if (pos.y < yDown) batch.draw(gameScreen.blackTex, pos.x, yDown)
        }
    }
}


open class Fruit(gameScreen: GameScreen, val textureRegion: TextureRegion, pos: IntPoint, val score: Int)
    : GridItem(gameScreen, pos, 0f) {

    override fun getNewDirection() = NONE

    override fun render(batch: SpriteBatch) {
        batch.draw(textureRegion, pos.x, pos.y)
    }
}
