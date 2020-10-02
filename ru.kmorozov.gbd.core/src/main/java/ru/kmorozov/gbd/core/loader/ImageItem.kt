package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.constants.GoogleConstants
import ru.kmorozov.gbd.core.logic.model.book.base.IPage
import ru.kmorozov.gbd.utils.Images
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.*
import javax.imageio.metadata.IIOInvalidTreeException
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode

open class ImageItem(outputFile: File) : RawFileItem(outputFile) {

    override fun isImage(): Boolean {
        return true
    }

    override fun init() {
        outputStream = ByteArrayOutputStream()
    }

    override fun flush() {
        val formatName = Images.getImageFormat(outputFile.toPath())

        val iw: Iterator<ImageWriter> = ImageIO.getImageWritersByFormatName(formatName)
        while (iw.hasNext()) {
            val writer: ImageWriter = iw.next()
            val writeParam: ImageWriteParam = writer.getDefaultWriteParam()
            val typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB)
            val metadata: IIOMetadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam)
            if (metadata.isReadOnly || !metadata.isStandardMetadataFormatSupported) {
                continue
            }

            setDPI(metadata)
            val stream = ImageIO.createImageOutputStream(outputFile)
            val data = ByteArrayInputStream((outputStream as ByteArrayOutputStream).toByteArray())
            val image = IIOImage(ImageIO.read(data), null, null)
            writer.setOutput(stream)

            try {
                writer.write(metadata, image, writeParam)
            }
            catch (ex: Exception) {
                writer.write(null, image, writeParam)
            }
            finally {
                stream.close()
                outputStream.close()
            }
            break
        }
    }

    override fun validate(): Boolean {
        return !Images.isInvalidImage(asFile(), if (0 == GBDOptions.imageWidth) GoogleConstants.DEFAULT_PAGE_WIDTH else GBDOptions.imageWidth)
    }

    @Throws(IIOInvalidTreeException::class)
    fun setDPI(metadata: IIOMetadata) { // for PMG, it's dots per millimeter
        val dotsPerMilli = 100.0
        val horiz = IIOMetadataNode("HorizontalPixelSize")
        horiz.setAttribute("value", java.lang.Double.toString(dotsPerMilli))
        val vert = IIOMetadataNode("VerticalPixelSize")
        vert.setAttribute("value", java.lang.Double.toString(dotsPerMilli))
        val dim = IIOMetadataNode("Dimension")
        dim.appendChild(horiz)
        dim.appendChild(vert)
        val root = IIOMetadataNode("javax_imageio_1.0")
        root.appendChild(dim)
        metadata.mergeTree("javax_imageio_1.0", root)
    }
}