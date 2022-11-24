package signals;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import console.CommandLineParser;
import exceptions.SignalDoesNotExist;

public class PlotCommands {
    protected final Map<String, Signal> signals;
    protected CommandLineParser cp;

    public static final Map<String, String[]> plotcommands = new HashMap<String, String[]>() {
        {
            put("bode", new String[] { "BodeCi", "<name> <channel> <record>" });
            put("display", new String[] { "DisplayCi", "<name> <channel> <record>" });
            put("imaginary", new String[] { "ImagCi", "<name> <channel> <record>" });
            put("info", new String[] { "InfoCi", "<name>" });
            put("list", new String[] { "ListCi", "" });
            put("print", new String[] { "PrintCi", "<name> <channel> <record>" });
            put("real", new String[] { "RealCi", "<name> <channel> <record>" });
            put("rename", new String[] { "RenameCi", "<name> <new-name>" });
            put("readf", new String[] { "ReadCi", "<file-name> <new-name>" });
            put("signaldir", new String[] { "SignaldirCi", "<directory-name>" });
            put("writef", new String[] { "WriteCi", "<name> <file-name> <user-text> <description>" });
            put("xscale", new String[] { "XscaleCi", "<name> <scale-factor>" });
        }
    };

    public PlotCommands(Map<String, Signal> signals, CommandLineParser cp) {
        this.signals = signals;
        this.cp = cp;
    }

    public Signal DisplayCi(List<String> arguments, Signal signal) {
        int channel = cp.getInt(arguments, "Channel", 0, signal.getDataChannels() - 1, 0);
        int record = cp.getInt(arguments, "Record", 0, signal.getDataRecords() - 1, 0);
        signal.setChannel(channel);
        signal.setRecord(record);
        return signal;
    }

    public Signal RealCi(List<String> arguments, Signal signal) {
        if (!(signal.getDataDomain() == Signal.TIME || signal.getDataDomain() == Signal.FREQ)) {
            // TODO domain error
            cp.println("domain error");
            return null;
        }
        if (signal.getDataType() == Signal.IMAG) {
            cp.println("No real part present.");
            return null;
        }

        int channel = cp.getInt(arguments, "Channel", 0, signal.getDataChannels() - 1, 0);
        int record = cp.getInt(arguments, "Record", 0, signal.getDataRecords() - 1, 0);
        signal.setChannel(channel);
        signal.setRecord(record);
        signal.setMode(Signal.REAL_M);
        return signal;
    }

    public Signal ImagCi(List<String> arguments, Signal signal) {
        if (!(signal.getDataDomain() == Signal.TIME || signal.getDataDomain() == Signal.FREQ)) {
            // TODO domain error
            cp.println("domain error");
            return null;
        }
        if (signal.getDataType() == Signal.REAL) {
            cp.println("No imaginary part present.");
            return null;
        }

        int channel = cp.getInt(arguments, "Channel", 0, signal.getDataChannels() - 1, 0);
        int record = cp.getInt(arguments, "Record", 0, signal.getDataRecords() - 1, 0);
        signal.setChannel(channel);
        signal.setRecord(record);
        signal.setMode(Signal.IMAG_M);
        return signal;
    }

    public Signal BodeCi(List<String> arguments, Signal signal) {
        if (signal.getDataDomain() != Signal.FREQ) {
            // TODO domain error
            cp.println("domain error");
            return null;
        }

        int channel = cp.getInt(arguments, "Channel", 0, signal.getDataChannels() - 1, 0);
        int averageType = cp.getInt(arguments, "Average type", 0, 1, 0);
        signal.setChannel(channel);
        signal.setAverageType(averageType);
        signal.setMode(Signal.BODE_M);
        return signal;
    }

    public Signal XscaleCi(List<String> arguments, Signal signal) {
        double hscale = cp.getDouble(arguments, "Scalefactor", 0, 10, 1);
        if (hscale == 0.0) {
            cp.println("Illegal scale value 0.0");
            return null;
        }
        if (signal.getWindow().canRender(hscale))
            signal.setHScale(hscale);
        else
            cp.println("Scale factor too high.");
        return signal;
    }

    public Signal InfoCi(List<String> arguments, Signal signal) {
        cp.println(String.format("PixelFormat        : %d", signal.getPixelFormat()));
        cp.println(String.format("RecordLength       : %d", signal.getDataLength()));
        cp.println(String.format("Number of Records  : %d", signal.getDataRecords()));
        cp.println(String.format("Number of Channels : %d", signal.getDataChannels()));
        cp.println(String.format("Fileseq. Nr.       : %d", signal.getDataFileSeq()));
        cp.println(String.format("Bits per sample    : %d", signal.getDataBitsPerSample()));
        cp.println(String.format("Samplerate         : %d", signal.getDataSampleRate()));
        cp.println(String.format("Domain             : %d", signal.getDataDomain()));
        cp.println(String.format("Type               : %s", signal.getDataTypeToString()));
        cp.println(String.format("ASM-Id String      : %s", signal.getDataIdString()));
        cp.println(String.format("Signal-Name        : %s", signal.getName()));
        cp.println(String.format("UserText           : %s", signal.getDataUserText()));
        cp.println(String.format("Date               : %s", signal.getDate()));
        cp.println(String.format("Description        : %s", signal.getDataDescription()));
        cp.println(String.format("X Scale            : %f", signal.getHScale()));

        return null;
    }

    public Signal PrintCi(List<String> arguments, Signal signal) {
        int channel = cp.getInt(arguments, "Channel", 0, signal.getDataChannels() - 1, 0);
        int record = cp.getInt(arguments, "Record", 0, signal.getDataRecords() - 1, 0);

        double[] re = signal.getRealData();
        double[] im = signal.getImagData();
        int offset = (channel * signal.getDataRecords() + record) * signal.getDataLength();
        for (int i = 0; i < signal.getDataLength(); i++) {
            cp.println(String.format("%6d) Re: %g  Im: %g", i, re[i + offset], im[i + offset]));
        }
        return null;
    }

    public Signal RenameCi(List<String> arguments, Signal signal) {
        String newName = cp.getString(arguments, "new name", signal.getName());
        if (signals.get(newName) != null) {
            cp.println("Signal " + newName + " already exists.");
            return null;
        }

        signals.remove(signal.getName());
        signal.setName(newName);
        signals.put(newName, signal);
        return signal;
    }

    public Signal ReadCi(List<String> arguments) throws IOException {
        String fileName = cp.getString(arguments, "file name", "");
        String signalName = cp.getString(arguments, "signal name", "");

        Path path = Paths.get(fileName);
        if (path.normalize().getParent() == null) {
            // Just a file name. Add signal directory.
            path = Paths.get(cp.getSignalDirectory() + System.getProperty("file.separator") + path);
        }
        Signal signal = new Signal("readf");
        signal.read(path);
        if (!signalName.isEmpty()) {
            signal.setName(signalName);
        }
        if (signal.getDataDomain() == Signal.FREQ) {
            signal.setMode(Signal.BODE_M);
        }

        Signal signalSameName = signals.get(signal.getName());
        if (signalSameName != null) {
            cp.closeSignal(signalSameName);
        }

        signals.put(signal.getName(), signal);
        cp.println("Read file " + path.toAbsolutePath());
        return signal;
    }

    public Signal WriteCi(List<String> arguments, Signal signal) throws IOException {
        String fileName = cp.getString(arguments, "file name", signal.getName() + ".asm");
        String userText = cp.getString(arguments, "user text", "");
        String description = cp.getString(arguments, "description", "");
        Path path = Paths.get(fileName);
        if (path.normalize().getParent() == null) {
            // Just a file name. Add signal directory.
            path = Paths.get(cp.getSignalDirectory() + System.getProperty("file.separator") + path);
        }
        if (!userText.isEmpty())
            signal.setDataUserText(userText);
        if (!description.isEmpty())
            signal.setDataDescription(description);
        signal.write(path);
        cp.println("Wrote file " + path.toAbsolutePath());
        return signal;
    }

    public Signal SignaldirCi(List<String> arguments) throws IOException {
        String dirName = cp.getString(arguments, "directory name", "");

        if (!dirName.isEmpty()) {
            Path path = Paths.get(dirName);
            if (Files.isDirectory(path)) {
                cp.setSignalDirectory(path.toFile());
            } else {
                cp.println("Path " + path + " is not a directory.");
            }
        }
        cp.println("Signaldir is " + cp.getSignalDirectory());
        return null;
    }

    public Signal PlotCommandsCi(List<String> arguments, String command) throws SignalDoesNotExist, IOException {

        if (command.equals("readf")) {
            return ReadCi(arguments);
        }
        if (command.equals("signaldir")) {
            return SignaldirCi(arguments);
        }

        String signalname = cp.getString(arguments, "Signal", "");
        Signal signal = signals.get(signalname); /* Find the correct signal */
        if (signal == null) /* Signal does not exist */
        {
            throw new SignalDoesNotExist("Signal \"" + signalname + "\" does not exist.");
        }

        Signal outputSignal = null;

        Method method = null;
        String methodName = plotcommands.get(command)[0];
        try {
            method = this.getClass().getMethod(methodName, List.class, Signal.class);
        } catch (NoSuchMethodException | SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if (method == null)
            return null;

        try {
            outputSignal = (Signal) method.invoke(this, arguments, signal);
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