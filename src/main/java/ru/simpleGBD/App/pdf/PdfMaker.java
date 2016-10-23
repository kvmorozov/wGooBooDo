package ru.simpleGBD.App.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import ru.simpleGBD.App.Logic.ExecutionContext;
import ru.simpleGBD.App.Logic.model.book.BookInfo;
import ru.simpleGBD.App.Utils.Images;
import ru.simpleGBD.App.Utils.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by km on 17.12.2015.
 */
public class PdfMaker {

    private static final Logger logger = Logger.getLogger(ExecutionContext.output, PdfMaker.class.getName());

    private File imgDir;
    private BookInfo bookInfo;

    public PdfMaker(File imgDir, BookInfo bookInfo) {
        this.imgDir = imgDir;
        this.bookInfo = bookInfo;
    }

    public void make() {
        logger.info("Starting making pdf file...");
        try (PDDocument document = new PDDocument()) {
            try {
                Files.list(imgDir.toPath()).sorted((o1, o2) -> getPagenum(o1).compareTo(getPagenum(o2))).forEach(filePath -> {
                    if (Images.isImageFile(filePath)) {
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
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            File pdfFile = new File(imgDir.getPath() + File.separator + bookInfo.getBookData().getTitle() + ".pdf");
            pdfFile.createNewFile();
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
