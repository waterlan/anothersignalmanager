package dialogs;

import java.util.ArrayList;
import java.util.List;

import console.CommandLineParser;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import signals.Signal;

public class RecordDialog {

    private final Stage stage = new Stage();
    private final Label nrOfElementsLabel;
    private final ComboBox<String> nrOfElementsComboBox;
    private final Label samplerateLabel;
    private final TextField samplerateText;
    private final Button startButton = new Button("START");
    private final Button stopButton = new Button("STOP");

    public RecordDialog(CommandLineParser parser) {
        Dialog<List<String>> dialog = new Dialog<List<String>>();
        dialog.setTitle("Record");
        dialog.setResizable(true);

        Label nameLabel = new Label("Signal name:");
        TextField nameText = new TextField();
        nameText.setText("recording");

        nrOfElementsLabel = new Label("Record size (2^n):");
        nrOfElementsComboBox = new ComboBox<String>();
        nrOfElementsComboBox.getItems().add("7");
        nrOfElementsComboBox.getItems().add("8");
        nrOfElementsComboBox.getItems().add("9");
        nrOfElementsComboBox.getItems().add("10");
        nrOfElementsComboBox.getItems().add("11");
        nrOfElementsComboBox.getItems().add("12");
        nrOfElementsComboBox.getSelectionModel().select("9");

        samplerateLabel = new Label("Sample rate (Hz):");
        samplerateText = new TextField();
        samplerateText.setPromptText(Integer.toString(Signal.SAMPLE_RATE));

        GridPane grid = new GridPane();
        grid.setHgap(10.0);
        grid.setVgap(10.0);
        grid.setMinHeight(250);

        grid.add(nameLabel, 0, 0);
        grid.add(nameText, 1, 0);
        grid.add(nrOfElementsLabel, 0, 1);
        grid.add(nrOfElementsComboBox, 1, 1);
        grid.add(samplerateLabel, 0, 2);
        grid.add(samplerateText, 1, 2);
        grid.add(startButton, 0, 3);
        grid.add(stopButton, 1, 3);

        GridPane.setHgrow(nameText, Priority.ALWAYS);
        grid.setPrefWidth(600);
        
        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        ButtonType buttonTypeOk = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        dialog.setResultConverter(button -> {
            if (button == buttonTypeOk) {
                List<String> l = new ArrayList<String>();
                l.add("record");
                l.add(nameText.getText());
                l.add(nrOfElementsComboBox.getValue());
                return l;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(result -> {
            parser.parseCommand(result);
        });

        
        startButton.setOnAction(event -> {
            
        });
        
        stopButton.setOnAction(event -> {
            
        });
        
    }

}