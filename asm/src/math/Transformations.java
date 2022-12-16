package math;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import console.CommandLineParser;
import exceptions.SignalDoesNotExist;
import exceptions.WrongDomain;
import signals.ComplexArray;
import signals.Signal;
import signals.Windowing;

public class Transformations extends MathBase {

    public static final Map<String, String[]> transformations = new HashMap<String, String[]>() {
        /**
         * 
         */
        private static final long serialVersionUID = -5132642404065300951L;

        {
            put("fft", new String[] { "FFTCi", "<input> <output> <length> <window> <average-type>" });
            put("ifft", new String[] { "IFFTCi", "<input> <output>" });
            put("magnitude", new String[] { "MagnitudeCi", "<input> <output> <channel> <average-type> <log>" });
            put("phase", new String[] { "PhaseCi", "<input> <output> <channel> <average-type>" });
            put("histogram", new String[] { "HistCi", "<input> <output> <buckets>" });
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
        cp.println("fft length = " + fft_length);
        // cp.println("records_out = " + records_out);
        ComplexArray data_out = new ComplexArray(dataLength);

        for (int channel = 0; channel < signal.getDataChannels(); channel++) {

            for (int record = 0; record < records_out; record++) {
                int offset = (channel * records_out + record) * fft_length;
                Windowing.windowing(re, im, data_out.re, data_out.im, offset, fft_length, windowing);
                fft(data_out.re, data_out.im, offset, fft_length, 1);
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
        outputSignal.setData(data_out);
    }

    public Signal FFTCi(List<String> arguments, String command) throws SignalDoesNotExist, WrongDomain {
        String signalname = cp.getString(arguments, "Signal", "a");

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        if (signal.getDataDomain() != Signal.TIME) {
            throw new WrongDomain(String.format("Command \"%s\" can't operate on signal \"%s\" of domain %s.", command,
                    signalname, signal.getDataDomainToString()));
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
        int length = cp.getInt(arguments, "Length (2^n)", Signal.MIN_N, l_max, l_max);
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
        ComplexArray data_out = new ComplexArray(totalLength);

        for (int i = 0; i < totalLength; ++i) {
            data_out.re[i] = re[i];
            data_out.im[i] = im[i];
        }

        for (int channel = 0; channel < signal.getDataChannels(); channel++) {

            for (int record = 0; record < signal.getDataRecords(); record++) {
                int offset = (channel * signal.getDataRecords() + record) * dataLength;
                fft(data_out.re, data_out.im, offset, dataLength, -1);
            }
        }
        outputSignal.setDataDomain(Signal.TIME);
        outputSignal.setDataType(Signal.COMP);
        outputSignal.setMode(Signal.REAL_M);
        outputSignal.setRecord(0);
        outputSignal.setChannel(0);
        outputSignal.setData(data_out);
    }

    public Signal IFFTCi(List<String> arguments, String command) throws SignalDoesNotExist, WrongDomain {
        String signalname = cp.getString(arguments, "Signal", "a");

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        if (signal.getDataDomain() != Signal.FREQ) {
            throw new WrongDomain(String.format("Command \"%s\" can't operate on signal \"%s\" of domain %s.", command,
                    signalname, signal.getDataDomainToString()));
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
        ComplexArray data_out = new ComplexArray(signal.getDataChannels() * signal.getDataLength());

        double[] cumulative1 = new double[signal.getDataLength()];

        if (mid_type == 0) {
            for (int channel = 0; channel < signal.getDataChannels(); channel++) {
                int offset = channel * channelLength;
                for (int record = 0; record < signal.getDataRecords(); record++) {
                    for (int i = 0; i < dataLength; i++) {
                        int data_nr = offset + record * dataLength + i;
                        double temp = Math.sqrt(Math.pow(re[data_nr], 2) + Math.pow(im[data_nr], 2));
                        /* Add up the magnitudes */
                        cumulative1[i] = cumulative1[i] + temp;
                    }
                }
                for (int i = 0; i < dataLength; i++) {
                    /* Divide the cumulative magnitudes by the number of records */
                    cumulative1[i] = cumulative1[i] / (double) signal.getDataRecords();
                    /* In the magnitude domain channels have always 1 record */
                    data_out.re[i + channel * dataLength] = cumulative1[i];
                    data_out.im[i + channel * dataLength] = 0.0;
                    cumulative1[i] = 0.0; /* make empty for the next channel */
                }
            }
        }
        if (mid_type == 1) {
            double[] cumulative2 = new double[signal.getDataLength()];
            for (int h = 0; h < signal.getDataChannels(); h++) {
                /* offset of the concerning channel */
                int offs = h * signal.getDataRecords() * dataLength;

                for (int j = 0; j < signal.getDataRecords(); j++) {
                    /* Add up the records of a channel */
                    for (int i = 0; i < dataLength; i++) {
                        cumulative1[i] = cumulative1[i] + (re[offs + i + j * dataLength]);
                        cumulative2[i] = cumulative2[i] + (im[offs + i + j * dataLength]);
                    }
                }
                for (int i = 0; i < dataLength; i++) {
                    /* Divide by the number of records */
                    cumulative1[i] = cumulative1[i] / (double) (signal.getDataRecords());
                    cumulative2[i] = cumulative2[i] / (double) (signal.getDataRecords());

                    /* Determine the magnitude */
                    /* In the magnitude domain channels have always 1 record */
                    data_out.re[i + h * dataLength] = Math
                            .sqrt(Math.pow(cumulative1[i], 2) + Math.pow(cumulative2[i], 2));
                    data_out.im[i + h * dataLength] = 0.0;

                    cumulative1[i] = cumulative2[i] = 0.0; /* make empty for next channel */
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
        outputSignal.setData(data_out);
    }

    public Signal MagnitudeCi(List<String> arguments, String command) throws SignalDoesNotExist, WrongDomain {
        String signalname = cp.getString(arguments, "Signal", "a");

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        if (signal.getDataDomain() != Signal.FREQ) {
            throw new WrongDomain(String.format("Command \"%s\" can't operate on signal \"%s\" of domain %s.", command,
                    signalname, signal.getDataDomainToString()));
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
        ComplexArray data_out = new ComplexArray(signal.getDataChannels() * signal.getDataLength());

        double[] cumulative1 = new double[dataLength];

        if (mid_type == 0) {
            for (int h = 0; h < signal.getDataChannels(); h++) /* number of channels */
            {

                for (int j = 0; j < signal.getDataRecords(); j++) /* number of records */
                {
                    /* Calculate the address of record j in channel h */
                    int offset = h * channelLength + j * dataLength;
                    for (int i = 0; i < dataLength; i++) /* record-length */
                    {
                        /* determine the phase */
                        double temp = (180 / Math.PI) * Math.atan2((im[i + offset]), (re[i + offset]));
                        cumulative1[i] = cumulative1[i] + temp; /* Cumulative */
                    }
                }

                /* Calculate the address of channel h */
                /* In the phase domain a channel has always 1 record */
                int offset = h * dataLength;

                /* Divide the cumulative phase by the number of records */
                for (int i = 0; i < dataLength; i++) {
                    data_out.re[i + offset] = cumulative1[i] / (double) (signal.getDataRecords());
                    data_out.im[i + offset] = 0.0;
                    cumulative1[i] = 0.0; /* leeg maken voor volgende channel */
                }
            }
        }
        if (mid_type == 1) {
            double[] cumulative2 = new double[dataLength];
            for (int h = 0; h < signal.getDataChannels(); h++) {

                for (int j = 0; j < signal.getDataRecords(); j++) {
                    /* Calculate the address of record j in channel h */
                    int offset = h * channelLength + j * dataLength;
                    for (int i = 0; i < dataLength; i++) {
                        /* Add up the records */
                        cumulative1[i] = cumulative1[i] + re[i + offset];
                        cumulative2[i] = cumulative2[i] + im[i + offset];
                    }
                }

                /* Calculate the address of channel h */
                /* In the phase domain a channel has always 1 record */
                int offset = h * dataLength;

                for (int i = 0; i < dataLength; i++) {
                    /* divide by the number of records */
                    cumulative1[i] = cumulative1[i] / (double) (signal.getDataRecords());
                    cumulative2[i] = cumulative2[i] / (double) (signal.getDataRecords());
                    /* Determine the phase */
                    data_out.re[i + offset] = (180 / Math.PI) * Math.atan2(cumulative2[i], cumulative1[i]);
                    data_out.im[i + offset] = 0.0;

                    cumulative1[i] = cumulative2[i] = 0.0; /* make empty for next channel */
                }
            }
        }
        outputSignal.setDataRecords(1);
        outputSignal.setDataDomain(Signal.PHAS);
        outputSignal.setMode(Signal.PHAS_M);
        outputSignal.setDataType(Signal.REAL);
        outputSignal.setRecord(0);
        outputSignal.setChannel(channel_nr);
        outputSignal.setData(data_out);
    }

    public Signal PhaseCi(List<String> arguments, String command) throws SignalDoesNotExist, WrongDomain {
        String signalname = cp.getString(arguments, "Signal", "a");

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        if (signal.getDataDomain() != Signal.FREQ) {
            throw new WrongDomain(String.format("Command \"%s\" can't operate on signal \"%s\" of domain %s.", command,
                    signalname, signal.getDataDomainToString()));
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
        ComplexArray data_out = new ComplexArray(alloc_size);

        double[] real_hulp = Arrays.copyOfRange(re, 0, re.length);

        outputSignal.setData(data_out); /* Set the data pointers back */

        outputSignal.setDataLength(buckets);
        outputSignal.setDataRecords(1);
        outputSignal.setDataType(Signal.REAL);
        outputSignal.setDataDomain(Signal.AMPL);
        outputSignal.setMode(Signal.HIST_M);
        outputSignal.setRecord(0);
        outputSignal.setHScale(signal.getHScale());

        for (int channel = 0; channel < signal.getDataChannels(); channel++) {
            /* The histogram is determined per channel of the input signal */
            /* In the histogram it's about magnitude */

            /* Calculate the offset of the channel */
            offset_in = channel * lengte;
            offset_out = (channel) * outputSignal.getDataRecords() * outputSignal.getDataLength();

            double reMin, reMax;
            reMin = reMax = Math.sqrt(Math.pow(real_hulp[offset_in], 2) + Math.pow(im[offset_in], 2));

            /* Determine of the elements of the magnitude the maximum and minimum */
            for (int element = 0; element < lengte; element++) {
                real_hulp[element + offset_in] = Math
                        .sqrt(Math.pow(real_hulp[element + offset_in], 2) + Math.pow(im[element + offset_in], 2));
                if (real_hulp[element + offset_in] > reMax)
                    reMax = real_hulp[element + offset_in];
                if (real_hulp[element + offset_in] < reMin)
                    reMin = real_hulp[element + offset_in];
            }
            // FIXME: max and min are overwritten by every next channel.
            outputSignal.setRealMaximum(reMax);
            outputSignal.setRealMinimum(reMin);

            Delta = (double) ((reMax - reMin) / (double) (buckets));

            /* Make the elements of out_im 0 */
            for (int i = 0; i < (outputSignal.getDataRecords() * outputSignal.getDataLength()); i++) {
                data_out.re[i + offset_out] = 0.0;
                data_out.im[i + offset_out] = 0.0;
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
                data_out.re[offset_out + bucket] = data_out.re[offset_out + bucket] + 1;
            }
        }
    }

    public Signal HistCi(List<String> arguments, String command) throws SignalDoesNotExist, WrongDomain {
        String signalname = cp.getString(arguments, "Signal", "a");

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        if (signal.getDataDomain() != Signal.TIME) {
            throw new WrongDomain(String.format("Command \"%s\" can't operate on signal \"%s\" of domain %s.", command,
                    signalname, signal.getDataDomainToString()));
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

    public Signal TransformationCi(List<String> arguments, String command) throws SignalDoesNotExist, WrongDomain {
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
            else if (e.getCause() instanceof WrongDomain)
                throw new WrongDomain(e.getCause());
            else
                e.printStackTrace();
        }

        return outputSignal;
    }
}