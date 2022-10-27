package math;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import console.CommandLineParser;
import exceptions.SignalDoesNotExist;
import signals.Signal;
import signals.Windowing;

public class Transformations extends MathBase {

    public static final Map<String, String[]> transformations = new HashMap<String, String[]>() {
        {
            put("fft", new String[]{"FFTCi",""});
            put("ifft", new String[]{"IFFTCi",""});
            put("magnitude", new String[]{"MagnitudeCi",""});
            put("phase", new String[]{"PhaseCi",""});
            put("histogram", new String[]{"HistCi",""});
        }
    };

    public Transformations(Map<String, Signal> signals, CommandLineParser cp) {
        super(signals, cp);
    }
    
    public static void fft(double[] real, double[] imag, int offset, int fft_length, int dir) {
        double re[] = Arrays.copyOfRange(real, offset, offset + fft_length);
        double im[] = Arrays.copyOfRange(imag, offset, offset + fft_length);
        FFT.fft(re, im, fft_length, dir);
        for (int i = 0; i < fft_length; i++) {
            real[offset + i] = re[i];
            imag[offset + i] = im[i];
        }
    }

    public void fft(Signal signal, int length, int windowing, int mid_type, Signal outputSignal) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int dataLength = signal.getDataLength();
        int fft_length = (int) Math.pow(2.0, length);
        if (fft_length > signal.getDataLength())
            fft_length = signal.getDataLength();
        int records_out = ((signal.getDataLength() * signal.getDataRecords()) / fft_length);
        cp.println("fft_length = " + fft_length);
        cp.println("records_out = " + records_out);
        double[] re_out = new double[dataLength];
        double[] im_out = new double[dataLength];

        for (int channel = 0; channel < signal.getDataChannels(); channel++) {

            for (int record = 0; record < records_out; record++) {
                int offset = (channel * records_out + record) * fft_length;
                Windowing.windowing(re, im, re_out, im_out, offset, fft_length, windowing);
                fft(re_out, im_out, offset, fft_length, 1);
            }
        }
        outputSignal.setDataRecords(records_out);
        outputSignal.setDataLength(fft_length);
        outputSignal.setDataDomain(Signal.FREQ);
        outputSignal.setDataType(Signal.COMP);
        outputSignal.setMode(Signal.BODE_M);
        outputSignal.setAverageType(mid_type);
        outputSignal.setRecord(0);
        outputSignal.setChannel(0);
        outputSignal.setRealData(re_out);
        outputSignal.setImagData(im_out);
    }

    public Signal FFTCi(List<String> arguments, String command) throws SignalDoesNotExist {
        String signalname = cp.getString(arguments, "Signal", "a");

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        if (signal.getDataDomain() != Signal.TIME) {
            // TODO domain error
            cp.println("domain error");
            return null;
        }
        String outputSignalName = cp.getString(arguments, "Signal", command);

        Signal outputSignal = signals.get(outputSignalName);
        if (outputSignal == null) /* Signal did not exist yet */
        {
            outputSignal = new Signal(outputSignalName);
            signals.put(outputSignalName, outputSignal);
        }
        setSignalValues(outputSignal, signal);

        int l_max = (int) (Math.log10(signal.getDataLength()) / Math.log10(2.0));
        int length = cp.getInt(arguments, "Size (2^n)", Signal.MIN_N, l_max, l_max);
        int windowing = cp.getInt(arguments, "Window type", 0, 6, 1);
        int mid_type = cp.getInt(arguments, "Average type", 0, 1, 0);
        fft(signal, length, windowing, mid_type, outputSignal);
        return outputSignal;
    }

    public void ifft(Signal signal, Signal outputSignal) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int dataLength = signal.getDataLength();
        int totalLength = signal.getDataChannels() * signal.getDataRecords() * dataLength;
        double[] re_out = new double[totalLength];
        double[] im_out = new double[totalLength];

        for (int channel = 0; channel < signal.getDataChannels(); channel++) {

            for (int record = 0; record < signal.getDataRecords(); record++) {
                int offset = (channel * signal.getDataRecords() + record) * dataLength;
                fft(re_out, im_out, offset, dataLength, -1);
            }
        }
        outputSignal.setDataDomain(Signal.TIME);
        outputSignal.setDataType(Signal.COMP);
        outputSignal.setMode(Signal.REAL_M);
        outputSignal.setRecord(0);
        outputSignal.setChannel(0);
        outputSignal.setRealData(re_out);
        outputSignal.setImagData(im_out);
    }

    public Signal IFFTCi(List<String> arguments, String command) throws SignalDoesNotExist {
        String signalname = cp.getString(arguments, "Signal", "a");

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        if (signal.getDataDomain() != Signal.FREQ) {
            // TODO domain error
            cp.println("domain error");
            return null;
        }
        String outputSignalName = cp.getString(arguments, "Signal", command);

        Signal outputSignal = signals.get(outputSignalName);
        if (outputSignal == null) /* Signal did not exist yet */
        {
            outputSignal = new Signal(outputSignalName);
            signals.put(outputSignalName, outputSignal);
        }
        setSignalValues(outputSignal, signal);

        ifft(signal, outputSignal);
        return outputSignal;
    }

    public void magnitude(Signal signal, int channel_nr, int mid_type, int log, Signal outputSignal) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int dataLength = signal.getDataLength();
        int channelLength = signal.getDataRecords() * signal.getDataLength();
        // The output signals will have channels reduced to 1 record.
        double[] re_out = new double[signal.getDataChannels() * signal.getDataLength()];
        double[] im_out = new double[signal.getDataChannels() * signal.getDataLength()];

        double[] cum1 = new double[signal.getDataLength()];

        if (mid_type == 0) {
            for (int channel = 0; channel < signal.getDataChannels(); channel++) {
                int offset = channel * channelLength;
                for (int record = 0; record < signal.getDataRecords(); record++) {
                    for (int i = 0; i < dataLength; i++) {
                        int data_nr = offset + record * dataLength + i;
                        double temp = Math.sqrt(Math.pow(re[data_nr], 2) + Math.pow(im[data_nr], 2));
                        /* Tel de magnitudes bij elkaar op */
                        cum1[i] = cum1[i] + temp;
                    }
                }
                for (int i = 0; i < dataLength; i++) {
                    /* Deel de cumulatieve magnitudes door het aantal records */
                    cum1[i] = cum1[i] / (double) signal.getDataRecords();
                    /* In het magnitude domein bevatten de channels altijd maar 1 record */
                    re_out[i + channel * dataLength] = cum1[i];
                    im_out[i + channel * dataLength] = 0.0;
                    cum1[i] = 0.0; /* leeg maken voor volgende channel */
                }
            }
        }
        if (mid_type == 1) {
            double[] cum2 = new double[signal.getDataLength()];
            for (int h = 0; h < signal.getDataChannels(); h++) {
                /* offset van betreffende channel */
                int offs = h * signal.getDataRecords() * dataLength;

                for (int j = 0; j < signal.getDataRecords(); j++) {
                    /* Tel de records bij van een channel elkaar op */
                    for (int i = 0; i < dataLength; i++) {
                        cum1[i] = cum1[i] + (re[offs + i + j * dataLength]);
                        cum2[i] = cum2[i] + (im[offs + i + j * dataLength]);
                    }
                }
                for (int i = 0; i < dataLength; i++) {
                    /* Deel door het aantal records */
                    cum1[i] = cum1[i] / (double) (signal.getDataRecords());
                    cum2[i] = cum2[i] / (double) (signal.getDataRecords());

                    /* Bepaal de magnitude */
                    /* In het magnitude domein bevatten de channels altijd maar 1 record */
                    re_out[i + h * dataLength] = Math.sqrt(Math.pow(cum1[i], 2) + Math.pow(cum2[i], 2));
                    im_out[i + h * dataLength] = 0.0;

                    cum1[i] = cum2[i] = 0.0; /* leeg maken voor volgend channel */
                }
            }
        }
        outputSignal.setDataRecords(1);
        outputSignal.setDataDomain(Signal.MAGN);
        outputSignal.setMode(Signal.MAGN_M);
        outputSignal.setDataType(Signal.REAL);
        outputSignal.setRecord(0);
        outputSignal.setChannel(channel_nr);
        outputSignal.setLog(log);
        outputSignal.setRealData(re_out);
        outputSignal.setImagData(im_out);
    }

    public Signal MagnitudeCi(List<String> arguments, String command) throws SignalDoesNotExist {
        String signalname = cp.getString(arguments, "Signal", "a");

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        if (signal.getDataDomain() != Signal.FREQ) {
            // TODO domain error
            cp.println("domain error");
            return null;
        }
        String outputSignalName = cp.getString(arguments, "Signal", command);

        Signal outputSignal = signals.get(outputSignalName);
        if (outputSignal == null) /* Signal did not exist yet */
        {
            outputSignal = new Signal(outputSignalName);
            signals.put(outputSignalName, outputSignal);
        }
        setSignalValues(outputSignal, signal);

        int channel_nr = cp.getInt(arguments, "Channel", 0, signal.getDataChannels() - 1, 0);
        int mid_type = cp.getInt(arguments, "Average type", 0, 1, 0);
        int log = cp.getInt(arguments, "Log magnitude", 0, 1, 0);
        magnitude(signal, channel_nr, mid_type, log, outputSignal);
        return outputSignal;
    }

    public void phase(Signal signal, int channel_nr, int mid_type, Signal outputSignal) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int dataLength = signal.getDataLength();
        int channelLength = signal.getDataRecords() * signal.getDataLength();
        // The output signals will have channels reduced to 1 record.
        double[] re_out = new double[signal.getDataChannels() * signal.getDataLength()];
        double[] im_out = new double[signal.getDataChannels() * signal.getDataLength()];

        double[] cum1 = new double[dataLength];

        if (mid_type == 0) {
            for (int h = 0; h < signal.getDataChannels(); h++) /* aantal channels */
            {

                for (int j = 0; j < signal.getDataRecords(); j++) /* aantal records */
                {
                    /* Bereken het adres van record j in channel h */
                    int offset = h * channelLength + j * dataLength;
                    for (int i = 0; i < dataLength; i++) /* record-lengte */
                    {
                        /* bepaal de phase */
                        double temp = (180 / Math.PI) * Math.atan2((im[i + offset]), (re[i + offset]));
                        cum1[i] = cum1[i] + temp; /* Cumulatief */
                    }
                }

                /* Bereken het adres van channel h */
                /* In het fase domein bevat een channel altijd maar 1 record */
                int offset = h * dataLength;

                /* Deel de cumulatieve fase door het aantal records */
                for (int i = 0; i < dataLength; i++) {
                    re_out[i + offset] = cum1[i] / (double) (signal.getDataRecords());
                    im_out[i + offset] = 0.0;
                    cum1[i] = 0.0; /* leeg maken voor volgende channel */
                }
            }
        }
        if (mid_type == 1) {
            double[] cum2 = new double[dataLength];
            for (int h = 0; h < signal.getDataChannels(); h++) {

                for (int j = 0; j < signal.getDataRecords(); j++) {
                    /* Bereken het adres van record j in channel h */
                    int offset = h * channelLength + j * dataLength;
                    for (int i = 0; i < dataLength; i++) {
                        /* Tel de records bij elkaar op */
                        cum1[i] = cum1[i] + re[i + offset];
                        cum2[i] = cum2[i] + im[i + offset];
                    }
                }

                /* Bereken het adres van channel h */
                /* In het fase domein bevat een channel altijd maar 1 record */
                int offset = h * dataLength;

                for (int i = 0; i < dataLength; i++) {
                    /* deel door het aantal records */
                    cum1[i] = cum1[i] / (double) (signal.getDataRecords());
                    cum2[i] = cum2[i] / (double) (signal.getDataRecords());
                    /* Bepaal de fase */
                    re_out[i + offset] = (180 / Math.PI) * Math.atan2(cum2[i], cum1[i]);
                    im_out[i + offset] = 0.0;

                    cum1[i] = cum2[i] = 0.0; /* leeg maken voor volgend channel */
                }
            }
        }
        outputSignal.setDataRecords(1);
        outputSignal.setDataDomain(Signal.PHAS);
        outputSignal.setMode(Signal.PHAS_M);
        outputSignal.setDataType(Signal.REAL);
        outputSignal.setRecord(0);
        outputSignal.setChannel(channel_nr);
        outputSignal.setRealData(re_out);
        outputSignal.setImagData(im_out);
    }

    public Signal PhaseCi(List<String> arguments, String command) throws SignalDoesNotExist {
        String signalname = cp.getString(arguments, "Signal", "a");

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        if (signal.getDataDomain() != Signal.FREQ) {
            // TODO domain error
            cp.println("domain error");
            return null;
        }
        String outputSignalName = cp.getString(arguments, "Signal", command);

        Signal outputSignal = signals.get(outputSignalName);
        if (outputSignal == null) /* Signal did not exist yet */
        {
            outputSignal = new Signal(outputSignalName);
            signals.put(outputSignalName, outputSignal);
        }
        setSignalValues(outputSignal, signal);

        int channel_nr = cp.getInt(arguments, "Channel", 0, signal.getDataChannels() - 1, 0);
        int mid_type = cp.getInt(arguments, "Average type", 0, 1, 0);
        phase(signal, channel_nr, mid_type, outputSignal);
        return outputSignal;
    }

    public void histogram(Signal signal, int buckets, Signal outputSignal) {

        int lengte;
        int offset_in, offset_out;
        int bucket;
        double Delta;
        // char buffer1[128],buffer2[128];
        boolean bucket_found;
        int dataLength = signal.getDataLength();
        buckets = (int) Math.pow(2, buckets);
        int alloc_size = signal.getDataChannels() * buckets;
        lengte = signal.getDataRecords() * dataLength; /* channel lengte */

        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        double[] re_out = new double[alloc_size];
        double[] im_out = new double[alloc_size];

        double[] real_hulp = Arrays.copyOfRange(re, 0, re.length);

        outputSignal.setRealData(re_out); /* Zet de pointers van de data weer terug */
        outputSignal.setImagData(im_out);

        outputSignal.setDataLength(buckets);
        outputSignal.setDataRecords(1);
        outputSignal.setDataType(Signal.REAL);
        outputSignal.setDataDomain(Signal.AMPL);
        outputSignal.setMode(Signal.HIST_M);
        outputSignal.setRecord(0);
        outputSignal.setHScale(signal.getHScale());

        for (int channel = 0; channel < signal.getDataChannels(); channel++) {
            /* Het histogram wordt per channel van het ingangssignaal bepaalt */
            /* In het histogram gaat het om de magnitude */

            /* Bereken de offset van het channel */
            offset_in = channel * lengte;
            offset_out = (channel) * outputSignal.getDataRecords() * outputSignal.getDataLength();

            double reMin, reMax;
            reMin = reMax = Math.sqrt(Math.pow(real_hulp[offset_in], 2) + Math.pow(im[offset_in], 2));

            /* Bepaal van de elementen de magnitude en het maximum en minimum */
            for (int element = 0; element < lengte; element++) {
                real_hulp[element + offset_in] = Math.sqrt(
                        Math.pow(real_hulp[element + offset_in], 2) + Math.pow(im[element + offset_in], 2));
                if (real_hulp[element + offset_in] > reMax)
                    reMax = real_hulp[element + offset_in];
                if (real_hulp[element + offset_in] < reMin)
                    reMin = real_hulp[element + offset_in];
            }
            // FIXME: max and min are overwritten by every next channel. 
            outputSignal.setRealMaximum(reMax);
            outputSignal.setRealMinimum(reMin);

            Delta = (double) ((reMax - reMin) / (double) (buckets));

            /* Maak de elementen van out_im 0 */
            for (int i = 0; i < (outputSignal.getDataRecords() * outputSignal.getDataLength()); i++) {
                re_out[i + offset_out] = 0.0;
                im_out[i + offset_out] = 0.0;
            }

            for (int i = 0; i < lengte; i++) {
                bucket = buckets / 2;
                bucket_found = false;

                for (int j = (buckets / 2); (j > 0) && (bucket_found == false); j = (int) (j / 2)) {
                    if (real_hulp[i + offset_in] >= (reMin + ((double) bucket + 1.0) * Delta))
                        bucket = bucket + (int) (j / 2);
                    else {
                        if (real_hulp[i + offset_in] < (reMin + (double) bucket * Delta))
                            bucket = bucket - (int) (j / 2);
                        else
                            bucket_found = true;
                    }
                    if (real_hulp[i + offset_in] < (reMin + Delta)) {
                        bucket = 0;
                        bucket_found = true;
                    }
                }
                re_out[offset_out + bucket] = re_out[offset_out + bucket] + 1;
            }
        }
    }

    public Signal HistCi(List<String> arguments, String command) throws SignalDoesNotExist {
        String signalname = cp.getString(arguments, "Signal", "a");

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        if (signal.getDataDomain() != Signal.TIME) {
            // TODO domain error
            cp.println("domain error");
            return null;
        }
        String outputSignalName = cp.getString(arguments, "Signal", command);

        Signal outputSignal = signals.get(outputSignalName);
        if (outputSignal == null) /* Signal did not exist yet */
        {
            outputSignal = new Signal(outputSignalName);
            signals.put(outputSignalName, outputSignal);
        }
        setSignalValues(outputSignal, signal);

        int buckets = cp.getInt(arguments, "Buckets (2^n)", Signal.MIN_N,
                (int) (Math.log(signal.getDataLength()) / Math.log(2)),
                (int) (Math.log(signal.getDataLength()) / Math.log(2)));
        histogram(signal, buckets, outputSignal);
        return outputSignal;
    }

    public Signal TransformationCi(List<String> arguments, String command) throws SignalDoesNotExist {
        Signal outputSignal = null;

        Method method = null;
        String methodName = transformations.get(command)[0];
        try {
            method = this.getClass().getMethod(methodName, List.class, String.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (method == null)
            return null;

        try {
            outputSignal = (Signal) method.invoke(this, arguments, command);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof SignalDoesNotExist)
                throw new SignalDoesNotExist(e.getCause());
            else
                e.printStackTrace();
        }

        return outputSignal;
    }
}