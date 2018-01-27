package tibi.fruity

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import tibi.fruity.Apple.AppleState.*

const val FALL_HEIGHT_BEFORE_CRASH = 3  // in cells

class Apple(gameScreen: GameScreen, pos: IntPoint)
    : Fruit(gameScreen, gameScreen.appleTex, pos, 0) {

    enum class AppleState { IDLE, PUSHED, FALLING_SLOW, FALLING_FAST, CRASHING }

    private var state = IDLE
        set(value) {
            speedFactor = when (value) {
                FALLING_SLOW -> 0.037f
                else -> 1.0f
            }
            anim = if (value == CRASHING) gameScreen.appleCrashAnim else null
            if (field != FALLING_FAST && value == FALLING_FAST) fastFallStart = gridPos.y
            field = value
        }

    private var animTime = 0f
    private var anim: Animation<TextureAtlas.AtlasRegion>? = null
    private var fastFallStart = 0
    var dead = false

    override fun update(deltaTime: Float) {
        if (state == FALLING_SLOW && pos.y < grid2Pos(gridPos).y - LOW_CELL_CEILING) {
            state = FALLING_FAST
        }
        super.update(deltaTime)
        anim?.let { anima ->
            animTime += deltaTime
            if (animTime > anima.animationDuration) {
                dead = true
            }
        }
    }

    override fun getNewDirection(): Direction {
        if (state == PUSHED) {
            state = IDLE
        }
        val below = gridPos.plus(0, -1)
        val canFall = below in gameScreen.blackBlocks && gameScreen.fruitAt(below) == null
        val canFallFast = below in gameScreen.freeHighBlocks()
        if (!isFalling()) {
            if (canFall && state != CRASHING) {
                state = if (canFallFast) FALLING_FAST else FALLING_SLOW
            }
        } else {
            state = when {
                canFallFast -> FALLING_FAST
                fastFallStart - gridPos.y >= FALL_HEIGHT_BEFORE_CRASH -> CRASHING
                canFall -> FALLING_SLOW
                else -> IDLE
            }
        }
        return when (state) {
            FALLING_FAST, FALLING_SLOW -> Direction.DOWN
            IDLE, CRASHING -> Direction.NONE
            else -> direction
        }
    }

    override fun dig(pos: IntPoint, direction: Direction) {
        if (state != IDLE) gameScreen.dig(pos, direction)
    }

    fun push(dir: Direction): Boolean {
        if (state != IDLE || dir != Direction.LEFT && dir != Direction.RIGHT) {
            return false
        }
        val newPos = gridPos + dir
        if (gameScreen.isOut(newPos) || gameScreen.fruitAt(newPos) != null || gameScreen.monsterAt(newPos) != null ) {
            return false
        }
        state = PUSHED
        move(dir)
        return true
    }

    override fun detectCollision(newPos: Vector2): Vector2 {
        if (isFalling()) {
            gameScreen.monsters.toList().filter { collides(it) }.forEach { gameScreen.killMonster(it) }
            if (collides(gameScreen.frank)) {
                gameScreen.killFrank()
            }
        }
        return newPos
    }

    override fun render(batch: SpriteBatch) {
        renderDigging(batch)
        val frame = anim?.getKeyFrame(animTime) ?: textureRegion
        batch.draw(frame, pos.x, pos.y)
    }

    fun isFalling() = state == FALLING_SLOW || state == FALLING_FAST

}