package ru.kmorozov.gbd.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import ru.kmorozov.gbd.core.logic.context.BookContext;
import ru.kmorozov.gbd.core.logic.extractors.IPostProcessor;
import ru.kmorozov.gbd.core.logic.model.book.BookInfo;
import ru.kmorozov.gbd.core.utils.Images;
import ru.kmorozov.gbd.core.utils.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.kmorozov.gbd.core.logic.context.ExecutionContext.INSTANCE;

/**
 * Created by km on 17.12.2015.
 */
public class PdfMaker implements IPostProcessor {

    private static final Logger logger = INSTANCE.getLogger(PdfMaker.class);

    @Override
    public void make(BookContext bookContext) {
        File imgDir = bookContext.getOutputDir();
        int existPages = 0;
        BookInfo bookInfo = bookContext.getBookInfo();
        File pdfFile = new File(imgDir.getPath() + File.separator + bookInfo.getBookData().getTitle().replaceAll("[^А-Яа-яa-zA-Z0-9.-]", " ") + ".pdf");
        try {
            if (Files.exists(pdfFile.toPath()))
                existPages = PDDocument.load(pdfFile).getNumberOfPages();
            else pdfFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        logger.info("Starting making pdf file...");
        try (PDDocument document = new PDDocument()) {
            try {
                long imgCount = Files.list(imgDir.toPath()).filter(Images::isImageFile).count();
                if (imgCount <= existPages) {
                    logger.info("No new pages, exiting...");
                    return;
                }

                Files.list(imgDir.toPath()).filter(Images::isImageFile).sorted((o1, o2) -> getPagenum(o1).compareTo(getPagenum(o2))).forEach(filePath -> {
                    try (InputStream in = new FileInputStream(filePath.toFile())) {
                        BufferedImage bimg = ImageIO.read(in);
                        float width = bimg.getWidth();
                        float height = bimg.getHeight();
                        PDPage page = new PDPage(new PDRectangle(width, height));
                        document.addPage(page);
                        PDImageXObject img = PDImageXObject.createFromFile(filePath.toString(), document);
                        PDPageContentStream contentStream = new PDPageContentStream(document, page);
                        contentStream.drawImage(img, 0, 0);
                        contentStream.close();
                    } catch (IOException e) {
                        try {
                            Files.delete(filePath);
                            logger.severe(String.format("Image %s was deleted!", filePath.getFileName()));
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            document.save(pdfFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Pdf completed...");
    }

    private Integer getPagenum(Path pagePath) {
        String name = pagePath.getFileName().toString();
        try {
            return Integer.parseInt(name.split("_")[0]);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }
}
