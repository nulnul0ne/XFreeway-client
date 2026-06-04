package com.android.xrayfa.utils

import android.graphics.Bitmap
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

object BarcodeUtils {
    const val TAG = "BarcodeUtils"

    const val BG_COLOR: Int = 0xFFFFFFFF.toInt()
    const val FG_COLOR: Int = 0xFF000000.toInt()

    fun encode(contents: String, format: BarcodeFormat, width: Int, height: Int): BitMatrix? {
        return try {
            MultiFormatWriter().encode(contents, format, width, height)
        } catch (e: Exception) {
            Log.i(TAG, "encode: throw Error ${e.message}")
            null
        }
    }

    fun createBitmap(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix.get(x,y)) FG_COLOR else BG_COLOR
            }
        }
        val bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun encodeBitmap(contents: String, format: BarcodeFormat, width: Int, height: Int): Bitmap? {
        val matrixOrNull= encode(contents,format,width,height)
        return matrixOrNull?.run { createBitmap(matrixOrNull) }
    }
}
