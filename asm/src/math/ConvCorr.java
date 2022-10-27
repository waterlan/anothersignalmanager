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
import util.Util;

public class ConvCorr extends MathBase {

    public static final Map<String, String[]> convcorr = new HashMap<String, String[]>() {
        {
            put("convolution", new String[] {"ConvolutionCi",""});
            put("correlation", new String[] {"CorrelationCi",""});
        }
    };

    public ConvCorr(Map<String, Signal> signals, CommandLineParser cp) {
        super(signals, cp);
    }

    private void convolutionCorrelation(Signal signal1, Signal signal2, int windowing, Signal outputSignal, boolean convolution) {

        //if ((DataDomain(vp[in_im1]) != REAL) || (DataDomain(vp[in_im2]) != REAL))
        //    return ERR_DOMAIN;    /*verkeerde domein  */

        if ( (signal1.getDataLength() != signal2.getDataLength()) || (signal1.getDataRecords() != signal2.getDataRecords()) ||
            (signal1.getDataChannels() != signal2.getDataChannels()) ) {
            // TODO size error.
            cp.println("size error");
            return;
        }

        if(signal1.getDataLength() > (Math.pow(2,(Signal.MAX_N-1)))) {
            // TODO too long error
            cp.println("too long error");
            return ;
        }

        double [] real_in1 = signal1.getRealData();
        double [] imag_in1 = signal1.getImagData();
        double [] real_in2 = signal2.getRealData();
        double [] imag_in2 = signal2.getImagData();
        int alloc_size = 2*real_in1.length;
        double [] real_data_out = new double[alloc_size];
        double [] imag_data_out = new double[alloc_size];

        outputSignal.setDataType(Signal.COMP);
        outputSignal.setRecord(0);
        outputSignal.setChannel(0);
        outputSignal.setRealData(real_data_out);
        outputSignal.setImagData(imag_data_out);
        outputSignal.setDataLength(2*signal1.getDataLength());

        int inputDataLength = signal1.getDataLength();
        outputSignal.setDataLength(2*signal1.getDataLength()); /* Vanwege zeropad */

        int window_lengte = inputDataLength;
        int fft_lengte = outputSignal.getDataLength();
        cp.println(String.format("fft_length %d", fft_lengte));

        /* Vraag geheugen aan voor de hulp pointers */
        double [] real_hulp1 = new double[alloc_size];
        double [] imag_hulp1 = new double[alloc_size];
        double [] real_hulp2 = new double[alloc_size];
        double [] imag_hulp2 = new double[alloc_size];
        
        /* Copieer en Zeropad de ingangs signalen */
        for (int channel = 0;channel < signal1.getDataChannels(); channel++)
        {
            for (int record = 0;record < signal1.getDataRecords(); record++)
            {
                /* offset betreffende record */
                int offset_in = (channel*signal1.getDataRecords() + record)*inputDataLength;
                int offset_out = 2*offset_in;
                for (int i=0;i < inputDataLength;i++)
                {
                    real_hulp1[i+offset_out] = real_in1[i+offset_in];
                    imag_hulp1[i+offset_out] = imag_in1[i+offset_in];
                    real_hulp2[i+offset_out] = real_in2[i+offset_in];
                    imag_hulp2[i+offset_out] = imag_in2[i+offset_in];
                }
                // default double value is 0.0.
            }
        }

        for (int channel=0; channel < signal1.getDataChannels(); channel++)
        {
            for (int record=0; record < signal1.getDataRecords(); record++)
            {
                /* Vermenigvuldig de ingangsdata met een window en doe de fft */
                int offset = (channel*signal1.getDataRecords() + record)*inputDataLength;
                Windowing.windowing(real_hulp1,imag_hulp1,real_hulp1,imag_hulp1,offset,window_lengte,windowing);
                Windowing.windowing(real_hulp2,imag_hulp2,real_hulp2,imag_hulp2,offset,window_lengte,windowing);

                Transformations.fft(real_hulp1,imag_hulp1,offset,fft_lengte,1);
                Transformations.fft(real_hulp2,imag_hulp2,offset,fft_lengte,1);
            }
        }

        if (convolution) {
            /* Multiply hulp1 with hulp2 */
            for (int i=0;i < real_hulp1.length;i++) {
                double temp = real_hulp1[i];
                real_hulp1[i] = (real_hulp1[i]*real_hulp2[i] - imag_hulp1[i]*imag_hulp2[i]);
                imag_hulp1[i] = (temp*imag_hulp2[i] + imag_hulp1[i]*real_hulp2[i]);
            }
        } else {
            // Correlation. Multiply complex conjugate of hulp1 with hulp2;
            for (int i=0;i < real_hulp1.length;i++) {
                double temp = real_hulp1[i];
                real_hulp1[i] = (real_hulp1[i]*real_hulp2[i] - (-imag_hulp1[i])*imag_hulp2[i]);
                imag_hulp1[i] = (temp*imag_hulp2[i] + (-imag_hulp1[i])*real_hulp2[i]);
            }
        }

        /* Doe de inverse FFT van het resultaat */
        for (int channel=0; channel < signal1.getDataChannels(); channel++)
        {
            for (int record=0; record < signal1.getDataRecords(); record++)
            {
                /* Doe de ifft */
                int offset = (channel*signal1.getDataRecords() + record)*inputDataLength;
                Transformations.fft(real_hulp1,imag_hulp1,offset,fft_lengte,-1);
            }
        }

        /* Zet de data in out_im */
        for (int i=0;i < real_data_out.length;i++)
        {
            real_data_out[i] = real_hulp1[i];
            imag_data_out[i] = imag_hulp1[i];
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
