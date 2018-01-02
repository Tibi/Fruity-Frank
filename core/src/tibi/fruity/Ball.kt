package tibi.fruity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import tibi.fruity.Direction.*
import kotlin.math.abs

/** The ball frank throws. */
class Ball(val level: Level, atlas: TextureAtlas, val pos: Vector2, frankDir: Direction) {

    val speed = ballSpeed(frankDir) * level.speed
    val tex = atlas.findRegion("frank/ball")

    fun update(deltaTime: Float) {

        updateMove(deltaTime)
      
        // Kill monsters
        level.monsters.filter { it.collides(pos) }.forEach { level.monsters.remove(it) }
    }

    /** returns newPos, newSpeed */
    fun updateMove(deltaTime: Float) {
        val newPos = pos + speed * deltaTime
        val newGpos = pos2Grid(newPos)

        // TODO consider other corners of the ball depending on the direction
        val gpos = pos2Grid(pos)

        if (gpos == newGpos) {
            pos.set(newPos)
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
                //TODO for top and right, move away from wall
                bringInCell(pos, gpos)
                speed.set(reboundSpeed(wallDir))
            } else {
                bringInCell(pos, newGpos)
                updateMove(deltaTime / 2f)  //TODO compute real remaining dt?
            }
            break
        }
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


    fun render(batch: SpriteBatch) {
        batch.draw(tex, pos.x, pos.y)
    }

    fun ballSpeed(frankDir: Direction) = when (frankDir) {
        UP -> Vector2(-1f,  1f)
        DOWN -> Vector2(-1f, -1f)
        RIGHT -> Vector2(1f, 1f)
        LEFT -> Vector2(-1f, -1f)
        NONE -> Vector2(1f, 1f)
    }
}