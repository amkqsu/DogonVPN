package com.dogon.vpn.qr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.tasks.await

/** Decodes a QR code from a gallery-picked image (the "galeriden QR seç" flow). */
object GalleryQrDecoder {
    suspend fun decode(context: Context, uri: Uri): String? {
        val image = InputImage.fromFilePath(context, uri)
        val scanner = BarcodeScanning.getClient()
        val results = runCatching { scanner.process(image).await() }.getOrNull() ?: return null
        return results.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }?.rawValue
            ?: results.firstOrNull()?.rawValue
    }
}
