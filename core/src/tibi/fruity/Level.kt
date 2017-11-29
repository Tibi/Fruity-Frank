package tibi.fruity

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array as GdxArray


const val SCREEN_WIDTH = 649F // was 646, 3 columns were only 40 px wide
const val SCREEN_HEIGHT = 378F
const val HEADER_HEIGHT = 28F

const val GRID_START_X = 17F
const val GRID_START_Y = 7F

const val CELL_WIDTH = 41F
const val CELL_HEIGHT = 28F
const val GRID_MARGIN = 6F

const val GRID_WIDTH = 15
const val GRID_HEIGHT = 10


class Level(val player: Perso, val monsters: Array<Perso>, val fruits: List<Fruit>) {

    val tiles: Array<Array<TextureRegion>> = Array(GRID_HEIGHT, { Array(GRID_WIDTH, { TextureRegion() }) })

    fun fill(tile: TextureRegion) {
        for (y in 0 until GRID_HEIGHT) {
            for (x in 0 until GRID_WIDTH) {
                tiles[y][x] = tile
            }
        }
    }

    fun horizLine(y: Int, tile: TextureRegion) {
        for (x in 0 until GRID_WIDTH) {
            tiles[y][x] = tile
        }
    }

    fun set(x: Int, y: Int, tile: TextureRegion) {
        tiles[y][x] = tile
    }

    fun draw(batch: SpriteBatch, xInit: Float, yInit: Float) {
        var y = yInit
        for (yi in 0 until GRID_HEIGHT) {
            var x = xInit
            for (xi in 0 until GRID_WIDTH) {
                batch.draw(tiles[yi][xi], x, y)
                x += CELL_WIDTH
            }
            y += CELL_HEIGHT
        }
    }

    fun movePlayer(dir: Direction) {
        player.move(dir)
    }

    fun render(batch: SpriteBatch, deltaTime: Float) {
        player.render(batch, deltaTime)
        monsters.forEach { it.render(batch, deltaTime) }
        fruits.forEach { it.render(batch, deltaTime) }
    }

    fun throwBall() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


open class GridItem(var gridX: Int, var gridY: Int) {

    var x = 0F
    var y = 0F
    var xSpeed = 0f
    var ySpeed = 0f

    init {
        x = gridX2x()
        y = gridY2y()
    }

    private fun gridY2y() = GRID_START_Y + gridY * (CELL_HEIGHT + GRID_MARGIN)

    private fun gridX2x() = GRID_START_X + gridX * CELL_WIDTH


    open fun update(deltaTime: Float) {
        if (xSpeed != 0f) {
            x += xSpeed * deltaTime
            if (xSpeed > 0 && x >= gridX2x()
                    || xSpeed < 0 && x <= gridX2x()) {
                xSpeed = 0f
            }
        }
        if (ySpeed != 0f) {
            y += ySpeed * deltaTime
            if (ySpeed > 0 && y >= gridY2y()
                    || ySpeed < 0 && y <= gridY2y()) {
                ySpeed = 0f
            }
        } 
    }

    open fun move(dir: Direction) {
        val speed = 80f
        if (xSpeed != 0f || ySpeed != 0f) return
        when (dir) {
            Direction.RIGHT -> if (gridX < GRID_WIDTH - 1) {
                gridX++
                xSpeed = speed
            }
            Direction.LEFT -> if (gridX > 0) {
                gridX--
                xSpeed = -speed
            }
            Direction.UP -> if (gridY < GRID_HEIGHT - 1) {
                gridY++
                ySpeed = speed
            }
            Direction.DOWN -> if (gridY > 0) {
                gridY--
                ySpeed = -speed
            }
        }
    }

    open fun render(batch: SpriteBatch, deltaTime: Float) {
        update(deltaTime)
    }
}


class Perso(val anims: Map<State, Animation<out TextureRegion>>, gridX: Int, gridY: Int) : GridItem(gridX, gridY) {

    enum class State { IDLE, RIGHT, LEFT, UP, DOWN, FALLING;
        companion object {
            fun fromDirection(dir: Direction): State = when (dir) {
                Direction.UP -> UP
                Direction.DOWN -> DOWN
                Direction.LEFT -> LEFT
                Direction.RIGHT -> RIGHT
            }
        }
    }

    var state = State.IDLE

    init {
        anims.values.forEach { it.playMode = Animation.PlayMode.LOOP }
    }

    var stateTime: Float = 0f

    override fun update(deltaTime: Float) {
        stateTime += deltaTime
        super.update(deltaTime)
        if (xSpeed == 0f && ySpeed == 0f) {
            state = State.IDLE
        }
    }

    override fun render(batch: SpriteBatch, deltaTime: Float) {
        super.render(batch, deltaTime)
        val anim = anims[state]
//        println("$state -> $anim")
        if (anim != null) {
            batch.draw(anim.getKeyFrame(stateTime), x, y)
        }
    }

    override fun move(dir: Direction) {
        super.move(dir)
        state = State.fromDirection(dir)
    }

    companion object {
        fun create(atlas: TextureAtlas, name: String, x: Int, y: Int): Perso {
              val leftRegions = atlas.findRegions(name + "right")
              val rightRegions = GdxArray(leftRegions.map { TextureRegion(it).apply { it.flip(true, false) } }.toTypedArray())
              val downRegions = atlas.findRegions(name + "down")
              return Perso(mapOf(
                      State.RIGHT to Animation(0.15F, rightRegions),
                      State.LEFT to Animation(0.15F, leftRegions),
                      State.UP to Animation(0.15F, downRegions),
                      State.DOWN to Animation(0.15F, downRegions),
                      State.IDLE to Animation(1F, rightRegions[0])
              ), x, y)
          }
    }
}

class Fruit(val textureRegion: TextureRegion, gridX: Int, gridY: Int) : GridItem(gridX, gridY) {
    override fun render(batch: SpriteBatch, deltaTime: Float) {
        super.render(batch, deltaTime)
        batch.draw(textureRegion, x, y)
    }
}

//646*378
//
//612*335
//41*28
//15*12
//
// top and bottom margins 7px
//        left and right 17px