package tibi.fruity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.Vector2
import tibi.fruity.Apple.AppleState.IDLE
import kotlin.math.sign


/** An animated GridItem. */
abstract class Perso(level: Level, val anims: AnimationMap, pos: IntPoint, speedFactor: Float)
    : GridItem(level, pos, speedFactor) {

    var lastFrame: AtlasRegion? = anims[Direction.RIGHT]?.getKeyFrame(0f)
    var stateTime: Float = 0f

    override fun update(deltaTime: Float) {
        stateTime += deltaTime
        super.update(deltaTime)
    }

    override fun render(batch: SpriteBatch) {
        // when digging up, draw a black square below perso to clear the small ground piece left
        if (direction == Direction.UP) {
            val yUp = grid2Pos(gridPos).y + CELL_HEIGHT - 5
            if (pos.y > yUp) batch.draw(level.blackTex, pos.x, yUp)
        }
        else if (direction == Direction.DOWN) {
            val yDown = grid2Pos(gridPos).y + 5
            if (pos.y < yDown) batch.draw(level.blackTex, pos.x, yDown)
        }
        val anim = anims[direction]
        if (anim != null) {
            lastFrame = anim.getKeyFrame(stateTime)
        }
        batch.draw(lastFrame, pos.x, pos.y)
    }
}


open class Monster(level: Level, anims: AnimationMap, pos: IntPoint, speedFactor: Float)
    : Perso(level, anims, pos, speedFactor) {

    override fun getNewDirection(closestGridPos: IntPoint): Direction {
        val onPath = level.getDirectionsOnPath(closestGridPos)
        if (onPath.isEmpty()) return direction
        if (onPath.size == 1) return onPath.first()
        return onPath.filter { it != direction.reverse() }.shuffled()[0]
    }
    override fun detectCollision(newPos: Vector2): Boolean {
        val col = super.detectCollision(newPos)
        if (col) {
            direction = direction.reverse()
        }
        //TODO kill player
        return col
    }
}


class Frank(level: Level, atlas: TextureAtlas)
    : Perso(level, createAnimations(atlas, "frank/ball "), IntPoint(0, 0), 1f) {

    override fun dig(oldPos: IntPoint, newPos: IntPoint) {
        level.dig(direction, oldPos, newPos)
    }

    override fun detectCollision(newPos: Vector2): Boolean {
        if (level.monsters.any { it.collides(this) }) {
            die()
        }
        level.fruits.find { it.collides(this) }?.let { level.eat(it) }
        return level.apples.find { it.collides(newPos) }?.let { !pushApple(it, newPos) } ?: false
    }

    /** Returns true when the apple could be pushed or false if Frank should stop. */
    private fun pushApple(apple: Apple, newPos: Vector2): Boolean {
        // Can't push up or down
        if (pos.y != newPos.y || apple.state != IDLE) return false
        // Can't push 2 apples at a time
        val directionSign = sign(newPos.x - pos.x)
//        val otherApplePos = apple.gridPos.plus(directionSign, 0)
//        if (otherApplePos.x < 0 || otherApplePos.x >= GRID_WIDTH
// || level.apples.any { it.gridPos == otherApplePos }) {
// || level.fruits.any { it.gridPos == otherApplePos }) {
        // push apple against monster => ?
//            return false
//        }
        apple.push(if (directionSign > 0) Direction.RIGHT else Direction.LEFT)
//        apple.pos.x = newPos.x + directionSign * CELL_WIDTH
//        println(apple.pos.x)
        return true
    }

    fun die() {
        println("DEAD")
    }
}
