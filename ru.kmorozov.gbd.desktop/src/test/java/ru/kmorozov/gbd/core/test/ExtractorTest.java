package ru.kmorozov.gbd.core.test;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import static org.hamcrest.CoreMatchers.*;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ExtractorTest {

    private static final String SHPL_HTML_RESOURCE = "shpl/book.html";
    private static final String GOOGLE_HTML_RESOURCE = "google/book.html";

    @Before
    public void initServer() {
        ExecutionContext.initContext(new DummyBookInfoOutput(), true);
        final IGBDOptions mockOptions = Mockito.mock(IGBDOptions.class);
        Mockito.when(mockOptions.getOutputDir()).thenReturn("E:\\Work\\gbd\\");
        Mockito.when(mockOptions.getProxyListFile()).thenReturn("E:\\Work\\gbd\\proxy.txt");
        GBDOptions.init(mockOptions);

        final Server server = new Server(80);
        server.setHandler(new DefaultHandler() {

            @Override
            public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
                response.setStatus(200);
                response.setContentType(Type.TEXT_HTML.toString());

                String resFileName = null;
                if (request.getRequestURI().contains(ShplMetadata.SHPL_BASE_URL)) resFileName = SHPL_HTML_RESOURCE;
                else if (request.getRequestURI().contains("google")) resFileName = GOOGLE_HTML_RESOURCE;

                if (null == resFileName) return;

                final File res = new File(getClass().getClassLoader().getResource(resFileName).getFile());
                final String data = IOUtils.toString(new FileInputStream(res), Charset.forName("UTF-8"));
                try (OutputStream os = response.getOutputStream()) {
                    os.write(data.getBytes());
                }
            }
        });

        try {
            server.start();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shplBookInfoTest() {
        final ShplBookExtractor extractor = new ShplBookExtractor("http://localhost");

        Assert.assertThat(extractor.getBookInfo(), notNullValue());
    }

    @Test
    public void bookContextLoadTest() {
        final AbstractContextProvider contextProvider = AbstractContextProvider.getContextProvider();

        final int ctxSizeBefore = contextProvider.getContextSize();
        Assert.assertTrue(0 < ctxSizeBefore);

        ExecutionContext.INSTANCE.addBookContext(new OptionsBasedProducer(), null, null);
        ExecutionContext.INSTANCE.addBookContext(new SingleBookProducer("http://localhost/elib.shpl.ru"), null, null);

        contextProvider.updateContext();
        contextProvider.refreshContext();

        final int ctxSizeAfter = contextProvider.getContextSize();
        Assert.assertThat(ctxSizeAfter, is(ctxSizeBefore + 1));
    }

    @Test
    public void googleExtractorTest() {
        final GoogleBookInfoExtractor extractor = new GoogleBookInfoExtractor("test") {
            @Override
            protected String getBookUrl() {
                return "http://localhost/google";
            }
        };

        Assert.assertThat(extractor.getBookInfo(), notNullValue());
    }
}
