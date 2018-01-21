package tibi.fruity

import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys.*
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
        when (keycode) {
            RIGHT_BRACKET, Input.Keys.SPACE -> level.frank.throwBall()
            ESCAPE -> level.game.restartLevel()
            P -> level.paused  = ! level.paused
            N -> if (level.debug) level.game.restartLevel(true)
        }
        return true
    }

}
