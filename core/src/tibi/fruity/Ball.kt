package tibi.fruity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import tibi.fruity.Direction.*

/** The ball frank throws. */
class Ball(val level: Level, atlas: TextureAtlas, val pos: Vector2, frankDir: Direction) {

    val speed = ballSpeed(frankDir) * level.speed
    val tex = atlas.findRegion("frank/ball")

    fun update(deltaTime: Float) {
        var newPos = pos + speed * deltaTime

        // TODO consider other corners of the ball depending on the direction
        val gpos = pos2Grid(pos)
        val walls = level.getWalls(gpos, speed)
        for (wallDir in walls) {
            val (wall1, wall2) = wallPoints(gpos, wallDir)
            val intersection = Vector2()
            if (Intersector.intersectSegments(wall1, wall2, pos, newPos, intersection)) {
                newPos = intersection
                rebound(wallDir)
            }
        }
        /*
        for each wall:
            if intersects with pos->newpos => newPos = intersection; speed = rebound(speed, wall)
        if no intersection found and newpos is not in a direct adjacant cell => ???
         */
        pos.set(newPos)

        // Kill monsters
        level.monsters.filter { it.collides(pos) }.forEach { level.monsters.remove(it) }
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

    private fun rebound(wallDir: Direction) {
        when (wallDir) {
            RIGHT, LEFT -> speed.x *= -1
            UP, DOWN    -> speed.y *= -1
            else -> { }
        }
    }

    fun render(batch: SpriteBatch) {
        batch.draw(tex, pos.x, pos.y)
    }

    fun ballSpeed(frankDir: Direction) = when (frankDir) {
        UP -> Vector2(-1f,  1f)
        DOWN -> Vector2(-1f, -1f)
        RIGHT -> Vector2(1f, -1f)
        LEFT -> Vector2(-1f, -1f)
        NONE -> Vector2(0f, 0f)
    }
}