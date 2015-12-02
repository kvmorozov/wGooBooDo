package Logic;

import org.junit.Test;
import ru.simpleGBD.app.logic.runtime.ImageExtractor;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by km on 21.11.2015.
 */
public class ExtractorTest {

    private static final String TEST_BOOK_URL = "https://books.google.ru/books?id=BEvEV9OVzacC";

    @Test
    public void testExtractor() {
        ImageExtractor extractor = new ImageExtractor(TEST_BOOK_URL);

        assertTrue(extractor.validate());

        extractor.process();

        assertTrue(extractor.getPagesCount() > 0);

        try {
            for(;;)
                Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
