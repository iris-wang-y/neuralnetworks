import java.io.*;

public class PelArrayDriver {

    public static void main(String[] args) {

        if (args.length != 7) {

            System.out.println(
                "Usage: java PelArrayDriver input.bin width height cropWidth cropHeight finalWidth finalHeight"
            );

            return;
        }

        String filename = args[0];

        // clean names
        String binName = new File(filename).getName();
        String bmpName = binName.replace(".bin", ".bmp");

        try {

            // -------- STEP 1: READ IMAGE --------

            int width = Integer.parseInt(args[1]);
            int height = Integer.parseInt(args[2]);

            int cropWidth = Integer.parseInt(args[3]);
            int cropHeight = Integer.parseInt(args[4]);

            int finalWidth = Integer.parseInt(args[5]);
            int finalHeight = Integer.parseInt(args[6]);

            FileInputStream fis = new FileInputStream(filename);

            int[][] data = new int[height][width];

            for (int i = 0; i < height; i++) {

                for (int j = 0; j < width; j++) {

                    int val = fis.read();

                    if (val == -1) {

                        System.out.println("File ended early!");
                        fis.close();
                        return;
                    }

                    data[i][j] = (val << 16) | (val << 8) | val;
                }
            }

            fis.close();

            // -------- STEP 2: CREATE PELARRAY --------

            PelArray img = new PelArray(data);

            // invert because background is white
            img = img.onesComplimentImage();

            // -------- STEP 3: CENTER OF MASS --------

            img.calcCOM();

            int xcom = img.getXcom();
            int ycom = img.getYcom();

            System.out.println("Center of Mass:");
            System.out.println("X: " + xcom);
            System.out.println("Y: " + ycom);

            // -------- STEP 4: CROP --------

            int x1 = xcom - cropWidth / 2;
            int y1 = ycom - cropHeight / 2;

            int x2 = x1 + cropWidth - 1;
            int y2 = y1 + cropHeight - 1;

            // clamp bounds

            if (x1 < 0) {
                x1 = 0;
            }

            if (y1 < 0) {
                y1 = 0;
            }

            if (x2 >= width) {
                x2 = width - 1;
            }

            if (y2 >= height) {
                y2 = height - 1;
            }

            PelArray cropped = img.crop(x1, y1, x2, y2);

            // -------- STEP 5: SCALE --------

            PelArray finalImg = cropped.scale(finalWidth, finalHeight);

            // -------- STEP 6: SAVE BIN --------

            String binFolder = "cropped com bin";
            String bmpFolder = "cropped com";

            String outBin = binFolder + "/cropped_" + binName;
            String outBmp = bmpFolder + "/cropped_" + bmpName;

            int[][] outData = finalImg.getPelArray();

            FileOutputStream fos = new FileOutputStream(outBin);

            for (int i = 0; i < finalHeight; i++) {

                for (int j = 0; j < finalWidth; j++) {

                    int val = outData[i][j] & 0xFF;
                    fos.write(val);
                }
            }

            fos.close();

            System.out.println("Saved binary file: " + outBin);

            // -------- STEP 7: BIN -> BMP --------

            Process p = new ProcessBuilder(
                "java",
                "BGR2BMP",
                "gray",
                String.valueOf(finalWidth),
                String.valueOf(finalHeight),
                outBin,
                outBmp
            ).inheritIO().start();

            try {

                p.waitFor();

            } catch (InterruptedException e) {

                System.out.println("Process interrupted.");
            }

            System.out.println("Saved preview BMP: " + outBmp);

        } catch (IOException e) {

            System.out.println("Error: " + e.getMessage());
        }
    }
}