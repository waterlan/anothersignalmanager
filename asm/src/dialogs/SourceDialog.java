package dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import console.CommandLineParser;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import signals.Signal;
import signals.Sources;

public class SourceDialog {

    private final Label offsetLabel;
    private final TextField offsetText;
    private final Label amplitudeLabel;
    private final TextField amplitudeText;
    private final Label frequencyLabel;
    private final TextField frequencyText;
    private final Label phaseLabel;
    private final TextField phaseText;
    private final Label deltatLabel;
    private final TextField deltatText;
    private final Label t63Label;
    private final TextField t63Text;
    private final Label tDelayLabel;
    private final TextField tDelayText;
    private final Label dutyCycleLabel;
    private final TextField dutyCycleText;
    private final Label datatypeLabel;
    private final ComboBox<String> datatype;
    private final Label nrOfElementsLabel;
    private final TextField nrOfElementsText;
    private final Label samplerateLabel;
    private final TextField samplerateText;
    private final Label seedLabel;
    private final TextField seedText;

    public SourceDialog(CommandLineParser parser) {
        Dialog<List<String>> dialog = new Dialog<List<String>>();
        dialog.setTitle("Sources");
        dialog.setResizable(true);

        Label functionLabel = new Label("function:");
        ComboBox<String> functions = new ComboBox<String>();
        List<String> functionList = new ArrayList<String>(Sources.functions.keySet()); // set -> list
        Collections.sort(functionList);
        for (String s : functionList) {
            functions.getItems().add(s.substring(1));
        }
        functions.getSelectionModel().select("sine");

        Label nameLabel = new Label("Signal name:");
        TextField nameText = new TextField();
        nameText.setPromptText((String) functions.getValue());

        offsetLabel = new Label("Offset:");
        offsetText = new TextField();
        offsetText.setPromptText("0");

        amplitudeLabel = new Label("Amplitude:");
        amplitudeText = new TextField();
        amplitudeText.setPromptText("100");

        frequencyLabel = new Label("Frequency (Hz):");
        frequencyText = new TextField();
        frequencyText.setPromptText("100");

        phaseLabel = new Label("Phase (rad):");
        phaseText = new TextField();
        phaseText.setPromptText("0");

        deltatLabel = new Label("Delta-t (millisec):");
        deltatText = new TextField();
        deltatText.setPromptText("0");

        t63Label = new Label("t 63.2% (millisec):");
        t63Text = new TextField();
        t63Text.setPromptText("10.0");

        tDelayLabel = new Label("t-delay (ms):");
        tDelayText = new TextField();
        tDelayText.setPromptText("0.0");

        dutyCycleLabel = new Label("Duty-cycle <0% .. 100%>:");
        dutyCycleText = new TextField();
        dutyCycleText.setPromptText("50.0");

        datatypeLabel = new Label("Data type:");
        datatype = new ComboBox<String>();
        datatype.getItems().add("real");
        datatype.getItems().add("imaginary");
        datatype.getItems().add("complex");
        datatype.getSelectionModel().select("real");

        nrOfElementsLabel = new Label("Nr of elements (2^n):");
        nrOfElementsText = new TextField();
        nrOfElementsText.setPromptText("9");

        samplerateLabel = new Label("Sample rate (10 Hz):");
        samplerateText = new TextField();
        samplerateText.setPromptText(Integer.toString(Signal.SAMPLE_RATE));

        seedLabel = new Label("Seed:");
        seedText = new TextField();
        seedText.setPromptText("1");

        GridPane grid = new GridPane();
        grid.setHgap(10.0);
        grid.setVgap(10.0);

        grid.add(functionLabel, 0, 0);
        grid.add(functions, 1, 0);
        grid.add(nameLabel, 0, 1);
        grid.add(nameText, 1, 1);
        updateGrid(grid, (String) functions.getValue());
        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        ButtonType buttonTypeOk = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        functions.setOnAction(e -> {
            grid.getChildren().removeIf(node -> GridPane.getRowIndex(node) > 1);
            nameText.setPromptText((String) functions.getValue());
            updateGrid(grid, (String) functions.getValue());
        });

        dialog.setResultConverter(button -> {
            if (button == buttonTypeOk) {
                String functionType = (String) functions.getValue();
                List<String> l = new ArrayList<String>();
                l.add("f" + functionType);
                l.add(nameText.getText());
                if (functionType.equals("cosine") || functionType.equals("ramp") || functionType.startsWith("sin")
                        || functionType.equals("square") || functionType.equals("step")
                        || functionType.equals("triangle")) {
                    l.add(offsetText.getText());
                }
                l.add(amplitudeText.getText());
                if (functionType.equals("delta")) {
                    l.add(deltatText.getText());
                }
                if (functionType.equals("exp")) {
                    l.add(t63Text.getText());
                }
                if (functionType.equals("step")) {
                    l.add(tDelayText.getText());
                }
                if (functionType.equals("cosine") || functionType.startsWith("sin") || functionType.equals("square")
                        || functionType.equals("triangle")) {
                    l.add(frequencyText.getText());
                }
                if (functionType.equals("cosine") || functionType.equals("sine") || functionType.equals("triangle")) {
                    l.add(phaseText.getText());
                }
                if (functionType.equals("square")) {
                    l.add(dutyCycleText.getText());
                }
                l.add(Integer.toString(datatype.getSelectionModel().getSelectedIndex()));
                if (functionType.equals("noise")) {
                    l.add(seedText.getText());
                }
                l.add(nrOfElementsText.getText());
                l.add(samplerateText.getText());
                return l;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(result -> {
            parser.parseCommand(result);
        });
        ;
    }

    private void updateGrid(GridPane grid, String func) {
        int r = 2;
        if (func.equals("cosine") || func.equals("ramp") || func.startsWith("sin") || func.equals("square")
                || func.equals("step") || func.equals("triangle")) {
            grid.add(offsetLabel, 0, r);
            grid.add(offsetText, 1, r++);
        }
        grid.add(amplitudeLabel, 0, r);
        grid.add(amplitudeText, 1, r++);
        if (func.equals("delta")) {
            grid.add(deltatLabel, 0, r);
            grid.add(deltatText, 1, r++);
        }
        if (func.equals("exp")) {
            grid.add(t63Label, 0, r);
            grid.add(t63Text, 1, r++);
        }
        if (func.equals("step")) {
            grid.add(tDelayLabel, 0, r);
            grid.add(tDelayText, 1, r++);
        }
        if (func.equals("cosine") || func.startsWith("sin") || func.equals("square") || func.equals("triangle")) {
            grid.add(frequencyLabel, 0, r);
            grid.add(frequencyText, 1, r++);
        }
        if (func.equals("cosine") || func.equals("sine") || func.equals("triangle")) {
            grid.add(phaseLabel, 0, r);
            grid.add(phaseText, 1, r++);
        }
        if (func.equals("square")) {
            grid.add(dutyCycleLabel, 0, r);
            grid.add(dutyCycleText, 1, r++);
        }
        grid.add(datatypeLabel, 0, r);
        grid.add(datatype, 1, r++);
        if (func.equals("noise")) {
            grid.add(seedLabel, 0, r);
            grid.add(seedText, 1, r++);
        }
        grid.add(nrOfElementsLabel, 0, r);
        grid.add(nrOfElementsText, 1, r++);
        grid.add(samplerateLabel, 0, r);
        grid.add(samplerateText, 1, r++);
    }
}
