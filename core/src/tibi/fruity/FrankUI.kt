package tibi.fruity

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.Viewport
import tibi.fruity.Direction.*

/**
 * Four buttons to move Frank
 *
 * @author YCHT
 */
class FrankUI(gameScreen: GameScreen, viewport: Viewport) : Stage(viewport) {

    private val leftTex = gameScreen.game.atlas.findRegion("UI/keys/left_key")
    private val rightTex = gameScreen.game.atlas.findRegion("UI/keys/right_key")
    private val upTex = gameScreen.game.atlas.findRegion("UI/keys/up_key")
    private val downTex = gameScreen.game.atlas.findRegion("UI/keys/down_key")
    private val throwTex = gameScreen.game.atlas.findRegion("UI/keys/]_key")

    private val leftBt = ImageButton(TextureRegionDrawable(leftTex))
    private val rightBt = ImageButton(TextureRegionDrawable(rightTex))
    private val upBt = ImageButton(TextureRegionDrawable(upTex))
    private val downBt = ImageButton(TextureRegionDrawable(downTex))
    private val throwBt = ImageButton(TextureRegionDrawable(throwTex))

    init {

        leftBt.width = 100f
        leftBt.height = 200f
        leftBt.setPosition(-200f, 0f)
        addActor(leftBt)

        rightBt.width = 100f
        rightBt.height = 200f
        rightBt.setPosition(-100f, 0f)
        addActor(rightBt)

        upBt.width = 100f
        upBt.height = 100f
        upBt.setPosition(GAME_WIDTH, 100f)
        addActor(upBt)

        downBt.width = 100f
        downBt.height = 100f
        downBt.setPosition(GAME_WIDTH, 0f)
        addActor(downBt)

        throwBt.width = 100f
        throwBt.height = 100f
        throwBt.setPosition(GAME_WIDTH, 250f)
        addActor(throwBt)

        throwBt.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                gameScreen.frank.throwBall()
                return true
            }
        })
    }

    fun getDirection() = when {
        leftBt.isPressed -> LEFT
        rightBt.isPressed -> RIGHT
        upBt.isPressed -> UP
        downBt.isPressed -> DOWN
        else -> null
    }

}