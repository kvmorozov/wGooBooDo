package ru.kmorozov.gbd.pdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentInformation
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.IStoredItem
import ru.kmorozov.gbd.core.config.constants.GBDConstants
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.DEFAULT_PAGE_WIDTH
import ru.kmorozov.gbd.core.config.options.PdfOptions
import ru.kmorozov.gbd.core.loader.LocalFSStorage
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor
import ru.kmorozov.gbd.utils.Images
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.FileSystemException
import java.util.*
import javax.imageio.ImageIO

/**
 * Created by km on 17.12.2015.
 */
class PdfMaker : IPostProcessor {

    override lateinit var uniqueObject: BookContext

    constructor(uniqueObject: BookContext) {
        this.uniqueObject = uniqueObject
    }

    constructor()

    override fun make() {
        if (PdfOptions.SKIP === GBDOptions.pdfOptions)
            return

        if (!uniqueObject.pdfCompleted.compareAndSet(false, true)) return

        val logger = ExecutionContext.INSTANCE.getLogger(PdfMaker::class.java, uniqueObject)
        logger.info("Starting making pdf file...")

        var existPages = 0L
        val bookInfo = uniqueObject.bookInfo

        val pdfFile = (uniqueObject.storage as LocalFSStorage).getOrCreatePdf(bookInfo.bookData.title)

        try {
            if (pdfFile.lastModified() < bookInfo.lastPdfChecked)
                existPages = uniqueObject.pagesBefore
            else
                try {
                    PDDocument.load(pdfFile).use { existDocument -> existPages = existDocument.numberOfPages.toLong() }
                } catch (ex: Exception) {
                    pdfFile.createNewFile()
                }
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }

        try {
            val imgCount = (uniqueObject.storage as LocalFSStorage).imgCount()
            if (imgCount <= existPages) {
                logger.finest("No new pages, exiting...")
                bookInfo.lastPdfChecked = System.currentTimeMillis()
                return
            } else
                logger.info(String.format("Rewriting pdf from %d to %d pages", existPages, imgCount))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val imgWidth = if (0 == GBDOptions.imageWidth) DEFAULT_PAGE_WIDTH else GBDOptions.imageWidth

        try {
            PDDocument().use { document ->
                var documentInfo = PDDocumentInformation();
                documentInfo.title = bookInfo.bookData.title;
                documentInfo.producer = GBDConstants.GBD_APP_NAME;
                document.documentInformation = documentInfo;

                (uniqueObject.storage as LocalFSStorage).items.sorted(Comparator.comparing<IStoredItem, Int> { it.pageNum }).forEach { item ->
                    try {
                        FileInputStream(item.asFile()).use { fis ->
                            if (!Images.isInvalidImage(item.asFile(), imgWidth)) {
                                val bimg = ImageIO.read(fis)

                                if (null == bimg) {
                                    item.delete()
                                    logger.severe("Image ${item.asFile().toPath().fileName} was deleted!")
                                } else {
                                    val width = bimg.width.toFloat()
                                    val height = bimg.height.toFloat()
                                    val page = PDPage(PDRectangle(width, height))

                                    document.addPage(page)
                                    val img = PDImageXObject.createFromFile(item.asFile().toPath().toString(), document)
                                    PDPageContentStream(document, page).use { contentStream -> contentStream.drawImage(img, 0.toFloat(), 0.toFloat()) }
                                }
                            } else {
                                item.delete()
                                logger.severe("Image ${item.asFile().toPath().fileName} was deleted!")
                            }
                        }
                    } catch (fse: FileSystemException) {
                        fse.printStackTrace()
                    } catch (e: IOException) {
                        try {
                            item.delete()
                            logger.severe("Image ${item.asFile().toPath().fileName} was deleted!")
                        } catch (ioe: IOException) {
                            ioe.printStackTrace()
                        }

                    }
                }

                document.save(pdfFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        bookInfo.lastPdfChecked = System.currentTimeMillis()
        logger.info("Pdf completed.")
    }

    override fun run() {
        make()
    }

    override fun toString(): String {
        return "Pdf maker:$uniqueObject"
    }
}
