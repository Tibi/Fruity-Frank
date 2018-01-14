package tibi.fruity

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.viewport.Viewport

/**
 * Four buttons to move Frank
 *
 * @author YCHT
 */
class FrankUI(viewport: Viewport) : Stage(viewport) {

    val skin = Skin(Gdx.files.internal("skin/uiskin.json"))

    val leftBt = TextButton("Left", skin, "default")
    val rightBt = TextButton("Right", skin, "default")
    
    val upBt = TextButton("Up", skin, "default")
    val downBt = TextButton("Down", skin, "default")

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
    }

}