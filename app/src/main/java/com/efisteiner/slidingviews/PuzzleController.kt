package com.efisteiner.slidingviews

import android.view.View
import androidx.customview.widget.ViewDragHelper

class PuzzleController(
    private val view: DraggableGridLayout,
    private val model: SlidingPuzzleGame
) {
    private val dragHelper: ViewDragHelper = ViewDragHelper.create(view, 1.0f, DragHelperCallback())

    inner class DragHelperCallback : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            TODO("Not yet implemented")
        }

    }
}