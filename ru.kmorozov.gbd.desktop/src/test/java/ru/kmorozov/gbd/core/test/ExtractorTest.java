package ru.kmorozov.gbd.core.test;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.junit.Before;
import org.junit.Test;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.config.IGBDOptions;
import ru.kmorozov.gbd.core.config.storage.AbstractContextProvider;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleBookInfoExtractor;
import ru.kmorozov.gbd.core.logic.extractors.shpl.ShplBookExtractor;
import ru.kmorozov.gbd.core.logic.library.metadata.ShplMetadata;
import ru.kmorozov.gbd.core.logic.output.consumers.DummyBookInfoOutput;
import ru.kmorozov.gbd.desktop.library.OptionsBasedProducer;
import ru.kmorozov.gbd.desktop.library.SingleBookProducer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ExtractorTest {

    private static final String SHPL_HTML_RESOURCE = "shpl/book.html";
    private static final String GOOGLE_HTML_RESOURCE = "google/book.html";

    @Before
    public void initServer() {
        ExecutionContext.initContext(new DummyBookInfoOutput(), true);
        IGBDOptions mockOptions = mock(IGBDOptions.class);
        when(mockOptions.getOutputDir()).thenReturn("E:\\Work\\gbd\\");
        when(mockOptions.getProxyListFile()).thenReturn("E:\\Work\\gbd\\proxy.txt");
        GBDOptions.init(mockOptions);

        Server server = new Server(80);
        server.setHandler(new DefaultHandler() {

            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                response.setStatus(200);
                response.setContentType(MimeTypes.Type.TEXT_HTML.toString());

                String resFileName = null;
                if (request.getRequestURI().contains(ShplMetadata.SHPL_BASE_URL)) resFileName = SHPL_HTML_RESOURCE;
                else if (request.getRequestURI().contains("google")) resFileName = GOOGLE_HTML_RESOURCE;

                if (resFileName == null) return;

                File res = new File(getClass().getClassLoader().getResource(resFileName).getFile());
                String data = IOUtils.toString(new FileInputStream(res), Charset.forName("UTF-8"));
                response.getOutputStream().write(data.getBytes());
            }
        });

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shplBookInfoTest() {
        ShplBookExtractor extractor = new ShplBookExtractor("http://localhost");

        assertNotNull(extractor.getBookInfo());
    }

    @Test
    public void bookContextLoadTest() {
        AbstractContextProvider contextProvider = AbstractContextProvider.getContextProvider();

        int ctxSizeBefore = contextProvider.getContextSize();
        assertTrue(ctxSizeBefore > 0);

        ExecutionContext.INSTANCE.addBookContext(new OptionsBasedProducer(), null, null);
        ExecutionContext.INSTANCE.addBookContext(new SingleBookProducer("http://localhost/elib.shpl.ru"), null, null);

        contextProvider.updateContext();
        contextProvider.refreshContext();

        int ctxSizeAfter = contextProvider.getContextSize();
        assertEquals(ctxSizeBefore + 1, ctxSizeAfter);
    }

    @Test
    public void googleExtractorTest() {
        GoogleBookInfoExtractor extractor = new GoogleBookInfoExtractor("test") {
            @Override
            protected String getBookUrl() {
                return "http://localhost/google";
            }
        };

        assertNotNull(extractor.getBookInfo());
    }
}
