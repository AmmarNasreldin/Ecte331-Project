package partA;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class MyMain {
    public static short[][] grayImage;
    public static int widthtemplate;
    public static int heighttemplate;
    public static int width;
    public static int height;
    public static int widthSource;
    public static int heightSource;
    private static BufferedImage image;
    private static BufferedImage image1;

    public static void main(String[] args) throws IOException {
        String SourceName = "TenCardG.jpg";
        String TemplateName = "Template.jpg";
        String ResultName = "ResultImage.jpg";  // New file name for the result image

        File inp = new File(SourceName);
        image = ImageIO.read(inp);
        widthSource = image.getWidth();
        heightSource = image.getHeight();
        short[][] Source_image = readColourImage(SourceName);

        File inp1 = new File(TemplateName);
        image1 = ImageIO.read(inp1);
        widthtemplate = image1.getWidth();
        heighttemplate = image1.getHeight();
        short[][] temp_image = readColourImage(TemplateName);

        int Tempsize = widthtemplate * heighttemplate;
        double Minimum = Double.MAX_VALUE;

        double[][] absDiffMat = new double[heightSource - heighttemplate + 1][widthSource - widthtemplate + 1];
        
        for (int i = 0; i <= heightSource - heighttemplate; i++) {
            for (int j = 0; j <= widthSource - widthtemplate; j++) {
                double SumDiff = 0.0;
                for (int y = 0; y < heighttemplate; y++) {
                    for (int x = 0; x < widthtemplate; x++) {
                        SumDiff += Math.abs(Source_image[i + y][j + x] - temp_image[y][x]);
                    }
                }
                double meanDiff = SumDiff / Tempsize;
                absDiffMat[i][j] = meanDiff;

                if (meanDiff < Minimum) {
                    Minimum = meanDiff;
                }
            }
        }

        int ratio = 10;
        double Threshold = ratio * Minimum;

        // Create a copy of the source image to draw rectangles on
        BufferedImage resultImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2D = resultImage.createGraphics();
        g2D.drawImage(image, 0, 0, null);
        g2D.setColor(Color.RED);

        // Draw rectangles on the source image at coordinates with value less than or equal to the threshold
        for (int i = 0; i < absDiffMat.length; i++) {
            for (int j = 0; j < absDiffMat[0].length; j++) {
                if (absDiffMat[i][j] <= Threshold) {
                    System.out.println("Coordinate: (" + i + ", " + j + ")");
                    // Draw rectangle at each found coordinate
                    g2D.drawRect(j, i, widthtemplate, heighttemplate);
                }
            }
        }

        g2D.dispose();
        ImageIO.write(resultImage, "jpg", new File("Modified_" + ResultName)); // Save to a new file
        System.out.println(">> Completed! Check the rectangles on the generated Modified_" + ResultName + " image under this project folder.");

        // Print the template image in gray scale
        for (int i = 0; i < heighttemplate; i++) {
            for (int j = 0; j < widthtemplate; j++) {
                System.out.print(temp_image[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static short[][] readColourImage(String fileName) {
        try {
            byte[] pixels;

            File inp = new File(fileName);
            BufferedImage img = ImageIO.read(inp);
            width = img.getWidth();
            height = img.getHeight();

            pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
            System.out.println("Dimension of the Template image: W x H = " + width + " x " + height + " | Number of Pixels: " + pixels.length);

            int pr, pg, pb;
            grayImage = new short[height][width];
            int coord;
            for (int i = 0; i < height; i++)
                for (int j = 0; j < width; j++) {
                    coord = 3 * (i * width + j);
                    pr = ((short) pixels[coord] & 0xff);
                    pg = ((short) pixels[coord + 1] & 0xff);
                    pb = ((short) pixels[coord + 2] & 0xff);

                    grayImage[i][j] = (short) Math.round(0.299 * pr + 0.587 * pg + 0.114 * pb);
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return grayImage;
    }

    /**
     * 
     * @param fileName
     * @param xCoord
     * @param yCoord
     * @param rectWidth
     * @param rectHeight
     */
    public static void writeColourImage(String fileName, short xCoord, short yCoord, short rectWidth, short rectHeight) {   
        try {                   
            BufferedImage img = add_Rectangle(image, xCoord, yCoord, rectWidth, rectHeight);
            ImageIO.write(img, "jpg", new File("Modified_" + fileName)); // Save to a new file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param img
     * @param xCoord
     * @param yCoord
     * @param rectWidth
     * @param rectHeight
     * @return
     */
    public static BufferedImage add_Rectangle(BufferedImage img, short xCoord, short yCoord, short rectWidth, short rectHeight) {
        // Create a copy of the original image to draw on
        BufferedImage bi = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2D = bi.createGraphics();
        g2D.drawImage(img, 0, 0, null);
        g2D.setColor(Color.RED);
        g2D.drawRect(xCoord, yCoord, rectWidth, rectHeight);
        g2D.dispose();
        return bi;
    }
}
