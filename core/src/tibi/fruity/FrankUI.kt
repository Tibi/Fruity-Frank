package tibi.fruity

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.Viewport

/**
 * Four buttons to move Frank
 *
 * @author YCHT
 */
class FrankUI(level: Level, viewport: Viewport) : Stage(viewport) {

    val leftTex = TextureRegion(Texture(Gdx.files.internal("skin/left_key.png")))
    val leftBt = ImageButton(TextureRegionDrawable(leftTex))

    val rightTex = TextureRegion(Texture(Gdx.files.internal("skin/right_key.png")))
    val rightBt = ImageButton(TextureRegionDrawable(rightTex))
    
    val upTex = TextureRegion(Texture(Gdx.files.internal("skin/up_key.png")))
    val upBt = ImageButton(TextureRegionDrawable(upTex))

    val downTex = TextureRegion(Texture(Gdx.files.internal("skin/down_key.png")))
    val downBt = ImageButton(TextureRegionDrawable(downTex))

    val throwTex = TextureRegion(Texture(Gdx.files.internal("skin/]_key.png")))
    val throwBt = ImageButton(TextureRegionDrawable(throwTex))

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
                level.frank.throwBall()
                return true
            }
        })
    }

}