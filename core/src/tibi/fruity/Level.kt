package tibi.fruity

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
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

enum class Direction { NONE, UP, DOWN, LEFT, RIGHT}


class Level(private val game: FruityFrankGame) : Screen {

    data class IntPoint(val x: Int, val y: Int)

    private val bg = game.atlas.findRegion("backgrounds/level1")
    private val header = game.atlas.findRegion("backgrounds/header")
    private val player = Frank(this, game.atlas)
    private val fruits = ArrayList<Fruit>()
    private val monsters = ArrayList<Perso>()
    private val blackBlocks = HashSet<IntPoint>()
    private val black = game.atlas.findRegion("backgrounds/black")
    private val blackHigh = game.atlas.findRegion("backgrounds/black_high")
    private val gate = Animation(.40f, game.atlas.findRegions("backgrounds/gate"), Animation.PlayMode.LOOP)
    private var stateTime: Float = 0f

    var speed = 100f
    private var score: Int = 0


    val cam = OrthographicCamera()

    init {
        cam.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT)

        bg.texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)

        blackCross(6, 6)

        val guy = Monster(this, createAnimations(game.atlas, "guy/"), 1, 3)
        monsters.add(guy)
        guy.xSpeed = speed
        val prune = Monster(this, createAnimations(game.atlas, "prune/"), 5, 5)
        monsters.add(prune)
        prune.ySpeed = speed * 1.5f

        val cherry = game.atlas.findRegion("fruits/cherry")
        fruits.addAll(List(10, { _ -> Fruit(this, cherry,
                game.rand(0, GRID_WIDTH), game.rand(0, GRID_HEIGHT), 10) }))
        Gdx.input.inputProcessor = FruityInput(this)
    }

    fun update(deltaTime: Float) {
        processInput()
        detectCollisions()
        player.update(deltaTime)
        monsters.forEach { it.update(deltaTime) }
        fruits.forEach { it.update(deltaTime) }
    }

    override fun render(deltaTime: Float) {
        update(deltaTime)
        stateTime += deltaTime
        
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        game.batch.projectionMatrix = cam.combined
        game.batch.begin()
        game.batch.disableBlending()

        // Background
        TiledDrawable(bg).draw(game.batch, 0F, 0F, SCREEN_WIDTH, SCREEN_HEIGHT - HEADER_HEIGHT - 1)
        // Header
        game.batch.draw(header, 0F, SCREEN_HEIGHT - HEADER_HEIGHT-3)
        // Black paths
        for (blackBlock in blackBlocks) {
            val tex = if (blackBlock.y == GRID_HEIGHT - 1) black else blackHigh
            game.batch.draw(tex, GridItem.gridX2x(blackBlock.x), GridItem.gridY2y(blackBlock.y))
        }
        // Monster gate
        val keyFrame = gate.getKeyFrame(stateTime)
        println(keyFrame.index)
        game.batch.draw(keyFrame, GridItem.gridX2x(6), GridItem.gridY2y(6))

        player.render(game.batch)
        monsters.forEach { it.render(game.batch) }
        fruits.forEach { it.render(game.batch) }
        game.batch.end()
        
        if (fruits.isEmpty()) {
            println("WINNER!!")
        }
    }

    fun processInput() {
        when {
            isKeyPressed(Keys.X, Keys.D, Keys.RIGHT) -> player.nextDirection = Direction.RIGHT
            isKeyPressed(Keys.Z, Keys.A, Keys.LEFT) -> player.nextDirection = Direction.LEFT
            isKeyPressed(Keys.SEMICOLON, Keys.W, Keys.UP) -> player.nextDirection = Direction.UP
            isKeyPressed(Keys.PERIOD, Keys.S, Keys.DOWN) -> player.nextDirection = Direction.DOWN
            else -> player.nextDirection = Direction.NONE
        }
    }

    private fun isKeyPressed(vararg keys: Int) = keys.any { Gdx.input.isKeyPressed(it) }

    override fun show() {}
    override fun hide() {}
    override fun pause() {}
    override fun resume() {}
    override fun dispose() {}
    override fun resize(width: Int, height: Int) {
        // TODO update viewport
    }
    
    private fun detectCollisions() {
        if (monsters.any { it.collides(player) }) {
            println("DEAD")
        }
        val fruitsCol = fruits.filter { it.collides(player) }
        if (fruitsCol.isNotEmpty()) {
            score += fruitsCol.map { it.points }.sum()
            println("MIAM niam + $score")
            fruits.removeAll(fruitsCol)
            speed += 10
        }
    }

    private fun blackCross(x: Int, y: Int) {
        for (x1 in 0 until GRID_WIDTH) {
            blackBlocks.add(IntPoint(x1, y))
        }
        for (y1 in 0 until GRID_HEIGHT) {
            blackBlocks.add(IntPoint(x, y1))
        }
    }

    class FruityInput(val level: Level) : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            if (keycode == Keys.RIGHT_BRACKET) level.throwBall()
            return true
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