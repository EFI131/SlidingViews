package com.efisteiner.slidingviews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.efisteiner.slidingviews.PuzzleGameActivity.Companion
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.max

class BitmapUtils {
    companion object {
        private val TAG:String = "BitmapUtils"
        fun scaleToFitWidth(b: Bitmap, width: Int): Bitmap {
            val factor = width / b.width.toFloat()
            return Bitmap.createScaledBitmap(b, width, (b.height * factor).toInt(), true)
        }

        fun scaleToFitHeight(b: Bitmap, height: Int): Bitmap {
            val factor = height / b.height.toFloat()
            return Bitmap.createScaledBitmap(b, (b.width * factor).toInt(), height, true)
        }

        fun getBitmap(context: Context, photoUri: Uri): Bitmap {
            val originalBitmap  = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)
            }
            return originalBitmap
//            Log.i(TAG, "getImageByteArray: Original width ${originalBitmap.width} and height ${originalBitmap.height}")
//            val scaledBitmap = scaleToFitHeight(originalBitmap, 250)
//            Log.i(TAG, "getImageByteArray: Scaled width ${originalBitmap.width} and height ${originalBitmap.height}")
//            val byteOutputStream = ByteArrayOutputStream()
//            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
//            return scaledBitmap
        }

        fun splitBitmap(bitmap: Bitmap, rows: Int, columns: Int): List<Bitmap> {
            val bmpWidth = bitmap.width
            val bmpHeight = bitmap.height

            val tiles = mutableListOf<Bitmap>()

            // Calculate the width of each tile and distribute any remainder
            val tileWidths = IntArray(columns) { bmpWidth / columns }
            var widthRemainder = bmpWidth % columns
            for (i in 0 until widthRemainder) {
                tileWidths[i] += 1
            }

            // Calculate the height of each tile and distribute any remainder
            val tileHeights = IntArray(rows) { bmpHeight / rows }
            var heightRemainder = bmpHeight % rows
            for (i in 0 until heightRemainder) {
                tileHeights[i] += 1
            }

            // Now create the tiles using the calculated widths and heights
            var y = 0
            for (row in 0 until rows) {
                var x = 0
                for (col in 0 until columns) {
                    val tileWidth = tileWidths[col]
                    val tileHeight = tileHeights[row]

                    // Ensure the tile dimensions are within the bitmap bounds
                    if (x + tileWidth > bmpWidth) {
                        tileWidths[col] = bmpWidth - x
                    }
                    if (y + tileHeight > bmpHeight) {
                        tileHeights[row] = bmpHeight - y
                    }

                    val tile = Bitmap.createBitmap(bitmap, x, y, tileWidths[col], tileHeights[row])
                    tiles.add(tile)

                    x += tileWidths[col] // Move to the next tile position horizontally
                }
                y += tileHeights[row] // Move to the next tile position vertically
            }

            return tiles
        }



    }
}