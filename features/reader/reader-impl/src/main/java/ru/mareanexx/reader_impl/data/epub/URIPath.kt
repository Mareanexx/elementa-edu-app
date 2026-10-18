package ru.mareanexx.reader_impl.data.epub

internal object URIPath {
    fun encode(value: String): String = java.net.URI(null, null, "/$value", null).rawPath.removePrefix("/")
}
