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

    private final Label lengthLabel;
    private final TextField lengthText;
    private final Label channelLabel;
    private final TextField channelText;
    private final Label averageTypeLabel;
    private final Label logLabel;
    private final Label outputSignalLabel;
    private final TextField outputSignalText;
    private final Label windowTypeLabel;
    private final ComboBox<String> windowType;
    private final Label bucketsLabel;
    private final ComboBox<String> buckets;
    private final ComboBox<String> averageType;
    private final ComboBox<String> log;

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

        lengthLabel = new Label("length (2^n):");
        lengthText = new TextField();
        lengthText.setPromptText("0");

        channelLabel = new Label("channel:");
        channelText = new TextField();
        channelText.setPromptText("0");

        outputSignalLabel = new Label("output signal:");
        outputSignalText = new TextField();
        outputSignalText.setPromptText(functions.getValue());

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

        averageTypeLabel = new Label("average type:");
        averageType = new ComboBox<String>();
        averageType.getItems().add("0");
        averageType.getItems().add("1");
        averageType.getSelectionModel().select("0");

        logLabel = new Label("log:");
        log = new ComboBox<String>();
        log.getItems().add("0");
        log.getItems().add("1");
        log.getSelectionModel().select("0");

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
                if (functionType.equals("fft")) {
                    l.add(lengthText.getText());
                    l.add(Integer.toString(windowType.getSelectionModel().getSelectedIndex()));

                }
                if (functionType.equals("magnitude") || functionType.equals("phase")) {
                    l.add(channelText.getText());
                }
                if (functionType.equals("fft") || functionType.equals("magnitude") || functionType.equals("phase")) {
                    l.add(averageType.getValue());
                }
                if (functionType.equals("magnitude")) {
                    l.add(log.getValue());
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
            grid.add(lengthLabel, 0, r);
            grid.add(lengthText, 1, r++);
            lengthText.setPromptText("9");

            grid.add(windowTypeLabel, 0, r);
            grid.add(windowType, 1, r++);

            grid.add(averageTypeLabel, 0, r);
            grid.add(averageType, 1, r++);
        }
        if (func.equals("magnitude") || func.equals("phase")) {
            grid.add(channelLabel, 0, r);
            grid.add(channelText, 1, r++);
            channelText.setPromptText("0");

            grid.add(averageTypeLabel, 0, r);
            grid.add(averageType, 1, r++);
        }
        if (func.equals("magnitude")) {
            grid.add(logLabel, 0, r);
            grid.add(log, 1, r++);
        }
        if (func.equals("histogram")) {
            grid.add(bucketsLabel, 0, r);
            grid.add(buckets, 1, r++);
        }
    }
}