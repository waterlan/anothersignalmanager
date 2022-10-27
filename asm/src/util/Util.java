package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Util {

    public Util() {
        // TODO Auto-generated constructor stub
    }
    public static void dumpArray(double [] re, double [] im, String fileName) {
        try {
            PrintWriter writer = new PrintWriter(new File(fileName));
            int offset = 0;
            for (int i = 0; i < re.length; i++) {
                writer.format(String.format("%6d) Re: %g  Im: %g\n", i, re[i + offset], im[i + offset]));
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
