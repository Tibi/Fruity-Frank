package tibi.fruity

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
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

    data class IntPoint(val x: Int, val y: Int)

    val bg: TextureRegion = game.atlas.findRegion("backgrounds/level1")
    val header: TextureRegion = game.atlas.findRegion("backgrounds/header")
    val player = Frank(this, game.atlas)
    val fruits = ArrayList<Fruit>()
    val monsters = ArrayList<Perso>()
    val blackBlocks = HashSet<IntPoint>()
    val black = game.atlas.findRegion("backgrounds/black")
    val blackHigh = game.atlas.findRegion("backgrounds/black_high")
    val gate = game.atlas.findRegion("backgrounds/gate")
    var speedMult = 100f

    init {
        bg.texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)

        blackCross(6, 6)

        val guy = Monster(this, createAnimations(game.atlas, "guy/"), 1, 3)
        monsters.add(guy)
        guy.move(Direction.RIGHT)
        val prune = Monster(this, createAnimations(game.atlas, "prune/"), 5, 5)
        monsters.add(prune)
        prune.move(Direction.UP)

        val cherry = game.atlas.findRegion("fruits/cherry")
        fruits.addAll(List(10, { _ -> Fruit(this, cherry,
                game.rand(0, GRID_WIDTH), game.rand(0, GRID_HEIGHT), 10) }))
    }

    fun movePlayer(dir: Direction) {
        player.move(dir)
    }

    fun render(batch: SpriteBatch, deltaTime: Float) {
        TiledDrawable(bg).draw(batch, 0F, 0F, SCREEN_WIDTH, SCREEN_HEIGHT - HEADER_HEIGHT - 1)
        batch.draw(header, 0F, SCREEN_HEIGHT - HEADER_HEIGHT-3)
        for (blackBlock in blackBlocks) {
            val tex = if (blackBlock.y == GRID_HEIGHT - 1) black else blackHigh
            batch.draw(tex, GridItem.gridX2x(blackBlock.x), GridItem.gridY2y(blackBlock.y))
        }
        batch.draw(gate, GridItem.gridX2x(6), GridItem.gridY2y(6))
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
            println("MIAM niam + $score")
            fruits.removeAll(fruitsCol)
            speedMult += 10
        }
    }

    fun blackCross(x: Int, y: Int) {
        for (x1 in 0 until GRID_WIDTH) {
            blackBlocks.add(IntPoint(x1, y))
        }
        for (y1 in 0 until GRID_HEIGHT) {
            blackBlocks.add(IntPoint(x, y1))
        }
    }

    fun throwBall() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}



typealias AnimationMap = Map<Direction, Animation<out TextureRegion>>

fun createAnimations(atlas: TextureAtlas, name: String): AnimationMap {
    val leftRegions = atlas.findRegions(name + "right")
    val rightRegions = com.badlogic.gdx.utils.Array(leftRegions.map { TextureRegion(it).apply { it.flip(true, false) } }.toTypedArray())
    val downRegions = atlas.findRegions(name + "down")
    return mapOf(
            Direction.RIGHT to Animation(0.15F, rightRegions),
            Direction.LEFT to Animation(0.15F, leftRegions),
            Direction.UP to Animation(0.15F, downRegions),
            Direction.DOWN to Animation(0.15F, downRegions))
}

//646*378
//
//612*335
//41*28
//15*12
//
// top and bottom margins 7px
//        left and right 17px