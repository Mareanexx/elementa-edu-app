package ru.mareanexx.reader_impl.data.epub

import com.google.gson.Gson
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist
import ru.mareanexx.reader_api.domain.model.*
import java.io.File
import java.util.zip.ZipFile

/** Распаковывает EPUB в закрытый каталог и удаляет исполняемое и внешнее содержимое. */
internal class EpubProcessor {
    fun prepare(epub: File, directory: File): PreparedPublication {
        val sources = File(directory, "resources").apply { mkdirs() }
        extract(epub, sources)
        if (File(sources, "META-INF/encryption.xml").exists()) throw ReaderFileException(ReaderError.UnsupportedFormat)
        val container = xml(EpubPaths.file(sources, "META-INF/container.xml"))
        val packagePath = container.getAllElements().firstOrNull { it.tagName().substringAfter(':') == "rootfile" }?.attr("full-path")?.takeIf(String::isNotBlank)
            ?: throw ReaderFileException(ReaderError.CorruptBook)
        val opf = xml(EpubPaths.file(sources, packagePath))
        if (opf.getAllElements().any { it.tagName().substringAfter(':') == "meta" && it.attr("property") == "rendition:layout" && it.text() == "pre-paginated" }) throw ReaderFileException(ReaderError.UnsupportedFormat)
        val manifest = opf.getAllElements().filter { it.tagName().substringAfter(':') == "item" }.associateBy { it.id() }
        val spine = opf.getAllElements().filter { it.tagName().substringAfter(':') == "itemref" && it.attr("linear") != "no" }.map { ref ->
            val item = manifest[ref.attr("idref")] ?: throw ReaderFileException(ReaderError.CorruptBook)
            if (item.attr("media-type") !in listOf("application/xhtml+xml", "text/html")) throw ReaderFileException(ReaderError.UnsupportedFormat)
            EpubPaths.resolve(packagePath, item.attr("href"))
        }
        if (spine.isEmpty() || spine.size > 500) throw ReaderFileException(ReaderError.CorruptBook)
        val chapterIds = spine.mapIndexed { index, path -> path to "chapter_${index + 1}" }.toMap()
        val chapters = mutableListOf<ReaderChapter>()
        val html = StringBuilder()
        var characters = 0
        spine.forEach { path ->
            val document = Jsoup.parse(EpubPaths.file(sources, path), "UTF-8")
            document.outputSettings().prettyPrint(false)
            document.select("script,style,iframe,object,embed,form,input,button,textarea,link,base,video,audio,source").remove()
            val title = document.selectFirst("h1,h2,h3")?.text()?.takeIf(String::isNotBlank) ?: document.title().takeIf(String::isNotBlank) ?: ""
            val chapterId = chapterIds.getValue(path)
            val ids = document.body().getAllElements().filter { it.id().isNotBlank() }.associate { it.id() to "${chapterId}_${it.id()}" }
            document.body().getAllElements().forEach { node ->
                if (node.id().isNotBlank()) node.attr("id", ids.getValue(node.id()))
                if (node.tagName() == "img") {
                    val resource = runCatching { EpubPaths.resolve(path, node.attr("src")) }.getOrNull()
                    if (resource == null || resource.substringAfterLast('.').lowercase() !in setOf("png", "jpg", "jpeg", "gif", "webp") || !EpubPaths.file(sources, resource).isFile) node.remove()
                    else node.attr("src", "/publication/" + URIPath.encode(resource))
                }
                if (node.tagName() == "a") {
                    val href = node.attr("href")
                    val target = runCatching { EpubPaths.resolve(path, href) }.getOrNull()
                    val id = target?.let(chapterIds::get)
                    val fragment = runCatching { java.net.URI(href).fragment }.getOrNull()
                    if (id != null) node.attr("href", "#" + id + if (fragment.isNullOrEmpty()) "" else "_$fragment") else node.removeAttr("href")
                }
            }
            val cleaned = Cleaner(safelist()).clean(document)
            cleaned.outputSettings().prettyPrint(false).syntax(Document.OutputSettings.Syntax.html)
            val body = cleaned.body()
            body.select("math[display=block]").forEach { math -> math.wrap("<div class='reader-formula'></div>") }
            val section = Element("section").attr("data-chapter", chapterId).attr("id", chapterId).attr("data-title", title)
            body.childNodes().toList().forEach { section.appendChild(it) }
            html.append(section.outerHtml())
            characters += section.wholeText().length
            chapters += ReaderChapter(chapterId, title.take(200))
        }
        if (characters < 100 || characters > 5_000_000) throw ReaderFileException(ReaderError.CorruptBook)
        File(directory, "content.html").writeText(html.toString())
        return PreparedPublication(chapters, characters).also { File(directory, "publication.json").writeText(Gson().toJson(it)) }
    }
    fun read(directory: File): PreparedPublication = Gson().fromJson(File(directory, "publication.json").readText(), PreparedPublication::class.java)
    private fun xml(file: File): Document {
        if (!file.isFile || file.length() > 2_000_000) throw ReaderFileException(ReaderError.CorruptBook)
        return Jsoup.parse(file.readText(), "", Parser.xmlParser())
    }
    private fun extract(epub: File, directory: File) {
        ZipFile(epub).use { zip ->
            if (zip.size() !in 3..5000) throw ReaderFileException(ReaderError.CorruptBook)
            val type = zip.getEntry("mimetype") ?: throw ReaderFileException(ReaderError.UnsupportedFormat)
            if (type.size !in 1..64 || zip.getInputStream(type).bufferedReader().use { it.readText() }.trim() != "application/epub+zip") throw ReaderFileException(ReaderError.UnsupportedFormat)
            var expanded = 0L
            val paths = mutableSetOf<String>()
            zip.entries().asSequence().forEach { entry ->
                val output = try { EpubPaths.file(directory, entry.name) } catch (_: IllegalArgumentException) { throw ReaderFileException(ReaderError.CorruptBook) }
                if (!paths.add(output.path)) throw ReaderFileException(ReaderError.CorruptBook)
                if (entry.isDirectory) output.mkdirs()
                else {
                    if (entry.size > 12_000_000) throw ReaderFileException(ReaderError.TooLarge)
                    output.parentFile!!.mkdirs()
                    var size = 0L
                    zip.getInputStream(entry).use { input -> output.outputStream().use { sink ->
                        val buffer = ByteArray(32 * 1024)
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            size += count; expanded += count
                            if (size > 12_000_000 || expanded > 150_000_000) throw ReaderFileException(ReaderError.TooLarge)
                            sink.write(buffer, 0, count)
                        }
                    } }
                }
            }
        }
    }
    private fun safelist() = Safelist.none().addTags(
        "p", "div", "span", "h1", "h2", "h3", "h4", "h5", "h6", "b", "strong", "i", "em", "u", "s", "sup", "sub", "br", "hr", "a", "blockquote", "ul", "ol", "li", "dl", "dt", "dd", "table", "thead", "tbody", "tr", "th", "td", "figure", "figcaption", "img", "pre", "code", "small", "q", "section", "aside",
        "math", "mrow", "mi", "mn", "mo", "mtext", "ms", "mspace", "mfrac", "msqrt", "mroot", "msub", "msup", "msubsup", "munder", "mover", "munderover", "mtable", "mtr", "mtd", "menclose", "mphantom", "mpadded", "semantics", "annotation", "mmultiscripts", "mprescripts", "none",
    ).addAttributes(":all", "id", "lang", "dir").addAttributes("a", "href").addAttributes("img", "src", "alt", "width", "height")
        .addAttributes("math", "display", "xmlns").addAttributes("mo", "stretchy", "fence", "separator").addAttributes("mspace", "width", "height", "depth")
        .addAttributes("mi", "mathvariant").addAttributes("mn", "mathvariant").addAttributes("mtable", "columnalign").addAttributes("td", "colspan", "rowspan").addAttributes("th", "colspan", "rowspan")
}
