package ru.mareanexx.reader_impl.data.epub

import java.io.File
import java.net.URI

internal object EpubPaths {
    fun file(root: File, relative: String): File {
        require(relative.isNotBlank() && !relative.startsWith('/') && !relative.contains('\\') && !relative.contains('\u0000'))
        val target = File(root, relative).canonicalFile
        require(target.path.startsWith(root.canonicalPath + File.separator))
        return target
    }
    fun resolve(base: String, reference: String): String {
        val uri = URI(reference)
        require(!uri.isAbsolute && uri.authority == null && uri.query == null)
        val resolved = URI(base).resolve(uri).normalize().path
        require(!resolved.startsWith('/') && resolved.split('/').none { it == ".." } && !resolved.contains('\\'))
        return resolved
    }
}
