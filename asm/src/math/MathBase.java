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

    protected void setSignalValues(Signal outPutSignal, Signal inputSignal) {
        outPutSignal.setDate();
        outPutSignal.setPixelFormat(Signal.DATA_PIXEL_FORMAT);
        outPutSignal.setDataLength(inputSignal.getDataLength());
        outPutSignal.setDataRecords(inputSignal.getDataRecords());
        outPutSignal.setDataFileSeq(Signal.DATA_FILE_SEQ);
        outPutSignal.setDataBitsPerSample(Signal.BPS_SAMPLE);
        outPutSignal.setDataChannels(inputSignal.getDataChannels());
        outPutSignal.setDataSampleRate(inputSignal.getDataSampleRate());
        outPutSignal.setDataDomain(Signal.TIME);
        outPutSignal.setDataType(inputSignal.getDataType());
        outPutSignal.setDataIdString(Signal.ASM_ID_STRING);
        outPutSignal.setDataUserText("DataUserText");
        outPutSignal.setDataDescription(inputSignal.getDataDescription());
        outPutSignal.setMode(inputSignal.getMode());
        outPutSignal.setRecord(inputSignal.getRecord());
        outPutSignal.setChannel(inputSignal.getChannel());
        outPutSignal.setHScale(inputSignal.getHScale());
    }
}
