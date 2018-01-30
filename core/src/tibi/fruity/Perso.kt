package tibi.fruity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.MathUtils.randomBoolean
import com.badlogic.gdx.math.Vector2
import tibi.fruity.Direction.NONE


/** An animated GridItem. */
abstract class Perso(gameScreen: GameScreen, var anims: AnimationMap, pos: IntPoint, speedFactor: Float)
    : GridItem(gameScreen, pos, speedFactor) {

    var stateTime: Float = 0f

    override fun update(deltaTime: Float) {
        if (direction != NONE) stateTime += deltaTime
        super.update(deltaTime)
    }

    override fun render(batch: SpriteBatch) {
        renderDigging(batch)
        val anim = anims[if (direction == NONE) lastDir else direction]
        if (anim != null) {
            batch.draw(anim.getKeyFrame(stateTime), pos.x, pos.y)
        }
    }
}

enum class MonsterType { GUY, PRUNE, FRAISE, BONUS }

class Monster(gameScreen: GameScreen, val type: MonsterType, anims: AnimationMap, pos: IntPoint, speedFactor: Float)
    : Perso(gameScreen, anims, pos, speedFactor) {

    override fun getNewDirection(): Direction {
        val onPath = gameScreen.getDirectionsOnPath(gridPos)
        if (onPath.isEmpty()) return NONE
        if (onPath.size == 1) return onPath.first()
        // > 70% chances to keep same direction
        if (direction in onPath && randomBoolean(0.7f)) return direction
        // never reverse if there's anothe choice
        return onPath.filter { it != direction.reverse() }.shuffled()[0]
    }

    override fun detectCollision(newPos: Vector2): Vector2 {
        val monsterCollision = gameScreen.monsters.any { it != this && it.collides(newPos) }
        // There's an apple being pushed at our target position
        val appleCollision: Boolean by lazy { gameScreen.apples.any { it.collides(grid2Pos(targetGridPos)) }}
        if (monsterCollision || appleCollision) {
            direction = direction.reverse()
            targetGridPos += direction
            return pos
        }
        if (gameScreen.frank.collides(this)) {
            gameScreen.killFrank()
        }
        return newPos
    }
}


/** The Hero! */
class Frank(gameScreen: GameScreen, atlas: TextureAtlas)
    : Perso(gameScreen, createAnimations(atlas, "frank/ball "), IntPoint(0, 0), 1f) {

    var numBalls = NUM_BALLS
        set(value) {
            anims = if (value > 0) animsWithBall else animsNoBall
            field = value
        }
    val animsWithBall = anims
    val animsNoBall = createAnimations(atlas, "frank/")
    val ballTex: AtlasRegion = atlas.findRegion("frank/ball")
    var isDead = false

    override fun update(deltaTime: Float) {
        if (!isDead) super.update(deltaTime)
    }

    override fun getNewDirection(): Direction {
        val dir = gameScreen.getInputDirection()
        if (gameScreen.isOut(gridPos + dir)) {
            return NONE
        }
        val fruit = gameScreen.fruitAt(gridPos + dir)
        when (fruit) {
            is Apple -> if (!fruit.push(dir)) return NONE
            is Fruit -> gameScreen.eat(fruit)
        }
        return dir
    }

    override fun dig(pos: IntPoint, direction: Direction) {
        gameScreen.dig(pos, direction)
    }

    fun throwBall() {
        if (numBalls == 0) return
        if (!gameScreen.addBall(Ball(gameScreen, ballTex, pos, lastDir))) return
        numBalls--
    }

    fun catchBall() {
        if (numBalls < NUM_BALLS) numBalls++
    }

    fun regainBalls() {
        numBalls = NUM_BALLS
    }
}
