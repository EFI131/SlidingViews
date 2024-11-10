package com.efisteiner.slidingviews

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import kotlin.math.log

/**
 * A grid layout that acts both as an input device and a game display
 * */
class DraggableGridLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    companion object {
        const val TAG = "DraggableGridLayout"
    }
    /* Our Draggable Grid acts as an input device, therefore the event that interests the user is a tile drag*/
    interface InteractionListener {
        /* let the listener know that a tile was dragged */
        fun onTileDragged(startRow:Int, startCol:Int, endRow: Int, endCol: Int)
    }

    var solved: Boolean = false // temporary -> change solved game logic
        set(value) {
            if (value) {
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    child.setBackgroundColor(Color.GREEN)
                }
                refreshDrawableState()
            }
            field = value
        }
    var interactionListener: InteractionListener? = null

    var numRows: Int
    var numColumns: Int

    private var gridCells: Array<Array<View?>>
    private var childPositions: MutableMap<View, GridPosition>

    private val dragHelper: ViewDragHelper = ViewDragHelper.create(this, 1.0f, DragHelperCallback())

    data class GridPosition(var row: Int, var col: Int)

    // Primary constructor block
    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.GridLayoutParams,
            0,0).apply {

                try {
                    numRows = getInt(R.styleable.GridLayoutParams_numRows, 4)
                    numColumns = getInt(R.styleable.GridLayoutParams_numColumns, 4)
                } finally {
                    recycle()
                }
            }

            gridCells = Array(numRows) { arrayOfNulls<View>(numColumns) }
            childPositions = mutableMapOf<View, GridPosition>()
    }

    // Secondary constructor for programmatic creation
    constructor(
        context: Context, numRows: Int, numColumns: Int
    ) : this(context) {
        this.numRows = numRows
        this.numColumns = numColumns

        gridCells = Array(numRows) { arrayOfNulls<View>(numColumns) }
        childPositions = mutableMapOf<View, GridPosition>()
    }

    override fun addView(child: View, params: LayoutParams) {
        if (params !is GridLayoutParams) {
            throw IllegalArgumentException("LayoutParams must be of type GridLayoutParams")
        }

        val row = params.row
        val col = params.col

        if ( row !in 0 until numRows || col !in 0 until numColumns ) {
            throw IllegalArgumentException("Invalid grid position: ($row, $col)")
        }

        if (gridCells[row][col] != null) {
            throw IllegalStateException("Cell ($row, $col) is already occupied")
        }

        gridCells[row][col] = child
        childPositions[child] = GridPosition(row, col)

        super.addView(child, params)
    }

    override fun addView(child: View, index: Int) {
        val row: Int = index / numColumns
        val col: Int = index % numColumns

        if (row !in 0 until numRows || col !in 0 until numColumns) {
            throw IllegalArgumentException("Invalid grid position")
        }

        if (gridCells[row][col] != null) {
            throw IllegalStateException("Cell ($row, $col) is already occupied")
        }

        gridCells[row][col] = child
        childPositions[child] = GridPosition(row, col)

        super.addView(child)
    }

    override fun removeView(view: View) {
        val position = childPositions[view]
        if (position != null) {
            gridCells[position.row][position.col] = null
            childPositions.remove(view)
        }

        super.removeView(view)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalWidth = MeasureSpec.getSize(widthMeasureSpec)
        val totalHeight = MeasureSpec.getSize(heightMeasureSpec)

        val cellWidth = totalWidth / numColumns
        val cellHeight = totalHeight / numRows

        val cellWidthSpec = MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.EXACTLY)
        val cellHeightSpec = MeasureSpec.makeMeasureSpec(cellHeight, MeasureSpec.EXACTLY)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.measure(cellWidthSpec, cellHeightSpec)
        }

        // we are asking for full available real estate
        setMeasuredDimension(totalWidth, totalHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val cellWidth = width / numColumns
        val cellHeight = height / numRows

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val position = childPositions[child]
            if (position != null) {
                if (!child.isLaidOut) {
                    // Initial layout
                    val left = position.col * cellWidth
                    val top = position.row * cellHeight
                    child.layout(left, top, left + cellWidth, top + cellHeight)
                } else {
                    // Preserve current position during dragging
                    child.layout(child.left, child.top, child.left + child.measuredWidth, child.top + child.measuredHeight)
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // let the drag helper to make the decision
        return dragHelper.shouldInterceptTouchEvent(ev)
    }

    // TODO: add click detection
    override fun onTouchEvent(event: MotionEvent): Boolean {
        dragHelper.processTouchEvent(event)
        return true
    }

    // TODO: replace deprecated method
    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private inner class DragHelperCallback : ViewDragHelper.Callback() {

        // returns true if the child should be draggable
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return true
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return width - child.width
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return height - child.height
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            val position = childPositions[child]!!

            val minLeft = if (position.col > 0 && gridCells[position.row][position.col-1] == null) {
                (position.col - 1) * child.width
            } else position.col * child.width  // check whether the child had an empty square to it's left

            val maxLeft = if (position.col < numColumns - 1 && gridCells[position.row][position.col + 1] == null) {
                (position.col + 1) * child.width
            } else position.col * child.width // check whether the child had an empty square to it's left

            return left.coerceIn(minLeft, maxLeft)
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val position = childPositions[child]!!

            val minTop = if (position.row > 0 && gridCells[position.row - 1][position.col] == null) {
                (position.row - 1) * child.height
            } else position.row * child.height  // check whether the child had an empty square to it's left

            val maxTop = if (position.row <  numRows - 1 && gridCells[position.row + 1][position.col] == null) {
                (position.row + 1) * child.height
            } else position.row * child.height  // check whether the child had an empty square to it's left

            return top.coerceIn(minTop, maxTop)
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            invalidate()
        }

        override fun onViewReleased(child: View, xvel: Float, yvel: Float) {

            val releasePosition = calculateGridPosition(child)
            val initialPosition = childPositions[child]
            val finalPosition = if(gridCells[releasePosition.row][releasePosition.col] == null) {
                releasePosition
            } else {
                initialPosition
            }

            snapViewToGrid(child, initialPosition!!, finalPosition!!)

            // Notify Controller about drag event
            if ( initialPosition != finalPosition ){
                interactionListener?.onTileDragged(initialPosition.row, initialPosition.col, finalPosition.row, finalPosition.col)
            }

        }
    }

    private fun calculateGridPosition(child: View): GridPosition{
        val cellWidth = width / numColumns
        val cellHeight = height / numRows

        val centerX = child.left + child.width / 2.0
        val centerY = child.top + child.height / 2.0

        val newCol = (centerX / cellWidth).toInt().coerceIn(0, numColumns - 1)
        val newRow = (centerY / cellHeight).toInt().coerceIn(0, numRows - 1)

        Log.i(TAG, "calculateGridPosition: ( $newRow, $newCol)")

        return GridPosition(newRow, newCol)
    }

    // animate post release placement of dragged child
    private fun snapViewToGrid(child: View, prev: GridPosition, curr: GridPosition) {
            gridCells[prev.row][prev.col] = null
            gridCells[curr.row][curr.col] = child

            // Update child's position
            childPositions[child] = curr

            // Animate to the new position
            val finalLeft = curr.col * child.width
            val finalTop = curr.row * child.height
            if (dragHelper.smoothSlideViewTo(child, finalLeft, finalTop)) {
                ViewCompat.postInvalidateOnAnimation(this)
            }
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return GridLayoutParams(context , attrs)
    }

    public override fun generateDefaultLayoutParams(): LayoutParams {
        return GridLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0)
    }

    public override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return if (p is GridLayoutParams) {
            GridLayoutParams(p.width, p.height, p.row, p.col)
        } else {
            GridLayoutParams(p)
        }
    }

    public override fun checkLayoutParams(p: LayoutParams?): Boolean {
        return p is GridLayoutParams
    }
}
