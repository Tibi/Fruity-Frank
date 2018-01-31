package tibi.fruity

import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys.*
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.input.GestureDetector

/** Moves Frank with your finger. */
class FrankGestureListener(val gameScreen: GameScreen) : GestureDetector.GestureAdapter() {

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        gameScreen.frank.throwBall()
        return true
    }
}


class FruityInput(private val gameScreen: GameScreen) : InputAdapter() {

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            RIGHT_BRACKET, Input.Keys.SPACE -> gameScreen.frank.throwBall()
            ESCAPE -> gameScreen.retryLevel()
            P -> gameScreen.paused  = ! gameScreen.paused
            N -> if (gameScreen.debug) gameScreen.nextLevel()
        }
        return true
    }

}
