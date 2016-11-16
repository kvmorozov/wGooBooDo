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
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.shpl.ShplBookExtractor;
import ru.kmorozov.gbd.core.logic.output.consumers.DummyBookInfoOutput;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by sbt-morozov-kv on 16.11.2016.
 */
public class ShplExtractorTest {

    private static final String SHPL_HTML_RESOURCE = "shpl/book.html";

    @Before
    public void initServer() {
        ExecutionContext.initContext(new DummyBookInfoOutput(), true);
        IGBDOptions mockOptions = mock(IGBDOptions.class);
        when(mockOptions.getOutputDir()).thenReturn("");
        GBDOptions.init(mockOptions);

        Server server = new Server(80);
        server.setHandler(new DefaultHandler() {

            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                response.setStatus(200);
                response.setContentType(MimeTypes.Type.TEXT_HTML.toString());

                File res = new File(getClass().getClassLoader().getResource(SHPL_HTML_RESOURCE).getFile());
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
}
