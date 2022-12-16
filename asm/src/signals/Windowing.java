package signals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import console.CommandLineParser;

public class Windowing extends SourcesBase {

    public static final Map<String, String[]> windows = new HashMap<String, String[]>() {
        /**
         * 
         */
        private static final long serialVersionUID = -7725808670019627458L;

        {
            put("wblackman", new String[] { "", "<name> <elements> <samplerate>" });
            put("wblock", new String[] { "", "<name> <elements> <samplerate>" });
            put("wgauss", new String[] { "", "<name> <elements> <samplerate>" });
            put("whanning", new String[] { "", "<name> <elements> <samplerate>" });
            put("whamming", new String[] { "", "<name> <elements> <samplerate>" });
            put("wkaiser", new String[] { "", "<name> <elements> <samplerate>" });
            put("wtriangle", new String[] { "", "<name> <elements> <samplerate>" });
        }
    };

    public Windowing(Map<String, Signal> signals, CommandLineParser cp) {
        super(signals, cp);
    }

    public static void windowing(double[] re, double[] im, double[] re_out, double[] im_out, int offset, int length,
            int windowing) {
        switch (windowing) {
        case 0:
            wblock(re, im, re_out, im_out, offset, length);
            break;
        case 1:
            whanning(re, im, re_out, im_out, offset, length);
            break;
        case 2:
            whamming(re, im, re_out, im_out, offset, length);
            break;
        case 3:
            wgauss(re, im, re_out, im_out, offset, length);
            break;
        case 4:
            wblackman(re, im, re_out, im_out, offset, length);
            break;
        case 5:
            wkaiser(re, im, re_out, im_out, offset, length);
            break;
        case 6:
            wtriangle(re, im, re_out, im_out, offset, length);
            break;
        }
    }

    public static void wblackman(double[] re, double[] im, double[] re_out, double[] im_out, int offset, int length) {
        for (int i = 0; i < length; i++) {
            double blackman = (0.42 - 0.50 * Math.cos((2 * Math.PI * i) / (length - 1.0))
                    + 0.08 * Math.cos((4.0 * Math.PI * i) / (length - 1)));
            int index = i + offset;
            re_out[index] = re[index] * blackman;
            im_out[index] = im[index] * blackman;
        }
    }

    public void wblackman(Signal s) {
        double[] data_real = s.getRealData();
        double[] data_imag = s.getImagData();
        int length = s.getDataLength();

        for (int i = 0; i < length; i++) {
            data_real[i] = data_imag[i] = 1.0;
        }
        wblackman(data_real, data_imag, data_real, data_imag, 0, length);
    }

    public static void wblock(double[] re, double[] im, double[] re_out, double[] im_out, int offset, int length) {
        for (int i = offset; i < length + offset; i++) {
            re_out[i] = re[i];
            im_out[i] = im[i];
        }
    }

    public void wblock(Signal s) {
        double[] data_real = s.getRealData();
        double[] data_imag = s.getImagData();
        int length = s.getDataLength();

        for (int i = 0; i < length; i++) {
            data_real[i] = data_imag[i] = 1.0;
        }
    }

    public static void wgauss(double[] re, double[] im, double[] re_out, double[] im_out, int offset, int length) {
        for (int i = 0; i < length; i++) {
            double gaus = Math.exp(-0.5 * Math.pow((3.0 * (i - (length - 1.0) / 2.0) * 2.0 / (length - 1.0)), 2.0));
            int index = i + offset;
            re_out[index] = re[index] * gaus;
            im_out[index] = im[index] * gaus;
        }
    }

    public void wgauss(Signal s) {
        double[] data_real = s.getRealData();
        double[] data_imag = s.getImagData();
        int length = s.getDataLength();

        for (int i = 0; i < length; i++) {
            data_real[i] = data_imag[i] = 1.0;
        }
        wgauss(data_real, data_imag, data_real, data_imag, 0, length);
    }

    public static void whanning(double[] re, double[] im, double[] re_out, double[] im_out, int offset, int length) {
        for (int i = 0; i < length; i++) {
            double hanning = (1.0 - Math.cos((2.0 * Math.PI * i) / (length - 1.0))) / 2.0;
            int index = i + offset;
            re_out[index] = re[index] * hanning;
            im_out[index] = im[index] * hanning;
        }
    }

    public void whanning(Signal s) {
        double[] data_real = s.getRealData();
        double[] data_imag = s.getImagData();
        int length = s.getDataLength();

        for (int i = 0; i < length; i++) {
            data_real[i] = data_imag[i] = 1.0;
        }
        whanning(data_real, data_imag, data_real, data_imag, 0, length);
    }

    public static void whamming(double[] re, double[] im, double[] re_out, double[] im_out, int offset, int length) {
        for (int i = 0; i < length; i++) {
            double hamming = 0.538 - 0.462 * Math.cos(2.0 * Math.PI * i / (length - 1.0));
            int index = i + offset;
            re_out[index] = re[index] * hamming;
            im_out[index] = im[index] * hamming;
        }
    }

    public void whamming(Signal s) {
        double[] data_real = s.getRealData();
        double[] data_imag = s.getImagData();
        int length = s.getDataLength();

        for (int i = 0; i < length; i++) {
            data_real[i] = data_imag[i] = 1.0;
        }
        whamming(data_real, data_imag, data_real, data_imag, 0, length);
    }

    public static void wkaiser(double[] re, double[] im, double[] re_out, double[] im_out, int offset, int length) {
        for (int i = 0; i < length; i++) {
            double kaiser = (1 - 1.24 * Math.cos(2 * Math.PI * i / (length - 1))
                    + 0.244 * Math.cos(4 * Math.PI * i / (length - 1))
                    - 0.00305 * Math.cos(6 * Math.PI * i / (length - 1))) / 2.48;
            int index = i + offset;
            re_out[index] = re[index] * kaiser;
            im_out[index] = im[index] * kaiser;
        }
    }

    public void wkaiser(Signal s) {
        double[] data_real = s.getRealData();
        double[] data_imag = s.getImagData();
        int length = s.getDataLength();

        for (int i = 0; i < length; i++) {
            data_real[i] = data_imag[i] = 1.0;
        }
        wkaiser(data_real, data_imag, data_real, data_imag, 0, length);
    }

    public static void wtriangle(double[] re, double[] im, double[] re_out, double[] im_out, int offset, int length) {
        for (int i = 0; i < length; i++) {
            double triangle = 1 - Math.abs(i - ((length - 1) / 2.0)) / ((length - 1) / 2.0);
            int index = i + offset;
            re_out[index] = re[index] * triangle;
            im_out[index] = im[index] * triangle;
        }
    }

    public void wtriangle(Signal s) {
        double[] data_real = s.getRealData();
        double[] data_imag = s.getImagData();
        int length = s.getDataLength();

        for (int i = 0; i < length; i++) {
            data_real[i] = data_imag[i] = 1.0;
        }
        wtriangle(data_real, data_imag, data_real, data_imag, 0, length);
    }

    public Signal WindowCi(List<String> arguments, String window) {

        String signalname = cp.getString(arguments, "Signal", window.substring(1));
        int n = cp.getInt(arguments, "Number of elements (2^n)", Signal.MIN_N, Signal.MAX_N, DEF_N);
        int sample = cp.getInt(arguments, "Samplerate in 10Hz", 1, Short.MAX_VALUE, Signal.SAMPLE_RATE);

        int number = (int) Math.pow(2, n);
        Signal im = signals.get(signalname); /* Find the correct signal */

        if (im == null) /* Signal did not exist yet */
        {
            im = new Signal(signalname);
            signals.put(signalname, im);
        }

        setSignalValues(im, number, sample, Signal.COMP, window);
        im.setMode(Signal.REAL_M);
        ComplexArray data = new ComplexArray(number);
        im.setData(data);

        Method method = null;
        String methodName = window;
        try {
            method = this.getClass().getMethod(methodName, Signal.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (method == null)
            return null;

        try {
            method.invoke(this, im);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return im;
    }
}