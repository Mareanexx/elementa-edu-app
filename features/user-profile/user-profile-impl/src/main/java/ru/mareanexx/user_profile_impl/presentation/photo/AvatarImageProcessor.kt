package ru.mareanexx.user_profile_impl.presentation.photo

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import ru.mareanexx.user_profile_api.domain.model.AvatarUpload
import ru.mareanexx.user_profile_api.domain.model.AvatarCrop

/** Нормализует ориентацию и ограничивает размер декодирования. JPEG-результат не содержит EXIF. */
internal class AvatarImageProcessor(
    private val context: Context,
) {
    fun copyToDraft(uri: Uri): String {
        val folder = File(context.cacheDir, "avatar_edit").apply { mkdirs() }
        val file = File(folder, UUID.randomUUID().toString() + ".image")
        try {
            context.contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "Не удалось открыть фото" }
                file.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= 20 * 1024 * 1024) { "Выбери исходное фото размером до 20 МБ" }
                        output.write(buffer, 0, count)
                    }
                }
            }
            require(file.length() > 0) { "Файл фото пуст" }
            return Uri.fromFile(file).toString()
        } catch (error: Exception) {
            file.delete()
            throw error
        }
    }

    fun load(uri: String, rotation: Int): Bitmap {
        val file = File(requireNotNull(Uri.parse(uri).path))
        require(file.canonicalFile.parentFile == File(context.cacheDir, "avatar_edit").canonicalFile)
        require(file.length() in 1..20L * 1024 * 1024)
        val bitmap = if (Build.VERSION.SDK_INT >= 28) decodeModern(file) else decodeLegacy(file)
        return if (rotation % 360 == 0) bitmap else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height,
            Matrix().apply { postRotate(rotation.toFloat()) }, true)
    }

    @androidx.annotation.RequiresApi(28)
    private fun decodeModern(file: File): Bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, info, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        val scale = minOf(1f, 2048f / maxOf(info.size.width, info.size.height))
        decoder.setTargetSize(maxOf(1, (info.size.width * scale).toInt()), maxOf(1, (info.size.height * scale).toInt()))
    }

    // Android 8 и 8.1 не поддерживают ImageDecoder: уменьшаем фото до декодирования и учитываем EXIF вручную.
    private fun decodeLegacy(file: File): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 2048) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val bitmap = requireNotNull(BitmapFactory.decodeFile(file.absolutePath, options))
        val orientation = runCatching {
            ExifInterface(file.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> { setRotate(90f); postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> { setRotate(270f); postScale(-1f, 1f) }
                ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(270f)
            }
        }
        return if (matrix.isIdentity) bitmap else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun crop(bitmap: Bitmap, crop: AvatarCrop): AvatarUpload {
        val output = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.WHITE)
        val scale = maxOf(512f / bitmap.width, 512f / bitmap.height) * crop.zoom
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate((512 - bitmap.width * scale) / 2 + crop.offsetX * 512,
                (512 - bitmap.height * scale) / 2 + crop.offsetY * 512)
        }
        canvas.drawBitmap(bitmap, matrix, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        val bytes = ByteArrayOutputStream().use { stream ->
            check(output.compress(Bitmap.CompressFormat.JPEG, 92, stream))
            stream.toByteArray()
        }
        output.recycle()
        return AvatarUpload(bytes)
    }

    fun discard(uri: String?) {
        if (uri == null) return
        runCatching {
            val file = File(requireNotNull(Uri.parse(uri).path))
            if (file.canonicalFile.parentFile == File(context.cacheDir, "avatar_edit").canonicalFile) file.delete()
        }
    }
}
