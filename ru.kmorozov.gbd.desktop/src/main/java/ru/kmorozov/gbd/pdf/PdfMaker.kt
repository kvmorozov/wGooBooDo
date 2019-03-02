package ru.kmorozov.gbd.pdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.DEFAULT_PAGE_WIDTH
import ru.kmorozov.gbd.core.config.options.PdfOptions
import ru.kmorozov.gbd.core.loader.LocalFSStorage
import ru.kmorozov.gbd.core.logic.context.BookContext
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import ru.kmorozov.gbd.core.logic.extractors.base.IPostProcessor
import ru.kmorozov.gbd.utils.Images
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors
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

        val logger = ExecutionContext.INSTANCE.getLogger(PdfMaker::class.java, uniqueObject)
        logger.info("Starting making pdf file...")

        if (!uniqueObject.pdfCompleted.compareAndSet(false, true)) return

        val imgDir = (uniqueObject.storage as LocalFSStorage).storageDir
        var existPages = 0L
        val bookInfo = uniqueObject.bookInfo

        var pdfFile: File? = null
        try {
            val pdfFiles = Files.list(imgDir.toPath()).filter(Predicate<Path> { filePath -> Images.isPdfFile(filePath) }).collect(Collectors.toList())
            if (null != pdfFiles && 1 == pdfFiles.size) pdfFile = pdfFiles[0].toFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (null == pdfFile)
            pdfFile = File(imgDir.path + File.separator + bookInfo.bookData.title.replace("[^А-Яа-яa-zA-Z0-9-]".toRegex(), " ") + ".pdf")
        try {
            if (Files.exists(pdfFile.toPath())) {
                if (pdfFile.lastModified() < bookInfo.lastPdfChecked)
                    existPages = uniqueObject.pagesBefore
                else
                    try {
                        PDDocument.load(pdfFile).use { existDocument -> existPages = existDocument.numberOfPages.toLong() }
                    } catch (ex: Exception) {
                        pdfFile.createNewFile()
                    }

            } else
                pdfFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }

        try {
            val imgCount = Files.list(imgDir.toPath()).filter(Predicate<Path> { filePath -> Images.isImageFile(filePath) }).count()
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
                Files.list(imgDir.toPath()).filter({ Images.isImageFile(it) }).sorted(Comparator.comparing<Path, Int> { getPagenum(it) }).forEach { filePath ->
                    try {
                        FileInputStream(filePath.toFile()).use { `in` ->
                            if (!Images.isInvalidImage(filePath, imgWidth)) {
                                val bimg = ImageIO.read(`in`)

                                if (null == bimg) {
                                    Files.delete(filePath)
                                    logger.severe("Image ${filePath.fileName} was deleted!")
                                } else {
                                    val width = bimg.width.toFloat()
                                    val height = bimg.height.toFloat()
                                    val page = PDPage(PDRectangle(width, height))
                                    document.addPage(page)
                                    val img = PDImageXObject.createFromFile(filePath.toString(), document)
                                    PDPageContentStream(document, page).use { contentStream -> contentStream.drawImage(img, 0.toFloat(), 0.toFloat()) }
                                }
                            } else {
                                Files.delete(filePath)
                                logger.severe("Image ${filePath.fileName} was deleted!")
                            }
                        }
                    } catch (fse: FileSystemException) {
                        fse.printStackTrace()
                    } catch (e: IOException) {
                        try {
                            Files.delete(filePath)
                            logger.severe("Image ${filePath.fileName} was deleted!")
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

    private fun getPagenum(pagePath: Path): Int? {
        val name = pagePath.fileName.toString()
        try {
            return Integer.parseInt(name.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])
        } catch (nfe: NumberFormatException) {
            return -1
        }

    }

    override fun run() {
        make()
    }

    override fun toString(): String {
        return "Pdf maker:$uniqueObject"
    }
}
