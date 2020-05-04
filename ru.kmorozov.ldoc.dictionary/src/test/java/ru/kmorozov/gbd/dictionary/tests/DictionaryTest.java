package ru.kmorozov.gbd.dictionary.tests;

import com.logicaldoc.core.document.Document;
import com.logicaldoc.core.folder.Folder;
import com.logicaldoc.gbd.dictionary.GbdDictionary;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DictionaryTest {

    @Test
    public void loadBookTest() {
        var gbd = new GbdDictionary();

        var folder = mock(Folder.class);
        when(folder.getName()).thenReturn("A Companion to Anglican Eucharistic Theology YjY2q6aLaMEC");
        var document = mock(Document.class);
        when(document.getFolder()).thenReturn(folder);

        var bookInfo = gbd.getBookInfo(document);
        assertNotNull(bookInfo);
    }
}
