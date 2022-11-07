package console;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import exceptions.SignalDoesNotExist;
import math.Calculations;
import math.ConvCorr;
import math.Transformations;
import signals.PlotCommands;
import signals.Signal;
import signals.SignalWindow;
import signals.Sources;
import signals.Windowing;

public class CommandLineParser {
    private final Sources sources;
    private final Windowing windowing;
    private final Calculations calculations;
    private final Transformations transformations;
    private final PlotCommands plotCommands;
    private final ConvCorr convcorr;
    private final Console console;
    private final Commands commands = new Commands();
    private boolean don = true;
    private boolean bon = false;
    private File signalDirectory = new File(System.getProperty("user.home"));
    Map<String, Signal> signals;

    public CommandLineParser(Map<String, Signal> signals, Console console) {
        this.signals = signals;
        this.sources = new Sources(signals, this);
        this.windowing = new Windowing(signals, this);
        this.calculations = new Calculations(signals, this);
        this.transformations = new Transformations(signals, this);
        this.plotCommands = new PlotCommands(signals, this);
        this.convcorr = new ConvCorr(signals, this);
        this.console = console;
    }

    public String getString(List<String> args, String name, String defaultValue) {
        if (args.isEmpty()) {
            return defaultValue;
        } else {
            String result = args.get(0) == null || args.get(0).isEmpty() ? defaultValue : args.get(0);
            args.remove(0);
            return result;
        }
    }

    public void print(String message) {
        console.print(message);
    }

    public void println(String message) {
        console.println(message);
    }

    public int getInt(List<String> args, String name, int min, int max, int defaultValue) {
        int result;
        if (args.isEmpty()) {
            return defaultValue;
        } else {
            try {
                result = args.get(0) == null || args.get(0).isEmpty() || args.get(0).equals(".") ? defaultValue
                        : Integer.parseInt(args.get(0));
            } catch (NumberFormatException e) {
                println("error: invalid expression for (integer) " + name);
                result = defaultValue;
            }
            args.remove(0);
            if (result < min)
                return min;
            else if (result > max)
                return max;
            else
                return result;
        }
    }

    public double getDouble(List<String> args, String name, double min, double max, double defaultValue) {
        double result;
        if (args.isEmpty()) {
            return defaultValue;
        } else {
            try {
                result = args.get(0) == null || args.get(0).isEmpty() || args.get(0).equals(".") ? defaultValue
                        : Double.parseDouble(args.get(0));
            } catch (NullPointerException e) {
                result = defaultValue;
            } catch (NumberFormatException e) {
                println("error: invalid expression for (double) " + name);
                result = defaultValue;
            }
            args.remove(0);
            if (result < min)
                return min;
            else if (result > max)
                return max;
            else
                return result;
        }
    }

    int parseCommand(String command_with_args) {

        if (command_with_args == null) {
            return 0;
        }
        String cmd = command_with_args.trim();
        if (cmd.isEmpty()) {
            return 0;
        }

        String[] args = cmd.split("\\s+");
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        return parseCommand(argList);
    }

    public void showSignal(Signal s, boolean override) {
        if (!don && !override)
            return;
        if (s == null)
            return;
        SignalWindow w = s.getWindow();
        if (w == null) {
            w = new SignalWindow(s, this, signals);
        } else {
            w.show(bon);
        }
    }

    public void closeSignal(Signal s) {
        if (s == null)
            return;
        SignalWindow w = s.getWindow();
        if (w == null) {
            return;
        } else {
            w.close();
        }
    }

    public void showConsole() {
        console.show();
    }

    private void printCommand(List<String> argList) {
        StringBuffer line = new StringBuffer();
        line.append(">");
        for (String arg : argList) {
            if (arg == null || arg.isEmpty()) {
                line.append(" .");
            } else {
                line.append(" " + arg);
            }
        }
        println(line.toString());
    }

    public int parseCommand(List<String> argList) {

        if (argList == null || argList.isEmpty()) {
            return 0;
        }
        printCommand(argList);
        String command = argList.get(0);
        argList.remove(0);
        List<String> cmdMatches = commands.getCommand(command);
        if (cmdMatches.size() > 1) {
            println("Ambiguous command: " + command);
            return 0;
        } else if (cmdMatches.size() == 1)
            command = cmdMatches.get(0);
        if (!argList.isEmpty() && argList.get(0) != null && argList.get(0).matches("^\\?$|-h|--help")) {
            String help = commands.getHelp(command);
            if (help != null) {
                println(help);
                return 0;
            }
        }

        try {
            if (command.equals("list")) {
                for (String signalName : signals.keySet()) {
                    println(signalName);
                }
            } else if (command.equals("?")) {
                List<String> cmds = commands.getAllCommands();
                for (String cmd : cmds)
                    println(cmd);
            } else if (command.equals("don")) {
                don = true;
                println("Display is ON");
            } else if (command.equals("doff")) {
                don = false;
                println("Display is OFF");
            } else if (command.equals("bon")) {
                bon = true;
                println("Bar graph is ON");
            } else if (command.equals("boff")) {
                bon = false;
                println("Bar graph is OFF");
            } else if (command.equals("exit") || command.equals("quit")) {
                System.exit(0);
            } else if (PlotCommands.plotcommands.containsKey(command)) {
                Signal s = plotCommands.PlotCommandsCi(argList, command);
                showSignal(s, command.equals("display"));
            } else if (Sources.functions.containsKey(command)) {
                Signal s = sources.SourcesCi(argList, command);
                showSignal(s, false);
            } else if (Windowing.windows.containsKey(command)) {
                Signal s = windowing.WindowCi(argList, command);
                showSignal(s, false);
            } else if (Calculations.calculations.containsKey(command)) {
                Signal s = calculations.CalculateCi(argList, command);
                showSignal(s, false);
            } else if (Transformations.transformations.containsKey(command)) {
                Signal s = transformations.TransformationCi(argList, command);
                showSignal(s, false);
            } else if (ConvCorr.convcorr.containsKey(command)) {
                Signal s = convcorr.ConvCorrCi(argList, command);
                showSignal(s, false);
            } else {
                println("error: command not found.");
            }
        } catch (SignalDoesNotExist e) {
            println("error: " + e.getMessage());
        } catch (IOException e) {
            println("error: " + e.getMessage());
        }
        return 0;
    }

    public File getSignalDirectory() {
        return signalDirectory;
    }

    public void setSignalDirectory(File signalDirectory) {
        this.signalDirectory = signalDirectory;
    }
}
