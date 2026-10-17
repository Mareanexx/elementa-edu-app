package ru.mareanexx.user_profile_impl.data.local

import android.content.Context
import coil3.SingletonImageLoader

internal class ProfileImageCache(
    private val context: Context,
) {
    fun clear() {
        val loader = SingletonImageLoader.get(context)
        loader.memoryCache?.clear()
        loader.diskCache?.clear()
    }
}
