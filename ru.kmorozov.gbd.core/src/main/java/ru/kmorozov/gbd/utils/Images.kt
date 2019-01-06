package ru.kmorozov.gbd.utils

import com.google.common.io.MoreFiles
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

    fun isImageFile(filePath: Path): Boolean {
        if (!Files.isRegularFile(filePath)) return false

        val ext = MoreFiles.getFileExtension(filePath).toLowerCase()

        when (ext) {
            "png", "jpg", "jpeg" -> return true
            "pdf" -> return false
            else -> {
                logger.severe(String.format("Unknown img format for %s", filePath))
                return false
            }
        }
    }

    fun isInvalidImage(filePath: Path, imgWidth: Int): Boolean {
        return isInvalidImage(filePath.toFile(), imgWidth)
    }

    fun isInvalidImage(imgfile: File, imgWidth: Int): Boolean {
        val fileSize = imgfile.length()

        when (imgWidth) {
            1280 -> return if (96183L <= fileSize && 97200L > fileSize) {
                try {
                    val bimg = ImageIO.read(imgfile)
                    1670 == bimg.height
                } catch (e: IOException) {
                    true
                }

            } else
                false
            else -> return false
        }
    }

    fun isPdfFile(filePath: Path): Boolean {
        if (!Files.isRegularFile(filePath)) return false

        val ext = MoreFiles.getFileExtension(filePath).toLowerCase()

        return "pdf" == ext
    }

    fun getImageFormat(response: Response): String {
        return response.imageFormat
    }
}
