package partA;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
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

    public static void main(String[] args) throws IOException, InterruptedException {
        String SourceName = "TenCardG.jpg";
        String TemplateName = "Template.jpg";
        String ResultName = "ResultImage.jpg";  // New file name for the result image

        int numOfThreads = 4; // Example number of threads for multi-threading
        int numIterations = 3; // Number of iterations for averaging the execution times

        // Single-threaded execution
        long singleThreadedTotalTime = 0;
        for (int i = 0; i < numIterations; i++) {
            long startTime = System.currentTimeMillis();
            runSingleThreaded(SourceName, TemplateName, ResultName);
            long endTime = System.currentTimeMillis();
            singleThreadedTotalTime += (endTime - startTime);
        }
        long singleThreadedAverageTime = singleThreadedTotalTime / numIterations;
        System.out.println("Average single-threaded execution time: " + singleThreadedAverageTime + "ms");

        // Multi-threaded execution
        long multiThreadedTotalTime = 0;
        for (int i = 0; i < numIterations; i++) {
            long startTime = System.currentTimeMillis();
            runMultiThreaded(SourceName, TemplateName, ResultName, numOfThreads);
            long endTime = System.currentTimeMillis();
            multiThreadedTotalTime += (endTime - startTime);
        }
        long multiThreadedAverageTime = multiThreadedTotalTime / numIterations;
        System.out.println("Average multi-threaded execution time with " + numOfThreads + " threads: " + multiThreadedAverageTime + "ms");
    }

    public static void runSingleThreaded(String SourceName, String TemplateName, String ResultName) throws IOException {
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
        ImageIO.write(resultImage, "jpg", new File("SingleThreaded_" + ResultName)); // Save to a new file
        System.out.println(">> Single-threaded completed! Check the rectangles on the generated SingleThreaded_" + ResultName + " image under this project folder.");
    }

    public static void runMultiThreaded(String SourceName, String TemplateName, String ResultName, int numOfThreads) throws IOException, InterruptedException {
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
        AtomicReference<Double> Minimum = new AtomicReference<>(Double.MAX_VALUE);

        double[][] absDiffMat = new double[heightSource - heighttemplate + 1][widthSource - widthtemplate + 1];

        Thread[] threads = new Thread[numOfThreads];
        int chunkHeight = (heightSource - heighttemplate + 1) / numOfThreads;

        for (int t = 0; t < numOfThreads; t++) {
            final int threadIndex = t;
            threads[t] = new Thread(() -> {
                int startRow = threadIndex * chunkHeight;
                int endRow = (threadIndex == numOfThreads - 1) ? (heightSource - heighttemplate + 1) : startRow + chunkHeight;

                for (int i = startRow; i < endRow; i++) {
                    for (int j = 0; j <= widthSource - widthtemplate; j++) {
                        double SumDiff = 0.0;
                        for (int y = 0; y < heighttemplate; y++) {
                            for (int x = 0; x < widthtemplate; x++) {
                                SumDiff += Math.abs(Source_image[i + y][j + x] - temp_image[y][x]);
                            }
                        }
                        double meanDiff = SumDiff / Tempsize;
                        absDiffMat[i][j] = meanDiff;

                        Minimum.updateAndGet(min -> Math.min(min, meanDiff));
                    }
                }
            });
            threads[t].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        int ratio = 10;
        double Threshold = ratio * Minimum.get();

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
        ImageIO.write(resultImage, "jpg", new File("MultiThreaded_" + ResultName)); // Save to a new file
        System.out.println(">> Multi-threaded completed! Check the rectangles on the generated MultiThreaded_" + ResultName + " image under this project folder.");
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
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    coord = 3 * (i * width + j);
                    pr = ((short) pixels[coord] & 0xff);
                    pg = ((short) pixels[coord + 1] & 0xff);
                    pb = ((short) pixels[coord + 2] & 0xff);

                    grayImage[i][j] = (short) Math.round(0.299 * pr + 0.587 * pg + 0.114 * pb);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return grayImage;
    }

    public static BufferedImage add_Rectangle(Image img, short xCoord, short yCoord, short rectWidth, short rectHeight) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);

        Graphics2D g2D = bi.createGraphics();
        g2D.drawImage(img, 0, 0, null);
        g2D.setColor(Color.RED);
        g2D.drawRect(xCoord, yCoord, rectWidth, rectHeight);
        g2D.dispose();
        return bi;
    }
}
