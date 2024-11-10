package com.efisteiner.slidingviews

import android.net.Uri
import java.io.Serializable

data class Puzzle(
    val id: Int,
    val name: String,
    val imageUriString: String,
    val size: BoardSize = BoardSize.SMALL,
    val difficulty: Difficulty = Difficulty.EASY
) :  Serializable {
    enum class Difficulty {
        EASY,
        MEDIUM,
        HARD;
    }
}
