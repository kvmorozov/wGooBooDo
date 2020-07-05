package ru.kmorozov.gbd.utils

import com.google.common.io.MoreFiles
import net.sourceforge.tess4j.Tesseract
import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.logic.connectors.Response
import ru.kmorozov.gbd.core.logic.context.ExecutionContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

/**
 * Created by km on 27.12.2015.
 */
object Images {

    private val logger = ExecutionContext.getLogger(Images::class.java)

    private lateinit var ocr: Tesseract
    private var ocrInitialized = false

    init {
        try {
            ocr = Tesseract() // create a new OCR engine
            ocr.setDatapath("J:\\OCR\\tesseract\\tessdata")
            ocrInitialized = true
        } catch (ex: Exception) {

        }
    }

    fun isImageFile(filePath: Path): Boolean {
        if (!Files.isRegularFile(filePath)) return false

        val ext = getImageFormat(filePath)

        when (ext) {
            "png", "jpg", "jpeg" -> return true
            "pdf" -> return false
            else -> {
                logger.severe("Unknown img format for $filePath")
                return false
            }
        }
    }

    fun getImageFormat(filePath: Path): String {
        return MoreFiles.getFileExtension(filePath).toLowerCase()
    }

    fun isInvalidImage(filePath: Path, imgWidth: Int): Boolean {
        return isInvalidImage(filePath.toFile(), imgWidth)
    }

    fun isInvalidImage(imgfile: File, imgWidth: Int): Boolean {
        val fileSize = imgfile.length()

        when (imgWidth) {
            1280 -> return if (fileSize > 96183L || fileSize < 70000L) {
                try {
                    val bimg = ImageIO.read(imgfile)
                    1670 == bimg.height
                } catch (e: IOException) {
                    true
                }
            } else {
                val s = doOCR(imgfile)
                return (s.length < 50 && (s.contains("image")
                        || s.contains("not") || s.contains("available") || s.lines().size == 4))
            }
            else -> return false
        }
    }

    @Synchronized
    public fun doOCR(imgfile: File): String {
        if (!GBDOptions.scanEnabled || !ocrInitialized)
            return ""

        try {
            return ocr.doOCR(imgfile);
        } catch (ex: Exception) {
            logger.error(ex.localizedMessage)
            return ""
        }
    }

    fun isPdfFile(filePath: Path): Boolean {
        if (!Files.isRegularFile(filePath)) return false

        val ext = MoreFiles.getFileExtension(filePath).toLowerCase()

        return "pdf" == ext && filePath.toFile().length() > 0
    }

    fun getImageFormat(response: Response): String {
        return response.imageFormat
    }
}
