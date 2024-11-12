package com.efisteiner.slidingviews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

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

        /**
         * split bitmap splits the provided bitmap to rows * cols pieces
         */
        fun splitBitmap(bitmap: Bitmap, rows: Int, columns: Int): List<Bitmap> {
            val bmpWidth = bitmap.width
            val bmpHeight = bitmap.height

            val tiles = mutableListOf<Bitmap>()


            val tileWidth = bmpWidth / columns
            val tileHeight = bmpHeight / rows
            val widthRemainder = bmpWidth % columns
            val heightRemainder = bmpHeight % rows

            var y = 0 + heightRemainder/2
            for (row in 0 until rows){
                var x = 0 + widthRemainder/2
                for ( col in 0 until columns ){
                    val tile = Bitmap.createBitmap(bitmap, x, y, tileWidth, tileHeight)
                    tiles.add(tile)
                    x += tileWidth
                }
                y += tileHeight
            }

            return tiles
        }



    }
}