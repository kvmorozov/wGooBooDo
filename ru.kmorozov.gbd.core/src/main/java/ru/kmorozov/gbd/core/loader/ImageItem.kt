package ru.kmorozov.gbd.core.loader

import ru.kmorozov.gbd.core.config.GBDOptions
import ru.kmorozov.gbd.core.config.constants.GoogleConstants
import ru.kmorozov.gbd.utils.Images
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
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

    override fun write(inStream: InputStream) {
        val formatName = Images.getImageFormat(outputFile.toPath())

        val iw: Iterator<ImageWriter> = ImageIO.getImageWritersByFormatName(formatName)
        while (iw.hasNext()) {
            val writer: ImageWriter = iw.next()
            val writeParam: ImageWriteParam = writer.defaultWriteParam
            val typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB)
            val metadata: IIOMetadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam)
            if (metadata.isReadOnly || !metadata.isStandardMetadataFormatSupported) {
                continue
            }

            when (formatName) {
                "png" -> setDPIpng(metadata)
                else -> setDPI(metadata)
            }

            val stream = ImageIO.createImageOutputStream(outputFile)
            val image = IIOImage(ImageIO.read(inStream), null, null)
            writer.output = stream

            try {
                writer.write(metadata, image, writeParam)
            } catch (ex: Exception) {
                writer.write(null, image, writeParam)
            } finally {
                stream.close()
                writer.dispose()
            }
            break
        }
    }

    override fun flush() {

    }

    override fun validate(): Boolean {
        return !Images.isInvalidImage(asFile(), if (0 == GBDOptions.imageWidth) GoogleConstants.DEFAULT_PAGE_WIDTH else GBDOptions.imageWidth)
    }

    @Throws(IIOInvalidTreeException::class)
    fun setDPI(metadata: IIOMetadata) {
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

    fun setDPIpng(metadata: IIOMetadata) {
        val pngMetadataFormatName = "javax_imageio_png_1.0"

        val metersToInches = 39.3701
        val dotsPerMeter = Math.round(100 * metersToInches).toInt()

        val pHYs_node = IIOMetadataNode("pHYs")
        pHYs_node.setAttribute("pixelsPerUnitXAxis", Integer.toString(dotsPerMeter))
        pHYs_node.setAttribute("pixelsPerUnitYAxis", Integer.toString(dotsPerMeter))
        pHYs_node.setAttribute("unitSpecifier", "meter")

        val root = IIOMetadataNode(pngMetadataFormatName)
        root.appendChild(pHYs_node)

        metadata.mergeTree(pngMetadataFormatName, root)
    }
}