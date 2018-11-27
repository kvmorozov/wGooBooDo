package ru.kmorozov.gbd.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.config.options.PdfOptions;
import ru.kmorozov.gbd.core.loader.LocalFSStorage;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor;
import ru.kmorozov.db.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.logger.Logger;
import ru.kmorozov.gbd.utils.Images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.DEFAULT_PAGE_WIDTH;

/**
 * Created by km on 17.12.2015.
 */
public class PdfMaker implements IPostProcessor {

    private BookContext bookContext;

    public PdfMaker(BookContext bookContext) {
        this.bookContext = bookContext;
    }

    public PdfMaker() {
    }

    @Override
    public void make() {
        if (PdfOptions.SKIP == GBDOptions.pdfOptions())
            return;

        Logger logger = ExecutionContext.INSTANCE.getLogger(PdfMaker.class, this.bookContext);
        logger.info("Starting making pdf file...");

        if (!this.bookContext.pdfCompleted.compareAndSet(false, true)) return;

        File imgDir = ((LocalFSStorage) this.bookContext.getStorage()).getStorageDir();
        long existPages = 0L;
        BookInfo bookInfo = this.bookContext.getBookInfo();

        File pdfFile = null;
        try {
            List<Path> pdfFiles = Files.list(imgDir.toPath()).filter(Images::isPdfFile).collect(Collectors.toList());
            if (null != pdfFiles && 1 == pdfFiles.size()) pdfFile = pdfFiles.get(0).toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (null == pdfFile)
            pdfFile = new File(imgDir.getPath() + File.separator + bookInfo.getBookData().getTitle().replaceAll("[^А-Яа-яa-zA-Z0-9-]", " ") + ".pdf");
        try {
            if (Files.exists(pdfFile.toPath())) {
                if (pdfFile.lastModified() < bookInfo.getLastPdfChecked()) existPages = this.bookContext.getPagesBefore();
                else try (final PDDocument existDocument = PDDocument.load(pdfFile)) {
                    existPages = (long) existDocument.getNumberOfPages();
                } catch (Exception ex) {
                    pdfFile.createNewFile();
                }
            } else pdfFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            long imgCount = Files.list(imgDir.toPath()).filter(Images::isImageFile).count();
            if (imgCount <= existPages) {
                logger.finest("No new pages, exiting...");
                bookInfo.setLastPdfChecked(System.currentTimeMillis());
                return;
            } else logger.info(String.format("Rewriting pdf from %d to %d pages", existPages, imgCount));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int imgWidth = 0 == GBDOptions.getImageWidth() ? DEFAULT_PAGE_WIDTH : GBDOptions.getImageWidth();

        try (final PDDocument document = new PDDocument()) {
            Files.list(imgDir.toPath()).filter(Images::isImageFile).sorted(Comparator.comparing(PdfMaker::getPagenum)).forEach(filePath -> {
                try (final InputStream in = new FileInputStream(filePath.toFile())) {
                    if (!Images.isInvalidImage(filePath, imgWidth)) {
                        BufferedImage bimg = ImageIO.read(in);

                        if (null == bimg) {
                            Files.delete(filePath);
                            logger.severe(String.format("Image %s was deleted!", filePath.getFileName()));
                        } else {
                            float width = (float) bimg.getWidth();
                            float height = (float) bimg.getHeight();
                            PDPage page = new PDPage(new PDRectangle(width, height));
                            document.addPage(page);
                            PDImageXObject img = PDImageXObject.createFromFile(filePath.toString(), document);
                            try (final PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                                contentStream.drawImage(img, (float) 0, (float) 0);
                            }
                        }
                    } else {
                        Files.delete(filePath);
                        logger.severe(String.format("Image %s was deleted!", filePath.getFileName()));
                    }
                } catch (FileSystemException fse) {

                } catch (IOException e) {
                    try {
                        Files.delete(filePath);
                        logger.severe(String.format("Image %s was deleted!", filePath.getFileName()));
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            });

            document.save(pdfFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        bookInfo.setLastPdfChecked(System.currentTimeMillis());
        logger.info("Pdf completed.");
    }

    private static Integer getPagenum(Path pagePath) {
        String name = pagePath.getFileName().toString();
        try {
            return Integer.parseInt(name.split("_")[0]);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }

    @Override
    public PdfMaker getPostProcessor(BookContext bookContext) {
        return new PdfMaker(bookContext);
    }

    @Override
    public void run() {
        this.make();
    }

    @Override
    public BookContext getUniqueObject() {
        return this.bookContext;
    }

    @Override
    public String toString() {
        return "Pdf maker:" + this.bookContext;
    }
}
