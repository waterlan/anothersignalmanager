package dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import console.CommandLineParser;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class OpenSignalDialog {

    private final Label inputFileLabel;
    private final TextField inputFileText;
    private final Label signalNameLabel;
    private final TextField signalNameText;
    private final Button browseButton = new Button("Browse");
    private final Stage stage = new Stage();
    private final FileChooser fileChooser = new FileChooser();

    public OpenSignalDialog(CommandLineParser parser) {
        Dialog<List<String>> dialog = new Dialog<List<String>>();
        dialog.setTitle("Open signal");
        dialog.setResizable(true);

        inputFileLabel = new Label("input file:");
        inputFileText = new TextField();
        inputFileText.setPromptText("");

        signalNameLabel = new Label("signal name:");
        signalNameText = new TextField();
        signalNameText.setPromptText("");

        GridPane grid = new GridPane();
        grid.setHgap(10.0);
        grid.setVgap(10.0);
        grid.setMinHeight(250);

        grid.add(inputFileLabel, 0, 0);
        grid.add(inputFileText, 1, 0);
        grid.add(browseButton, 2, 0);
        grid.add(signalNameLabel, 0, 1);
        grid.add(signalNameText, 1, 1);

        GridPane.setHgrow(inputFileText, Priority.ALWAYS);
        grid.setPrefWidth(600);

        dialog.getDialogPane().setContent(grid);

        fileChooser.setTitle("Open Signal File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("ASM", "*.asm"),
                new FileChooser.ExtensionFilter("All files", "*.*"));

        browseButton.setOnAction(event -> {
            fileChooser.setInitialDirectory(parser.getSignalDirectory());
            File inputSignalFile = fileChooser.showOpenDialog(stage);
            if (inputSignalFile != null) {
                inputFileText.setText(inputSignalFile.getAbsolutePath());
                File signalDir = inputSignalFile.getParentFile();
                if (signalDir != null)
                    parser.setSignalDirectory(signalDir);
            }
        });

        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        ButtonType buttonTypeOk = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        dialog.setResultConverter(button -> {
            if (button == buttonTypeOk) {
                List<String> l = new ArrayList<String>();
                l.add("readf");
                l.add(inputFileText.getText());
                l.add(signalNameText.getText());
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
