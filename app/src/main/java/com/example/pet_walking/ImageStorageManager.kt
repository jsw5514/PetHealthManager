package com.example.pet_walking.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.OutputStream

object ImageStorageManager {

    // URI로부터 Bitmap 가져오기
    fun decodeUriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 내부 저장소에 이미지 저장
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PetWalking")
        }

        val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let { uri ->
            try {
                val stream: OutputStream? = resolver.openOutputStream(uri)
                stream?.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                return uri
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }
}