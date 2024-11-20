package com.example.loyalisttest.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import android.util.Log

object QrCodeGenerator {

    fun generateQrCode(data: String, width: Int, height: Int): Bitmap? {
        try {
            val hints = hashMapOf<EncodeHintType, Int>().apply {
                put(EncodeHintType.MARGIN, 0) // Убираем белые границы
            }
            val bitMatrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, width, height, hints)
            val w = bitMatrix.width
            val h = bitMatrix.height
            val pixels = IntArray(w * h)
            for (y in 0 until h) {
                val offset = y * w
                for (x in 0 until w) {
                    pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
            return bitmap
        } catch (e: Exception) {
            Log.e("QrCodeGenerator", "Error generating QR code", e)
            return null
        }
    }
}