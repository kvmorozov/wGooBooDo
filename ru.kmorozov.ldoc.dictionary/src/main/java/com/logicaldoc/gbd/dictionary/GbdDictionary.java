package com.logicaldoc.gbd.dictionary;

import com.logicaldoc.core.automation.AutomationDictionary;
import com.logicaldoc.core.automation.DocTool;
import com.logicaldoc.core.document.Document;
import com.logicaldoc.core.document.dao.DocumentDAO;
import com.logicaldoc.core.folder.Folder;
import com.logicaldoc.core.folder.FolderDAO;
import com.logicaldoc.core.metadata.Template;
import com.logicaldoc.core.metadata.TemplateDAO;
import com.logicaldoc.core.security.User;
import com.logicaldoc.core.security.dao.UserDAO;
import com.logicaldoc.util.Context;
import ru.kmorozov.db.core.config.EmptyContextLoader;
import ru.kmorozov.db.core.logic.model.book.BookInfo;
import ru.kmorozov.db.core.logic.model.book.google.GoogleBookData;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.library.LibraryFactory;

@AutomationDictionary(key = "gbd")
public class GbdDictionary {

    private final DictionaryOptions options = new DictionaryOptions();

    private final FolderDAO folderDao;
    private final DocumentDAO documentDao;
    private final UserDAO userDao;
    private final TemplateDAO templateDao;

    private final DocTool docTool = new DocTool();

    private final User adminUser;
    private final Template bookTemplate;

    public GbdDictionary() {
        GBDOptions.INSTANCE.init(options);

        folderDao = (FolderDAO) Context.get().getBean(FolderDAO.class);
        documentDao = (DocumentDAO) Context.get().getBean(DocumentDAO.class);
        userDao = (UserDAO) Context.get().getBean(UserDAO.class);
        templateDao = (TemplateDAO) Context.get().getBean(TemplateDAO.class);

        adminUser = userDao.findByUsernameIgnoreCase("admin");
        bookTemplate = templateDao.findByName("book", adminUser.getTenantId());
    }

    public BookInfo getBookInfo(Document document) {
        var folderNameParts = document.getFolder().getName().split(" ");
        var bookId = folderNameParts[folderNameParts.length - 1];
        if (LibraryFactory.INSTANCE.isValidId(bookId))
            return LibraryFactory.INSTANCE.getMetadata(bookId)
                    .getBookInfoExtractor(bookId, EmptyContextLoader.Companion.getEMPTY_CONTEXT_LOADER()).getBookInfo();
        else
            return BookInfo.Companion.getEMPTY_BOOK();
    }

    public void setDocumentInfo(Document document) {
        var bookInfo = getBookInfo(document);
        if (bookInfo.getEmpty())
            return;

        document.setTemplate(bookTemplate);
        document.setTemplateId(bookTemplate.getId());

        templateDao.initialize(bookTemplate);
        documentDao.initialize(document);

        for (var attribute : bookTemplate.getAttributes().entrySet())
            document.getAttributes().put(attribute.getKey(), attribute.getValue());

        docTool.store(document);

        document.setValue("bookId", bookInfo.getBookId());
        document.setValue("title", bookInfo.getBookData().getTitle());

        if (bookInfo.getBookData() instanceof GoogleBookData) {
            var gBookData = (GoogleBookData) bookInfo.getBookData();
            document.setValue("publisherName", gBookData.getPublisher());
            document.setValue("publicationDate", gBookData.getPublicationDate());
            document.setValue("numPages", gBookData.getNumPages());
            document.setValue("bookType", "Google");
        }

        docTool.store(document);
    }

    public void setFolderInfo(Folder folder) {
        var documents = documentDao.findByFolder(folder.getId(), 0);
        for (var document : documents)
            setDocumentInfo(document);

        var children = folderDao.findChildren(folder.getId(), adminUser.getId());
        for (var child : children)
            setFolderInfo(child);
    }

}
