package ru.kmorozov.gbd.pdf

import org.apache.pdfbox.cos.COSDictionary
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentInformation
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.constants.GBDConstants
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.DEFAULT_PAGE_WIDTH
import ru.kmorozov.gbd.core.config.options.PdfOptions
import ru.kmorozov.gbd.core.loader.LocalFSStorage
import ru.kmorozov.gbd.core.loader.MayBePageItem
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

    private var internalPagesMap: MutableMap<Int, PDPage> = TreeMap<Int, PDPage>()

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

        existPages = uniqueObject.pagesBefore

        val imgWidth = if (0 == GBDOptions.imageWidth) DEFAULT_PAGE_WIDTH else GBDOptions.imageWidth

        (if (existPages == 0L) PDDocument() else PDDocument.load(pdfFile)).use { pdfDocument ->
            existPages = pdfDocument.numberOfPages.toLong()

            val imgCount = (uniqueObject.storage as LocalFSStorage).imgCount()
            if (imgCount <= existPages) {
                logger.finest("No new pages, exiting...")
                bookInfo.lastPdfChecked = System.currentTimeMillis()
                return
            } else
                logger.info(String.format("Rewriting pdf from %d to %d pages", existPages, imgCount))

            val itr = pdfDocument.pages.iterator()
            while (itr.hasNext()) {
                var page = itr.next()
                internalPagesMap.put(page.getCOSObject().getInt(COSName.ORDER), page)
            }

            var documentInfo = PDDocumentInformation();
            documentInfo.title = bookInfo.bookData.title;
            documentInfo.producer = GBDConstants.GBD_APP_NAME;
            pdfDocument.documentInformation = documentInfo;

            (uniqueObject.storage as LocalFSStorage).items.stream()
                    .filter { item -> item is MayBePageItem }
                    .map { item -> item as MayBePageItem }
                    .filter { item -> !internalPagesMap.containsKey(item.page.order) }
                    .sorted(Comparator.comparing<MayBePageItem, Int> { it.pageNum }).forEach { item ->
                        try {
                            FileInputStream(item.asFile()).use { fis ->
                                if (item.page.isScanned || !Images.isInvalidImage(item.asFile(), imgWidth)) {
                                    val bimg = ImageIO.read(fis)

                                    if (null == bimg) {
                                        item.delete()
                                        logger.severe("Image ${item.asFile().toPath().fileName} was deleted!")
                                    } else {
                                        val width = bimg.width.toFloat()
                                        val height = bimg.height.toFloat()

                                        val page = COSDictionary()
                                        page.setItem(COSName.TYPE, COSName.PAGE)
                                        page.setItem(COSName.MEDIA_BOX, PDRectangle(width, height))
                                        page.setString(COSName.ID, item.page.pid)
                                        page.setInt(COSName.ORDER, item.page.order)

                                        val pdfPage = PDPage(page)

                                        if (internalPagesMap.entries.count { entry -> item.page.order > entry.key } > 0)
                                            pdfDocument.pages.insertAfter(pdfPage, internalPagesMap.entries.last { entry -> item.page.order > entry.key }.value)
                                        else {
                                            val opNearestBefore = internalPagesMap.entries.stream()
                                                    .filter { entry -> item.page.order < entry.key }.findFirst()
                                            if (opNearestBefore.isPresent)
                                                pdfDocument.pages.insertBefore(pdfPage, opNearestBefore.get().value)
                                            else
                                                pdfDocument.addPage(pdfPage)
                                        }

                                        internalPagesMap.put(item.page.order, pdfPage)

                                        val img = PDImageXObject.createFromFile(item.asFile().toPath().toString(), pdfDocument)
                                        PDPageContentStream(pdfDocument, pdfPage).use { contentStream -> contentStream.drawImage(img, 0.toFloat(), 0.toFloat()) }
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

            pdfDocument.save(pdfFile)
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
