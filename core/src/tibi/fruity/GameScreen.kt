package tibi.fruity

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.MathUtils.*
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.StretchViewport
import ktx.app.KtxScreen
import ktx.app.use
import tibi.fruity.Direction.*
import tibi.fruity.MonsterType.GUY
import tibi.fruity.MonsterType.PRUNE
import kotlin.math.min
import com.badlogic.gdx.utils.Array as GdxArray


class GameScreen(val game: FruityFrankGame, val header: Header) : KtxScreen {

    private var levelNo = 1
    val frank = Frank(this, game.atlas)
    private val fruits = mutableListOf<Fruit>()
    val apples = mutableListOf<Apple>()
    val monsters = mutableListOf<Monster>()
    private val balls = mutableListOf<Ball>()
    val blackBlocks = mutableSetOf<IntPoint>()
    private val highBlackBlocks = mutableSetOf<IntPoint>()

    private val backgrounds = (1..7).map { game.atlas.findRegion("backgrounds/level$it").also {
        it.texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
    } }
    val blackTex: AtlasRegion = game.atlas.findRegion("backgrounds/black")
    private val blackHighTex = game.atlas.findRegion("backgrounds/black_high")
    private val fruitTextures = listOf("cherry", "banana", "pear", "blueberry", "grape", "lemon", "peach").map {
        game.atlas.findRegion("fruits/" + it)
    }
    val appleTex: AtlasRegion = game.atlas.findRegion("fruits/apple")
    val appleCrashAnim = Animation(.2f, game.atlas.findRegions("fruits/apple_crash"))
    private val gate = Animation(.4f, game.atlas.findRegions("backgrounds/gate"), LOOP)
    val gatePos = IntPoint(random(1, GRID_WIDTH -2), random(1, GRID_HEIGHT -2))
    private val whiteSquareTex: AtlasRegion = game.atlas.findRegion("backgrounds/white_square")
    private var explodeAnims = mutableListOf<ExplodeAnim>()
    private var isRegainingBall = false

    private var stateTime = 0f
    private var monsterSpawnInterval = 2f  // seconds between monsters
    private var monsterSpawnStateTime = 0f
    private var winning = false
    private val blinkColors = listOf(Color.valueOf("000063"), Color.valueOf("0000FD"), Color.valueOf("660200"), Color.valueOf("660266"), Color.valueOf("6602FE"), Color.valueOf("FE0100"), Color.valueOf("FE0265"), Color.valueOf("FF03FE"), Color.valueOf("006700"), Color.valueOf("006766"), Color.valueOf("0067FF"), Color.valueOf("666700"), Color.valueOf("686868"), Color.valueOf("6768FE"), Color.valueOf("FF6901"), Color.valueOf("FF6968"), Color.valueOf("FF69FF"), Color.valueOf("01FF02"), Color.valueOf("02FF66"), Color.valueOf("03FFFF"), Color.valueOf("67FF03"), Color.valueOf("67FF68"), Color.valueOf("69FFFF"), Color.valueOf("FFFF03"), Color.valueOf("FFFF6A"), Color.valueOf("FFFFFF"))
    private var blinkIndex = 0

    var paused = false
        set(value) { game.musicPlayer.pause(value); field = value }
    private var speedFactor = 1.0f
    val speed: Float get() = 90f * speedFactor
    var score: Int = 0
    private var livesLeft = 3

    private val isAndroid = Gdx.app.type == Application.ApplicationType.Android
    private val viewport = if (isAndroid) StretchViewport(SCREEN_WIDTH, SCREEN_HEIGHT)
                           else FitViewport(GAME_WIDTH, GAME_HEIGHT)
    private val ui = FrankUI(this, viewport)
    private val input = FruityInput(this)
    private val gestureListener = FrankGestureListener(this)
    private val shader = ShaderProgram(Gdx.files.internal("shaders/passthrough.vert"), Gdx.files.internal("shaders/cycle_black.frag"))

    val debug = true

    init {
        if (isAndroid) {
            (viewport.camera as OrthographicCamera).setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT)
            viewport.camera.position.y -= KEY_BUTTON_SIZE
            viewport.camera.update()
        }
        ShaderProgram.pedantic = false
        if (!shader.isCompiled) Gdx.app.log("", shader.log)
    }

    override fun show() {
        Gdx.input.inputProcessor = InputMultiplexer(ui, input, GestureDetector(gestureListener))
//        game.batch.disableBlending()
        levelNo = 1
        livesLeft = 3
        startLevel()
    }

    private fun startLevel(numFruits: Int = 2, numApples: Int = 10) {
        clear()
        speedFactor = 1.0f + levelNo / 8f
        Gdx.app.log("", "Starting level $levelNo")
        drawBlackCross(gatePos)
        frank.putAt(gatePos.copy(y = 0))

        val randPoints = getFreeCells()
        var pointIndex = 0

        // Creates 20 fruits to eat
        for (i in 1..numFruits) {
            val textureIndex = randomTriangular(0f, levelNo.toFloat(), 0f).toInt()
            fruits.add(Fruit(this, fruitTextures[textureIndex], randPoints[pointIndex++], 10))
        }
        // Creates 10 apples to push
        for (i in 1..numApples) {
            var point: IntPoint
            do {
                point = randPoints[pointIndex++]
            } while (point.y == gatePos.y + 1)  // Don't put apples on the row above the gate
            apples.add(Apple(this, point))
            blackBlocks.add(point)
        }
        game.musicPlayer.play("level $levelNo", speedFactor)
    }

    fun retryLevel() {
        startLevel(fruits.size, apples.size)
    }

    fun nextLevel() {
        levelNo = if (levelNo < NUM_LEVELS) levelNo + 1 else 1
        startLevel()
    }

    private fun clear() {
        frank.isDead = false
        winning = false
        paused = false
        stateTime = 0f
        monsterSpawnInterval = 2f
        monsterSpawnStateTime = 0f

        blackBlocks.clear()
        highBlackBlocks.clear()
        fruits.clear()
        monsters.clear()
        apples.clear()
    }

    private fun getFreeCells(): List<IntPoint> {
        val allPoints = ArrayList<IntPoint>()
        (0 until GRID_WIDTH).forEach { x -> (0 until GRID_HEIGHT).forEach { y -> allPoints.add(IntPoint(x, y)) } }
        allPoints.removeAll(blackBlocks)
        allPoints.shuffle()
        return allPoints
    }


    private fun update(dt: Float) {
        if (paused) return
        val deltaTime = min(0.3f, dt)
        stateTime += deltaTime

        monsterSpawnStateTime += deltaTime
        if (monsterSpawnStateTime > monsterSpawnInterval) {
            if (spawnMonster()) {
                monsterSpawnStateTime = 0f
            }
        }
        explodeAnims.forEach { it.update(deltaTime) }
        explodeAnims.removeAll { it.finished }
        balls.forEach { it.update(dt) }
        balls.removeAll(balls.filter { it.dead })
        if (balls.isEmpty() && frank.numBalls == 0 && !isRegainingBall) {
            isRegainingBall = true
            schedule(7f) {
                explodeAnims.add(ExplodeAnim(frank, whiteSquareTex, Color.YELLOW, false) {
                    frank.regainBalls()
                    isRegainingBall = false
                })
            }
        }
        frank.update(deltaTime)
        apples.forEach { it.update(deltaTime) }
        monsters.forEach { it.update(deltaTime) }
        apples.removeAll(apples.filter { it.dead })

        if (fruits.isEmpty() && !winning) {
            winning = true
            paused = true
            schedule(3f, { nextLevel() })
        }
    }

    fun killFrank() {
        if (!frank.isDead) {
            val anim = ExplodeAnim(frank, whiteSquareTex, Color.RED, true) {
                if (livesLeft > 0) {
                    retryLevel()
                    livesLeft--
                } else {
                    game.gameOver(score)
                }
            }
            explodeAnims.add(anim)
            frank.isDead = true
        }
    }

    fun killMonster(monster: Monster, byApple: Boolean = false) {
        explodeAnims.add(ExplodeAnim(monster, whiteSquareTex, Color.BLUE))
        monsters.remove(monster)
        score += monster.type.score * if (byApple) 8 else 1
    }


    override fun render(delta: Float) {
        ui.act(delta)
        update(delta)
        // TODO can be put in startLevel or init?
        val batch = game.batch
        batch.projectionMatrix = viewport.camera.combined
        batch.shader = shader
        shader.use {
            val color = if (winning) blinkColors[blinkIndex++] else Color.BLACK
            if (blinkIndex == blinkColors.size) blinkIndex = 0
            shader.setUniformf("u_color", color)
        }

        batch.use {

            // Header
            header.render(batch, score, game.highScore, livesLeft - 1)
            // Background
            TiledDrawable(backgrounds[levelNo - 1]).draw(batch, 0F, 0F, GAME_WIDTH, GAME_HEIGHT - HEADER_HEIGHT - 1)

            // Black paths
            for (blackBlock in blackBlocks) {
                val tex = if (blackBlock in highBlackBlocks) blackHighTex else blackTex
                val gridPos = grid2Pos(blackBlock)
                batch.draw(tex, gridPos.x, gridPos.y)
            }

            // Monster gate
            val gridPos = grid2Pos(gatePos)
            batch.draw(gate.getKeyFrame(stateTime), gridPos.x, gridPos.y)

            monsters.forEach { it.render(batch) }
            fruits.forEach { it.render(batch) }
            apples.forEach { it.render(batch) }
            frank.render(batch)

            balls.forEach { it.render(batch) }
            explodeAnims.forEach { it.render(batch) }
        }
        ui.draw()
    }

    fun getInputDirection(): Direction {
        ui.getDirection()?.let { return it }

        fun isKeyPressed(vararg keys: Int) = keys.any { Gdx.input.isKeyPressed(it) }
        return when {
            isKeyPressed(Keys.X, Keys.D, Keys.RIGHT)       -> RIGHT
            isKeyPressed(Keys.Z, Keys.A, Keys.LEFT)        -> LEFT
            isKeyPressed(Keys.SEMICOLON, Keys.W, Keys.UP)  -> UP
            isKeyPressed(Keys.PERIOD, Keys.S, Keys.DOWN)   -> DOWN
            else -> NONE
        }
    }

    override fun hide() {
        game.batch.enableBlending()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, false)
        // TODO test, useless
//        ui.viewport.update(width, height)
    }

    private fun drawBlackCross(pt: IntPoint) {
        blackBlocks.addAll((0 until GRID_WIDTH).map { IntPoint(it, pt.y) })
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
        val maxNumPrunes = ceil(levelNo / 2f)
        val maxNumGuys = levelNo + 1
        if (monsters.size >= maxNumGuys + maxNumPrunes) {
            return false
        }
        if (monsters.any { it.collides(grid2Pos(gatePos)) }) {
            return false
        }
        val monster = if (monsters.count { it.type == GUY } < maxNumGuys) {
            Monster(this, GUY, createAnimations(game.atlas, "guy/"), gatePos, 0.7f)
        } else {
            Monster(this, PRUNE, createAnimations(game.atlas, "prune/"), gatePos, 1f)
        }
        monsters.add(monster)
        monster.move(Direction.values()[random(1, 4)])
        if (monsters.size >= maxNumGuys + maxNumPrunes) {
            // Once all monsters are out, respawn dead ones only after 7 seconds
            monsterSpawnInterval = 7f
            monsterSpawnStateTime = 0f
        }
        return true
    }

    fun addBall(ball: Ball) =
        if (pos2Grid(ball.pos) in freeBlocks()) {
            balls.add(ball)
            true
        } else false

    fun dig(pos: IntPoint, dir: Direction) {
        blackBlocks.add(pos)
        if (dir == DOWN) {
            highBlackBlocks.add(pos)
        } else if (dir == UP) {
            highBlackBlocks.add(pos.copy(y = pos.y - 1))
        }
    }

    fun getDirectionsOnPath(p: IntPoint) : Set<Direction> {
        val res = HashSet<Direction>(4)
        freeBlocks().filter { it !in apples.map { it.gridPos } }.forEach {block ->
            if (block.y == p.y && block.x == p.x + 1) res.add(Direction.RIGHT)
            if (block.y == p.y && block.x == p.x - 1) res.add(Direction.LEFT)
            if (block.x == p.x && block.y == p.y + 1 &&     p in highBlackBlocks) res.add(Direction.UP)
            if (block.x == p.x && block.y == p.y - 1 && block in highBlackBlocks) res.add(Direction.DOWN)
        }
        return res
    }

    fun eat(fruit: Fruit) {
        score += fruit.score
        Gdx.app.log("", "MIAM niam + $score")
        fruits.remove(fruit)
        dig(fruit.gridPos, NONE)
    }

    fun fruitAt(pos: IntPoint) = (fruits + apples).firstOrNull { it.gridPos == pos }

    fun monsterAt(pos: IntPoint) = monsters.firstOrNull { it.collides(grid2Pos(pos)) }

    fun isOut(pos: IntPoint) = pos.x < 0 || pos.x >= GRID_WIDTH || pos.y < 0 || pos.y >= GRID_HEIGHT

    fun hasWall(gpos: IntPoint, wallDir: Direction) = when (wallDir) {
        RIGHT -> gpos + RIGHT !in freeBlocks()
        LEFT  -> gpos + LEFT !in freeBlocks()
        UP    -> gpos !in highBlackBlocks || gpos + UP !in blackBlocks
        DOWN  -> gpos + DOWN !in freeHighBlocks()
        NONE  -> false
    }

    private fun freeBlocks() = blackBlocks - apples.map { it.gridPos }
    fun freeHighBlocks() = highBlackBlocks - apples.map { it.gridPos }
}


