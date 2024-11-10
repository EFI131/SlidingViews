package com.efisteiner.slidingviews

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.ViewGroup

class GridLayoutParams: ViewGroup.LayoutParams{
    var row: Int
    var col: Int

    constructor(width: Int, height: Int, row: Int, col: Int): super(width, height) {
        this.row = row
        this.col = col
    }

    constructor(context: Context, attrs: AttributeSet)  : super(context, attrs) {
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.GridElementLayoutParams)
        try {
            row = a.getInt(R.styleable.GridElementLayoutParams_row, 0)
            col = a.getInt(R.styleable.GridElementLayoutParams_col, 0)
        } finally {
            a.recycle()
        }
    }

    constructor(source: ViewGroup.LayoutParams) : super(source) {
        row = 0
        col = 0
    }


}