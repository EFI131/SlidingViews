package com.efisteiner.slidingviews

import android.util.Log
import kotlin.math.log
import kotlin.random.Random

/**
 * A model for a sliding puzzle game
 * holds indices of tiles in ordered puzzle instead of pieces
 */
class SlidingPuzzleGame(private val numRows: Int = 4, private val numColumns: Int = 4) {
    companion object {
        const val TAG = "SlidingPuzzleGame"
    }

    private var solved: Boolean = false
    private val numbers: MutableList<Int>

    init {
        numbers = generateSolvableShuffle(numRows, numColumns).toMutableList()
    }

    /**
     * end row should correspond to 0 in numbers
     */
    fun move(startRow: Int, startCol: Int, endRow: Int, endCol: Int) {
        if(numbers[endRow * numColumns + endCol] != 0) throw IllegalArgumentException("0 should be assigned as the empty tile")

        numbers[endRow * numColumns + endCol] = numbers[startRow * numColumns + startCol].also {
            numbers[startRow * numColumns + startCol] = numbers[endRow * numColumns + endCol]
        }


        for (j in 1 ..< numbers.size){
            if(numbers[j-1] != j)
                return
        }

        this.solved = true
    }

    /**
     * return: true if puzzle solved, false otherwise
     */
    fun isSolved(): Boolean {
        return this.solved
    }

    /**
     * get tile's index at ordered puzzle
     */
    fun getTileAt(row: Int, col: Int): Int {
        return numbers[row * numColumns + col]
    }

    private fun generateSolvableShuffle(rows: Int, cols: Int): List<Int> {
        val size = rows * cols
        val numbers = (1 until size).toMutableList()
        numbers.add(0)
        var z = numbers.size - 1
        for ( i in 1 .. 5){
            // row movement
            val rm = (z/cols)*cols + Random.nextInt(0, cols)

            while(z < rm) {
                numbers[z] = numbers[z+1].also { numbers[z+1] = numbers[z] }
                z += 1
            }

            while (z > rm) {
                numbers[z] = numbers[z-1].also { numbers[z-1] = numbers[z] }
                z -= 1
            }

            // col movement
            val cm =  Random.nextInt(0, rows)*cols + z%cols
            while(z < cm) {
                numbers[z] = numbers[z+cols].also { numbers[z+cols] = numbers[z] }
                z += cols
            }

            while (z > cm) {
                numbers[z] = numbers[z-cols].also { numbers[z-cols] = numbers[z] }
                z -= cols
            }
        }

        // row movement
        val rm = (z/cols)*cols + cols - 1

        while(z < rm) {
            numbers[z] = numbers[z+1].also { numbers[z+1] = numbers[z] }
            z += 1
        }

        while (z > rm) {
            numbers[z] = numbers[z-1].also { numbers[z-1] = numbers[z] }
            z -=1
        }

        // col movement
        val cm =  (rows-1)*cols + cols-1
        while(z < cm) {
            numbers[z] = numbers[z+cols].also { numbers[z+cols] = numbers[z] }
            z += cols
        }

        while (z > cm) {
            numbers[z] = numbers[z-cols].also { numbers[z-cols] = numbers[z] }
            z -= cols
        }

        return numbers
    }

    fun getState(): List<Int> {
        return numbers
    }
}