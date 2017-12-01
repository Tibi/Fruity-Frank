package tibi.fruity

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array as GdxArray


const val SCREEN_WIDTH = 649F // was 646 in original game, 3 columns were only 40 px wide
const val SCREEN_HEIGHT = 378F
const val HEADER_HEIGHT = 28F

const val GRID_START_X = 17F
const val GRID_START_Y = 7F

const val CELL_WIDTH = 41F
const val CELL_HEIGHT = 28F
const val GRID_MARGIN = 6F

const val GRID_WIDTH = 15
const val GRID_HEIGHT = 10


class Level(val game: FruityFrankGame) {

    val player = Frank(this, game.atlas)
    val fruits = ArrayList<Fruit>()
    val monsters = ArrayList<Perso>()
    var speedMult = 100f

    init {
        val guy = Perso(this, createAnimations(game.atlas, "guy/"), 1, 3)
        monsters.add(guy)
        guy.move(Direction.RIGHT)
        val prune = Perso(this, createAnimations(game.atlas, "prune/"), 5, 5)
        monsters.add(prune)
        prune.move(Direction.UP)

        val cherry = game.atlas.findRegion("fruits/cherry")
        fruits.addAll(List(10, { _ -> Fruit(this, cherry,
                game.rand(0, GRID_WIDTH), game.rand(0, GRID_HEIGHT), 10) }))
    }
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
        if (monsters[0].xSpeed == 0f) {
            monsters[0].move(Direction.RIGHT)
        }
        detectCollisions()
        player.render(batch, deltaTime)
        monsters.forEach { it.render(batch, deltaTime) }
        fruits.forEach { it.render(batch, deltaTime) }
        if (fruits.isEmpty()) {
            println("WINNER!!")
        }
    }

    private var score: Int = 0

    private fun detectCollisions() {
        if (monsters.any { it.collides(player) }) {
            println("DEAD")
        }
        val fruitsCol = fruits.filter { it.collides(player) }
        if (fruitsCol.isNotEmpty()) {
            score += fruitsCol.map { it.points }.sum()
            println("MIAM + $score")
            fruits.removeAll(fruitsCol)
            speedMult += 10
        }
    }

    fun throwBall() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}



typealias AnimationMap = Map<Perso.State, Animation<out TextureRegion>>

fun createAnimations(atlas: TextureAtlas, name: String): AnimationMap {
    val leftRegions = atlas.findRegions(name + "right")
    val rightRegions = com.badlogic.gdx.utils.Array(leftRegions.map { TextureRegion(it).apply { it.flip(true, false) } }.toTypedArray())
    val downRegions = atlas.findRegions(name + "down")
    return mapOf(
            Perso.State.RIGHT to Animation(0.15F, rightRegions),
            Perso.State.LEFT to Animation(0.15F, leftRegions),
            Perso.State.UP to Animation(0.15F, downRegions),
            Perso.State.DOWN to Animation(0.15F, downRegions),
            Perso.State.IDLE to Animation(1F, rightRegions[0]))
}

//646*378
//
//612*335
//41*28
//15*12
//
// top and bottom margins 7px
//        left and right 17px