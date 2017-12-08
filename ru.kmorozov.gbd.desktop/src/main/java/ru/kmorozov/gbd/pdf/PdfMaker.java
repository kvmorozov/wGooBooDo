package ru.kmorozov.gbd.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import ru.kmorozov.gbd.core.PdfOptions;
import ru.kmorozov.gbd.core.config.GBDOptions;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.context.ExecutionContext;
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor;
import ru.kmorozov.gbd.core.logic.extractors.google.GoogleImageExtractor;
import ru.kmorozov.gbd.core.logic.model.book.base.BookInfo;
import ru.kmorozov.gbd.core.utils.Images;
import ru.kmorozov.gbd.core.utils.Logger;

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

/**
 * Created by km on 17.12.2015.
 */
public class PdfMaker implements IPostProcessor {

    private BookContext bookContext;

    public PdfMaker(final BookContext bookContext) {
        this.bookContext = bookContext;
    }

    public PdfMaker() {
    }

    @Override
    public void make() {
        if (PdfOptions.SKIP == GBDOptions.pdfOptions())
            return;

        final Logger logger = ExecutionContext.INSTANCE.getLogger(PdfMaker.class, bookContext);
        logger.info("Starting making pdf file...");

        if (!bookContext.pdfCompleted.compareAndSet(false, true)) return;

        final File imgDir = bookContext.getOutputDir();
        long existPages = 0;
        final BookInfo bookInfo = bookContext.getBookInfo();

        File pdfFile = null;
        try {
            final List<Path> pdfFiles = Files.list(imgDir.toPath()).filter(Images::isPdfFile).collect(Collectors.toList());
            if (null != pdfFiles && 1 == pdfFiles.size()) pdfFile = pdfFiles.get(0).toFile();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        if (null == pdfFile) pdfFile = new File(imgDir.getPath() + File.separator + bookInfo.getBookData().getTitle().replaceAll("[^А-Яа-яa-zA-Z0-9-]", " ") + ".pdf");
        try {
            if (Files.exists(pdfFile.toPath())) {
                if (pdfFile.lastModified() < bookInfo.getLastPdfChecked()) existPages = bookContext.getPagesBefore();
                else try (PDDocument existDocument = PDDocument.load(pdfFile)) {
                    existPages = existDocument.getNumberOfPages();
                } catch (final Exception ex) {
                    pdfFile.createNewFile();
                }
            }
            else pdfFile.createNewFile();
        } catch (final IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            final long imgCount = Files.list(imgDir.toPath()).filter(Images::isImageFile).count();
            if (imgCount <= existPages) {
                logger.finest("No new pages, exiting...");
                bookInfo.setLastPdfChecked(System.currentTimeMillis());
                return;
            }
            else logger.info(String.format("Rewriting pdf from %d to %d pages", existPages, imgCount));
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final int imgWidth = 0 == GBDOptions.getImageWidth() ? GoogleImageExtractor.DEFAULT_PAGE_WIDTH : GBDOptions.getImageWidth();

        try (PDDocument document = new PDDocument()) {
            Files.list(imgDir.toPath()).filter(Images::isImageFile).sorted(Comparator.comparing(PdfMaker::getPagenum)).forEach(filePath -> {
                try (InputStream in = new FileInputStream(filePath.toFile())) {
                    if (!Images.isInvalidImage(filePath, imgWidth)) {
                        final BufferedImage bimg = ImageIO.read(in);

                        if (null == bimg) {
                            Files.delete(filePath);
                            logger.severe(String.format("Image %s was deleted!", filePath.getFileName()));
                        }
                        else {
                            final float width = bimg.getWidth();
                            final float height = bimg.getHeight();
                            final PDPage page = new PDPage(new PDRectangle(width, height));
                            document.addPage(page);
                            final PDImageXObject img = PDImageXObject.createFromFile(filePath.toString(), document);
                            try(PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                                contentStream.drawImage(img, 0, 0);
                            }
                        }
                    }
                    else {
                        Files.delete(filePath);
                        logger.severe(String.format("Image %s was deleted!", filePath.getFileName()));
                    }
                } catch (final FileSystemException fse) {

                } catch (final IOException e) {
                    try {
                        Files.delete(filePath);
                        logger.severe(String.format("Image %s was deleted!", filePath.getFileName()));
                    } catch (final IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            });

            document.save(pdfFile);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        bookInfo.setLastPdfChecked(System.currentTimeMillis());
        logger.info("Pdf completed.");
    }

    private static Integer getPagenum(final Path pagePath) {
        final String name = pagePath.getFileName().toString();
        try {
            return Integer.parseInt(name.split("_")[0]);
        } catch (final NumberFormatException nfe) {
            return -1;
        }
    }

    @Override
    public PdfMaker getPostProcessor(final BookContext bookContext) {
        return new PdfMaker(bookContext);
    }

    @Override
    public void run() {
        make();
    }

    @Override
    public BookContext getUniqueObject() {
        return bookContext;
    }

    @Override
    public String toString() {
        return "Pdf maker:" + bookContext;
    }
}
