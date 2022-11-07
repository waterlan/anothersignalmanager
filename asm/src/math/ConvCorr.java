package math;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import console.CommandLineParser;
import exceptions.SignalDoesNotExist;
import signals.Signal;
import signals.Windowing;

public class ConvCorr extends MathBase {

    public static final Map<String, String[]> convcorr = new HashMap<String, String[]>() {
        {
            put("convolution", new String[] { "ConvolutionCi", "<input1> <input2> <output> <window>" });
            put("correlation", new String[] { "CorrelationCi", "<input1> <input2> <output> <window>" });
        }
    };

    public ConvCorr(Map<String, Signal> signals, CommandLineParser cp) {
        super(signals, cp);
    }

    private void convolutionCorrelation(Signal signal1, Signal signal2, int windowing, Signal outputSignal,
            boolean convolution) {

        // if ((DataDomain(vp[in_im1]) != REAL) || (DataDomain(vp[in_im2]) != REAL))
        // return ERR_DOMAIN; /* wrong domain */

        if ((signal1.getDataLength() != signal2.getDataLength())
                || (signal1.getDataRecords() != signal2.getDataRecords())
                || (signal1.getDataChannels() != signal2.getDataChannels())) {
            // TODO size error.
            cp.println("size error");
            return;
        }

        if (signal1.getDataLength() > (Math.pow(2, (Signal.MAX_N - 1)))) {
            // TODO too long error
            cp.println("too long error");
            return;
        }

        double[] real_in1 = signal1.getRealData();
        double[] imag_in1 = signal1.getImagData();
        double[] real_in2 = signal2.getRealData();
        double[] imag_in2 = signal2.getImagData();
        int alloc_size = 2 * real_in1.length;
        double[] real_data_out = new double[alloc_size];
        double[] imag_data_out = new double[alloc_size];

        outputSignal.setDataType(Signal.COMP);
        outputSignal.setRecord(0);
        outputSignal.setChannel(0);
        outputSignal.setRealData(real_data_out);
        outputSignal.setImagData(imag_data_out);
        outputSignal.setDataLength(2 * signal1.getDataLength());

        int inputDataLength = signal1.getDataLength();
        outputSignal.setDataLength(2 * signal1.getDataLength()); /* Because of zeropad */

        int window_length = inputDataLength;
        int fft_length = outputSignal.getDataLength();
        cp.println(String.format("fft_length %d", fft_length));

        /* help pointers */
        double[] real_help1 = new double[alloc_size];
        double[] imag_help1 = new double[alloc_size];
        double[] real_help2 = new double[alloc_size];
        double[] imag_help2 = new double[alloc_size];

        /* Copy and Zeropad the input signals */
        for (int channel = 0; channel < signal1.getDataChannels(); channel++) {
            for (int record = 0; record < signal1.getDataRecords(); record++) {
                /* offset concerning record */
                int offset_in = (channel * signal1.getDataRecords() + record) * inputDataLength;
                int offset_out = 2 * offset_in;
                for (int i = 0; i < inputDataLength; i++) {
                    real_help1[i + offset_out] = real_in1[i + offset_in];
                    imag_help1[i + offset_out] = imag_in1[i + offset_in];
                    real_help2[i + offset_out] = real_in2[i + offset_in];
                    imag_help2[i + offset_out] = imag_in2[i + offset_in];
                }
                // default double value is 0.0.
            }
        }

        for (int channel = 0; channel < signal1.getDataChannels(); channel++) {
            for (int record = 0; record < signal1.getDataRecords(); record++) {
                /* Multiply the input data with a window and do the fft */
                int offset = (channel * signal1.getDataRecords() + record) * inputDataLength;
                Windowing.windowing(real_help1, imag_help1, real_help1, imag_help1, offset, window_length, windowing);
                Windowing.windowing(real_help2, imag_help2, real_help2, imag_help2, offset, window_length, windowing);

                Transformations.fft(real_help1, imag_help1, offset, fft_length, 1);
                Transformations.fft(real_help2, imag_help2, offset, fft_length, 1);
            }
        }

        if (convolution) {
            /* Multiply hulp1 with hulp2 */
            for (int i = 0; i < real_help1.length; i++) {
                double temp = real_help1[i];
                real_help1[i] = (real_help1[i] * real_help2[i] - imag_help1[i] * imag_help2[i]);
                imag_help1[i] = (temp * imag_help2[i] + imag_help1[i] * real_help2[i]);
            }
        } else {
            // Correlation. Multiply complex conjugate of hulp1 with hulp2;
            for (int i = 0; i < real_help1.length; i++) {
                double temp = real_help1[i];
                real_help1[i] = (real_help1[i] * real_help2[i] - (-imag_help1[i]) * imag_help2[i]);
                imag_help1[i] = (temp * imag_help2[i] + (-imag_help1[i]) * real_help2[i]);
            }
        }

        /* Do the inverse FFT of the result */
        for (int channel = 0; channel < signal1.getDataChannels(); channel++) {
            for (int record = 0; record < signal1.getDataRecords(); record++) {
                /* Do the ifft */
                int offset = (channel * signal1.getDataRecords() + record) * inputDataLength;
                Transformations.fft(real_help1, imag_help1, offset, fft_length, -1);
            }
        }

        /* Put the data in out_im */
        for (int i = 0; i < real_data_out.length; i++) {
            real_data_out[i] = real_help1[i];
            imag_data_out[i] = imag_help1[i];
        }
    }

    public Signal CorrelationCi(List<String> arguments, String command) throws SignalDoesNotExist {
        String signalname1 = cp.getString(arguments, "Signal", "a");
        String signalname2 = cp.getString(arguments, "Signal", "b");

        Signal signal1 = signals.get(signalname1); /* Find the correct signal */
        if (signal1 == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname1 + "\" does not exist.");
        }
        if (signal1.getDataDomain() != Signal.TIME) {
            // TODO domain error
            cp.println("domain error");
            return null;
        }
        Signal signal2 = signals.get(signalname2); /* Find the correct signal */
        if (signal2 == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname2 + "\" does not exist.");
        }
        if (signal2.getDataDomain() != Signal.TIME) {
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
        setSignalValues(outputSignal, signal1);

        int windowing = cp.getInt(arguments, "Window type", 0, 6, 0);
        convolutionCorrelation(signal1, signal2, windowing, outputSignal, false);
        return outputSignal;
    }

    public Signal ConvolutionCi(List<String> arguments, String command) throws SignalDoesNotExist {
        String signalname1 = cp.getString(arguments, "Signal", "a");
        String signalname2 = cp.getString(arguments, "Signal", "b");

        Signal signal1 = signals.get(signalname1); /* Find the correct signal */
        if (signal1 == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname1 + "\" does not exist.");
        }
        if (signal1.getDataDomain() != Signal.TIME) {
            // TODO domain error
            cp.println("domain error");
            return null;
        }
        Signal signal2 = signals.get(signalname2); /* Find the correct signal */
        if (signal2 == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname2 + "\" does not exist.");
        }
        if (signal2.getDataDomain() != Signal.TIME) {
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
        setSignalValues(outputSignal, signal1);

        int windowing = cp.getInt(arguments, "Window type", 0, 6, 0);
        convolutionCorrelation(signal1, signal2, windowing, outputSignal, true);
        return outputSignal;
    }

    public Signal ConvCorrCi(List<String> arguments, String command) throws SignalDoesNotExist {
        Signal outputSignal = null;

        Method method = null;
        String methodName = convcorr.get(command)[0];
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
