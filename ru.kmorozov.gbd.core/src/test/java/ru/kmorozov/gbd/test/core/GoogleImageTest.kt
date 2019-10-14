package ru.kmorozov.gbd.test.core

import org.junit.Assert
import org.junit.Test
import ru.kmorozov.gbd.core.config.constants.GoogleConstants.DEFAULT_PAGE_WIDTH
import ru.kmorozov.gbd.test.GbdTestBase
import ru.kmorozov.gbd.utils.Images
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import javax.imageio.ImageIO


/**
 * Created by km on 22.01.2017.
 */
class GoogleImageTest : GbdTestBase() {

    @Test
    fun testImageSimple() {
        Assert.assertFalse(Images.isInvalidImage(Paths.get(goodImg1), DEFAULT_PAGE_WIDTH))
        Assert.assertFalse(Images.isInvalidImage(Paths.get(goodImg2), DEFAULT_PAGE_WIDTH))
        Assert.assertFalse(Images.isInvalidImage(Paths.get(emptyImg), DEFAULT_PAGE_WIDTH))
        Assert.assertTrue(Images.isInvalidImage(Paths.get(badImg1), DEFAULT_PAGE_WIDTH))
    }

    @Test
    @Throws(IOException::class)
    fun testImageComplex() {
        val bimgComplexGood = ImageIO.read(File(complexGoodCase))
        val bimgEmpty = ImageIO.read(File(badImg1))

        for (width in 0 until bimgComplexGood.width)
            for (height in 0 until bimgComplexGood.height) {
                try {
                    val rgb1 = bimgComplexGood.getRGB(width, height)
                    val rgb2 = bimgEmpty.getRGB(width, height)
                    if (rgb1 != rgb2) {
                        println(String.format("x = %d, y=%d, rgb1=%d, rgb2=%d", width, height, rgb1, rgb2))
                        break
                    }
                } catch (ex: ArrayIndexOutOfBoundsException) {
                    println(String.format("x = %d, y=%d", width, height))
                    break
                }


            }

        Assert.assertFalse(Images.isInvalidImage(Paths.get(complexGoodCase), DEFAULT_PAGE_WIDTH))
    }

    @Test
    fun testOcr() {
        val s: String = Images.doOCR(File(invalidImg))
        Assert.assertTrue(s.contains("available"))
    }

    companion object {

        private const val baseDirName = "J:\\gbdBooks\\The Early Christian Book (CUA Studies in Early Christianity) 3vLeyIIjwGQC\\"
        private val goodImg1 = baseDirName + "108_PT108.png"
        private val goodImg2 = baseDirName + "99_PT99.png"
        private val badImg1 = baseDirName + "111_PT111.png"
        private val emptyImg = baseDirName + "110_PT110.png"

        private const val complexGoodCase = "J:\\gbdBooks\\Castles of Northwest Greece Xc5HAQAAQBAJ\\151_PA132.png"

        private const val invalidImg = "J:\\OneDrive\\6_PA6.png"
    }
}
