package signals;

import java.util.Map;

import console.CommandLineParser;

public class SourcesBase {
    protected final Map<String, Signal> signals;
    protected CommandLineParser cp;
    public static final int DEF_N = 9; /* 2^9 = 512 = default number of samples */

    public SourcesBase(Map<String, Signal> signals, CommandLineParser cp) {
        this.signals = signals;
        this.cp = cp;
    }

    protected void setSignalValues(Signal s, int length, int sampleRate, short dataType,
            String description) {
        s.setDate();
        s.setPixelFormat(Signal.DATA_PIXEL_FORMAT);
        s.setDataLength(length);
        s.setDataRecords(1);
        s.setDataFileSeq(Signal.DATA_FILE_SEQ);
        s.setDataBitsPerSample(Signal.BPS_SAMPLE);
        s.setDataChannels(1);
        s.setDataSampleRate(sampleRate);
        s.setDataDomain(Signal.TIME);
        s.setDataType(dataType);
        s.setDataIdString(Signal.ASM_ID_STRING);
        s.setDataUserText("DataUserText");
        s.setDataDescription(description);
        s.setHScale(1.0);
        s.setRecord(0);
        s.setChannel(0);
    }
}
