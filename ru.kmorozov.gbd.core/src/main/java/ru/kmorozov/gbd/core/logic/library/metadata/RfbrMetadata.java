package ru.kmorozov.gbd.core.logic.library.metadata;

import org.apache.commons.lang3.StringUtils;
import ru.kmorozov.gbd.core.logic.connectors.HttpConnector;
import ru.kmorozov.gbd.core.logic.connectors.apache.ApacheHttpConnector;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.AbstractBookExtractor;
import ru.kmorozov.gbd.core.logic.extractors.base.IImageExtractor;
import ru.kmorozov.gbd.core.logic.extractors.rfbr.RfbrBookExtractor;
import ru.kmorozov.gbd.core.logic.extractors.rfbr.RfbrImageExtractor;
import ru.kmorozov.gbd.core.logic.library.ILibraryMetadata;

import java.util.Arrays;
import java.util.List;

public final class RfbrMetadata implements ILibraryMetadata {

    public static final RfbrMetadata RFBR_METADATA = new RfbrMetadata();

    @Override
    public boolean isValidId(String bookId) {
        return StringUtils.isNumeric(bookId);
    }

    @Override
    public IImageExtractor getExtractor(BookContext bookContext) {
        return new RfbrImageExtractor(bookContext);
    }

    @Override
    public AbstractBookExtractor getBookExtractor(String bookId) {
        return new RfbrBookExtractor(bookId);
    }

    @Override
    public boolean needSetCookies() {
        return false;
    }

    @Override
    public List<HttpConnector> preferredConnectors() {
        return Arrays.asList(new ApacheHttpConnector());
    }
}
