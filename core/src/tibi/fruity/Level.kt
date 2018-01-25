package tibi.fruity

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.MathUtils.*
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.viewport.StretchViewport
import tibi.fruity.Direction.*
import com.badlogic.gdx.utils.Array as GdxArray


class Level(val levelNo: Int, val game: FruityFrankGame) : Screen {

    val frank = Frank(this, game.atlas)
    val fruits = mutableListOf<Fruit>()
    val apples = mutableListOf<Apple>()
    val monsters = mutableListOf<Monster>()
    val balls = mutableListOf<Ball>()
    val blackBlocks = mutableSetOf<IntPoint>()
    val highBlackBlocks = mutableSetOf<IntPoint>()

    private val bg = game.atlas.findRegion("backgrounds/level${levelNo}")
    private val header = game.atlas.findRegion("backgrounds/header")
    val blackTex: AtlasRegion = game.atlas.findRegion("backgrounds/black")
    private val blackHighTex = game.atlas.findRegion("backgrounds/black_high")
    val appleTex: AtlasRegion = game.atlas.findRegion("fruits/apple")
    val appleCrashAnim = Animation(.2f, game.atlas.findRegions("fruits/apple_crash"))
    private val gate = Animation(.4f, game.atlas.findRegions("backgrounds/gate"), LOOP)
    val gatePos = IntPoint(random(1, GRID_WIDTH-2), random(1, GRID_HEIGHT-2))
    val whiteSquareTex: AtlasRegion = game.atlas.findRegion("backgrounds/white_square")
    var explodeAnims = mutableListOf<ExplodeAnim>()
    private var isRegainingBall = false

    private var stateTime = 0f
    private var monsterSpawnStateTime = 0f
    private var winning = false

    var paused = false
    var speedFactor = 1.0f
    val speed: Float get() = 80f * speedFactor
    private var score: Int = 0

    private val viewport = StretchViewport(SCREEN_WIDTH, SCREEN_HEIGHT)// FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT)
    val ui = FrankUI(this, viewport)
    val input = FruityInput(this)
    val gestureListener = FrankGestureListener(this)
    val shader = ShaderProgram(Gdx.files.internal("passthrough.vsh"), Gdx.files.internal("cycle_black.fsh"))

    val debug = true

    init {
        (viewport.camera as OrthographicCamera).setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT)
        viewport.camera.position.x -= 200f
        viewport.camera.update()

        ShaderProgram.pedantic = false
        //println(if (shader.isCompiled) "shader compiled, yay" else shader.log)

        bg.texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)

        drawBlackCross(gatePos)
        frank.putAt(gatePos.copy(y = 0))

        val fruitTextures = listOf("cherry", "banana", "pear", "blueberry", "grape", "lemon", "peach").map {
            game.atlas.findRegion("fruits/" + it)
        }
        val randPoints = getRandomFreePoints()
        var pointIndex = 0
        for (i in 0..13) {
            val textureIndex = randomTriangular(0f, levelNo.toFloat(), 0f).toInt()
            fruits.add(Fruit(this, fruitTextures[textureIndex], randPoints[pointIndex++], 10))
        }
        for (i in 0..10) {
            var point: IntPoint
            do {
                point = randPoints[pointIndex++]
            } while (point.y == gatePos.y + 1)  // Don't put apples on the row above the gate
            apples.add(Apple(this, point))
            blackBlocks.add(point)
        }
        Gdx.input.inputProcessor = InputMultiplexer(ui, input, GestureDetector(gestureListener))
        game.musicPlayer?.play("level $levelNo.mid", speedFactor)
    }

    private fun getRandomFreePoints(): List<IntPoint> {
        val allPoints = ArrayList<IntPoint>()
        (0 until GRID_WIDTH).forEach { x -> (0 until GRID_HEIGHT).forEach { y -> allPoints.add(IntPoint(x, y)) } }
        allPoints.removeAll(blackBlocks)
        allPoints.shuffle()
        return allPoints
    }


    private fun update(dt: Float) {
        if (paused) return
        val deltaTime = if (dt > 0.3f) 0.3f else dt
        stateTime += deltaTime
        monsterSpawnStateTime += deltaTime
        if (monsterSpawnStateTime > MONSTER_SPAWN_RATE) {
            if (spawnMonster()) {
                monsterSpawnStateTime = 0f
            }
        }
        explodeAnims.forEach { it.update(deltaTime) }
        explodeAnims.removeAll { it.finished }
        balls.forEach { it.update(dt) }
        balls.removeAll(balls.filter { it.dead })
        if (balls.isEmpty() && frank.numBalls == 0 && !isRegainingBall) {
            val anim = ExplodeAnim(frank, whiteSquareTex, Color.YELLOW, false)
            explodeAnims.add(anim)
            isRegainingBall = true
            anim.whenFinished = {
                frank.regainBalls()
                isRegainingBall = false
            }
        }
        frank.update(deltaTime)
        apples.forEach { it.update(deltaTime) }
        monsters.forEach { it.update(deltaTime) }
        apples.removeAll(apples.filter { it.dead })

        if (fruits.isEmpty() && !winning) {
            winning = true
            paused = true
            schedule(2f, { game.restartLevel(true) })
        }
    }

    fun killFrank() {
        explodeAnims.add(ExplodeAnim(frank, whiteSquareTex, Color.RED))
    }

    fun killMonster(monster: Monster) {
        explodeAnims.add(ExplodeAnim(monster, whiteSquareTex, Color.BLUE))
        monsters.remove(monster)
    }


    override fun render(deltaTime: Float) {
        ui.act(deltaTime)
        update(deltaTime)

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        val batch = game.batch
        batch.projectionMatrix = viewport.camera.combined

        batch.shader = shader
        shader.begin()
        val color = if (winning) listOf(Color.BLUE, Color.CYAN, Color.RED, Color.GREEN, Color.YELLOW)[random(4)]
        else Color.BLACK
        shader.setUniformf("u_color", color)
        shader.end()

        batch.begin()
        batch.disableBlending()

        // Header
        batch.draw(header, 0F, SCREEN_HEIGHT - HEADER_HEIGHT-1)
        // Background
        TiledDrawable(bg).draw(batch, 0F, 0F, GAME_WIDTH, GAME_HEIGHT - HEADER_HEIGHT - 1)

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

        batch.end()
        if (batch.renderCalls > 1) {
            println("GPU access: ${batch.renderCalls}")
        }
        ui.draw()
    }

    fun getInputDirection(): Direction {
        if (ui.leftBt.isPressed) return LEFT
        if (ui.rightBt.isPressed) return RIGHT
        if (ui.upBt.isPressed) return UP
        if (ui.downBt.isPressed) return DOWN

        fun isKeyPressed(vararg keys: Int) = keys.any { Gdx.input.isKeyPressed(it) }
        return when {
            isKeyPressed(Keys.X, Keys.D, Keys.RIGHT)       -> RIGHT
            isKeyPressed(Keys.Z, Keys.A, Keys.LEFT)        -> LEFT
            isKeyPressed(Keys.SEMICOLON, Keys.W, Keys.UP)  -> UP
            isKeyPressed(Keys.PERIOD, Keys.S, Keys.DOWN)   -> DOWN
            else -> NONE
        }
    }

    override fun show() {}
    override fun hide() {}
    override fun pause() {}
    override fun resume() {}
    override fun dispose() {}
    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, false)
        ui.viewport.update(width, height)
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
        if (monsters.size > 3 + levelNo) {
            return false
        }
        if (monsters.any { it.collides(grid2Pos(gatePos)) }) {
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
        println("MIAM niam + $score")
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


class ExplodeAnim(val source: GridItem, val tex: TextureRegion, val color: Color, val isExplosion: Boolean = true) {

    var dist = if (isExplosion) 0f else GAME_WIDTH  // so it waits before starting ball regain anim

    var whenFinished: () -> Unit = {}
    var finished = false

    fun update(deltaTime: Float) {
        if (finished) return
        val speed = if (isExplosion) 300 else - 200
        dist += speed * deltaTime
        if (if (isExplosion) dist > GAME_WIDTH else dist < 0) {
            finished = true
            whenFinished()
        }
    }

    fun render(batch: SpriteBatch) {
        val oldColor = batch.color
        batch.color = color
        val targetX = source.pos.x + CELL_WIDTH / 2
        val targetY = source.pos.y + CELL_HEIGHT / 2
        batch.draw(tex, targetX + dist, targetY + dist)
        batch.draw(tex, targetX + dist, targetY - dist)
        batch.draw(tex, targetX - dist, targetY + dist)
        batch.draw(tex, targetX - dist, targetY - dist)
        if (isExplosion) {
            batch.draw(tex, targetX, targetY + dist)
            batch.draw(tex, targetX, targetY - dist)
            batch.draw(tex, targetX - dist, targetY)
            batch.draw(tex, targetX - dist, targetY)
        }
        batch.color = oldColor
    }
}
