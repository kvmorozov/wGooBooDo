package ru.kmorozov.library.data.loader.processors

import org.apache.commons.lang3.tuple.ImmutablePair
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import ru.kmorozov.gbd.core.logic.Proxy.EmptyProxyListProvider
import ru.kmorozov.gbd.core.logic.Proxy.ManagedProxyListProvider
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector
import ru.kmorozov.gbd.core.logic.connectors.google.GoogleHttpConnector
import ru.kmorozov.gbd.logger.Logger
import ru.kmorozov.library.data.model.book.Book
import ru.kmorozov.library.data.model.book.BookInfo
import ru.kmorozov.library.data.model.book.IdType
import ru.kmorozov.library.data.repository.BooksRepository
import java.io.IOException
import java.util.*
import java.util.stream.Collectors

@Component
class JstorProcessor : IProcessor {

    @Value("\${mongo.reactive.mode}")
    private val reactiveMode: Boolean = false

    @Autowired
    private val proxyProvider: ManagedProxyListProvider? = null

    @Autowired
    @Lazy
    private val jstorConnector: HttpConnector? = null

    @Autowired
    protected var booksRepository: BooksRepository? = null

    @Bean
    fun proxyProvider(): ManagedProxyListProvider {
        return ManagedProxyListProvider(EmptyProxyListProvider.INSTANCE, 500)
    }

    override fun process() {
        logger.info("Process JSTOR started.")

        booksRepository!!
                .findBooksByRegexBookInfoFileName("^\\d+.pdf")
                .stream()
                .filter { book -> book.bookInfo.getCustomFields() == null }
                .forEach { this.processJstorBook(it) }

        logger.info("Process JSTOR finished.")
    }

    private fun processJstorBook(jstorBook: Book) {
        val fileName = jstorBook.bookInfo.fileName
        val jstorId = fileName!!.substring(0, fileName.length - 4)

        val proxy = proxyProvider!!.proxy

        val doi: String
        try {
            val doc = jstorConnector!!.getString(JSTOR_ARTICLE_PREFIX + jstorId, proxy, true)
            val opDoi = Arrays.stream(doc.split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                    .filter { s -> s.contains("ST.discriminator") }
                    .findFirst()
            if (opDoi.isPresent)
                doi = Arrays.stream(opDoi.get().split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                        .filter { s -> s.contains("content") }
                        .map { s -> s.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] }
                        .findFirst().get().replace("\"", "")
            else {
                logger.error(String.format("%s not a valid JSTOR id", jstorId))
                return
            }
        } catch (e: IOException) {
            logger.error(String.format("%s not a valid JSTOR id", jstorId), e)
            return
        }

        jstorBook.addBookId(IdType.JSTOR, jstorId)
        booksRepository!!.save(jstorBook)

        try {
            val doiMap = parseArticleData(doiConnector.getString(JSTOR_CITATION_PREFIX + doi, proxy, true))
            if (!doiMap.isEmpty()) {
                jstorBook.bookInfo.bookType = BookInfo.BookType.ARTICLE
                jstorBook.bookInfo.setCustomFields(doiMap)
                booksRepository!!.save(jstorBook)

                logger.info(String.format("Saved DOI data for %s", doi))
            } else
                logger.info(String.format("Not saved DOI data for %s", doi))
        } catch (e: IOException) {
            logger.warn(String.format("Invalid DOI %s for %s", doi, jstorId))
            return
        }

        jstorBook.addBookId(IdType.DOI, doi)
        booksRepository!!.save(jstorBook)
    }

    private fun parseArticleData(data: String): MutableMap<String, String> {
        return Arrays.stream(data.split("\\r?\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                .filter { s -> s.contains("=") }
                .map<ImmutablePair<String, String>> { s ->
                    val items = s.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (items.size == 2) ImmutablePair(formatItem(items[0]), formatItem(items[1])) else null
                }
                .collect(Collectors.toMap({ it.getLeft() }, { it.getRight() }))
    }

    private fun formatItem(item: String): String {
        return item.trim { it <= ' ' }
                .replace("{", "")
                .replace("},", "")
                .replace("}", "")
                .trim { it <= ' ' }
    }

    companion object {

        private const val JSTOR_ARTICLE_PREFIX = "https://www.jstor.org/stable/"
        private const val JSTOR_CITATION_PREFIX = "https://www.jstor.org/citation/text/"

        protected val logger = Logger.getLogger(JstorProcessor::class.java)

        private val doiConnector = GoogleHttpConnector()
    }
}
