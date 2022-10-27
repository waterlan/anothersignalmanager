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
import math.Transformations;
import signals.Signal;

public class TransformationsDialog {

    private final Label valueLabel;
    private final TextField valueText;
    private final Label value2Label;
    private final TextField value2Text;
    private final Label value3Label;
    private final TextField value3Text;
    private final Label outputSignalLabel;
    private final TextField outputSignalText;
    private final Label windowTypeLabel;
    private final ComboBox<String> windowType;
    private final Label bucketsLabel;
    private final ComboBox<String> buckets;

    public TransformationsDialog(CommandLineParser parser, Map<String, Signal> signals) {
        Dialog<List<String>> dialog = new Dialog<List<String>>();
        dialog.setTitle("Transformations");
        dialog.setResizable(true);

        Label functionLabel = new Label("Transformation:");
        ComboBox<String> functions = new ComboBox<String>();
        List<String> functionList = new ArrayList<String>(Transformations.transformations.keySet()); // set -> list
        Collections.sort(functionList);
        for (String s : functionList) {
            functions.getItems().add(s);
        }
        functions.getSelectionModel().select("fft");

        Label inputLabel = new Label("input signal:");
        ComboBox<String> inputSignals = new ComboBox<String>();

        List<String> signalList = new ArrayList<String>(signals.keySet()); // set -> list
        Collections.sort(signalList);
        for (String s : signalList) {
            inputSignals.getItems().add(s);
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

        outputSignalLabel = new Label("output signal:");
        outputSignalText = new TextField();
        outputSignalText.setPromptText("b");

        windowTypeLabel = new Label("window type:");
        windowType = new ComboBox<String>();
        windowType.getItems().add("block");
        windowType.getItems().add("hanning");
        windowType.getItems().add("hamming");
        windowType.getItems().add("gauss");
        windowType.getItems().add("blackman");
        windowType.getItems().add("kaiser");
        windowType.getItems().add("triangle");
        windowType.getSelectionModel().select("hanning");

        bucketsLabel = new Label("buckets:");
        buckets = new ComboBox<String>();
        buckets.getItems().add("7");
        buckets.getItems().add("8");
        buckets.getItems().add("9");
        buckets.getItems().add("10");
        buckets.getItems().add("11");
        buckets.getItems().add("12");
        buckets.getSelectionModel().select("9");

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
                l.add(outputSignalText.getText());
                if (!functionType.equals("ifft")) {
                    l.add(valueText.getText());
                }
                if (functionType.equals("fft")) {
                    l.add(Integer.toString(windowType.getSelectionModel().getSelectedIndex()));

                }
                if (functionType.equals("fft") || functionType.equals("magnitude") || functionType.equals("phase")) {
                    l.add(value2Text.getText());
                }
                if (functionType.equals("fft") || functionType.equals("magnitude")) {
                    l.add(value3Text.getText());
                }
                if (functionType.equals("histogram")) {
                    l.add(buckets.getValue());
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
        grid.add(outputSignalLabel, 0, r);
        grid.add(outputSignalText, 1, r++);
        if (func.equals("fft")) {
            valueLabel.setText("length:");
            grid.add(valueLabel, 0, r);
            grid.add(valueText, 1, r++);
            valueText.setPromptText("9");

            grid.add(windowTypeLabel, 0, r);
            grid.add(windowType, 1, r++);

            value3Label.setText("average type:");
            grid.add(value3Label, 0, r);
            grid.add(value3Text, 1, r++);
            value3Text.setPromptText("0");
        }
        if (func.equals("magnitude") || func.equals("phase")) {
            valueLabel.setText("channel:");
            grid.add(valueLabel, 0, r);
            grid.add(valueText, 1, r++);
            valueText.setPromptText("0");

            value2Label.setText("average type:");
            grid.add(value2Label, 0, r);
            grid.add(value2Text, 1, r++);
            value2Text.setPromptText("0");
        }
        if (func.equals("magnitude")) {
            value3Label.setText("log:");
            grid.add(value3Label, 0, r);
            grid.add(value3Text, 1, r++);
            value3Text.setPromptText("0");
        }
        if (func.equals("histogram")) {
            grid.add(bucketsLabel, 0, r);
            grid.add(buckets, 1, r++);
        }
    }
}