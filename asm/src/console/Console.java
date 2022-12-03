package console;

import java.util.LinkedHashMap;
import java.util.Map;

import dialogs.AboutDialog;
import dialogs.CalculationDialog;
import dialogs.OpenSignalDialog;
import dialogs.OperationsDialog;
import dialogs.SourceDialog;
import dialogs.TransformationsDialog;
import dialogs.WindowingDialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import signals.Signal;

public class Console {
    private final MenuBar menuBar;
    private final TextArea consoleText;
    private final TextField consoleInput;
    private final CommandLineParser parser;
    private final Map<String, Signal> signals;
    private final Stage stage;

    public Console(VBox v, Stage stage) {
        this.stage = stage;
        menuBar = new MenuBar();
        consoleText = new TextArea();
        consoleInput = new TextField();
        signals = new LinkedHashMap<String, Signal>();
        parser = new CommandLineParser(signals, this);

        
        Menu menuFile = new Menu("File");
        MenuItem menuItemOpen = new MenuItem("Open");
        MenuItem menuItemExit = new MenuItem("Exit");
        menuFile.getItems().add(menuItemOpen);
        menuFile.getItems().add(menuItemExit);
        
        menuItemOpen.setOnAction(e -> {
            new OpenSignalDialog(parser);
        });
        menuItemExit.setOnAction(e -> {
            System.exit(0);
        });

        Menu menuCommands = new Menu("Commands");
        MenuItem menuSources = new MenuItem("Sources");
        MenuItem menuWindowing = new MenuItem("Windowing");
        MenuItem menuCalculations = new MenuItem("Math operations");
        MenuItem menuTransformations = new MenuItem("Transformations");
        MenuItem menuOtherOperations = new MenuItem("Other operations");
        menuCommands.getItems().add(menuSources);
        menuCommands.getItems().add(menuWindowing);
        menuCommands.getItems().add(menuCalculations);
        menuCommands.getItems().add(menuTransformations);
        menuCommands.getItems().add(menuOtherOperations);
        
        menuSources.setOnAction(e -> {
            new SourceDialog(parser);
        });
        menuWindowing.setOnAction(e -> {
            new WindowingDialog(parser);
        });
        menuCalculations.setOnAction(e -> {
            new CalculationDialog(parser, signals);
        });
        menuTransformations.setOnAction(e -> {
            new TransformationsDialog(parser, signals);
        });
        menuOtherOperations.setOnAction(e -> {
            new OperationsDialog(parser, signals);
        });

        Menu menuHelp = new Menu("Help");
        MenuItem menuAbout = new MenuItem("About");
        menuHelp.getItems().add(menuAbout);

        menuAbout.setOnAction(e -> {
            new AboutDialog();
        });

        menuBar.getMenus().add(menuFile);
        menuBar.getMenus().add(menuCommands);
        menuBar.getMenus().add(menuHelp);
        
        
        consoleText.setEditable(false);
        consoleInput.setPromptText("Enter command here");

        consoleInput.setOnAction(e -> {
            String inputText = consoleInput.getText();
            //consoleText.appendText("> " + inputText + "\n");
            parser.parseCommand(inputText);
            consoleInput.clear();
            // Put focus back to console
            stage.requestFocus();
        });

        v.getChildren().add(menuBar);
        VBox.setVgrow(consoleText, Priority.ALWAYS);
        v.getChildren().add(consoleText);
        v.getChildren().add(consoleInput);
    }

    public void print(String message) {
        consoleText.appendText(message);
    }

    public void println(String message) {
        consoleText.appendText(message + "\n");
    }
    
    public void show() {
        stage.show();
    }
}
