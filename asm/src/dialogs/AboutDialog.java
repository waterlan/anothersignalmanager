package dialogs;

import java.util.List;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class AboutDialog {

    private final Label nameLabel = new Label("ASM");
    private final Label fullNameLabel = new Label("Another Signal Manager");
    private final Label versionLabel = new Label("version 2.0.0 (Dec 3 2022)");
    private final TextField urlLabel = new TextField("https://waterlan.home.xs4all.nl/asm.html");

    public AboutDialog() {
        Dialog<List<String>> dialog = new Dialog<List<String>>();
        dialog.setTitle("About");
        dialog.setResizable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10.0);
        grid.setVgap(10.0);
        //grid.setMinHeight(250);
        
        urlLabel.setEditable(false);
        urlLabel.setStyle("-fx-background-color: -fx-control-inner-background;");
        GridPane.setHgrow(urlLabel, Priority.ALWAYS);

        grid.add(nameLabel, 0, 0);
        grid.add(fullNameLabel, 0, 1);
        grid.add(versionLabel, 0, 2);
        grid.add(urlLabel, 0, 3);

        grid.setPrefWidth(300);

        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk = new ButtonType("OK", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);

        dialog.setResultConverter(button -> {
            if (button == buttonTypeOk) {
                return null;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(result -> {
        });
        ;
    }

}