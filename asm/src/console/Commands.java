package console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import math.Calculations;
import math.ConvCorr;
import math.Transformations;
import signals.PlotCommands;
import signals.Sources;
import signals.Windowing;

public class Commands {

    public static final Map<String, String[]> commands = new HashMap<String, String[]>() {
        {
            put("doff", new String[] { "", "" });
            put("don", new String[] { "", "" });
            put("list", new String[] { "", "" });
            put("boff", new String[] { "", "" });
            put("bon", new String[] { "", "" });
            put("exit", new String[] { "", "" });
            put("quit", new String[] { "", "" });
        }
    };

    public Commands() {
        commands.putAll(Sources.functions);
        commands.putAll(PlotCommands.plotcommands);
        commands.putAll(Windowing.windows);
        commands.putAll(Calculations.calculations);
        commands.putAll(Transformations.transformations);
        commands.putAll(ConvCorr.convcorr);
    }

    public List<String> getCommand(String command) {
        List<String> matches = new ArrayList<String>();
        for (String cmd : commands.keySet()) {
            if (cmd.startsWith(command))
                matches.add(cmd);
        }
        return matches;
    }

    public List<String> getAllCommands() {
        List<String> cmds = new ArrayList<String>(commands.keySet());
        Collections.sort(cmds);
        return cmds;
    }

    public String getHelp(String command) {
        if (commands.containsKey(command))
            return command + " " + commands.get(command)[1];
        else
            return null;
    }

}
