package com.efisteiner.slidingviews

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.customview.widget.ViewDragHelper
import kotlin.math.abs

class SlidingLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    // Initialize ViewDragHelper
    private val dragHelper: ViewDragHelper = ViewDragHelper.create(this, 1.0f, DragHelperCallback())

    // Variables to track drag direction
    private var dragDirection: DragDirection = DragDirection.NONE

    private enum class DragDirection {
        NONE, HORIZONTAL, VERTICAL
    }

    private inner class DragHelperCallback : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            // Always allow the view to be captured
            return true
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)

            if (dragDirection == DragDirection.NONE) {
                if (abs(dx) > abs(dy)) {
                    dragDirection = DragDirection.HORIZONTAL
                } else if (abs(dy) > abs(dx)) {
                    dragDirection = DragDirection.VERTICAL
                }
            }

            // Force the parent to redraw
            invalidate()
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return if (dragDirection != DragDirection.VERTICAL) {
                val leftBound = paddingLeft
                val rightBound = width - child.width - paddingRight
                left.coerceIn(leftBound, rightBound)
            } else {
                child.left
            }
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return if (dragDirection != DragDirection.HORIZONTAL) {
                val topBound = paddingTop
                val bottomBound = height - child.height - paddingBottom
                top.coerceIn(topBound, bottomBound)
            } else {
                child.top
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            // Reset drag direction
            dragDirection = DragDirection.NONE
            // Optionally, implement snapping back or other release behavior
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return width - child.width
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return height - child.height
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxWidth = 0
        var maxHeight = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
            val lp = child.layoutParams as MarginLayoutParams

            maxWidth = maxOf(maxWidth, child.measuredWidth + lp.leftMargin + lp.rightMargin)
            maxHeight = maxOf(maxHeight, child.measuredHeight + lp.topMargin + lp.bottomMargin)
        }

        val width = resolveSize(maxWidth + paddingLeft + paddingRight, widthMeasureSpec)
        val height = resolveSize(maxHeight + paddingTop + paddingBottom, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as MarginLayoutParams

            val width = child.measuredWidth
            val height = child.measuredHeight

            var left = child.left
            var top = child.top

            // Check if the child has been positioned by ViewDragHelper
            if (left == 0 && top == 0) {
                // Initial layout positions
                left = lp.leftMargin + paddingLeft
                top = lp.topMargin + paddingTop
            }

            child.layout(left, top, left + width, top + height)
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is MarginLayoutParams
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return dragHelper.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        dragHelper.processTouchEvent(event)
        return true
    }

    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) {
            invalidate()
        }
    }
}


