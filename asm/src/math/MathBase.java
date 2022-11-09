package math;

import java.util.Map;

import console.CommandLineParser;
import signals.Signal;

public class MathBase {
    protected final Map<String, Signal> signals;
    protected CommandLineParser cp;

    public MathBase(Map<String, Signal> signals, CommandLineParser cp) {
        this.signals = signals;
        this.cp = cp;
    }

    protected void setSignalValues(Signal outputSignal, Signal inputSignal) {
        outputSignal.setDate();
        outputSignal.setPixelFormat(Signal.DATA_PIXEL_FORMAT);
        outputSignal.setDataLength(inputSignal.getDataLength());
        outputSignal.setDataRecords(inputSignal.getDataRecords());
        outputSignal.setDataFileSeq(Signal.DATA_FILE_SEQ);
        outputSignal.setDataBitsPerSample(Signal.BPS_SAMPLE);
        outputSignal.setDataChannels(inputSignal.getDataChannels());
        outputSignal.setDataSampleRate(inputSignal.getDataSampleRate());
        outputSignal.setDataDomain(Signal.TIME);
        outputSignal.setDataType(inputSignal.getDataType());
        outputSignal.setDataIdString(Signal.ASM_ID_STRING);
        outputSignal.setDataUserText("DataUserText");
        outputSignal.setDataDescription(inputSignal.getDataDescription());
        outputSignal.setMode(inputSignal.getMode());
        outputSignal.setRecord(inputSignal.getRecord());
        outputSignal.setChannel(inputSignal.getChannel());
        outputSignal.setHScale(inputSignal.getHScale());
    }

    protected void copySignalValues(Signal outputSignal, Signal inputSignal) {
        outputSignal.setPixelFormat(inputSignal.getPixelFormat());
        outputSignal.setDataFileSeq(inputSignal.getDataFileSeq());
        outputSignal.setDataBitsPerSample(inputSignal.getDataBitsPerSample());
        outputSignal.setDataDomain(inputSignal.getDataDomain());
        outputSignal.setDataIdString(inputSignal.getDataIdString());
        outputSignal.setDataUserText(inputSignal.getDataUserText());
    }
}
