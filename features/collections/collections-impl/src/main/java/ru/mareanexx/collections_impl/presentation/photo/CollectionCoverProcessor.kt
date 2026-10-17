package ru.mareanexx.collections_impl.presentation.photo

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import java.io.File
import ru.mareanexx.collections_api.domain.model.CollectionCoverUpload

/** Декодирует фото с ограничением памяти, учитывает EXIF и удаляет метаданные из результата. */
internal class CollectionCoverProcessor(
    private val context: Context,
) {
    fun prepare(uri: Uri): CollectionCoverUpload {
        val file = File.createTempFile("collection-cover-", ".image", context.cacheDir)
        try {
            context.contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input)
                file.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= 20 * 1024 * 1024)
                        output.write(buffer, 0, count)
                    }
                }
            }
            val source = if (Build.VERSION.SDK_INT >= 28) modern(file) else legacy(file)
            try {
                val output = Bitmap.createBitmap(1280, 800, Bitmap.Config.ARGB_8888)
                try {
                    val canvas = Canvas(output)
                    canvas.drawColor(Color.WHITE)
                    val scale = maxOf(1280f / source.width, 800f / source.height)
                    val matrix = Matrix().apply {
                        postScale(scale, scale)
                        postTranslate((1280 - source.width * scale) / 2, (800 - source.height * scale) / 2)
                    }
                    canvas.drawBitmap(source, matrix, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                    return CollectionCoverUpload(ByteArrayOutputStream().use { stream ->
                        check(output.compress(Bitmap.CompressFormat.JPEG, 90, stream))
                        stream.toByteArray().also { require(it.size <= 2 * 1024 * 1024) }
                    })
                } finally { output.recycle() }
            } finally { source.recycle() }
        } finally { file.delete() }
    }
    @androidx.annotation.RequiresApi(28)
    private fun modern(file: File): Bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, info, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        val scale = minOf(1f, 2048f / maxOf(info.size.width, info.size.height))
        decoder.setTargetSize(maxOf(1, (info.size.width * scale).toInt()), maxOf(1, (info.size.height * scale).toInt()))
    }
    private fun legacy(file: File): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 2048) sample *= 2
        val bitmap = requireNotNull(BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample }))
        val orientation = runCatching { ExifInterface(file.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) }.getOrDefault(1)
        val matrix = Matrix().apply {
            when (orientation) {
                2 -> setScale(-1f, 1f)
                3 -> setRotate(180f)
                4 -> setScale(1f, -1f)
                5 -> { setRotate(90f); postScale(-1f, 1f) }
                6 -> setRotate(90f)
                7 -> { setRotate(270f); postScale(-1f, 1f) }
                8 -> setRotate(270f)
            }
        }
        if (matrix.isIdentity) return bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also { if (it !== bitmap) bitmap.recycle() }
    }
}
