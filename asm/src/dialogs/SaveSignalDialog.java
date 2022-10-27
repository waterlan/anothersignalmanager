package dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import console.CommandLineParser;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import signals.Signal;

public class SaveSignalDialog {

    private final Label outputFileLabel;
    private final TextField outputFileText;
    private final Label userTextLabel;
    private final TextField userText;
    private final Label descriptionLabel;
    private final TextField descriptionText;
    private final Button browseButton = new Button("Browse");
    private final Stage stage = new Stage();
    private final FileChooser fileChooser = new FileChooser();

    public SaveSignalDialog(CommandLineParser parser, Map<String, Signal> signals, String signalName) {
        Dialog<List<String>> dialog = new Dialog<List<String>>();
        dialog.setTitle("Save signal");
        dialog.setResizable(true);

        Label inputLabel = new Label("signal:");
        ComboBox<String> inputSignals = new ComboBox<String>();

        List<String> signalList = new ArrayList<String>(signals.keySet()); // set -> list
        Collections.sort(signalList);
        for (String s : signalList) {
            inputSignals.getItems().add(s);
        }
        if (signalName != null)
            inputSignals.getSelectionModel().select(signalName);

        outputFileLabel = new Label("output file:");
        outputFileText = new TextField();
        if (signalName != null)
            outputFileText.setText(signalName + ".asm");
        else
            outputFileText.setPromptText("a.asm");

        userTextLabel = new Label("user text:");
        userText = new TextField();
        userText.setPromptText("DataUserText");

        descriptionLabel = new Label("description:");
        descriptionText = new TextField();
        descriptionText.setPromptText("description");

        GridPane grid = new GridPane();
        grid.setHgap(10.0);
        grid.setVgap(10.0);
        grid.setMinHeight(250);

        grid.add(inputLabel, 0, 0);
        grid.add(inputSignals, 1, 0);
        grid.add(outputFileLabel, 0, 1);
        grid.add(outputFileText, 1, 1);
        grid.add(browseButton, 2, 1);
        grid.add(userTextLabel, 0, 2);
        grid.add(userText, 1, 2);
        grid.add(descriptionLabel, 0, 3);
        grid.add(descriptionText, 1, 3);
        dialog.getDialogPane().setContent(grid);

        fileChooser.setTitle("Save Signal File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("ASM", "*.asm"),
                new FileChooser.ExtensionFilter("All files", "*.*"));

        browseButton.setOnAction(event -> {
            fileChooser.setInitialDirectory(parser.getSignalDirectory());
            File outputSignalFile = fileChooser.showSaveDialog(stage);
            if (outputSignalFile != null) {
                outputFileText.setText(outputSignalFile.getAbsolutePath());
                File signalDir = outputSignalFile.getParentFile();
                if (signalDir != null)
                    parser.setSignalDirectory(signalDir);
            }
        });

        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        ButtonType buttonTypeOk = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        inputSignals.setOnAction(e -> {
            outputFileText.setText(inputSignals.getValue() + ".asm");
        });

        
        dialog.setResultConverter(button -> {
            if (button == buttonTypeOk) {
                List<String> l = new ArrayList<String>();
                l.add("writef");
                l.add((String) inputSignals.getValue());
                l.add(outputFileText.getText());
                l.add(userText.getText());
                l.add(descriptionText.getText());
                return l;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(result -> {
            parser.parseCommand(result);
        });
        ;
    }

}