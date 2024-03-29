package math;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import console.CommandLineParser;
import exceptions.IncompatibleData;
import exceptions.SignalDoesNotExist;
import exceptions.WrongDomain;
import signals.ComplexArray;
import signals.Signal;

public class Calculations extends MathBase {
    public static final Map<String, String[]> calculations = new HashMap<String, String[]>() {
        /**
         * 
         */
        private static final long serialVersionUID = 6926951807167296484L;

        {
            put("absolute", new String[] { "twoInputsOneOutputCi", "<input1> <input2> <output>" });
            put("add", new String[] { "twoInputsOneOutputCi", "<input1> <input2> <output>" });
            put("assign", new String[] { "assign", "<input> <real-value> <imag-value>" });
            put("cabs", new String[] { "oneInputOneOutputCi", "<input> <output>" });
            put("cadd", new String[] { "oneInputOneValueOneOutputCi", "<input> <constant> <output>" });
            put("cdivide", new String[] { "oneInputOneValueOneOutputCi", "<input> <constant> <output>" });
            put("clear", new String[] { "clear", "<input>" });
            put("clip", new String[] { "clip",
                    "<input> <left> <right> <output> (time)\nclip <input> <left freq> <right freq> <attenuation> <output> (freq)" });
            put("cmultiply", new String[] { "oneInputOneValueOneOutputCi", "<input> <constant> <output>" });
            put("conjugate", new String[] { "oneInputOneOutputCi", "<input> <output>" });
            put("copy", new String[] { "oneInputOneOutputCi", "<input> <output>" });
            put("cosine", new String[] { "oneInputOneOutputCi", "<input> <output>" });
            put("divide", new String[] { "twoInputsOneOutputCi", "<input1> <input2> <output>" });
            put("epow", new String[] { "oneInputOneOutputCi", "<input> <output>" });
            put("inv", new String[] { "oneInputOneOutputCi", "<input> <output>" });
            put("ln", new String[] { "oneInputOneOutputCi", "<input> <output>" });
            put("log", new String[] { "oneInputOneOutputCi", "<input> <output>" });
            put("maximum", new String[] { "twoInputsOneOutputCi", "<input1> <input2> <output>" });
            put("minimum", new String[] { "twoInputsOneOutputCi", "<input1> <input2> <output>" });
            put("multiply", new String[] { "multiply", "<input1> <input2> <constant> <output>" });
            put("rotate", new String[] { "oneInputOneValueOneOutputCi", "<input> <rotate> <output>" });
            put("shift", new String[] { "oneInputOneValueOneOutputCi", "<input> <shift> <output>" });
            put("sine", new String[] { "oneInputOneOutputCi", "<input> <output>" });
            put("subtract", new String[] { "twoInputsOneOutputCi", "<input1> <input2> <output>" });
            put("tenpow", new String[] { "oneInputOneOutputCi", "<input> <output>" });
            put("zeropad", new String[] { "oneInputOneOutputCi", "<input> <output>" });
        }
    };

    public Calculations(Map<String, Signal> signals, CommandLineParser cp) {
        super(signals, cp);
    }

    private void clear(Signal signal) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();

        for (int i = 0; i < length; i++) {
            re[i] = im[i] = 0;
        }
    }

    public Signal clear(List<String> arguments, String command) throws SignalDoesNotExist {
        String signalname = cp.getString(arguments, "Signal", command);

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        clear(signal);
        return signal;
    }

    private void assign(Signal signal, double realValue, double imagValue) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();

        for (int i = 0; i < length; i++) {
            re[i] = realValue;
            im[i] = imagValue;
        }
    }

    public Signal assign(List<String> arguments, String command) throws SignalDoesNotExist {
        String signalname = cp.getString(arguments, "Signal", command);
        double realValue = cp.getDouble(arguments, "Real part", Integer.MIN_VALUE, Integer.MAX_VALUE, 1.0);
        double imagValue = cp.getDouble(arguments, "Imag part", Integer.MIN_VALUE, Integer.MAX_VALUE, 1.0);

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }

        if (signal.getDataType() == Signal.REAL)
            imagValue = 0.0;
        else if (signal.getDataType() == Signal.IMAG)
            realValue = 0.0;

        assign(signal, realValue, imagValue);
        return signal;
    }

    public void multiply(Signal signal1, Signal signal2, double constant, Signal outputSignal) {
        double[] re1 = signal1.getRealData();
        double[] im1 = signal1.getImagData();
        double[] re2 = signal2.getRealData();
        double[] im2 = signal2.getImagData();
        int length = signal1.getDataChannels() * signal1.getDataRecords() * signal1.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = (re1[i] * re2[i] - im1[i] * im2[i]) / constant;
            data.im[i] = (re1[i] * im2[i] + im1[i] * re2[i]) / constant;
        }
    }

    public Signal multiply(List<String> arguments, String command) throws SignalDoesNotExist {
        String signalname1 = cp.getString(arguments, "Signal", "a");
        String signalname2 = cp.getString(arguments, "Signal", "b");
        double constant = cp.getDouble(arguments, "Constant", 1, Integer.MAX_VALUE, 1.0);
        String outputSignalName = cp.getString(arguments, "Signal", command);

        Signal signal1 = signals.get(signalname1); /* Find the correct signal */
        if (signal1 == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname1 + "\" does not exist.");
        }
        Signal signal2 = signals.get(signalname2); /* Find the correct signal */
        if (signal2 == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname2 + "\" does not exist.");
        }
        if (signal1.getDataDomain() != signal2.getDataDomain()) {
            return null;
        }
        if (signal1.getDataLength() != signal2.getDataLength() || signal1.getDataRecords() != signal2.getDataRecords()
                || signal1.getDataChannels() != signal2.getDataChannels()) {
            return null;
        }
        Signal outputSignal = signals.get(outputSignalName);
        if (outputSignal == null) /* Signal did not exist yet */
        {
            outputSignal = new Signal(outputSignalName);
            signals.put(outputSignalName, outputSignal);
        }

        setSignalValues(outputSignal, signal1);
        if (signal1.getDataType() != signal2.getDataType()) {
            outputSignal.setDataType(Signal.COMP);
        }

        multiply(signal1, signal2, constant, outputSignal);

        return outputSignal;
    }

    public void cabs(Signal signal, Signal outputSignal) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = Math.sqrt(Math.pow(re[i], 2.0) + Math.pow(im[i], 2.0));
            data.im[i] = 0.0;
        }
    }

    public void conjugate(Signal signal, Signal outputSignal) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = re[i];
            data.im[i] = -im[i];
        }
    }

    public void cosine(Signal signal, Signal outputSignal) {
        double[] re = signal.getRealData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = Math.cos(re[i]);
            data.im[i] = 0.0;
        }
    }

    public void epow(Signal signal, Signal outputSignal) {
        double[] re = signal.getRealData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = Math.exp(re[i]);
            data.im[i] = 0.0;
        }
    }

    public void copy(Signal signal, Signal outputSignal) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = re[i];
            data.im[i] = im[i];
        }
        copySignalValues(outputSignal, signal);
    }

    public void inv(Signal signal, Signal outputSignal) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = -re[i];
            data.im[i] = -im[i];
        }
    }

    public void ln(Signal signal, Signal outputSignal) {
        double[] re = signal.getRealData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = Math.log(re[i]);
            data.im[i] = 0.0;
        }
    }

    public void log(Signal signal, Signal outputSignal) {
        double[] re = signal.getRealData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = Math.log10(re[i]);
            data.im[i] = 0.0;
        }
    }

    public void sine(Signal signal, Signal outputSignal) {
        double[] re = signal.getRealData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = Math.sin(re[i]);
            data.im[i] = 0.0;
        }
    }

    public void tenpow(Signal signal, Signal outputSignal) {
        double[] re = signal.getRealData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = Math.pow(10.0, re[i]);
            data.im[i] = 0.0;
        }
    }

    public void zeropad(Signal signal, Signal outputSignal) {
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        if (length > Math.pow(2, Signal.MAX_N)) {
            // TODO max length error
        }
        // TODO zeropad per record.

        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        ComplexArray data = new ComplexArray(2*length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = re[i];
            data.im[i] = im[i];
        }
        outputSignal.setDataLength((short) (2 * signal.getDataLength()));
    }

    public Signal oneInputOneOutputCi(List<String> arguments, String command) throws SignalDoesNotExist, WrongDomain {
        String signalname = cp.getString(arguments, "Signal", "a");
        String outputSignalName = cp.getString(arguments, "Signal", command);

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        if (command.equals("zeropad") && signal.getDataDomain() != Signal.TIME) {
            throw new WrongDomain(String.format("Command \"%s\" can't operate on signal \"%s\" of domain %s.", command,
                    signalname, signal.getDataDomainToString()));
        }

        Signal outputSignal = signals.get(outputSignalName);
        if (outputSignal == null) /* Signal did not exist yet */
        {
            outputSignal = new Signal(outputSignalName);
            signals.put(outputSignalName, outputSignal);
        }
        setSignalValues(outputSignal, signal);

        Method method = null;
        String methodName = command;
        try {
            method = this.getClass().getMethod(methodName, Signal.class, Signal.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (method == null)
            return null;

        try {
            method.invoke(this, signal, outputSignal);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return outputSignal;
    }

    public void cadd(Signal signal, Signal outputSignal, double value) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = re[i] + value;
            data.im[i] = im[i];
        }
    }

    public void cdivide(Signal signal, Signal outputSignal, double value) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = re[i] / value;
            data.im[i] = im[i] / value;
        }
    }

    public void cmultiply(Signal signal, Signal outputSignal, double value) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = re[i] * value;
            data.im[i] = im[i] * value;
        }
    }

    public void rotate(Signal signal, Signal outputSignal, double value) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        int channelLength = signal.getDataRecords() * signal.getDataLength();
        int rotate = (int) value;
        rotate = rotate % channelLength;
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        // Rotate per channel.
        for (int channel = 0; channel < signal.getDataChannels(); channel++) {
            int chan_offset = channel * channelLength;
            for (int i = 0; i < channelLength; i++) {

                if (rotate >= 0) {
                    if (i < rotate) {
                        data.re[i + chan_offset] = re[channelLength - rotate + i + chan_offset];
                        data.im[i + chan_offset] = im[channelLength - rotate + i + chan_offset];
                    }
                    if (i >= rotate) {
                        data.re[i + chan_offset] = re[i - rotate + chan_offset];
                        data.im[i + chan_offset] = im[i - rotate + chan_offset];
                    }
                }
                if (rotate < 0) {
                    if (i >= channelLength + rotate) {
                        data.re[i + chan_offset] = re[i - (channelLength + rotate) + chan_offset];
                        data.im[i + chan_offset] = im[i - (channelLength + rotate) + chan_offset];
                    }
                    if (i < channelLength + rotate) {
                        data.re[i + chan_offset] = re[i - rotate + chan_offset];
                        data.im[i + chan_offset] = im[i - rotate + chan_offset];
                    }
                }
            }
        }
    }

    public void shift(Signal signal, Signal outputSignal, double value) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int length = signal.getDataChannels() * signal.getDataRecords() * signal.getDataLength();
        int channelLength = signal.getDataRecords() * signal.getDataLength();
        int shift = (int) value;
        shift = shift % channelLength;
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        // Shift per channel.
        for (int channel = 0; channel < signal.getDataChannels(); channel++) {
            int chan_offset = channel * channelLength;
            for (int i = 0; i < channelLength; i++) {

                if (shift >= 0) {
                    if (i < shift) {
                        data.re[i + chan_offset] = 0;
                        data.im[i + chan_offset] = 0;
                    }
                    if (i >= shift) {
                        data.re[i + chan_offset] = re[i - shift + chan_offset];
                        data.im[i + chan_offset] = im[i - shift + chan_offset];
                    }
                }
                if (shift < 0) {
                    if (i >= channelLength + shift) {
                        data.re[i + chan_offset] = 0;
                        data.im[i + chan_offset] = 0;
                    }
                    if (i < channelLength + shift) {
                        data.re[i + chan_offset] = re[i - shift + chan_offset];
                        data.im[i + chan_offset] = im[i - shift + chan_offset];
                    }
                }
            }
        }
    }

    public Signal oneInputOneValueOneOutputCi(List<String> arguments, String command) throws SignalDoesNotExist {
        String signalname = cp.getString(arguments, "Signal", "a");
        double defaultValue;
        if (command.equals("cadd"))
            defaultValue = 0.0;
        else
            defaultValue = 1.0;
        double minValue;
        if (command.equals("cdivide"))
            minValue = 1.0;
        else
            minValue = Integer.MIN_VALUE;
        double value = cp.getDouble(arguments, "Constant", minValue, Integer.MAX_VALUE, defaultValue);
        String outputSignalName = cp.getString(arguments, "Signal", command);

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        Signal outputSignal = signals.get(outputSignalName);
        if (outputSignal == null) /* Signal did not exist yet */
        {
            outputSignal = new Signal(outputSignalName);
            signals.put(outputSignalName, outputSignal);
        }
        setSignalValues(outputSignal, signal);

        Method method = null;
        String methodName = command;
        try {
            method = this.getClass().getMethod(methodName, Signal.class, Signal.class, double.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (method == null)
            return null;

        try {
            method.invoke(this, signal, outputSignal, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return outputSignal;
    }

    public void clip(Signal signal, int left, int right, Signal outputSignal) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int dataLength = signal.getDataLength();
        int length = signal.getDataChannels() * signal.getDataRecords() * dataLength;
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        // Clip per channel.
        for (int channel = 0; channel < signal.getDataChannels(); channel++) {
            {
                for (int record = 0; record < signal.getDataRecords(); record++) {
                    /* Calculate offset of the concerning record */
                    int offset = (channel * signal.getDataRecords() + record) * dataLength;

                    for (int i = 0; i < dataLength; i++) {
                        if ((i < left) || (i > right)) {
                            data.re[i + offset] = 0.0;
                            data.im[i + offset] = 0.0;
                        } else {
                            data.re[i + offset] = re[i + offset];
                            data.im[i + offset] = im[i + offset];
                        }
                    }

                }
            }
        }
    }

    public void clip(Signal signal, double leftfreq, double rightfreq, double attenuation, Signal outputSignal) {
        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int dataLength = signal.getDataLength();
        int length = signal.getDataChannels() * signal.getDataRecords() * dataLength;
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        attenuation = Math.pow(10.0, attenuation / 10);
        int left = (int) ((leftfreq / (double) signal.getDataSampleRate()) * (double) dataLength);
        int right = (int) ((rightfreq / (double) signal.getDataSampleRate()) * (double) dataLength);
        if (left < 0.0)
            left = 0;

        for (int channel = 0; channel < signal.getDataChannels(); channel++) {

            for (int record = 0; record < signal.getDataRecords(); record++) {
                /* Calculate offset of the concerning record */
                int offset = (channel * signal.getDataRecords() + record) * dataLength;
                for (int i = 0; i < dataLength; i++) {
                    if ((i < left) || ((i > right) && (i < (dataLength - right - 1)))
                            || (i > (dataLength - left - 1))) {
                        data.re[i + offset] = re[i + offset] / attenuation;
                        data.im[i + offset] = im[i + offset] / attenuation;
                    } else {
                        data.re[i + offset] = re[i + offset];
                        data.im[i + offset] = im[i + offset];
                    }
                }
            }
        }
    }

    public Signal clip(List<String> arguments, String command) throws SignalDoesNotExist, WrongDomain {
        String signalname = cp.getString(arguments, "Signal", "a");

        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }
        if (signal.getDataDomain() != Signal.TIME && signal.getDataDomain() != Signal.FREQ) {
            throw new WrongDomain(String.format("Command \"%s\" can't operate on signal \"%s\" of domain %s.", command,
                    signalname, signal.getDataDomainToString()));
        }
        int left = 0, right = signal.getDataLength();
        double leftfreq = 1.0, rightfreq = signal.getDataSampleRate() / 2;
        double attenuation = 50.0;
        if (signal.getDataDomain() == Signal.FREQ) {
            double maxValue = signal.getDataSampleRate() / 2;
            leftfreq = cp.getDouble(arguments, "left (frequency)", 1.0, maxValue, 1.0);
            rightfreq = cp.getDouble(arguments, "right (frequency)", leftfreq, maxValue, maxValue);
            attenuation = cp.getDouble(arguments, "attenuation", 0.0, 100.0, 50.0);
        } else {
            int maxValue = signal.getDataLength();
            left = cp.getInt(arguments, "left (element)", 0, maxValue, 0);
            right = cp.getInt(arguments, "right (element)", 0, maxValue, 0);
            // The GUI dialog passes attenuation. Command line not.
            if (arguments.size() > 1) {
                // dummy read.
                attenuation = cp.getDouble(arguments, "attenuation", 0.0, 100.0, 50.0);
            }
        }
        String outputSignalName = cp.getString(arguments, "Signal", command);

        Signal outputSignal = signals.get(outputSignalName);
        if (outputSignal == null) /* Signal did not exist yet */
        {
            outputSignal = new Signal(outputSignalName);
            signals.put(outputSignalName, outputSignal);
        }
        setSignalValues(outputSignal, signal);

        if (signal.getDataDomain() == Signal.FREQ) {
            clip(signal, leftfreq, rightfreq, attenuation, outputSignal);
            outputSignal.setDataDomain(Signal.FREQ);
        } else {
            clip(signal, left, right, outputSignal);
        }
        return outputSignal;
    }

    public void abs(Signal signal1, Signal signal2, Signal outputSignal) {
        double[] re1 = signal1.getRealData();
        double[] im1 = signal1.getImagData();
        double[] re2 = signal2.getRealData();
        double[] im2 = signal2.getImagData();
        int length = signal1.getDataChannels() * signal1.getDataRecords() * signal1.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = Math.sqrt(Math.pow(re1[i] - re2[i], 2.0) + Math.pow(im1[i] - im2[i], 2.0));
            data.im[i] = 0.0;
        }
    }

    public void add(Signal signal1, Signal signal2, Signal outputSignal) {
        double[] re1 = signal1.getRealData();
        double[] im1 = signal1.getImagData();
        double[] re2 = signal2.getRealData();
        double[] im2 = signal2.getImagData();
        int length = signal1.getDataChannels() * signal1.getDataRecords() * signal1.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = re1[i] + re2[i];
            data.im[i] = im1[i] + im2[i];
        }
    }

    public void divide(Signal signal1, Signal signal2, Signal outputSignal) {
        double[] re1 = signal1.getRealData();
        double[] im1 = signal1.getImagData();
        double[] re2 = signal2.getRealData();
        double[] im2 = signal2.getImagData();
        int length = signal1.getDataChannels() * signal1.getDataRecords() * signal1.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = Math.sqrt(Math.pow(re1[i], 2.0) + Math.pow(im1[i], 2.0))
                    / (1.0 + Math.sqrt(Math.pow(re2[i], 2.0) + Math.pow(im2[i], 2.0)));
            data.im[i] = 0.0;
        }
    }

    public void maximum(Signal signal1, Signal signal2, Signal outputSignal) {
        double[] re1 = signal1.getRealData();
        double[] im1 = signal1.getImagData();
        double[] re2 = signal2.getRealData();
        double[] im2 = signal2.getImagData();
        int length = signal1.getDataChannels() * signal1.getDataRecords() * signal1.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            if ((Math.pow(re1[i], 2.0) + Math.pow(im1[i], 2.0)) >= (Math.pow(re2[i], 2.0) + Math.pow(im2[i], 2.0))) {
                data.re[i] = re1[i];
                data.im[i] = im1[i];
            } else {
                data.re[i] = re2[i];
                data.im[i] = im2[i];
            }
        }
    }

    public void minimum(Signal signal1, Signal signal2, Signal outputSignal) {
        double[] re1 = signal1.getRealData();
        double[] im1 = signal1.getImagData();
        double[] re2 = signal2.getRealData();
        double[] im2 = signal2.getImagData();
        int length = signal1.getDataChannels() * signal1.getDataRecords() * signal1.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            if ((Math.pow(re1[i], 2.0) + Math.pow(im1[i], 2.0)) < (Math.pow(re2[i], 2.0) + Math.pow(im2[i], 2.0))) {
                data.re[i] = re1[i];
                data.im[i] = im1[i];
            } else {
                data.re[i] = re2[i];
                data.im[i] = im2[i];
            }
        }
    }

    public void subtract(Signal signal1, Signal signal2, Signal outputSignal) {
        double[] re1 = signal1.getRealData();
        double[] im1 = signal1.getImagData();
        double[] re2 = signal2.getRealData();
        double[] im2 = signal2.getImagData();
        int length = signal1.getDataChannels() * signal1.getDataRecords() * signal1.getDataLength();
        ComplexArray data = new ComplexArray(length);
        outputSignal.setData(data);

        for (int i = 0; i < length; i++) {
            data.re[i] = re1[i] - re2[i];
            data.im[i] = im1[i] - im2[i];
        }
    }

    public Signal twoInputsOneOutputCi(List<String> arguments, String command)
            throws SignalDoesNotExist, IncompatibleData {
        String signalname1 = cp.getString(arguments, "Signal", "a");
        String signalname2 = cp.getString(arguments, "Signal", "b");
        String outputSignalName = cp.getString(arguments, "Signal", command);

        Signal signal1 = signals.get(signalname1); /* Find the correct signal */
        if (signal1 == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname1 + "\" does not exist.");
        }
        Signal signal2 = signals.get(signalname2); /* Find the correct signal */
        if (signal2 == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname2 + "\" does not exist.");
        }
        if (signal1.getDataDomain() != signal2.getDataDomain()) {
            throw new IncompatibleData(
                    String.format("Signal \"%s\" and \"%s\" have different domain.", signalname1, signalname2));
        }
        if (!signal1.equalSize(signal2)) {
            throw new IncompatibleData(
                    String.format("Signal \"%s\" and \"%s\" have different size.", signalname1, signalname2));
        }
        if (signal1.getDataLength() != signal2.getDataLength() || signal1.getDataRecords() != signal2.getDataRecords()
                || signal1.getDataChannels() != signal2.getDataChannels()) {
            return null;
        }
        Signal outputSignal = signals.get(outputSignalName);
        if (outputSignal == null) /* Signal did not exist yet */
        {
            outputSignal = new Signal(outputSignalName);
            signals.put(outputSignalName, outputSignal);
        }

        setSignalValues(outputSignal, signal1);
        if (signal1.getDataType() != signal2.getDataType()) {
            outputSignal.setDataType(Signal.COMP);
        }

        Method method = null;
        String methodName = command;
        try {
            method = this.getClass().getMethod(methodName, Signal.class, Signal.class, Signal.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (method == null)
            return null;

        try {
            method.invoke(this, signal1, signal2, outputSignal);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return outputSignal;
    }

    public Signal CalculateCi(List<String> arguments, String command)
            throws SignalDoesNotExist, IncompatibleData, WrongDomain {
        Signal outputSignal = null;

        Method method = null;
        String methodName = calculations.get(command)[0];
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
            else if (e.getCause() instanceof IncompatibleData)
                throw new IncompatibleData(e.getCause());
            else if (e.getCause() instanceof WrongDomain)
                throw new WrongDomain(e.getCause());
            else
                e.printStackTrace();
        }

        return outputSignal;
    }
}
