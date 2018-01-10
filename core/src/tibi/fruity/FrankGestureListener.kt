package tibi.fruity

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.input.GestureDetector
import tibi.fruity.Direction.*
import kotlin.math.abs

const val PAN_THRESHOLD = 1f

/** Moves Frank with your finger. */
class FrankGestureListener(val level: Level) : GestureDetector.GestureAdapter() {

    var lastDir = Direction.NONE

    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
        val absDx = abs(deltaX)
        val absDy = abs(deltaY)
        if (absDx > PAN_THRESHOLD && absDx > absDy) {
            lastDir = if (deltaX > 0) RIGHT else LEFT
        } else if (absDy > PAN_THRESHOLD && absDy > absDx) {
            lastDir = if (deltaY > 0) DOWN else UP  // Y points down
        }
        return true
    }

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

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        level.gestureListener.lastDir = NONE
        return false
    }
}
