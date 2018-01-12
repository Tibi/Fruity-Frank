package tibi.fruity

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.input.GestureDetector

/** Moves Frank with your finger. */
class FrankGestureListener(val level: Level) : GestureDetector.GestureAdapter() {

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        level.frank.throwBall()
        return true
    }
}


class FruityInput(private val level: Level) : InputAdapter() {
    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.RIGHT_BRACKET || keycode == Input.Keys.SPACE) level.frank.throwBall()
        else if (keycode == Input.Keys.ESCAPE) level.restart()
        return true
    }
}
