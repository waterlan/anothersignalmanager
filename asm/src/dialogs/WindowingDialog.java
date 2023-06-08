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
import signals.Windowing;

public class WindowingDialog {

    private final Label nrOfElementsLabel;
    private final TextField nrOfElementsText;
    private final Label samplerateLabel;
    private final TextField samplerateText;

    public WindowingDialog(CommandLineParser parser) {
        Dialog<List<String>> dialog = new Dialog<List<String>>();
        dialog.setTitle("Windows");
        dialog.setResizable(true);

        Label functionLabel = new Label("window:");
        ComboBox<String> functions = new ComboBox<String>();
        List<String> windowList = new ArrayList<String>(Windowing.windows.keySet()); // set -> list
        Collections.sort(windowList);
        for (String s : windowList) {
            functions.getItems().add(s.substring(1));
        }
        functions.getSelectionModel().select("block");

        Label nameLabel = new Label("Signal name:");
        TextField nameText = new TextField();
        nameText.setPromptText((String) functions.getValue());

        nrOfElementsLabel = new Label("Nr of elements (2^n):");
        nrOfElementsText = new TextField();
        nrOfElementsText.setPromptText("9");

        samplerateLabel = new Label("Sample rate (Hz):");
        samplerateText = new TextField();
        samplerateText.setPromptText(Integer.toString(Signal.SAMPLE_RATE));

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
            //grid.getChildren().removeIf(node -> GridPane.getRowIndex(node) > 1);
            //updateGrid(grid, (String) functions.getValue());
            nameText.setPromptText((String) functions.getValue());
        });

        dialog.setResultConverter(button -> {
            if (button == buttonTypeOk) {
                String functionType = (String) functions.getValue();
                List<String> l = new ArrayList<String>();
                l.add("w" + functionType);
                l.add(nameText.getText());
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
        grid.add(nrOfElementsLabel, 0, r);
        grid.add(nrOfElementsText, 1, r++);
        grid.add(samplerateLabel, 0, r);
        grid.add(samplerateText, 1, r++);
    }
}
