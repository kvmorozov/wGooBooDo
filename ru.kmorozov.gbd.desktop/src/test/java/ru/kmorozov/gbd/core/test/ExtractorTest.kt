package ru.kmorozov.gbd.core.test

import org.apache.commons.io.IOUtils
import org.eclipse.jetty.http.MimeTypes
import org.eclipse.jetty.http.MimeTypes.Type
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.DefaultHandler
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.db.core.config.IContextLoader
import ru.kmorozov.gbd.core.config.IGBDOptions
import ru.kmorozov.gbd.core.loader.LocalFSStorage
import ru.kmorozov.gbd.core.logic.context.ContextProvider
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleBookInfoExtractor
import ru.kmorozov.gbd.core.logic.extractors.shpl.ShplBookExtractor
import ru.kmorozov.gbd.core.logic.library.metadata.ShplMetadata
import ru.kmorozov.gbd.core.producers.OptionsBasedProducer
import ru.kmorozov.gbd.core.producers.SingleBookProducer
import ru.kmorozov.gbd.logger.output.DummyReceiver

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert
import ru.kmorozov.db.core.logic.model.book.BookInfo
import ru.kmorozov.gbd.core.config.IStorage

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
class ExtractorTest {

    @Before
    fun initServer() {
        ExecutionContext.initContext(DummyReceiver(), true)
        val mockOptions = Mockito.mock(IGBDOptions::class.java)
        Mockito.`when`<IStorage>(mockOptions.storage).thenReturn(LocalFSStorage("E:\\Work\\gbd\\"))
        Mockito.`when`(mockOptions.proxyListFile).thenReturn("E:\\Work\\gbd\\proxy.txt")
        GBDOptions.init(mockOptions)

        val server = Server(80)
        server.handler = object : DefaultHandler() {

            @Throws(IOException::class)
            override fun handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) {
                response.status = 200
                response.contentType = Type.TEXT_HTML.toString()

                var resFileName: String? = null
                if (request.requestURI.contains(ShplMetadata.SHPL_BASE_URL))
                    resFileName = SHPL_HTML_RESOURCE
                else if (request.requestURI.contains("google")) resFileName = GOOGLE_HTML_RESOURCE

                if (null == resFileName) return

                val res = File(javaClass.classLoader.getResource(resFileName)!!.file)
                val data = IOUtils.toString(FileInputStream(res), Charset.forName("UTF-8"))
                response.outputStream.use { os -> os.write(data.toByteArray()) }
            }
        }

        try {
            server.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Test
    fun shplBookInfoTest() {
        val extractor = ShplBookExtractor("http://localhost")

        MatcherAssert.assertThat<BookInfo>(extractor.bookInfo, notNullValue())
    }

    @Test
    fun bookContextLoadTest() {
        val contextProvider = ContextProvider.getContextProvider()

        val ctxSizeBefore = contextProvider!!.contextSize
        Assert.assertTrue(0 < ctxSizeBefore)

        ExecutionContext.INSTANCE.addBookContext(OptionsBasedProducer(), null!!, null!!)
        ExecutionContext.INSTANCE.addBookContext(SingleBookProducer("http://localhost/elib.shpl.ru"), null!!, null!!)

        contextProvider.updateContext()
        contextProvider.refreshContext()

        val ctxSizeAfter = contextProvider.contextSize
        MatcherAssert.assertThat(ctxSizeAfter, `is`(ctxSizeBefore + 1))
    }

    @Test
    fun googleExtractorTest() {
        val extractor = object : GoogleBookInfoExtractor("test") {
            override val bookUrl: String
                get() = "http://localhost/google"
        }

        MatcherAssert.assertThat<BookInfo>(extractor.bookInfo, notNullValue())
    }

    companion object {

        private const val SHPL_HTML_RESOURCE = "shpl/book.html"
        private const val GOOGLE_HTML_RESOURCE = "google/book.html"
    }
}
