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
import math.ConvCorr;
import math.Transformations;
import signals.Signal;

public class OperationsDialog {

    private final Label input2Label;
    private final ComboBox<String> input2Signals = new ComboBox<String>();
    private final Label outputSignalLabel;
    private final TextField outputSignalText;
    private final Label windowTypeLabel;
    private final ComboBox<String> windowType;

    public OperationsDialog(CommandLineParser parser, Map<String, Signal> signals) {
        Dialog<List<String>> dialog = new Dialog<List<String>>();
        dialog.setTitle("Operations");
        dialog.setResizable(true);

        Label functionLabel = new Label("Operation:");
        ComboBox<String> functions = new ComboBox<String>();
        List<String> functionList = new ArrayList<String>(ConvCorr.convcorr.keySet()); // set -> list
        Collections.sort(functionList);
        for (String s : functionList) {
            functions.getItems().add(s);
        }
        functions.getSelectionModel().select("correlation");

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
        windowType.getSelectionModel().select("block");

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
                l.add((String) input2Signals.getValue());
                l.add(outputSignalText.getText());
                l.add(Integer.toString(windowType.getSelectionModel().getSelectedIndex()));

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
        grid.add(input2Label, 0, r);
        grid.add(input2Signals, 1, r++);

        grid.add(outputSignalLabel, 0, r);
        grid.add(outputSignalText, 1, r++);

        grid.add(windowTypeLabel, 0, r);
        grid.add(windowType, 1, r++);

    }
}