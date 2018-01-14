package tibi.fruity

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector3

/** Moves Frank with your finger. */
class FrankGestureListener(val level: Level) : GestureDetector.GestureAdapter() {

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        level.frank.throwBall()
        return true
    }
}


class FruityInput(private val level: Level) : InputAdapter() {

    var lastDownFinger = 0
    private var previousFinger = 0

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.RIGHT_BRACKET || keycode == Input.Keys.SPACE) level.frank.throwBall()
        else if (keycode == Input.Keys.ESCAPE) level.restart()
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        previousFinger = lastDownFinger
        lastDownFinger = pointer
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (pointer == lastDownFinger) {
            lastDownFinger = previousFinger
        }
        return false
    }

    var touchPos = Vector3()
        get() = Vector3(Gdx.input.getX(lastDownFinger).toFloat() , Gdx.input.getY(lastDownFinger).toFloat(), 0f)

    var isTouched = false
        get() = Gdx.input.isTouched(lastDownFinger)
}
