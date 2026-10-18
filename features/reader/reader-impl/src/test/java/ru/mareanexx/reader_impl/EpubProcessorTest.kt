package ru.mareanexx.reader_impl

import org.junit.*
import org.junit.Assert.*
import org.junit.rules.TemporaryFolder
import ru.mareanexx.reader_impl.data.epub.*
import ru.mareanexx.reader_api.domain.model.ReaderError
import java.io.File
import java.util.zip.*

class EpubProcessorTest {
    @get:Rule val files = TemporaryFolder()
    private fun epub(body: String = "<h1>Тест</h1><p>${"Описание структуры вещества. ".repeat(10)}</p>", extra: Pair<String, String>? = null): File {
        val file = files.newFile()
        ZipOutputStream(file.outputStream()).use { zip ->
            fun entry(name: String, value: String) { zip.putNextEntry(ZipEntry(name)); zip.write(value.toByteArray()); zip.closeEntry() }
            entry("mimetype", "application/epub+zip")
            entry("META-INF/container.xml", "<container><rootfiles><rootfile full-path=\"EPUB/content.opf\"/></rootfiles></container>")
            entry("EPUB/content.opf", "<package><manifest><item id=\"a\" href=\"text.xhtml\" media-type=\"application/xhtml+xml\"/></manifest><spine><itemref idref=\"a\"/></spine></package>")
            entry("EPUB/text.xhtml", "<html><body>$body</body></html>")
            extra?.let { entry(it.first, it.second) }
        }
        return file
    }
    @Test fun sanitizesExecutableContentButPreservesFormulasAndTypography() {
        val folder = files.newFolder()
        val book = epub("<h1>Атомы</h1><script>alert(1)</script><iframe src='https://example.com'></iframe><p onclick='alert(1)'>${"Атомы и молекулы. ".repeat(20)}<strong>Связь</strong> H<sub>2</sub>O <sup>+</sup><img src='https://evil.test/tracker.png'/></p><math display='block'><mfrac><mi>m</mi><mi>M</mi></mfrac></math>")
        val publication = EpubProcessor().prepare(book, folder)
        val html = File(folder, "content.html").readText()
        assertEquals("Атомы", publication.chapters.single().title)
        assertTrue(html.contains("<mfrac>")); assertTrue(html.contains("<sub>2</sub>")); assertTrue(html.contains("<strong>"))
        assertFalse(html.contains("script")); assertFalse(html.contains("onclick")); assertFalse(html.contains("iframe")); assertFalse(html.contains("evil.test"))
        assertEquals(publication, EpubProcessor().read(folder))
    }
    @Test fun rejectsZipTraversalBeforeWritingOutsidePublication() {
        val folder = files.newFolder()
        val failure = runCatching { EpubProcessor().prepare(epub(extra = "../../escaped" to "bad"), folder) }.exceptionOrNull() as ReaderFileException
        assertEquals(ReaderError.CorruptBook, failure.error)
        assertFalse(File(folder.parentFile.parentFile, "escaped").exists())
    }
    @Test fun rejectsEncryptedPublication() {
        val failure = runCatching { EpubProcessor().prepare(epub(extra = "META-INF/encryption.xml" to "<encryption/>"), files.newFolder()) }.exceptionOrNull() as ReaderFileException
        assertEquals(ReaderError.UnsupportedFormat, failure.error)
    }
    @Test fun pathResolutionRejectsExternalAndRootEscapes() {
        assertEquals("images/a.png", EpubPaths.resolve("text/a.xhtml", "../images/a.png"))
        listOf("https://bad.test/a", "//bad.test/a", "../../outside", "/etc/passwd", "a?b=1", "a\\b").forEach { assertTrue(it, runCatching { EpubPaths.resolve("text/a.xhtml", it) }.isFailure) }
    }
}
