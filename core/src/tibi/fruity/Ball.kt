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

        val (newPos, newSpeed) = updateMove(deltaTime, pos.cpy(), speed.cpy())
        pos.set(newPos)
        speed.set(newSpeed)

        // Kill monsters
        level.monsters.filter { it.collides(pos) }.forEach { level.monsters.remove(it) }
    }

    /** returns newPos, newSpeed */
    fun updateMove(deltaTime: Float, posLoc: Vector2, speedLoc: Vector2): Pair<Vector2, Vector2> {
        var newPos = posLoc + speedLoc * deltaTime
        val newGpos = pos2Grid(newPos)

        // TODO consider other corners of the ball depending on the direction?
        val gpos = pos2Grid(posLoc)

        if (gpos != newGpos) {  // check for rebound
            /*

        for each of the 2 borders of the cell:
            in the one that intersects:
                 if it's a wall, rebound
                 else update in the cell on the other side of the wall, pos = intersect, deltatime reduced

              */

            val walls = getWalls(speedLoc)
            for (wallDir in walls) {
                val (wall1, wall2) = wallPoints(gpos, wallDir)
                val intersection = Vector2()
                if (!Intersector.intersectSegments(wall1, wall2, posLoc, newPos, intersection)) {
                    continue
                }
                newPos = intersection
                if (level.hasWall(gpos, wallDir)) {
                    speedLoc.set(reboundSpeed(wallDir))
                } else {
                    newPos =
                }
            }

            /*
        val walls = level.getWalls(gpos, speed)
        for (wallDir in walls) {
            val (wall1, wall2) = wallPoints(gpos, wallDir)
            val intersection = Vector2()
            if (Intersector.intersectSegments(wall1, wall2, pos, newPos, intersection)) {
                newPos = intersection
                rebound(wallDir)
            } else {
                if (newGpos !in adjacent4(gpos)) {
                    println("OUT")
                }
            }
        }
        */
        }
    }

    fun getWalls(speed: Vector2): Set<Direction> {
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
            LEFT -> Pair(Vector2(pos.x, pos.y), Vector2(pos.x, pos.y + CELL_HEIGHT))
            UP -> Pair(Vector2(pos.x, pos.y + CELL_HEIGHT), Vector2(pos.x + CELL_WIDTH, pos.y + CELL_HEIGHT))
            DOWN -> Pair(Vector2(pos.x, pos.y), Vector2(pos.x + CELL_WIDTH, pos.y))
            NONE -> Pair(Vector2(), Vector2())
        }
    }

    private fun reboundSpeed(wallDir: Direction) = when (wallDir) {
        RIGHT, LEFT -> Vector2(-speed.x, speed.y)
        UP, DOWN -> Vector2(speed.x, -speed.y)
        else -> speed
    }


    private fun adjacent4(gpos: IntPoint) = listOf(
            gpos.plus(-1,0),
            gpos.plus(1,0),
            gpos.plus(0, -1),
            gpos.plus(0, 1)
    )

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