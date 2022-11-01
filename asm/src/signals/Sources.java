package signals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import console.CommandLineParser;

public class Sources extends SourcesBase {
    public static final double DEF_FREQ = 100.0; /* Default frequency */
    public static final double DEF_AMPL = 100.0; /* Default amplitude */
    private static final int RAND_MAX = 0x7fff;
    public static final Map<String, String[]> functions = new HashMap<String, String[]>() {
        {
            put("fconstant", new String[] { "FconstCi", "" });
            put("fcosine", new String[] { "FcosineCi", "" });
            put("fdelta", new String[] { "FdeltaCi", "" });
            put("fexp", new String[] { "FexpCi", "" });
            put("fnoise", new String[] { "FnoiseCi", "" });
            put("framp", new String[] { "FrampCi", "" });
            put("fsinc", new String[] { "FsinecCi", "" });
            put("fsine", new String[] { "FsineCi",
                    "fsine <name> <offset> <amplitude> <freq> <phase> <type> <elements> <samplerate>" });
            put("fsquare", new String[] { "FsquarewaveCi", "" });
            put("fstep", new String[] { "FstepCi", "" });
            put("ftriangle", new String[] { "FtriangleCi", "" });
        }
    };

    public Sources(Map<String, Signal> signals, CommandLineParser cp) {
        super(signals, cp);
    }

    private void source(Signal s, List<Double> parameters) {

        int number = s.getDataLength();
        short dtype = s.getDataType();
        // Sources are always 1 channel, 1 record.
        double[] real_data = new double[number];
        double[] imag_data = new double[number];
        s.setRealData(real_data);
        s.setImagData(imag_data);

        Method method = null;
        String methodName = s.getDataDescription();
        try {
            method = this.getClass().getMethod(methodName, Signal.class, List.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (method == null)
            return;

        try {
            method.invoke(this, s, parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        switch (dtype) {
        case Signal.REAL: /* Real data */
            for (int i = 0; i < number; i++) {
                imag_data[i] = 0;
            }
            s.setMode(Signal.REAL_M);
            break;
        case Signal.IMAG: /* Imaginary data */
            for (int i = 0; i < number; i++) {
                real_data[i] = 0;
            }
            s.setMode(Signal.IMAG_M);
            break;
        case Signal.COMP: /* Complex data */
            for (int i = 0; i < number; i++) {
                imag_data[i] = real_data[i];
            }
            s.setMode(Signal.REAL_M);
            break;
        default:
            s.setMode(Signal.REAL_M);
            break;
        }
    }

    public static void constant(double[] data, double ampl) {
        for (int i = 0; i < data.length; i++) {
            data[i] = ampl;
        }
    }

    public void constant(Signal s, List<Double> parameters) {
        double ampl = parameters.get(0);

        double data[];
        if (s.getDataType() == Signal.IMAG)
            data = s.getImagData();
        else
            data = s.getRealData();

        constant(data, ampl);
    }

    public Signal FconstCi(Signal s, List<String> arguments) {

        double ampl = cp.getDouble(arguments, "Amplitude", 0.0, Integer.MAX_VALUE, DEF_AMPL);
        short dtype = (short) cp.getInt(arguments, "Type <0=Real,1=Imag,2=Complex>", Signal.REAL, Signal.COMP,
                Signal.REAL);
        int n = cp.getInt(arguments, "Number of elements (2^n)", Signal.MIN_N, Signal.MAX_N, DEF_N);
        int sample = cp.getInt(arguments, "Samplerate in 10Hz", 1, Short.MAX_VALUE, Signal.SAMPLE_RATE);

        int number = (int) Math.pow(2, n);

        setSignalValues(s, number, sample, dtype, "constant");
        List<Double> parameters = new ArrayList<Double>();
        parameters.add(ampl);

        source(s, parameters);
        return s;
    }

    public static void delta(double data[], double ampl, double t0, double sampleRate) {
        boolean flag = true;
        for (int i = 0; i < data.length; i++) {
            double t = (double) i / (double) sampleRate;
            if (t < t0)
                data[i] = (double) (0);
            else if ((t >= t0) && flag) {
                data[i] = ampl;
                flag = false;
            } else
                data[i] = 0.0;
        }
    }

    public void delta(Signal s, List<Double> parameters) {
        double ampl = parameters.get(0);
        double t0 = parameters.get(1);
        t0 = t0 / 1000.0; /* t0 in sec */
        int sampleRate = s.getDataSampleRate() * 10;

        double data[];
        if (s.getDataType() == Signal.IMAG)
            data = s.getImagData();
        else
            data = s.getRealData();

        delta(data, ampl, t0, sampleRate);
    }

    public Signal FdeltaCi(Signal im, List<String> arguments) {

        double ampl = cp.getDouble(arguments, "Amplitude", 0.0, Integer.MAX_VALUE, DEF_AMPL);
        double t0 = cp.getDouble(arguments, "Delta-t (millisec)", 0.0, Integer.MAX_VALUE, 0);
        short dtype = (short) cp.getInt(arguments, "Type <0=Real,1=Imag,2=Complex>", Signal.REAL, Signal.COMP,
                Signal.REAL);
        int n = cp.getInt(arguments, "Number of elements (2^n)", Signal.MIN_N, Signal.MAX_N, DEF_N);
        int sample = cp.getInt(arguments, "Samplerate in 10Hz", 1, Short.MAX_VALUE, Signal.SAMPLE_RATE);

        int number = (int) Math.pow(2, n);

        setSignalValues(im, number, sample, dtype, "delta");
        List<Double> parameters = new ArrayList<Double>();
        parameters.add(ampl);
        parameters.add(t0);

        source(im, parameters);

        return im;
    }

    private static void step(double data[], double offs, double ampl, double t0, int sampleRate) {
        double t;
        for (int i = 0; i < data.length; i++) {
            t = (double) i / (double) sampleRate;
            if (t < t0)
                data[i] = offs;
            if (t >= t0)
                data[i] = ampl + offs;
        }
    }

    public void step(Signal s, List<Double> parameters) {
        double offs = parameters.get(0);
        double ampl = parameters.get(1);
        double t0 = parameters.get(2);
        t0 = t0 / 1000.0; /* t0 in sec */
        int sampleRate = s.getDataSampleRate() * 10;

        double data[];
        if (s.getDataType() == Signal.IMAG)
            data = s.getImagData();
        else
            data = s.getRealData();

        step(data, offs, ampl, t0, sampleRate);
    }

    public Signal FstepCi(Signal s, List<String> arguments) {

        double offs = cp.getDouble(arguments, "Offset", 0.0, Integer.MAX_VALUE, 0);
        double ampl = cp.getDouble(arguments, "Amplitude", 0.0, Integer.MAX_VALUE, DEF_AMPL);
        double t0 = cp.getDouble(arguments, "t-delay (millisec)", 0.0, Integer.MAX_VALUE, 0);
        short dtype = (short) cp.getInt(arguments, "Type <0=Real,1=Imag,2=Complex>", Signal.REAL, Signal.COMP,
                Signal.REAL);
        int n = cp.getInt(arguments, "Number of elements (2^n)", Signal.MIN_N, Signal.MAX_N, DEF_N);
        int sample = cp.getInt(arguments, "Samplerate in 10Hz", 1, Short.MAX_VALUE, Signal.SAMPLE_RATE);

        int number = (int) Math.pow(2, n);

        setSignalValues(s, number, sample, dtype, "step");
        List<Double> parameters = new ArrayList<Double>();
        parameters.add(offs);
        parameters.add(ampl);
        parameters.add(t0);

        source(s, parameters);

        return s;
    }

    private static void noise(double data[], double ampl, int seed) {
        Random rand = new Random(seed);
        for (int i = 0; i < data.length; i++) {
            data[i] = ampl / (RAND_MAX / 2.0) * (rand.nextInt(RAND_MAX) - (RAND_MAX / 2.0));
        }
    }

    public void noise(Signal s, List<Double> parameters) {
        double ampl = parameters.get(0);
        int seed = parameters.get(1).intValue();

        double data[];
        if (s.getDataType() == Signal.IMAG)
            data = s.getImagData();
        else
            data = s.getRealData();

        noise(data, ampl, seed);
    }

    public Signal FnoiseCi(Signal s, List<String> arguments) {

        double ampl = cp.getDouble(arguments, "Amplitude", 0.0, Integer.MAX_VALUE, DEF_AMPL);
        short dtype = (short) cp.getInt(arguments, "Type <0=Real,1=Imag,2=Complex>", Signal.REAL, Signal.COMP,
                Signal.REAL);
        Integer seed = cp.getInt(arguments, "Seed <0 .. 512>", 0, 512, 1);
        int n = cp.getInt(arguments, "Number of elements (2^n)", Signal.MIN_N, Signal.MAX_N, DEF_N);
        int sample = cp.getInt(arguments, "Samplerate in 10Hz", 1, Short.MAX_VALUE, Signal.SAMPLE_RATE);

        int number = (int) Math.pow(2, n);

        setSignalValues(s, number, sample, dtype, "noise");
        List<Double> parameters = new ArrayList<Double>();
        parameters.add(ampl);
        parameters.add(seed.doubleValue());

        source(s, parameters);
        return s;
    }

    private static void sine(double data[], double offs, double ampl, double freq, double phi0, int sampleRate) {
        for (int i = 0; i < data.length; i++) {
            data[i] = offs + ampl * Math.sin(2 * Math.PI * freq * i / sampleRate + phi0);
        }
    }

    public void sine(Signal s, List<Double> parameters) {
        double offs = parameters.get(0);
        double ampl = parameters.get(1);
        double freq = parameters.get(2);
        double phi0 = parameters.get(3);
        int sampleRate = s.getDataSampleRate() * 10;

        double data[];
        if (s.getDataType() == Signal.IMAG)
            data = s.getImagData();
        else
            data = s.getRealData();

        sine(data, offs, ampl, freq, phi0, sampleRate);
    }

    public Signal FsineCi(Signal s, List<String> arguments) {

        double offs = cp.getDouble(arguments, "Offset", Integer.MIN_VALUE, Integer.MAX_VALUE, 0.0);
        double ampl = cp.getDouble(arguments, "Amplitude", 0.0, Integer.MAX_VALUE, DEF_AMPL);
        double freq = cp.getDouble(arguments, "Frequency", 1.0, Integer.MAX_VALUE, DEF_FREQ);
        double phi0 = cp.getDouble(arguments, "Phase (rad)", 0.0, 2.0 * Math.PI, 0.0);
        short dtype = (short) cp.getInt(arguments, "Type <0=Real,1=Imag,2=Complex>", Signal.REAL, Signal.COMP,
                Signal.REAL);
        int n = cp.getInt(arguments, "Number of elements (2^n)", Signal.MIN_N, Signal.MAX_N, DEF_N);
        int sample = cp.getInt(arguments, "Samplerate in 10Hz", 1, Short.MAX_VALUE, Signal.SAMPLE_RATE);

        int number = (int) Math.pow(2, n);

        setSignalValues(s, number, sample, dtype, "sine");
        List<Double> parameters = new ArrayList<Double>();
        parameters.add(offs);
        parameters.add(ampl);
        parameters.add(freq);
        parameters.add(phi0);

        source(s, parameters);

        return s;
    }

    private static void sinec(double data[], double offs, double ampl, double freq, int sampleRate) {
        int number = data.length;
        for (int i = 0; i < data.length; i++) {
            double t = (double) (i - number / 2.0) / (double) sampleRate;
            double arg = 2.0 * Math.PI * freq * t;
            if (t != 0.0)
                data[i] = (double) (offs + ampl * Math.sin(arg) / arg);
            else
                data[i] = (double) (offs + ampl * Math.cos(arg));
        }
    }

    public void sinec(Signal s, List<Double> parameters) {
        double offs = parameters.get(0);
        double ampl = parameters.get(1);
        double freq = parameters.get(2);
        int sampleRate = s.getDataSampleRate() * 10;

        double data[];
        if (s.getDataType() == Signal.IMAG)
            data = s.getImagData();
        else
            data = s.getRealData();

        sinec(data, offs, ampl, freq, sampleRate);
    }

    /**
     * FsinecCi generates a (sine(t)) / t.
     * 
     * @param arguments
     * @return
     */

    public Signal FsinecCi(Signal s, List<String> arguments) {

        double offs = cp.getDouble(arguments, "Offset", Integer.MIN_VALUE, Integer.MAX_VALUE, 0.0);
        double ampl = cp.getDouble(arguments, "Amplitude", 0.0, Integer.MAX_VALUE, DEF_AMPL);
        double freq = cp.getDouble(arguments, "Frequency", 1.0, Integer.MAX_VALUE, DEF_FREQ);
        short dtype = (short) cp.getInt(arguments, "Type <0=Real,1=Imag,2=Complex>", Signal.REAL, Signal.COMP,
                Signal.REAL);
        int n = cp.getInt(arguments, "Number of elements (2^n)", Signal.MIN_N, Signal.MAX_N, DEF_N);
        int sample = cp.getInt(arguments, "Samplerate in 10Hz", 1, Short.MAX_VALUE, Signal.SAMPLE_RATE);

        int number = (int) Math.pow(2, n);

        setSignalValues(s, number, sample, dtype, "sinec");
        List<Double> parameters = new ArrayList<Double>();
        parameters.add(offs);
        parameters.add(ampl);
        parameters.add(freq);

        source(s, parameters);

        return s;
    }

    private static void cosine(double data[], double offs, double ampl, double freq, double phi0, int sampleRate) {
        for (int i = 0; i < data.length; i++) {
            data[i] = offs + ampl * Math.cos(2 * Math.PI * freq * i / sampleRate + phi0);
        }
    }

    public void cosine(Signal s, List<Double> parameters) {
        double offs = parameters.get(0);
        double ampl = parameters.get(1);
        double freq = parameters.get(2);
        double phi0 = parameters.get(3);
        int sampleRate = s.getDataSampleRate() * 10;

        double data[];
        if (s.getDataType() == Signal.IMAG)
            data = s.getImagData();
        else
            data = s.getRealData();

        cosine(data, offs, ampl, freq, phi0, sampleRate);
    }

    public Signal FcosineCi(Signal s, List<String> arguments) {

        double offs = cp.getDouble(arguments, "Offset", Integer.MIN_VALUE, Integer.MAX_VALUE, 0.0);
        double ampl = cp.getDouble(arguments, "Amplitude", 0.0, Integer.MAX_VALUE, DEF_AMPL);
        double freq = cp.getDouble(arguments, "Frequency", 1.0, Integer.MAX_VALUE, DEF_FREQ);
        double phi0 = cp.getDouble(arguments, "Phase (rad)", 0.0, 2.0 * Math.PI, 0.0);
        short dtype = (short) cp.getInt(arguments, "Type <0=Real,1=Imag,2=Complex>", Signal.REAL, Signal.COMP,
                Signal.REAL);
        int n = cp.getInt(arguments, "Number of elements (2^n)", Signal.MIN_N, Signal.MAX_N, DEF_N);
        int sample = cp.getInt(arguments, "Samplerate in 10Hz", 1, Short.MAX_VALUE, Signal.SAMPLE_RATE);

        int number = (int) Math.pow(2, n);

        setSignalValues(s, number, sample, dtype, "cosine");
        List<Double> parameters = new ArrayList<Double>();
        parameters.add(offs);
        parameters.add(ampl);
        parameters.add(freq);
        parameters.add(phi0);

        source(s, parameters);

        return s;
    }

    private static void triangle(double data[], double offs, double ampl, int sampleRate, double T, double rc,
            double t0) {
        double n = 0;
        for (int i = 0; i < data.length; i++) {
            double t = (double) (i) / (double) (sampleRate);
            if ((t - T * n + t0) < (T / 2.0)) {
                data[i] = (double) (rc * (t - n * T + t0) - ampl + offs);
            }
            if ((t - T * n + t0) >= (T / 2.0)) {
                data[i] = (double) (-rc * (t - n * T + t0) + 3.0 * ampl + offs);
            }
            if ((t + t0) >= ((n + 1.0) * T))
                n = n + 1.0;
        }
    }

    public void triangle(Signal s, List<Double> parameters) {
        double offs = parameters.get(0);
        double ampl = parameters.get(1);
        double freq = parameters.get(2);
        double phi0 = parameters.get(3);
        int sampleRate = s.getDataSampleRate() * 10;

        double data[];
        if (s.getDataType() == Signal.IMAG)
            data = s.getImagData();
        else
            data = s.getRealData();

        double T = (double) (1) / (double) (freq); /* period time */
        double rc = 4.0 * ampl * freq; /* slope */
        double t0 = (phi0 * T) / (double) (2.0 * Math.PI);
        triangle(data, offs, ampl, sampleRate, T, rc, t0);
    }

    public Signal FtriangleCi(Signal s, List<String> arguments) {

        double offs = cp.getDouble(arguments, "Offset", Integer.MIN_VALUE, Integer.MAX_VALUE, 0.0);
        double ampl = cp.getDouble(arguments, "Amplitude", 0.0, Integer.MAX_VALUE, DEF_AMPL);
        double freq = cp.getDouble(arguments, "Frequency", 1.0, Integer.MAX_VALUE, DEF_FREQ);
        double phi0 = cp.getDouble(arguments, "Phase (rad)", 0.0, 2.0 * Math.PI, 0.0);
        short dtype = (short) cp.getInt(arguments, "Type <0=Real,1=Imag,2=Complex>", Signal.REAL, Signal.COMP,
                Signal.REAL);
        int n = cp.getInt(arguments, "Number of elements (2^n)", Signal.MIN_N, Signal.MAX_N, DEF_N);
        int sample = cp.getInt(arguments, "Samplerate in 10Hz", 1, Short.MAX_VALUE, Signal.SAMPLE_RATE);

        int number = (int) Math.pow(2, n);

        setSignalValues(s, number, sample, dtype, "triangle");
        List<Double> parameters = new ArrayList<Double>();
        parameters.add(offs);
        parameters.add(ampl);
        parameters.add(freq);
        parameters.add(phi0);

        source(s, parameters);

        return s;
    }

    private static void exp(double data[], double ampl, double T, int sampleRate) {
        double t;
        T = T / 1000.0; // T in seconds
        for (int i = 0; i < data.length; i++) {
            t = (double) i / (double) sampleRate;
            data[i] = ampl * (Math.exp(-t / T));
        }
    }

    public void exp(Signal s, List<Double> parameters) {
        double ampl = parameters.get(0);
        double T = parameters.get(1);
        int sampleRate = s.getDataSampleRate() * 10;

        double data[];
        if (s.getDataType() == Signal.IMAG)
            data = s.getImagData();
        else
            data = s.getRealData();

        exp(data, ampl, T, sampleRate);
    }

    /********************************************************************
     * FexpCi generates a A*(1-e^(-t/T)).
     */
    public Signal FexpCi(Signal s, List<String> arguments) {

        double ampl = cp.getDouble(arguments, "Amplitude", 0.0, Integer.MAX_VALUE, DEF_AMPL);
        double T = cp.getDouble(arguments, "t 63.2% (millisec)", 0.000001, 1000000.0, 10.0);
        short dtype = (short) cp.getInt(arguments, "Type <0=Real,1=Imag,2=Complex>", Signal.REAL, Signal.COMP,
                Signal.REAL);
        int n = cp.getInt(arguments, "Number of elements (2^n)", Signal.MIN_N, Signal.MAX_N, DEF_N);
        int sample = cp.getInt(arguments, "Samplerate in 10Hz", 1, Short.MAX_VALUE, Signal.SAMPLE_RATE);

        int number = (int) Math.pow(2, n);

        setSignalValues(s, number, sample, dtype, "exp");
        List<Double> parameters = new ArrayList<Double>();
        parameters.add(ampl);
        parameters.add(T);

        source(s, parameters);

        return s;
    }

    private static void square(double data[], double offs, double ampl, double dutyCycle, int sampleRate, double T) {
        int n = 0;
        double B, t;
        double dc = dutyCycle / 100.0;
        for (int i = 0; i < data.length; i++) {
            t = (double) i / (double) sampleRate;
            if ((t - T * n) < (dc * T)) {
                B = 1;
            } else {
                B = -1;
            }
            data[i] = (double) (offs + ampl * B);
            if (t >= ((n + 1) * T))
                n++;
        }
    }

    public void square(Signal s, List<Double> parameters) {
        double offs = parameters.get(0);
        double ampl = parameters.get(1);
        double freq = parameters.get(2);
        double dutyCycle = parameters.get(3);
        int sampleRate = s.getDataSampleRate() * 10;

        double data[];
        if (s.getDataType() == Signal.IMAG)
            data = s.getImagData();
        else
            data = s.getRealData();

        double T = (double) (1) / (double) (freq); /* period time */
        square(data, offs, ampl, dutyCycle, sampleRate, T);
    }

    public Signal FsquarewaveCi(Signal s, List<String> arguments) {

        double offs = cp.getDouble(arguments, "Offset", Integer.MIN_VALUE, Integer.MAX_VALUE, 0.0);
        double ampl = cp.getDouble(arguments, "Amplitude", 0.0, Integer.MAX_VALUE, DEF_AMPL);
        double freq = cp.getDouble(arguments, "Frequency", 1.0, Integer.MAX_VALUE, DEF_FREQ);
        double dutyCycle = cp.getDouble(arguments, "Duty-cycle <0% .. 100%>", 0.0, 100.0, 50.0);
        short dtype = (short) cp.getInt(arguments, "Type <0=Real,1=Imag,2=Complex>", Signal.REAL, Signal.COMP,
                Signal.REAL);
        int n = cp.getInt(arguments, "Number of elements (2^n)", Signal.MIN_N, Signal.MAX_N, DEF_N);
        int sample = cp.getInt(arguments, "Samplerate in 10Hz", 1, Short.MAX_VALUE, Signal.SAMPLE_RATE);

        int number = (int) Math.pow(2, n);

        setSignalValues(s, number, sample, dtype, "square");
        List<Double> parameters = new ArrayList<Double>();
        parameters.add(offs);
        parameters.add(ampl);
        parameters.add(freq);
        parameters.add(dutyCycle);

        source(s, parameters);

        return s;
    }

    private static void ramp(double data[], double offs, double ampl, int sampleRate) {
        double rc = ampl * (double) sampleRate / (double) (data.length - 1);
        double t;
        for (int i = 0; i < data.length; i++) {
            t = (double) i / (double) sampleRate;
            data[i] = (double) (rc * t + offs);
        }
    }

    public void ramp(Signal s, List<Double> parameters) {
        double offs = parameters.get(0);
        double ampl = parameters.get(1);
        int sampleRate = s.getDataSampleRate() * 10;

        double data[];
        if (s.getDataType() == Signal.IMAG)
            data = s.getImagData();
        else
            data = s.getRealData();

        ramp(data, offs, ampl, sampleRate);
    }

    public Signal FrampCi(Signal s, List<String> arguments) {

        double offs = cp.getDouble(arguments, "Offset", Integer.MIN_VALUE, Integer.MAX_VALUE, 0.0);
        double ampl = cp.getDouble(arguments, "Amplitude", 0.0, Integer.MAX_VALUE, DEF_AMPL);
        short dtype = (short) cp.getInt(arguments, "Type <0=Real,1=Imag,2=Complex>", Signal.REAL, Signal.COMP,
                Signal.REAL);
        int n = cp.getInt(arguments, "Number of elements (2^n)", Signal.MIN_N, Signal.MAX_N, DEF_N);
        int sample = cp.getInt(arguments, "Samplerate in 10Hz", 1, Short.MAX_VALUE, Signal.SAMPLE_RATE);

        int number = (int) Math.pow(2, n);

        setSignalValues(s, number, sample, dtype, "ramp");
        List<Double> parameters = new ArrayList<Double>();
        parameters.add(offs);
        parameters.add(ampl);

        source(s, parameters);

        return s;
    }

    public Signal SourcesCi(List<String> arguments, String command) {
        String signalname = cp.getString(arguments, "Signal", command.substring(1));
        Signal outputSignal = null;

        Signal s = signals.get(signalname); /* Find the correct signal */

        if (s == null) /* Signal did not exist yet */
        {
            s = new Signal(signalname);
            signals.put(signalname, s);
        }

        Method method = null;
        String methodName = functions.get(command)[0];
        try {
            method = this.getClass().getMethod(methodName, Signal.class, List.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (method == null)
            return null;

        try {
            outputSignal = (Signal) method.invoke(this, s, arguments);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return outputSignal;
    }
}