package tibi.fruity

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion

/**
 *
 */

const val SCREEN_WIDTH = 646F
const val SCREEN_HEIGHT = 378F
const val HEADER_HEIGHT = 28F

const val WIDTH = 15
const val HEIGHT = 13
const val CELL_WIDTH = 41F
const val CELL_HEIGHT = 28F
const val GRID_START_X = 17F
const val GRID_START_Y = 7F

class Level {

    val tiles: Array<Array<TextureRegion>> = Array(HEIGHT, { Array(WIDTH, { TextureRegion() }) })

    fun fill(tile: TextureRegion) {
        for (y in 0 until HEIGHT) {
            for (x in 0 until WIDTH) {
                tiles[y][x] = tile
            }
        }
    }

    fun horizLine(y: Int, tile: TextureRegion) {
        for (x in 0 until WIDTH) {
            tiles[y][x] = tile
        }
    }

    fun set(x: Int, y: Int, tile: TextureRegion) {
        tiles[y][x] = tile
    }

    fun draw(batch: SpriteBatch, xInit: Float, yInit: Float) {
        var y = yInit
        for (yi in 0 until HEIGHT) {
            var x = xInit
            for (xi in 0 until WIDTH) {
                batch.draw(tiles[yi][xi], x, y)
                x += CELL_WIDTH
            }
            y += CELL_HEIGHT
        }
    }
}


//646*378
//
//612*335
//41*28
//15*12
//
// top and bottom margins 7px
//        left and right 17px