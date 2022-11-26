package dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import console.CommandLineParser;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import math.Calculations;
import signals.Signal;
import signals.Sources;
import signals.Windowing;

public class CalculationDialog {

    private final Label input2Label;
    private final ComboBox<String> input2Signals = new ComboBox<String>();
    private final Label valueLabel;
    private final TextField valueText;
    private final Label value2Label;
    private final TextField value2Text;
    private final Label value3Label;
    private final TextField value3Text;
    private final Label valueRealLabel;
    private final TextField valueRealText;
    private final Label valueImagLabel;
    private final TextField valueImagText;
    private final Label outputSignalLabel;
    private final TextField outputSignalText;
    
    // one input, same output
    private static final List<String> oneInputSameOutput = new ArrayList<String>() {
        {
            add("clear");
        }
    };
    // one input, two values, same output
    private static final List<String> oneInputTwoValuesSameOutput = new ArrayList<String>() {
        {
            add("assign");
        }
    };
    //   one input, one output
    private static final List<String> oneInputOneOutput = new ArrayList<String>() {
        {
            add("cabs");
            add("conjugate");
            add("cosine");
            add("copy");
            add("epow");
            add("inv");
            add("ln");
            add("log");
            add("sine");
            add("tenpow");
            add("zeropad");
        }
    };
    
    // one input, one value, one output
    private static final List<String> oneInputOneValueOneOutput = new ArrayList<String>() {
        {
            add("cadd");
            add("cdivide");
            add("cmultiply");
            add("rotate");
            add("shift");
        }
    };
    // two inputs, one output
    private static final List<String> twoInputsOneOutput = new ArrayList<String>() {
        {
            add("absolute");
            add("add");
            add("divide");
            add("maximum");
            add("minimum");
            add("subtract");
        }
    };
    
    // two inputs, one value, one output
    private static final List<String> twoInputsOneValueOneOutput = new ArrayList<String>() {
        {
            add("multiply");
        }
    };

    public CalculationDialog(CommandLineParser parser, Map<String, Signal> signals) {
        Dialog<List<String>> dialog = new Dialog<List<String>>();
        dialog.setTitle("Basic operations");
        dialog.setResizable(true);
        
        Label functionLabel = new Label("Operation:");
        ComboBox<String> functions = new ComboBox<String>();
        List<String> functionList = new ArrayList<String>(Calculations.calculations.keySet()); // set -> list
        Collections.sort(functionList);
        for (String s : functionList) {
            functions.getItems().add(s);
        }
        functions.getSelectionModel().select("multiply");

        Label inputLabel = new Label("input signal:");
        ComboBox<String> inputSignals = new ComboBox<String>();

        List<String> signalList = new ArrayList<String>(signals.keySet()); // set -> list
        Collections.sort(signalList);
        for (String s : signalList) {
            inputSignals.getItems().add(s);
        }

        input2Label = new Label("input signal:");
        for (String s : signalList) {
            input2Signals.getItems().add(s);
        }

        valueLabel = new Label("constant:");
        valueText = new TextField();
        valueText.setPromptText("0");

        value2Label = new Label("right:");
        value2Text = new TextField();
        value2Text.setPromptText("");

        value3Label = new Label("attenuation:");
        value3Text = new TextField();
        value3Text.setPromptText("50.0");

        valueRealLabel = new Label("real value:");
        valueRealText = new TextField();
        valueRealText.setPromptText("1");

        valueImagLabel = new Label("imag value:");
        valueImagText = new TextField();
        valueImagText.setPromptText("1");

        outputSignalLabel = new Label("output signal:");
        outputSignalText = new TextField();
        outputSignalText.setPromptText((String) functions.getValue());

        GridPane grid = new GridPane();
        grid.setHgap(10.0);
        grid.setVgap(10.0);
        grid.setMinHeight(250);
        
        grid.add(functionLabel, 0, 0);
        grid.add(functions, 1, 0);
        grid.add(inputLabel, 0, 1);
        grid.add(inputSignals, 1, 1);
        updateGrid(grid, (String) functions.getValue());
        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        ButtonType buttonTypeOk = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        functions.setOnAction(e -> {
            grid.getChildren().removeIf(node -> GridPane.getRowIndex(node) > 1);
            updateGrid(grid, (String) functions.getValue());
            outputSignalText.setPromptText((String) functions.getValue());
        });

        dialog.setResultConverter(button -> {
            if (button == buttonTypeOk) {
                String functionType = (String) functions.getValue();
                List<String> l = new ArrayList<String>();
                l.add(functionType);
                l.add((String) inputSignals.getValue());
                if (oneInputOneValueOneOutput.contains(functionType)) {
                    l.add(valueText.getText());
                }
                if (oneInputTwoValuesSameOutput.contains(functionType)) {
                    l.add(valueRealText.getText());
                    l.add(valueImagText.getText());
                }
                if (functionType.equals("clip")) {
                    l.add(valueText.getText());
                    l.add(value2Text.getText());
                    l.add(value3Text.getText());
                }
                if (twoInputsOneOutput.contains(functionType) ||
                    twoInputsOneValueOneOutput.contains(functionType)) {
                    l.add((String) input2Signals.getValue());
                }
                if (twoInputsOneValueOneOutput.contains(functionType)) {
                    l.add(valueText.getText());
                }
                if (!oneInputSameOutput.contains(functionType) && !oneInputTwoValuesSameOutput.contains(functionType)) {
                    l.add(outputSignalText.getText());
                }
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
        if (oneInputOneValueOneOutput.contains(func)) {
            grid.add(valueLabel, 0, r);
            grid.add(valueText, 1, r++);
            valueLabel.setText("constant:");
            if (func.equals("cadd"))
                valueText.setPromptText("0");
            else
                valueText.setPromptText("1");
        }
        if (func.equals("clip")) {
            valueLabel.setText("left:");
            grid.add(valueLabel, 0, r);
            grid.add(valueText, 1, r++);
            valueText.setPromptText("0");
            grid.add(value2Label, 0, r);
            grid.add(value2Text, 1, r++);
            grid.add(value3Label, 0, r);
            grid.add(value3Text, 1, r++);
        }
        if (oneInputTwoValuesSameOutput.contains(func)) {
            grid.add(valueRealLabel, 0, r);
            grid.add(valueRealText, 1, r++);
            grid.add(valueImagLabel, 0, r);
            grid.add(valueImagText, 1, r++);
        }
        if (twoInputsOneOutput.contains(func) ||
            twoInputsOneValueOneOutput.contains(func)) {
            grid.add(input2Label, 0, r);
            grid.add(input2Signals, 1, r++);
        }
        if (twoInputsOneValueOneOutput.contains(func)) {
            grid.add(valueLabel, 0, r);
            grid.add(valueText, 1, r++);
            valueText.setPromptText("1");
        }
        if (!oneInputSameOutput.contains(func) && !oneInputTwoValuesSameOutput.contains(func)) {
            grid.add(outputSignalLabel, 0, r);
            grid.add(outputSignalText, 1, r++);
        }
    }
}
