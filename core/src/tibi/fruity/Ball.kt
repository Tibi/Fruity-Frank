package tibi.fruity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import tibi.fruity.Direction.*
import kotlin.math.abs

/** The ball frank throws. */
class Ball(val level: Level, val tex: AtlasRegion, frankPos: Vector2, frankDir: Direction) {

    val size = Vector2(tex.originalWidth.toFloat(), tex.originalHeight.toFloat())
    val pos = startPos(frankPos, frankDir)
    val speed = startSpeed(frankDir) * level.speed
    var beingThrown = true
    var dead = false

    /** Inside Frank, on the side of its direction. */
    private fun startPos(frankPos: Vector2, frankDir: Direction) = when (frankDir) {
        RIGHT -> Vector2(frankPos.x + CELL_WIDTH - size.x - 1, frankPos.y + CELL_HEIGHT - size.y)
        LEFT  -> Vector2(frankPos.x + 1, frankPos.y + CELL_HEIGHT - size.y)
        UP    -> Vector2(frankPos.x + CELL_WIDTH - size.x, frankPos.y + CELL_HEIGHT - size.y - LOW_CELL_CEILING - 1)
        DOWN  -> Vector2(frankPos.x + CELL_WIDTH - size.x, frankPos.y + 1)
        else  -> Vector2()
    }

    fun startSpeed(frankDir: Direction) = when (frankDir) {
        UP    -> Vector2(-1f,  1f)
        DOWN  -> Vector2(-1f, -1f)
        RIGHT -> Vector2(1f, -1f)
        LEFT  -> Vector2(-1f, -1f)
        NONE  -> Vector2(1f, 1f)
    } * 3f

    fun update(deltaTime: Float) {
        updateMove(deltaTime)
        detectCollisions()
    }

    fun updateMove(deltaTime: Float) {
        movePosToCorner()
        val gpos = pos2Grid(pos)
        val newPos = pos + speed * deltaTime
        val newGpos = pos2Grid(newPos)
        if (gpos == newGpos) {
            restorePosFromCorner()
            pos.set(pos + speed * deltaTime)
            return
        }
        // check for rebound
        for (wallDir in getBorders(speed)) {
            val (wall1, wall2) = wallPoints(gpos, wallDir)
            val intersection = Vector2()
            if (!Intersector.intersectSegments(wall1, wall2, pos, newPos, intersection)) {
                continue  // wrong border
            }
            pos.set(intersection)
            if (level.hasWall(gpos, wallDir)) {
                bringInCell(pos, gpos)
                restorePosFromCorner()
                speed.set(reboundSpeed(wallDir))
                beingThrown = false
            } else {
                bringInCell(pos, newGpos)
                restorePosFromCorner()
                updateMove(deltaTime / 2f)  //TODO compute real remaining dt?
            }
            break
        }
    }

    /** Moves pos to the corner that can hit a wall */
    private fun movePosToCorner() {
        if (speed.x > 0) pos.x += size.x
        if (speed.y > 0) pos.y += size.y + LOW_CELL_CEILING
    }

    /** Moves pos back to the lower left corner */
    private fun restorePosFromCorner() {
        if (speed.x > 0) pos.x -= size.x
        if (speed.y > 0) pos.y -= size.y + LOW_CELL_CEILING
    }

    private fun bringInCell(pos: Vector2, gpos: IntPoint) {
        val grid = grid2Pos(gpos)
        val epsilon = 0.001f
        when {
            abs(grid.x - pos.x) < epsilon -> pos.x = grid.x
            abs(grid.x + CELL_WIDTH - pos.x) < epsilon -> pos.x = grid.x + CELL_WIDTH - epsilon
            abs(grid.y - pos.y) < epsilon -> pos.y = grid.y
            abs(grid.y + CELL_HEIGHT - pos.y) < epsilon -> pos.y = grid.y + CELL_HEIGHT - epsilon
        }
    }

    fun getBorders(speed: Vector2): Set<Direction> {
        val walls = HashSet<Direction>()
        if (speed.x > 0f) {
            walls.add(RIGHT)
        }
        if (speed.x < 0f) {
            walls.add(LEFT)
        }
        if (speed.y > 0f) {
            walls.add(UP)
        }
        if (speed.y < 0f) {
            walls.add(DOWN)
        }
        return walls
    }

    private fun wallPoints(gpos: IntPoint, wallDir: Direction): Pair<Vector2, Vector2> {
        val pos = grid2Pos(gpos)
        return when (wallDir) {
            RIGHT -> Pair(Vector2(pos.x + CELL_WIDTH, pos.y), Vector2(pos.x + CELL_WIDTH, pos.y + CELL_HEIGHT))
            LEFT  -> Pair(Vector2(pos.x, pos.y), Vector2(pos.x, pos.y + CELL_HEIGHT))
            UP    -> Pair(Vector2(pos.x, pos.y + CELL_HEIGHT), Vector2(pos.x + CELL_WIDTH, pos.y + CELL_HEIGHT))
            DOWN  -> Pair(Vector2(pos.x, pos.y), Vector2(pos.x + CELL_WIDTH, pos.y))
            NONE  -> Pair(Vector2(), Vector2())
        }
    }

    private fun reboundSpeed(wallDir: Direction) = when (wallDir) {
        RIGHT, LEFT -> Vector2(-speed.x, speed.y)
        UP, DOWN -> Vector2(speed.x, -speed.y)
        else -> speed
    }

    private fun detectCollisions() {
        // Kill monsters
        level.monsters.firstOrNull { collides(it) }?.let {
            level.killMonster(it)
            dead = true
        }
        // Let Frank catch the ball
        if (!beingThrown && collides(level.frank)) {
            level.frank.catchBall()
            dead = true
        }
        // Gate catches the ball
        if (collides(level.gatePos)) {
            dead = true
        }
    }

    private fun collides(gridPos: IntPoint) = collides(pos, size, grid2Pos(gridPos), gridItemSize)

    private fun collides(item: GridItem) = item.collides(pos, size)

    fun render(batch: SpriteBatch) {
        batch.draw(tex, pos.x, pos.y)
    }
}