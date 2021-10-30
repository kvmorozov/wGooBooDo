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
        GBDOptions.INSTANCE.init(this.options);

        this.folderDao = (FolderDAO) Context.get().getBean(FolderDAO.class);
        this.documentDao = (DocumentDAO) Context.get().getBean(DocumentDAO.class);
        this.userDao = (UserDAO) Context.get().getBean(UserDAO.class);
        this.templateDao = (TemplateDAO) Context.get().getBean(TemplateDAO.class);

        this.adminUser = this.userDao.findByUsernameIgnoreCase("admin");
        this.bookTemplate = this.templateDao.findByName("book", this.adminUser.getTenantId());
    }

    public BookInfo getBookInfo(final Document document) {
        final var folderNameParts = document.getFolder().getName().split(" ");
        final var bookId = folderNameParts[folderNameParts.length - 1];
        if (LibraryFactory.INSTANCE.isValidId(bookId))
            return LibraryFactory.INSTANCE.getMetadata(bookId)
                    .getBookInfoExtractor(bookId, EmptyContextLoader.Companion.getEMPTY_CONTEXT_LOADER()).getBookInfo();
        else
            return BookInfo.Companion.getEMPTY_BOOK();
    }

    public void setDocumentInfo(final Document document) {
        final var bookInfo = this.getBookInfo(document);
        if (bookInfo.getEmpty())
            return;

        document.setTemplate(this.bookTemplate);
        document.setTemplateId(this.bookTemplate.getId());

        this.templateDao.initialize(this.bookTemplate);
        this.documentDao.initialize(document);

        for (final var attribute : this.bookTemplate.getAttributes().entrySet())
            document.getAttributes().put(attribute.getKey(), attribute.getValue());

        this.docTool.store(document);

        document.setValue("bookId", bookInfo.getBookId());
        document.setValue("title", bookInfo.getBookData().getTitle());

        if (bookInfo.getBookData() instanceof GoogleBookData) {
            final var gBookData = (GoogleBookData) bookInfo.getBookData();
            document.setValue("publisherName", gBookData.getPublisher());
            document.setValue("publicationDate", gBookData.getPublicationDate());
            document.setValue("numPages", gBookData.getNumPages());
            document.setValue("bookType", "Google");
        }

        this.docTool.store(document);
    }

    public void setFolderInfo(final Folder folder) {
        final var documents = this.documentDao.findByFolder(folder.getId(), 0);
        for (final var document : documents)
            this.setDocumentInfo(document);

        final var children = this.folderDao.findChildren(folder.getId(), this.adminUser.getId());
        for (final var child : children)
            this.setFolderInfo(child);
    }

}
