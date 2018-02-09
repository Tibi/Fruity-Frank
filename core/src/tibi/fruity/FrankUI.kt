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

        val margin = 25

        leftBt.width = KEY_BUTTON_SIZE
        leftBt.height = KEY_BUTTON_SIZE
        leftBt.setPosition(0f, -KEY_BUTTON_SIZE)
        addActor(leftBt)

        rightBt.width = KEY_BUTTON_SIZE
        rightBt.height = KEY_BUTTON_SIZE
        rightBt.setPosition(KEY_BUTTON_SIZE + margin, -KEY_BUTTON_SIZE)
        addActor(rightBt)

        downBt.width = KEY_BUTTON_SIZE
        downBt.height = KEY_BUTTON_SIZE
        downBt.setPosition(GAME_WIDTH, 0f)
        addActor(downBt)

        upBt.width = KEY_BUTTON_SIZE
        upBt.height = KEY_BUTTON_SIZE
        upBt.setPosition(GAME_WIDTH, KEY_BUTTON_SIZE + margin)
        addActor(upBt)


        throwBt.width = KEY_BUTTON_SIZE
        throwBt.height = KEY_BUTTON_SIZE
        throwBt.setPosition(GAME_WIDTH, 2.5f * KEY_BUTTON_SIZE)
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