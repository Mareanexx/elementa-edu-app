package ru.mareanexx.collections_impl.data.mock

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.util.UUID
import ru.mareanexx.collections_impl.data.remote.dto.CollectionImageDto

internal class DemoCollectionCoverStore(
    context: Context,
    userId: String,
) {
    private val folder = File(context.filesDir, "demo_collection_covers/$userId")
    fun save(bytes: ByteArray): CollectionImageDto {
        require(bytes.size in 1..2 * 1024 * 1024)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth in 1..2048 && bounds.outHeight in 1..2048)
        folder.mkdirs()
        val id = UUID.randomUUID().toString()
        File(folder, "$id.jpg").writeBytes(bytes)
        return requireNotNull(get(id))
    }
    fun get(id: String?): CollectionImageDto? {
        if (id == null || !runCatching { UUID.fromString(id).toString() == id }.getOrDefault(false)) return null
        val file = File(folder, "$id.jpg")
        return if (file.isFile) CollectionImageDto(id, Uri.fromFile(file).toString()) else null
    }
}
