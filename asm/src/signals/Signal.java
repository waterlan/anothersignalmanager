package signals;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Signal {
    private SignalWindow window = null;
    public static final short DATA_PIXEL_FORMAT = 0;
    public static final short DATA_FILE_SEQ = 0;

    // Data domain
    public static final short TIME = 0;
    public static final short FREQ = 1;
    public static final short AMPL = 2;
    public static final short MAGN = 3;
    public static final short PHAS = 4;

    public static final short REAL = 0;
    public static final short IMAG = 1;
    public static final short COMP = 2;

    /* Mode of the signal to plot */
    public static final int REAL_M = 0;
    public static final int IMAG_M = 1;
    public static final int HIST_M = 2;
    public static final int MAGN_M = 3;
    public static final int PHAS_M = 4;
    public static final int BODE_M = 5;

    public static final short BPS_CHAR = 8;
    public static final short BPS_SHORT = 16;
    public static final short BPS_INT = 32;
    public static final short BPS_FLOAT = 3232;
    public static final short BPS_DOUBLE = 6464;
    public static final short BPS_SAMPLE = 6464;

    public static final int SAMPLE_RATE = 1024;
    public static final int MIN_N = 7; /* Minimal length of a record = 256 */
    public static final int MAX_N = 12; /* Maximal length of a record = 4096 */

    public static final String ASM_ID_STRING = "ASM 2.0";

    AsmHeader header;
    int Wlenx; /* x - dimension */
    int Wleny; /* y - dimension */
    int display_mode; /* don, <1-5> */
    double vscale = 1.0; /* vertikale scale factor */
    double hscale = 1.0; /* horizontale scale factor */
    int averageType = 0;
    double maximum; /* maximale waarde van een array */
    double minimum; /* minimale waarde van een array */
    byte[] windowName; /* window name */
    byte[] fileName;
    int log = 0; /* log axis ? */
    int record_nr = 0; /* number of displayed record */
    int channel_nr = 0; /* number of displayed channel */
    int Mode; /* window containes real,imag,bode,phase,magn */
    Boolean filled = false; /* window is used */
    int image; /* window handle */
    Diagram diagram; /* diagram structure */

    public Signal(String name) {
        this.windowName = new byte[40];
        this.fileName = new byte[256];
        this.header = new AsmHeader();
        this.diagram = new Diagram();
        setName(name);
    }

    public class AsmHeader {
        short pixel_format; /* 0 = unpacked 1 = packed */
        int length; /* Aantal samples per record */
        int n_channels; /* Aantal channels */
        short file_seq; /* File sequence number = 0 */
        short bits_p_samp; /* bps 8, 16, 32, 3232, 6464 */
        int n_records; /* Aantal records */
        short domain_id; /* 0=time, 1=freq, 2=ampl, 3=magnitude, 4= fase */
        short datatype_id; /* 0=real, 1=imaginair, 2= complex */
        double[] real_data; /* pointer to real data */
        double[] imag_data; /* pointer to imaginary data */
        int sample_rate; /* Sample-rate in 10 Hz */
        short[] reserved; /* Gereserveerd */
        double[] numerical;
        byte[] asm_id_string;
        byte[] signal_name;
        byte[] user_text;
        byte[] date;
        byte[] description;

        public AsmHeader() {
            reserved = new short[19];
            numerical = new double[24];
            asm_id_string = new byte[74];
            signal_name = new byte[32];
            user_text = new byte[46];
            date = new byte[30];
            description = new byte[74];
        }
    }

    public class Diagram {
        byte[] data;
        int length;
        int size;
    }

    /**
     * Convert a byte array with a fixed length to a string. The array may be longer
     * than the actual string length. The string stops at the first 0 byte, like a C
     * char array.
     * 
     * @param source
     * @return
     */

    private String arrayToString(byte[] source) {
        int len = 0;
        for (int i = 0; i < source.length; i++) {
            if (source[i] == '\0') {
                len = i;
                break;
            }
        }
        return new String(source, 0, len);
    }

    private void copyByteArray(byte[] target, String source) {
        int i;
        byte[] sourceByte = source.getBytes();
        for (i = 0; (i < sourceByte.length) && (i < target.length - 1); i++) {
            target[i] = sourceByte[i];
        }
        target[i] = '\0';
    }

    public String getName() {
        return arrayToString(header.signal_name);
    }

    public short getPixelFormat() {
        return header.pixel_format;
    }

    /**
     *
     * 
     * @return The number of samples per record.
     */
    public int getDataLength() {
        return header.length;
    }

    public String getDate() {
        return arrayToString(header.date);
    }

    public String getDataIdString() {
        return arrayToString(header.asm_id_string);
    }

    public String getDataUserText() {
        return arrayToString(header.user_text);
    }

    public String getDataDescription() {
        return arrayToString(header.description);
    }

    public double getHScale() {
        return this.hscale;
    }

    public double getVScale() {
        return this.vscale;
    }

    public int getAverageType() {
        return this.averageType;
    }

    public int getMode() {
        return this.Mode;
    }

    public int getWindowWidth() {
        return this.Wlenx;
    }

    public int getWindowHeight() {
        return this.Wleny;
    }

    public short getDataType() {
        return header.datatype_id;
    }

    public String getDataTypeToString() {
        String type;
        switch (header.datatype_id) {
        case Signal.REAL:
            type = "real";
            break;
        case Signal.IMAG:
            type = "imaginary";
            break;
        case Signal.COMP:
            type = "complex";
            break;
        default:
            type = "unknown";
            break;
        }
        return type;
    }

    public short getDataDomain() {
        return header.domain_id;
    }

    public short getDataFileSeq() {
        return header.file_seq;
    }

    public short getDataBitsPerSample() {
        return header.bits_p_samp;
    }

    public int getDataChannels() {
        return header.n_channels;
    }

    public int getDataSampleRate() {
        return header.sample_rate;
    }

    public double[] getRealData() {
        return header.real_data;
    }

    public double[] getImagData() {
        return header.imag_data;
    }

    public double getMinimum() {
        return this.minimum;
    }

    public double getMaximum() {
        return this.maximum;
    }

    public double getRealMaximum() {
        return header.numerical[0];
    }

    public double getImagMaximum() {
        return header.numerical[1];
    }

    public double getRealMinimum() {
        return header.numerical[2];
    }

    public double getImagMinimum() {
        return header.numerical[3];
    }

    public int getRecord() {
        return this.record_nr;
    }

    public int getChannel() {
        return this.channel_nr;
    }

    public int getDataRecords() {
        return header.n_records;
    }

    public int getLog() {
        return this.log;
    }

    public void setName(String name) {
        copyByteArray(header.signal_name, name);
    }

    public void setDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        copyByteArray(header.date, strDate);
    }

    public void setPixelFormat(short format) {
        header.pixel_format = format;
    }

    public void setDataLength(int length) {
        header.length = length;
    }

    public void setDataRecords(int records) {
        header.n_records = records;
    }

    public void setDataFileSeq(short file_seq) {
        header.file_seq = file_seq;
    }

    public void setDataBitsPerSample(short bps) {
        header.bits_p_samp = bps;
    }

    public void setDataChannels(int channels) {
        header.n_channels = channels;
    }

    public void setDataSampleRate(int sampleRate) {
        header.sample_rate = sampleRate;
    }

    public void setDataDomain(short domain) {
        header.domain_id = domain;
    }

    public void setDataType(short type) {
        header.datatype_id = type;
    }

    public void setDataIdString(String id) {
        copyByteArray(header.asm_id_string, id);
    }

    public void setDataUserText(String userText) {
        copyByteArray(header.user_text, userText);
    }

    public void setDataDescription(String description) {
        copyByteArray(header.description, description);
    }

    public void setHScale(double scale) {
        this.hscale = scale;
    }

    public void setRecord(int record) {
        this.record_nr = record;
    }

    public void setLog(int log) {
        this.log = log;
    }

    public void setChannel(int channel) {
        this.channel_nr = channel;
    }

    public void setAverageType(int type) {
        this.averageType = type;
    }

    public void setMode(int mode) {
        this.Mode = mode;
    }

    public void setRealData(double[] data) {
        header.real_data = data;
    }

    public void setImagData(double[] data) {
        header.imag_data = data;
    }

    public void setWindowWidth(int width) {
        this.Wlenx = width;
    }

    public void setWindowHeight(int height) {
        this.Wleny = height;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public void setRealMaximum(double max) {
        header.numerical[0] = max;
    }

    public void setImagMaximum(double max) {
        header.numerical[1] = max;
    }

    public void setRealMinimum(double min) {
        header.numerical[2] = min;
    }

    public void setImagMinimum(double min) {
        header.numerical[3] = min;
    }

    public void setVScale(double scale) {
        this.vscale = scale;
    }

    public SignalWindow getWindow() {
        return window;
    }

    public void setWindow(SignalWindow window) {
        this.window = window;
    }

    public void write(Path outputFile) throws IOException {

        try (OutputStream os = Files.newOutputStream(outputFile); DataOutputStream out = new DataOutputStream(os)) {
            out.writeShort(header.pixel_format);
            out.writeInt(header.length);
            out.writeInt(header.n_channels);
            out.writeShort(header.file_seq);
            out.writeShort(header.bits_p_samp);
            out.writeInt(header.n_records);
            out.writeShort(header.domain_id);
            out.writeShort(header.datatype_id);
            out.writeInt(header.sample_rate);
            for (short r : header.reserved)
                out.writeShort(r);
            for (double n : header.numerical)
                out.writeDouble(n);
            out.write(header.asm_id_string);
            out.write(header.signal_name);
            out.write(header.user_text);
            out.write(header.date);
            out.write(header.description);
            if (this.getDataType() == REAL || this.getDataType() == COMP) {
                for (int i = 0; i < header.real_data.length; i++) {
                    out.writeDouble(header.real_data[i]);
                }
            }
            if (this.getDataType() == IMAG || this.getDataType() == COMP) {
                for (int i = 0; i < header.imag_data.length; i++) {
                    out.writeDouble(header.imag_data[i]);
                }
            }
            out.flush();
        }
    }

    public void read(Path outputFile) throws IOException {

        try (InputStream is = Files.newInputStream(outputFile); DataInputStream in = new DataInputStream(is)) {
            this.setPixelFormat(in.readShort());
            this.setDataLength(in.readInt());
            this.setDataChannels(in.readInt());
            this.setDataFileSeq(in.readShort());
            this.setDataBitsPerSample(in.readShort());
            this.setDataRecords(in.readInt());
            this.setDataDomain(in.readShort());
            this.setDataType(in.readShort());
            this.setDataSampleRate(in.readInt());
            for (int i = 0; i < header.reserved.length; i++)
                header.reserved[i] = in.readShort();
            for (int i = 0; i < header.numerical.length; i++)
                header.numerical[i] = in.readDouble();
            in.read(header.asm_id_string);
            in.read(header.signal_name);
            in.read(header.user_text);
            in.read(header.date);
            in.read(header.description);
            int length = this.getDataLength()*this.getDataRecords()*this.getDataChannels();
            if (this.getDataType() == REAL || this.getDataType() == COMP) {
                double[] real_data = new double[length];
                for (int i = 0; i < length; i++) {
                    real_data[i] = in.readDouble();
                }
                this.setRealData(real_data);
            }
            if (this.getDataType() == IMAG || this.getDataType() == COMP) {
                double[] imag_data = new double[length];
                for (int i = 0; i < length; i++) {
                    imag_data[i] = in.readDouble();
                }
                this.setImagData(imag_data);
            }
            if (this.getDataType() == REAL) {
                double[] imag_data = new double[length];
                this.setImagData(imag_data);
            }
            if (this.getDataType() == IMAG) {
                double[] real_data = new double[length];
                this.setRealData(real_data);
            }
        }
    }
}
