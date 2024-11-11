package com.efisteiner.slidingviews

import java.io.Serializable

data class Puzzle(
    val id: Int,
    val name: String,
    val imageUriString: String,
    var size: BoardSize = BoardSize.SMALL,
    var difficulty: Difficulty = Difficulty.EASY
) :  Serializable {
    enum class Difficulty {
        EASY,
        MEDIUM,
        HARD;
    }
}
