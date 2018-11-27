package ru.kmorozov.gbd.test.core;

import org.junit.Assert;
import org.junit.Test;
import ru.kmorozov.gbd.test.GbdTestBase;
import ru.kmorozov.gbd.utils.Images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static ru.kmorozov.gbd.core.config.constants.GoogleConstants.DEFAULT_PAGE_WIDTH;

/**
 * Created by km on 22.01.2017.
 */
public class GoogleImageTest extends GbdTestBase {

    private static final String baseDirName = "J:\\gbdBooks\\The Early Christian Book (CUA Studies in Early Christianity) 3vLeyIIjwGQC\\";
    private static final String goodImg1 = GoogleImageTest.baseDirName + "108_PT108.png";
    private static final String goodImg2 = GoogleImageTest.baseDirName + "99_PT99.png";
    private static final String badImg1 = GoogleImageTest.baseDirName + "111_PT111.png";
    private static final String emptyImg = GoogleImageTest.baseDirName + "110_PT110.png";

    private static final String complexGoodCase = "J:\\gbdBooks\\Castles of Northwest Greece Xc5HAQAAQBAJ\\151_PA132.png";

    @Test
    public void testImageSimple() {
        Assert.assertFalse(Images.isInvalidImage(Paths.get(GoogleImageTest.goodImg1), DEFAULT_PAGE_WIDTH));
        Assert.assertFalse(Images.isInvalidImage(Paths.get(GoogleImageTest.goodImg2), DEFAULT_PAGE_WIDTH));
        Assert.assertFalse(Images.isInvalidImage(Paths.get(GoogleImageTest.emptyImg), DEFAULT_PAGE_WIDTH));
        Assert.assertTrue(Images.isInvalidImage(Paths.get(GoogleImageTest.badImg1), DEFAULT_PAGE_WIDTH));
    }

    @Test
    public void testImageComplex() throws IOException {
        BufferedImage bimgComplexGood = ImageIO.read(new File(GoogleImageTest.complexGoodCase));
        BufferedImage bimgEmpty = ImageIO.read(new File(GoogleImageTest.badImg1));

        for (int width = 0; width < bimgComplexGood.getWidth(); width++)
            for (int height = 0; height < bimgComplexGood.getHeight(); height++) {
                try {
                    int rgb1 = bimgComplexGood.getRGB(width, height);
                    int rgb2 = bimgEmpty.getRGB(width, height);
                    if (rgb1 != rgb2) {
                        System.out.println(String.format("x = %d, y=%d, rgb1=%d, rgb2=%d", width, height, rgb1, rgb2));
                        break;
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    System.out.println(String.format("x = %d, y=%d", width, height));
                    break;
                }


            }

        Assert.assertFalse(Images.isInvalidImage(Paths.get(GoogleImageTest.complexGoodCase), DEFAULT_PAGE_WIDTH));
    }
}
