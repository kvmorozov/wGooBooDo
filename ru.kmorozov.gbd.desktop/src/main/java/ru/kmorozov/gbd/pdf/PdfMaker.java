package ru.kmorozov.gbd.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor;
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

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

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
        Logger logger = INSTANCE.getLogger(PdfMaker.class, bookContext);
        logger.info("Starting making pdf file...");

        if (!bookContext.pdfCompleted.compareAndSet(false, true)) return;

        File imgDir = bookContext.getOutputDir();
        long existPages = 0;
        BookInfo bookInfo = bookContext.getBookInfo();

        File pdfFile = null;
        try {
            List<Path> pdfFiles = Files.list(imgDir.toPath()).filter(Images::isPdfFile).collect(Collectors.toList());
            if (pdfFiles != null && pdfFiles.size() == 1) pdfFile = pdfFiles.get(0).toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (pdfFile == null) pdfFile = new File(imgDir.getPath() + File.separator + bookInfo.getBookData().getTitle().replaceAll("[^А-Яа-яa-zA-Z0-9-]", " ") + ".pdf");
        try {
            if (Files.exists(pdfFile.toPath())) {
                if (pdfFile.lastModified() < bookInfo.getLastPdfChecked()) existPages = bookContext.getPagesBefore();
                else try (PDDocument existDocument = PDDocument.load(pdfFile)) {
                    existPages = existDocument.getNumberOfPages();
                } catch (Exception ex) {
                    pdfFile.createNewFile();
                }
            }
            else pdfFile.createNewFile();
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
            }
            else logger.info(String.format("Rewriting pdf from %d to %d pages", existPages, imgCount));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PDDocument document = new PDDocument()) {
            Files.list(imgDir.toPath()).filter(Images::isImageFile).sorted(Comparator.comparing(this::getPagenum)).forEach(filePath -> {
                try (InputStream in = new FileInputStream(filePath.toFile())) {
                    if (!Images.isInvalidImage(filePath)) {
                        BufferedImage bimg = ImageIO.read(in);

                        if (bimg == null) {
                            Files.delete(filePath);
                            logger.severe(String.format("Image %s was deleted!", filePath.getFileName()));
                        }
                        else {
                            float width = bimg.getWidth();
                            float height = bimg.getHeight();
                            PDPage page = new PDPage(new PDRectangle(width, height));
                            document.addPage(page);
                            PDImageXObject img = PDImageXObject.createFromFile(filePath.toString(), document);
                            PDPageContentStream contentStream = new PDPageContentStream(document, page);
                            contentStream.drawImage(img, 0, 0);
                            contentStream.close();
                        }
                    }
                    else {
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

    private Integer getPagenum(Path pagePath) {
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
        make();
    }

    @Override
    public BookContext getUniqueObject() {
        return bookContext;
    }

    @Override
    public String toString() {
        return "Pdf maker:" + bookContext.toString();
    }
}
