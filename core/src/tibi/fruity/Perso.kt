package tibi.fruity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.Vector2
import tibi.fruity.Direction.NONE


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
        renderDigging(batch)
        val anim = anims[direction]
        if (anim != null) {
            lastFrame = anim.getKeyFrame(stateTime)
        }
        batch.draw(lastFrame, pos.x, pos.y)
    }
}


open class Monster(level: Level, anims: AnimationMap, pos: IntPoint, speedFactor: Float)
    : Perso(level, anims, pos, speedFactor) {

    override fun getNewDirection(): Direction {
        val onPath = level.getDirectionsOnPath(gridPos)
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

    override fun getNewDirection(): Direction {
        val dir = level.getInputDirection()
        if (level.isOut(gridPos + dir)) {
            return NONE
        }
        val fruit = level.fruitAt(gridPos + dir)
        when (fruit) {
            is Apple -> if (!fruit.push(dir)) return NONE
            is Fruit -> level.eat(fruit)
        }
        return dir
    }

    override fun dig(pos: IntPoint, direction: Direction) {
        level.dig(pos, direction)
    }

    //FIXME never really called
    override fun detectCollision(newPos: Vector2): Boolean {
        if (level.monsters.any { it.collides(this) }) {
            die()
            return false
        }
        return true
    }


    fun die() {
        println("DEAD")
    }
}
