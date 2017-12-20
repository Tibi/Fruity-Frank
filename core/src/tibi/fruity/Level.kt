package tibi.fruity

import com.badlogic.gdx.*
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.MathUtils.*
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.sun.deploy.uitoolkit.ToolkitStore.dispose
import tibi.fruity.Direction.*
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import com.badlogic.gdx.utils.Array as GdxArray


class Level(val levelNo: Int, private val game: FruityFrankGame) : Screen {


    private val bg = game.atlas.findRegion("backgrounds/level1")
    private val header = game.atlas.findRegion("backgrounds/header")
    private val player = Frank(this, game.atlas)
    val fruits = ArrayList<Fruit>()
    val apples = ArrayList<Apple>()
    val monsters = ArrayList<Perso>()
    private val blackBlocks = HashSet<IntPoint>()
    private val highBlackBlocks = HashSet<IntPoint>()
    val blackTex: AtlasRegion = game.atlas.findRegion("backgrounds/black")
    private val blackHighTex = game.atlas.findRegion("backgrounds/black_high")
    val appleTex: AtlasRegion = game.atlas.findRegion("fruits/apple")
    private val gate = Animation(.40f, game.atlas.findRegions("backgrounds/gate"), LOOP)
    private val gatePos = IntPoint(random(1, GRID_WIDTH-2), random(1, GRID_HEIGHT-2))

    private var stateTime: Float = 0f
    private var monsterSpawnStateTime = 0f

    var speed = 100f
    private var score: Int = 0

    private val touchpadStage = Stage(FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT))
    val cam = touchpadStage.viewport.camera as OrthographicCamera
    private val touchpadStyle = TouchpadStyle()
    private val touchpad = Touchpad(10f, touchpadStyle)

    private val isAndroid = Gdx.app.type == Application.ApplicationType.Android

    init {
        cam.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT)
        bg.texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)

        drawBlackCross(gatePos)
        player.putAt(gatePos.copy(y = 0))

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
        if (isAndroid) {
            touchpadStyle.background = TextureRegionDrawable(game.atlas.findRegion("UI/touchBackground"))
            touchpadStyle.knob = TextureRegionDrawable(game.atlas.findRegion("UI/touchKnob"))
            touchpad.setBounds(15f, 15f, 100f, 100f)
            touchpadStage.addActor(touchpad)
        }
        Gdx.input.inputProcessor = InputMultiplexer(FruityInput(this), touchpadStage)
    }

    private fun randomPoint() : IntPoint {
        var pt: IntPoint
        do {
            pt = IntPoint(random(0, GRID_WIDTH - 1), random(0, GRID_HEIGHT - 1))
        } while (pt in blackBlocks || pt in fruits.map { it.gridPos })
        return pt
    }

    private fun update(deltaTime: Float) {
        processInput()
        stateTime += deltaTime
        monsterSpawnStateTime += deltaTime
        if (monsterSpawnStateTime > MONSTER_SPAWN_RATE) {
            if (spawnMonster()) {
                monsterSpawnStateTime = 0f
            }
        }
        player.update(deltaTime)
        monsters.forEach { it.update(deltaTime) }
        fruits.forEach { it.update(deltaTime) }
    }

    override fun render(deltaTime: Float) {
        if (isAndroid) touchpadStage.act(deltaTime)
        update(deltaTime)

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        game.batch.projectionMatrix = cam.combined
        game.batch.begin()
        game.batch.disableBlending()

        // Background
        TiledDrawable(bg).draw(game.batch, 0F, 0F, SCREEN_WIDTH, SCREEN_HEIGHT - HEADER_HEIGHT - 1)
        // Header
        game.batch.draw(header, 0F, SCREEN_HEIGHT - HEADER_HEIGHT-1)
        // Black paths
        for (blackBlock in blackBlocks) {
            val tex = if (blackBlock in highBlackBlocks) blackHighTex else blackTex
            val gridPos = grid2Pos(blackBlock)
            game.batch.draw(tex, gridPos.x, gridPos.y)
        }
        // Monster gate
        val gridPos = grid2Pos(gatePos)
        game.batch.draw(gate.getKeyFrame(stateTime), gridPos.x, gridPos.y)

        player.render(game.batch)
        monsters.forEach { it.render(game.batch) }
        fruits.forEach { it.render(game.batch) }

        game.batch.end()

        if (isAndroid) touchpadStage.draw()

        if (fruits.isEmpty()) {
            println("WINNER!!")
        }
    }

    private fun processInput() {
        val tx = touchpad.knobPercentX
        val ty = touchpad.knobPercentY
        if (abs(tx) + abs(ty) > .1) {
            val teta = atan2(ty, tx) * radiansToDegrees
            when (teta) {
                in  -45..  45 -> { player.requestMove(RIGHT); return }
                in   45.. 135 -> { player.requestMove(UP)   ; return }
                in  135.. 180 -> { player.requestMove(LEFT) ; return }
                in -180..-135 -> { player.requestMove(LEFT) ; return }
                in -135..- 45 -> { player.requestMove(DOWN) ; return }
            }
        }
        when {
            isKeyPressed(Keys.X, Keys.D, Keys.RIGHT)      ||  tx >  .1 -> player.requestMove(RIGHT)
            isKeyPressed(Keys.Z, Keys.A, Keys.LEFT)       ||  tx < -.1 -> player.requestMove(LEFT)
            isKeyPressed(Keys.SEMICOLON, Keys.W, Keys.UP) ||  ty >  .1 -> player.requestMove(UP)
            isKeyPressed(Keys.PERIOD, Keys.S, Keys.DOWN)  ||  ty < -.1 -> player.requestMove(DOWN)
            else -> player.requestMove(Direction.NONE)
        }
    }

    private fun isKeyPressed(vararg keys: Int) = keys.any { Gdx.input.isKeyPressed(it) }

    override fun show() {}
    override fun hide() {}
    override fun pause() {}
    override fun resume() {}
    override fun dispose() {
        touchpadStage.dispose()
    }
    override fun resize(width: Int, height: Int) {
        touchpadStage.viewport.update(width, height, true)
    }

    fun isPositionFree(pos: Vector2, toExclude: GridItem): Boolean {
        return monsters.filter { it != toExclude }.none { it.collides(pos) }
    }

    private fun drawBlackCross(pt: IntPoint) {
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

    /** Returns whether a monster could be spawned. */
    private fun spawnMonster(): Boolean {
        if (monsters.size > 3 + levelNo) {
            return false
        }
        if (monsters.any { it.collides(grid2Pos((gatePos))) }) {
            return false
        }
        val monster = if (randomBoolean()) {
            Monster(this, createAnimations(game.atlas, "guy/"), gatePos, 0.7f)
        } else {
            Monster(this, createAnimations(game.atlas, "prune/"), gatePos, 1f)
        }
        monsters.add(monster)
        monster.move(Direction.values()[random(1, 4)])
        return true
    }

    class FruityInput(private val level: Level) : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            if (keycode == Keys.RIGHT_BRACKET) level.throwBall()
            else if (keycode == Keys.ESCAPE) { dispose(); level.game.screen = Level(level.levelNo, level.game) }
            return true
        }

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (button != Input.Buttons.LEFT || pointer > 0) return false
            val vector3 = Vector3(screenX.toFloat(), screenY.toFloat(), 0f)
            level.cam.unproject(vector3)
            level.touchpad.x = max(vector3.x - level.touchpad.width / 2, 0f)
            level.touchpad.y = max(vector3.y - level.touchpad.height / 2, 0f)
            return false // to let the touchpad process the touch
        }
    }

    fun throwBall() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun dig(pos: IntPoint) {
        blackBlocks.add(pos)
    }
    fun dig(dir: Direction, oldPos: IntPoint, newPos: IntPoint) {
//        println("digging: $oldPos -> $newPos")
        if (dir == Direction.RIGHT || dir == Direction.UP) {
            blackBlocks.add(newPos)
        } else {
            blackBlocks.add(oldPos)
        }
        if (dir == Direction.DOWN && oldPos.y != 0) {
            highBlackBlocks.add(newPos)  // adds it in advance
//            println("high $newPos")
        }
        if (dir == Direction.UP && oldPos.y != GRID_HEIGHT - 1) {
            highBlackBlocks.add(oldPos)
//            println("high $oldPos")
        }
    }

    fun getDirectionsOnPath(p: IntPoint) : Set<Direction> {
        val res = HashSet<Direction>(4)
        blackBlocks.forEach {block ->
            if (block.y == p.y && block.x == p.x + 1) res.add(Direction.RIGHT)
            if (block.y == p.y && block.x == p.x - 1) res.add(Direction.LEFT)
            if (block.x == p.x && block.y == p.y + 1 &&     p in highBlackBlocks) res.add(Direction.UP)
            if (block.x == p.x && block.y == p.y - 1 && block in highBlackBlocks) res.add(Direction.DOWN)
        }
        return res
    }

    fun eat(fruit: Fruit) {
        score += fruit.score
        println("MIAM niam + $score")
        fruits.remove(fruit)
        dig(fruit.gridPos)
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
