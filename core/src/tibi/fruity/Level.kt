package tibi.fruity

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.MathUtils.*
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.sun.deploy.uitoolkit.ToolkitStore.dispose
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

enum class Direction { NONE, UP, DOWN, LEFT, RIGHT ;

    fun reverse() = when(this) {
        UP -> DOWN
        DOWN -> UP
        RIGHT -> LEFT
        LEFT -> RIGHT
        NONE -> NONE
    }
}


class Level(val levelNo: Int, private val game: FruityFrankGame) : Screen {

    data class IntPoint(val x: Int, val y: Int)

    private val bg = game.atlas.findRegion("backgrounds/level1")
    private val header = game.atlas.findRegion("backgrounds/header")
    private val player = Frank(this, game.atlas)
    private val fruits = ArrayList<Fruit>()
    private val apples = ArrayList<Apple>()
    private val monsters = ArrayList<Perso>()
    private val blackBlocks = HashSet<IntPoint>()
    private val highBlackBlocks = HashSet<IntPoint>()
    private val blackTex = game.atlas.findRegion("backgrounds/black")
    private val blackHighTex = game.atlas.findRegion("backgrounds/black_high")
    val appleTex = game.atlas.findRegion("fruits/apple")
    private val gate = Animation(.40f, game.atlas.findRegions("backgrounds/gate"), LOOP)
    val gatePos = IntPoint(random(1, GRID_WIDTH-2), random(1, GRID_HEIGHT-2))

    val MONSTER_SPAWN_RATE = 2  // in seconds between monster spawn

    private var stateTime: Float = 0f
    private var monsterSpawnStateTime = 0f

    var speed = 100f
    private var score: Int = 0


    val cam = OrthographicCamera()

    init {
        cam.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT)
        bg.texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)

        blackCross(gatePos)

        val fruitTextures = listOf("cherry", "banana", "pear", "blueberry", "grape", "lemon", "peach").map {
            game.atlas.findRegion("fruits/" + it)
        }
        for (i in 0..20) {
            val textureIndex = randomTriangular(0f, levelNo + 1f, 0f).toInt()
            fruits.add(Fruit(this, fruitTextures[textureIndex], randomPoint(), 10))
        }
        for (i in 0..20) {
            apples.add(Apple(this, randomPoint()))
        }
        Gdx.input.inputProcessor = FruityInput(this)
    }

    private fun randomPoint() : IntPoint {
        var pt: IntPoint
        do {
            pt = IntPoint(random(0, GRID_WIDTH - 1), random(0, GRID_HEIGHT - 1))
        } while (pt in blackBlocks || pt in fruits.map { it.pos() })
        return pt
    }

    fun update(deltaTime: Float) {
        processInput()
        detectCollisions()
        stateTime += deltaTime
        monsterSpawnStateTime += deltaTime
        if (monsterSpawnStateTime > MONSTER_SPAWN_RATE) {
            spawnMonster()
            monsterSpawnStateTime = 0f
        }
        player.update(deltaTime)
        monsters.forEach { it.update(deltaTime) }
        fruits.forEach { it.update(deltaTime) }
    }

    override fun render(deltaTime: Float) {
        update(deltaTime)

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        game.batch.projectionMatrix = cam.combined
        game.batch.begin()
        game.batch.disableBlending()

        // Background
        TiledDrawable(bg).draw(game.batch, 0F, 0F, SCREEN_WIDTH, SCREEN_HEIGHT - HEADER_HEIGHT - 1)
        // Header
        game.batch.draw(header, 0F, SCREEN_HEIGHT - HEADER_HEIGHT-2)
        // Black paths
        for (blackBlock in blackBlocks) {
            val tex = if (blackBlock in highBlackBlocks) blackHighTex else blackTex
            game.batch.draw(tex, GridItem.gridX2x(blackBlock.x), GridItem.gridY2y(blackBlock.y))
        }
        // Monster gate
        game.batch.draw(gate.getKeyFrame(stateTime), GridItem.gridX2x(gatePos.x), GridItem.gridY2y(gatePos.y))

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
            isKeyPressed(Keys.X, Keys.D, Keys.RIGHT) -> player.requestMove(Direction.RIGHT)
            isKeyPressed(Keys.Z, Keys.A, Keys.LEFT) -> player.requestMove(Direction.LEFT)
            isKeyPressed(Keys.SEMICOLON, Keys.W, Keys.UP) -> player.requestMove(Direction.UP)
            isKeyPressed(Keys.PERIOD, Keys.S, Keys.DOWN) -> player.requestMove(Direction.DOWN)
            else -> player.requestMove(Direction.NONE)
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
        for (monster in monsters) {
            if (fruits.any { it.collides(monster) }) {
                monster.move(monster.direction.reverse())
            }
//            if (monsters.any { it.collides(monster) }) {
//                monster.move(monster.direction.reverse())
//            }
        }
        if (monsters.any { it.collides(player) }) {
            player.die()
        }
        val fruitsCol = fruits.filter { it.collides(player) }
        if (fruitsCol.isNotEmpty()) {
            score += fruitsCol.map { it.score }.sum()
            println("MIAM niam + $score")
            fruits.removeAll(fruitsCol)
            speed += 10
        }
        apples.filter { it.collides(player) }.forEach { apple ->
            if (apple.isFalling()) {
                player.die()
            } else {
                apple.state = Apple.AppleState.PUSHED
                // TODO move it here?
            }
        }
    }

    private fun blackCross(pt: IntPoint) {
        for (x in 0 until GRID_WIDTH) {
            val block = IntPoint(x, pt.y)
            blackBlocks.add(block)
            highBlackBlocks.add(block)
        }
        for (y in 0 until GRID_HEIGHT) {
            val block = IntPoint(pt.x, y)
            blackBlocks.add(block)
            if (y < GRID_HEIGHT - 1) {
                highBlackBlocks.add(block)
            }
        }
    }

    fun spawnMonster() {
        if (monsters.size > 3 + levelNo) {
            return
        }
        val monster = if (randomBoolean()) {
            Monster(this, createAnimations(game.atlas, "guy/"), gatePos, 0.7f)
        } else {
            Monster(this, createAnimations(game.atlas, "prune/"), gatePos, 1f)
        }
        monsters.add(monster)
        monster.move(Direction.values()[random(1, 4)])
    }

    class FruityInput(val level: Level) : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            if (keycode == Keys.RIGHT_BRACKET) level.throwBall()
            else if (keycode == Keys.ESCAPE) { dispose(); level.game.screen = Level(level.levelNo, level.game) }
            return true
        }
    }

    fun throwBall() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun dig(pt: IntPoint, direction: Direction) {
        blackBlocks.add(pt)
        if (direction == Direction.DOWN || direction == Direction.UP && pt.y < GRID_HEIGHT-1) {
            highBlackBlocks.add(pt)
        }
    }

    fun getDirectionsOnPath(p: IntPoint) : Set<Direction> {
        val res = HashSet<Direction>(4)
        highBlackBlocks.forEach { hBlock ->
            if (hBlock == p) res.add(Direction.UP)
            if (hBlock.y == p.y - 1 && hBlock.x == p.x) res.add(Direction.DOWN)
        }
        blackBlocks.forEach {block ->
            if (block.y == p.y && block.x == p.x + 1) res.add(Direction.RIGHT)
            if (block.y == p.y && block.x == p.x - 1) res.add(Direction.LEFT)

        }
        return res
    }

}


typealias AnimationMap = Map<Direction, Animation<out AtlasRegion>>

fun createAnimations(atlas: TextureAtlas, name: String): AnimationMap {
    val leftRegions = atlas.findRegions(name + "right")
    val rightRegions = GdxArray(leftRegions.map { AtlasRegion(it).apply { it.flip(true, false) } }.toTypedArray())
    val downRegions = atlas.findRegions(name + "down")
    return mapOf(
            Direction.RIGHT to Animation(0.15F, rightRegions, LOOP),
            Direction.LEFT to Animation(0.15F, leftRegions, LOOP),
            Direction.UP to Animation(0.15F, downRegions, LOOP),
            Direction.DOWN to Animation(0.15F, downRegions, LOOP))
}
