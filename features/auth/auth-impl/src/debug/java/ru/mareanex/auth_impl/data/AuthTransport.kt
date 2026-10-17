package ru.mareanex.auth_impl.data

import android.content.Context
import okhttp3.OkHttpClient
import ru.mareanex.auth_impl.data.mock.DemoAuthInterceptor
import ru.mareanex.auth_impl.data.mock.DemoAccountStore

fun configureAuthTransport(context: Context, builder: OkHttpClient.Builder, useMocks: Boolean) {
    if (useMocks) builder.addInterceptor(DemoAuthInterceptor(context))
}

@Suppress("UNUSED_PARAMETER")
fun updateDemoAccount(context: Context, id: String, fullName: String, username: String): Boolean = DemoAccountStore(context).update(id, fullName, username)
