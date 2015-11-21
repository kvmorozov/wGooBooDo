package Logic;

import org.junit.Test;
import ru.kmorozov.App.Logic.BookProcessor;
import ru.kmorozov.App.Network.ImageExtractor;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by km on 21.11.2015.
 */
public class ExtractorTest {

    private static final String TEST_BOOK_URL = "https://books.google.ru/books?id=BEvEV9OVzacC";

    @Test
    public void testExtractor() {
        BookProcessor processor = new BookProcessor(TEST_BOOK_URL);

        assertTrue(processor.validate());

        ImageExtractor extractor = new ImageExtractor(processor.getBookAddress());
        extractor.process();
    }
}
